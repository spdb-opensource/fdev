package com.spdb.fdev.fdevinterface.spdb.dao.impl;

import com.jayway.jsonpath.JsonPath;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.CommonUtil;
import com.spdb.fdev.fdevinterface.spdb.dao.YapiDataExportDao;
import com.spdb.fdev.fdevinterface.spdb.entity.YapiInterface;
import com.spdb.fdev.fdevinterface.spdb.entity.YapiProject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spdb.fdev.fdevinterface.base.utils.YapiExportDataUtil.getURLResource;

@Repository
@RefreshScope
public class YapiDataExportDaoImpl implements YapiDataExportDao {
    //获取Yapi项目列表Api
    @Value("${yapi.api.interfacelist.url}")
    private String yapiInterfaceList;
    @Resource
    private MongoTemplate mongoTemplate;

    //根据Yapi_token查看项目是否存在
    @Override
    public YapiProject findOnlyProjectList(String projectID) {
        Query query = Query.query(Criteria.where(Dict.PROJECTID).is(projectID));
        return mongoTemplate.findOne(query, YapiProject.class);
    }


    //添加新的项目
    @Override
    public void addProject(YapiProject yapiProject) {
        mongoTemplate.insert(yapiProject, Dict.INTERFACE_YAPI_PROJECT);
    }

    //添加新的接口
    @Override
    public void addInterface(List<YapiInterface> yapiInterfaceList) {
        mongoTemplate.insert(yapiInterfaceList, Dict.INTERFACE_YAPI_INTERFACES);
    }

    //更新项目
    @Override
    public void updateProject(YapiProject yapiProject) {
        Query query = Query.query(Criteria.where(Dict.PROJECTID).is(yapiProject.getProject_id()));
        Update update = new Update();
        update.set("yapi_token", yapiProject.getYapi_token());
        update.set("project_name", yapiProject.getProject_name());
        update.set("update_time", yapiProject.getUpdate_time());
        mongoTemplate.updateFirst(query, update, YapiProject.class);
    }

    //更新接口列表
    @Override
    public void updateInterfaceList(List<YapiInterface> yapiInterfaceList, String projectID) {
        Query query = Query.query(Criteria.where(Dict.PROJECTID).is(projectID));
        mongoTemplate.remove(query, YapiInterface.class);
        mongoTemplate.insert(yapiInterfaceList, YapiInterface.class);

    }

    //更新接口信息
    @Override
    public void updateInterface(YapiInterface yapiInterface) {
        Query query = Query.query(Criteria.where(Dict.YAPI_TOKEN).is(yapiInterface.getYapi_token()).and(Dict.INTERFACE_ID).is(yapiInterface.getInterface_id()));
        Update update = new Update();
        update.set("project_id", yapiInterface.getProject_id());
        update.set("yapi_token", yapiInterface.getYapi_token());
        update.set("interface_name", yapiInterface.getInterface_name());
        update.set("json_schema", yapiInterface.getJson_schema());
        update.set("interface_path", yapiInterface.getInterface_path());
        update.set("update_time", yapiInterface.getCreate_time());
        mongoTemplate.updateFirst(query, update, YapiInterface.class);
    }

    //查询项目列表
    @Override
    public Map<String, Object> findProjectList(Map request) {
        Criteria criteriaProjectName = new Criteria();
        Criteria criteriaProjectId = new Criteria();
        Criteria criteriaYapiToken = new Criteria();
        Criteria criteria = new Criteria();
        String name = (String) request.get("str");
        if (!CommonUtil.isNullOrEmpty(name)) {
            criteriaProjectName.and(Dict.PROJECT_NAME).regex(".*" + name + ".*");
            criteriaProjectId.and(Dict.PROJECTID).regex(".*" + name + ".*");
        }
        criteria.orOperator(criteriaProjectName, criteriaProjectId);
        criteria.andOperator(criteriaYapiToken);
        Query query = new Query(criteria);
        Long total = mongoTemplate.count(query, YapiProject.class);
        Integer page = (Integer) request.get("page");
        Integer pageNum = (Integer) request.get("pageNum");
        if (page == null) {
            page = 1;
        }
        if (pageNum == null) {
            pageNum = 10;
        }
        query.skip((page - 1L) * pageNum).limit(pageNum);
        List<YapiProject> list = mongoTemplate.find(query, YapiProject.class, Dict.INTERFACE_YAPI_PROJECT);
        Map<String, Object> restMap = new HashMap<>();
        restMap.put(Dict.TOTAL, total);
        restMap.put(Dict.LIST, list);
        return restMap;
    }

    @Override
    public Map<String, Object> yapiProjectListWithInterfaces(Map request) {
        Query queryProject = new Query();
        Map<String, Object> restMap = new HashMap<>();
        Criteria criteriaInterfaceName = new Criteria();
        Criteria criteriaInterfacePath = new Criteria();
        Criteria criteriaYapiToken = new Criteria();
        Criteria criteriaYapiInterrfaceId = new Criteria();
        Criteria criteria = new Criteria();
        String yapi_token = (String) request.get(Dict.YAPI_TOKEN);
        Object str = request.get("str");
        if (!CommonUtil.isNullOrEmpty(yapi_token)) {
            queryProject.addCriteria(Criteria.where(Dict.YAPI_TOKEN).is(yapi_token));
            criteriaYapiToken.and(Dict.YAPI_TOKEN).is(yapi_token);
        }
        if (!CommonUtil.isNullOrEmpty(str)) {
            criteriaInterfaceName.and(Dict.INTERFACE_NAME).regex(".*" + str + ".*");
            criteriaInterfacePath.and(Dict.INTERFACE_PATH).regex(".*" + str + ".*");
            criteriaYapiInterrfaceId.and(Dict.INTERFACE_ID).regex(".*" + str + ".*");
        }
        criteria.orOperator(criteriaInterfaceName, criteriaInterfacePath, criteriaYapiInterrfaceId);
        criteria.andOperator(criteriaYapiToken);
        Query query = new Query(criteria);
        Long total = mongoTemplate.count(query, YapiInterface.class);
        Integer page = (Integer) request.get("page");
        Integer pageNum = (Integer) request.get("pageNum");
        if (page == null) {
            page = 1;
        }
        if (pageNum == null) {
            pageNum = 10;
        }
        query.skip((page - 1L) * pageNum).limit(pageNum);
        List<YapiProject> projectlist = mongoTemplate.find(queryProject, YapiProject.class, Dict.INTERFACE_YAPI_PROJECT);
        List<YapiInterface> interfacelist = mongoTemplate.find(query, YapiInterface.class, Dict.INTERFACE_YAPI_INTERFACES);
        YapiProject yapiProject = projectlist.get(0);
        restMap.put(Dict.TOTAL, total);
        restMap.put(Dict.PROJECTID, yapiProject.getProject_id());
        restMap.put(Dict.PROJECT_NAME, yapiProject.getProject_name());
        restMap.put(Dict.YAPI_TOKEN, yapiProject.getYapi_token());
        restMap.put(Dict.IMPORT_USER, yapiProject.getImport_user());
        restMap.put(Dict.CREATE_TIME, yapiProject.getCreate_time());
        restMap.put(Dict.UPDATE_TIME, yapiProject.getUpdate_time());
        restMap.put(Dict.L_INTERFACES, interfacelist);
        return restMap;
    }

    //分页修改
    @Override
    public List<Object> getYapiInterfaceList(String projectID, String yapi_token) {
        int i = 1;
        List<Object> interfaces = new ArrayList<>();
        while (true) {
            String urlInterafceList = yapiInterfaceList + "?project_id=" + projectID + "&token=" + yapi_token + "&page=" + i + "&limit=10";
            String strInterdaceInfor = getURLResource(urlInterafceList);
            List<Object> interfaceList = JsonPath.read(strInterdaceInfor, "$.data.list[*]");
            if (interfaceList.size() != 0) {
                for (Object o : interfaceList) {
                    interfaces.add(o);
                }
                interfaceList.clear();
            } else {
                break;
            }
            i++;
        }
        return interfaces;
    }

    //删除项目应用
    @Override
    public void deleteProjet(Map request) {
        Query query = new Query();
        String yapi_token = (String) request.get(Dict.YAPI_TOKEN);
        if (!CommonUtil.isNullOrEmpty(yapi_token)) {
            query = Query.query(Criteria.where(Dict.YAPI_TOKEN).is(yapi_token));
            mongoTemplate.remove(query, YapiInterface.class);
            mongoTemplate.remove(query, YapiProject.class);
        }
    }

    //删除单个接口
    @Override
    public void deleteInterface(Map request) {
        Query query = new Query();
        String yapi_token = (String) request.get(Dict.YAPI_TOKEN);
        String interface_id = (String) request.get(Dict.INTERFACE_ID);
        if (!CommonUtil.isNullOrEmpty(yapi_token) && !CommonUtil.isNullOrEmpty(interface_id)) {
            query = Query.query(Criteria.where(Dict.YAPI_TOKEN).is(yapi_token).and(Dict.INTERFACE_ID).is(interface_id));
            mongoTemplate.remove(query, YapiInterface.class);
        }

    }


}
