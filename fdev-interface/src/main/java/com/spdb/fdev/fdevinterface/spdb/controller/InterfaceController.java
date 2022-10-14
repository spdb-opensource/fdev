package com.spdb.fdev.fdevinterface.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.spdb.entity.RestApi;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceRelationService;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceService;
import com.spdb.fdev.fdevinterface.spdb.service.TransRelationService;
import com.spdb.fdev.fdevinterface.spdb.service.TransService;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceDetailShow;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceParamShow;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceRelationShow;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping(value = "/api/interface")
@RestController
@RefreshScope
public class InterfaceController {

    @Value("${max.batch.num}")
    private Integer maxNum;
    @Resource
    private InterfaceService interfaceService;
    @Resource
    private InterfaceRelationService interfaceRelationService;
    @Resource
    private TransService transService;
    @Resource
    private TransRelationService transRelationService;
    @Autowired
    UserVerifyUtil userVerifyUtil;

    /**
     * 查询接口列表 (fdev)
     *
     * @param interfaceParamShow
     * @return
     * @author hexy
     */
    @PostMapping("/queryInterfaceList")
    public JsonResult showInterface(@RequestBody InterfaceParamShow interfaceParamShow) {
        if (StringUtils.isEmpty(interfaceParamShow.getBranchDefault())
                || StringUtils.isEmpty(interfaceParamShow.getInterfaceType())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        List<String> transIdList = interfaceParamShow.getTransId();
        if (CollectionUtils.isNotEmpty(transIdList) && transIdList.size() > maxNum) {
            throw new FdevException(ErrorConstants.TRANS_CODE_LIST_OVER);
        }
        return JsonResultUtil.buildSuccess(interfaceService.showInterface(interfaceParamShow));
    }

    /**
     * 查询接口关系 (fdev)
     *
     * @param interfaceParamShow
     * @return
     * @author hexy
     */
    @PostMapping("/queryInterfaceRelation")
    public JsonResult showInterfaceRelation(@RequestBody InterfaceParamShow interfaceParamShow) {
        if (StringUtils.isEmpty(interfaceParamShow.getBranchDefault())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        if (FileUtil.isNullOrEmpty(interfaceParamShow.getServiceCalling()) && FileUtil.isNullOrEmpty(interfaceParamShow.getServiceId())
                && FileUtil.isNullOrEmpty(interfaceParamShow.getTransOrServiceOrOperation())&&FileUtil.isNullOrEmpty(interfaceParamShow.getBranch())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_ALLEMPTY);
        }
        List list = interfaceRelationService.showInterfaceRelation(interfaceParamShow);
        Map map = new HashMap();
        map.put("list", list);
        return JsonResultUtil.buildSuccess(map);
    }

    /**
     * 根据接口Id和接口类型获取接口详情 (fdev)
     *
     * @param params
     * @return
     * @author hexy
     */
    @PostMapping("/queryInterfaceDetailById")
    public JsonResult showInterfaceDetail(@RequestBody Map params) {
        return getInterfaceDetail(params);
    }

    /**
     * 根据接口Id和接口类型获取接口详情 (fdev，免登录的)
     *
     * @param params
     * @return
     * @author hexy
     */
    @PostMapping("/getInterfaceDetailById")
    public JsonResult showInterfaceDetailNotLogin(@RequestBody Map params) {
        return getInterfaceDetail(params);
    }

    /**
     * 根据接口Id和接口类型查询各个版本的接口详情 (fdev)
     *
     * @param params
     * @return
     * @author hexy
     */
    @PostMapping("/queryInterfaceVersions")
    public JsonResult getInterfaceVer(@RequestBody Map params) {
        String id = (String) params.get(Dict.ID);
        String interfaceType = (String) params.get(Dict.INTERFACE_TYPE);
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(interfaceType)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        return JsonResultUtil.buildSuccess(interfaceService.getInterfaceVer(id, interfaceType));
    }


    /**
     * @param params
     * @return
     * @throws Exception
     * @description 生成对外提供的接口链接
     * @author baizx
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/getInterfacesUrl")
    public JsonResult getInterfacesUrl(@RequestBody Map params) throws Exception {
        String branch = (String) params.get(Dict.BRANCH);
        String serviceId = (String) params.get(Dict.L_SERVICEID);
        List<Map> idsList = (List<Map>) params.get(Dict.IDS);
        return JsonResultUtil.buildSuccess(interfaceService.getInterfacesUrl(branch, serviceId, idsList));
    }

    /**
     * @param id
     * @return
     * @throws Exception
     * @description 解析链接并生成数据
     * @author baizx
     */
    @GetMapping("/queryInterfacesList")
    public JsonResult getInterfacesByUrl(String id) {
        return JsonResultUtil.buildSuccess(interfaceService.getInterfacesByUrl(id));
    }

    /**
     * 查询接口详情
     *
     * @param params
     * @return
     */
    public JsonResult getInterfaceDetail(Map params) {
        String id = (String) params.get(Dict.ID);
        String interfaceType = (String) params.get(Dict.INTERFACE_TYPE);
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(interfaceType)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        return JsonResultUtil.buildSuccess(interfaceService.getInterfaceDetailById(id, interfaceType));
    }

    /**
     * rest调用关系表 (fdev)
     *
     * @param interfaceParamShow
     * @return
     * @author hexy
     */
    @PostMapping("/downloadRestRelationExcel")
    public JsonResult downloadRestRelationExcel(@RequestBody InterfaceParamShow interfaceParamShow) {
        if (StringUtils.isEmpty(interfaceParamShow.getBranchDefault())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        if (FileUtil.isNullOrEmpty(interfaceParamShow.getServiceCalling()) && FileUtil.isNullOrEmpty(interfaceParamShow.getServiceId())
                && FileUtil.isNullOrEmpty(interfaceParamShow.getTransOrServiceOrOperation())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_ALLEMPTY);
        }
        List<RestApi> restApi = interfaceService.getInterface();
        List<InterfaceRelationShow> interfaceRelationShowList = interfaceRelationService.showInterfaceRelation(interfaceParamShow);
        Map resultMap = interfaceRelationService.ExcelList(restApi, interfaceRelationShowList);
        return JsonResultUtil.buildSuccess(interfaceRelationService.createExcel(resultMap));
    }

    /**
     * 导出文件 接口
     *
     * @param fileName
     * @return
     */
    @GetMapping("/exportFile/{fileName}")
    public void exportFile(@PathVariable String fileName, HttpServletResponse response) throws Exception {
        if (!fileName.startsWith(Constants.CONFIG_FILE_PRE)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"请求参数不被准许"});
        }
        this.interfaceRelationService.exportFile(fileName, response);
    }

    /**
     * 修改接口详情中的参数描述和备注
     *
     * @param interfaceDetailShow
     * @return
     * @throws Exception
     */
    @PostMapping("/updateParamDescription")
    public JsonResult updateParamDescription(@RequestBody InterfaceDetailShow interfaceDetailShow) throws Exception {
        if (StringUtils.isEmpty(interfaceDetailShow.getServiceId()) || StringUtils.isEmpty(interfaceDetailShow.getInterfaceType())
                || StringUtils.isEmpty(interfaceDetailShow.getTransId())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        interfaceService.modifiInterfaceDescription(interfaceDetailShow);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 将数据库中所有的数据关联esb
     *
     * @return
     */
    @PostMapping("/relateEsbData")
    public JsonResult relateEsbData() throws Exception {
        interfaceRelationService.relateEsbData();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 删除任务相关数据
     *
     * @param params
     * @return
     */
    @PostMapping("/deleteDataForTask")
    public JsonResult deleteDataForTask(@RequestBody Map params) throws Exception {
        if (StringUtils.isEmpty(params.get("serviceId")) || StringUtils.isEmpty(params.get("branch"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        deleteDataforApp(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 删除应用相关数据
     *
     * @param params
     * @return
     */
    @PostMapping("/deleteDataforApp")
    public JsonResult deleteDataforApp(@RequestBody Map params) throws Exception {
        if (StringUtils.isEmpty(params.get("serviceId"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        interfaceRelationService.deleteRestRelationData(params);
        interfaceRelationService.deleteSoapRelationData(params);
        interfaceRelationService.deleteSopRelationData(params);
        interfaceService.deleteRestData(params);
        interfaceService.deleteSoapData(params);
        transRelationService.deleteTransRelationData(params);
        transService.deleteTransData(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询服务链路信息
     *
     * @param params serviceId：应用名,branch:分支名
     * @return
     */
    @PostMapping("/getServiceChainInfo")
    public JsonResult getServiceChainInfo(@RequestBody Map params) {
        if (StringUtils.isEmpty(params.get("serviceId")) || StringUtils.isEmpty(params.get("branch"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        Map serviceChainInfo = interfaceRelationService.getServiceChainInfo(params);
        return JsonResultUtil.buildSuccess(serviceChainInfo);
    }

    /**
     * 根据报文类型查询全量master分支接口调用关系数据
     *
     * @param params
     * @return
     */
    @PostMapping("/queryAllRelationByType")
    public JsonResult getAllRelationByType(@RequestBody Map params) {
        if (StringUtils.isEmpty(params.get(Dict.INTERFACE_TYPE))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        String interfaceType = ((String) params.get(Dict.INTERFACE_TYPE)).toUpperCase();
        Map queryAllRelationByType = interfaceRelationService.queryAllRelationByType(interfaceType);
        return JsonResultUtil.buildSuccess(queryAllRelationByType);
    }
}