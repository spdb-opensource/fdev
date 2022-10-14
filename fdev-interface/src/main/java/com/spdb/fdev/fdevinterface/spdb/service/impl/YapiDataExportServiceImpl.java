package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.jayway.jsonpath.JsonPath;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.JsonVersionContentChangeUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dao.YapiDataExportDao;
import com.spdb.fdev.fdevinterface.spdb.entity.YapiInterface;
import com.spdb.fdev.fdevinterface.spdb.entity.YapiProject;
import com.spdb.fdev.fdevinterface.spdb.service.YapiDataExportService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spdb.fdev.fdevinterface.base.utils.YapiExportDataUtil.getURLResource;
import static com.spdb.fdev.fdevinterface.base.utils.YapiExportDataUtil.yapiDataContentChange;

@Service
@RefreshScope
public class YapiDataExportServiceImpl implements YapiDataExportService {
    //获取Yapi项目信息Api
    @Value("${yapi.api.project.url}")
    private String yapiProjectUrl;
    //获取Yapi接口详情Api
    @Value("${yapi.api.interfacedetils.url}")
    private String yapiInterfaceDetils;
    @Resource
    private YapiDataExportDao yapiDataExportDao;

    //添加项目的基本信息
    @Override
    public String addProjectInfor(Map request) {
        User user = null;
        String yapi_token = "";
        String msg = "";
        Map<String, Object> import_user = new HashMap<>();
        List<YapiInterface> yapiInterfaceList_erro = new ArrayList<>();
        try {
            user = Util.getSessionUser();
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"获取用户信息失败"});
        }
        if (user != null) {
            import_user.put("import_user_id", user.getId());
            import_user.put("import_name_en", user.getUser_name_en());
            import_user.put("import_name_cn", user.getUser_name_cn());
            yapi_token = (String) request.get(Dict.YAPI_TOKEN);
        } else {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户不存在"});
        }
        if (!yapi_token.equals("")) {
            YapiProject yapiProject = new YapiProject();
            //根据官网获得的API 以及请求参数拼接地址字符串
            String urlProjectnfor = yapiProjectUrl + "?token=" + yapi_token;
            //根据官网获得的API获取项目的数据
            String strProjectInfor = getURLResource(urlProjectnfor);
            //获取项目的名字
            String projectName = JsonPath.read(strProjectInfor, "$.data.name");
            //获取项目的id
            String projectID = JsonPath.read(strProjectInfor, "$.data._id") + "";
            //根据官网获得API 以及得到的项目ID参数  获取接口的列表
            List<Object> interfaceList = yapiDataExportDao.getYapiInterfaceList(projectID, yapi_token);
            yapiProject.setProject_name(projectName);
            yapiProject.setProject_id(projectID);
            yapiProject.setYapi_token(yapi_token);
            List<YapiInterface> yapiInterfaceList = new ArrayList<>();
            for (Object o : interfaceList) {
                String interfaceId = JsonPath.read(o, "$._id") + "";
                YapiInterface yapiInterface = interfaceDetils(interfaceId, yapi_token);
                if (!yapiInterface.getJson_schema().equals("no_json")) {
                    yapiInterfaceList.add(yapiInterface);
                } else {
                    yapiInterfaceList_erro.add(yapiInterface);
                }
            }
            //查看是否存在此项目
            YapiProject yapiOnlyProject = yapiDataExportDao.findOnlyProjectList(projectID);
            if (yapiOnlyProject != null) {
                yapiProject.setCreate_time(yapiOnlyProject.getCreate_time());
                yapiProject.setUpdate_time(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                yapiDataExportDao.updateProject(yapiProject);
                yapiDataExportDao.updateInterfaceList(yapiInterfaceList, projectID);
            } else {
                yapiProject.setCreate_time(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                yapiProject.setImport_user(import_user);
                yapiDataExportDao.addProject(yapiProject);
                yapiDataExportDao.addInterface(yapiInterfaceList);
            }
        } else {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        if (yapiInterfaceList_erro.size() != 0) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("未成功导入的接口ID为");
            for (YapiInterface yapiInterface : yapiInterfaceList_erro) {
                stringBuffer.append(yapiInterface.getInterface_id() + ",");
            }
            stringBuffer.append("原因是接口的请求或响应参数非json格式");
            msg = String.valueOf(stringBuffer);
        }
        return msg;
    }

    //添加接口信息
    @Override
    public void importInterface(Map request) {
        String interface_id = (String) request.get(Dict.INTERFACE_ID);
        String yapi_token = (String) request.get(Dict.YAPI_TOKEN);
        YapiInterface yapiInterface = new YapiInterface();
        if (!interface_id.equals("") && !yapi_token.equals("")) {
            try {
                yapiInterface = interfaceDetils(interface_id, yapi_token);
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[]{"Yapi端接口被移除，刷新失败"});
            }
            if (yapiInterface.getJson_schema().equals("no_json")) {
                throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[]{"Yapi接口请求或响应参数非json格式，刷新失败"});
            } else {
                yapiDataExportDao.updateInterface(yapiInterface);
            }
        } else {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
    }

    //查询项目列表返回 文档id 项目id 项目名称  录入人  接口列表为null
    @Override
    public Map<String, Object> yapiProjectList(Map request) {
        return yapiDataExportDao.findProjectList(request);
    }

    //jsonSchemaDrfat03到jsonSchemaDrfat04的转换
    @Override
    public String convertJsonSchema(Map request) {
        String jsonString;
        try {
            jsonString = (String) request.get("changeBeforJson");
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.SCHEMA_ERROR, new String[]{"只支持jsonSchema_draft03的转换,请填写正确的JsonSchemaDraft03"});
        }
        if (StringUtils.isNotEmpty(jsonString)) {
                String changeAfterJson = JsonVersionContentChangeUtil.jsonVersionContentChange(jsonString);
                return changeAfterJson;

        } else {
            throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA);
        }
    }

    //查询接口列表返回 文档id 项目id 项目名称  录入人  接口列表不为null
    @Override
    public Map<String, Object> yapiProjectListWithInterfaces(Map request) {
        return yapiDataExportDao.yapiProjectListWithInterfaces(request);
    }

    //删除项目应用
    @Override
    public void deleteProject(Map request) {
        yapiDataExportDao.deleteProjet(request);
    }

    //删除单个接口
    @Override
    public void deleteInterface(Map request) {
        yapiDataExportDao.deleteInterface(request);
    }

    public YapiInterface interfaceDetils(String interface_id, String yapi_token) {
        String urlProjectnfor = yapiProjectUrl + "?token=" + yapi_token;
        String strProjectInfor = getURLResource(urlProjectnfor);
        String projectName = JsonPath.read(strProjectInfor, "$.data.name");
        String urlInterafceSchema = yapiInterfaceDetils + "?id=" + interface_id + "&token=" + yapi_token;
        String strInterdaceSchema = getURLResource(urlInterafceSchema);
        String interfaceId = JsonPath.read(strInterdaceSchema, "$.data._id") + "";
        String interfaceProjectId = JsonPath.read(strInterdaceSchema, "$.data.project_id") + "";
        String interfaceName = JsonPath.read(strInterdaceSchema, "$.data.title");
        String interfacePath = JsonPath.read(strInterdaceSchema, "$.data.query_path.path");
        Map request = new HashMap();
        request.put("strInterdaceSchema", strInterdaceSchema);
        request.put("projectName", projectName);
        String formatJsonSchema = yapiDataContentChange(request);
        YapiInterface yapiInterface = new YapiInterface();
        yapiInterface.setYapi_token(yapi_token);
        yapiInterface.setInterface_id(interfaceId);
        yapiInterface.setProject_id(interfaceProjectId);
        yapiInterface.setInterface_name(interfaceName);
        yapiInterface.setInterface_path(interfacePath);
        yapiInterface.setJson_schema(formatJsonSchema);
        yapiInterface.setCreate_time(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        return yapiInterface;
    }

}
