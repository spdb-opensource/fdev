package com.spdb.fdev.fdevinterface.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.spdb.entity.RestRelation;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceApplicationService;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@RequestMapping(value = "/api/interfaceApplication")
@RestController
public class InterfaceApplicationController {

    @Resource
    private InterfaceApplicationService interfaceApplicationService;

    @Resource
    private InterfaceRelationService interfaceRelationService;

    @Autowired
    UserVerifyUtil userVerifyUtil;
    
    /**
     * 插入接口申请rest调用关系表初始记录（仅接口申请功能投产时用来初始化数据）
     * @return
     */
    @PostMapping("/add")
    public JsonResult add() throws Exception {
        interfaceApplicationService.addRecord();
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 获取接口申请列表
     * @param params
     * @return
     */
    @PostMapping("/queryApplicationList")
    public JsonResult queryApplicationList(@RequestBody Map params) {
        return JsonResultUtil.buildSuccess(interfaceApplicationService.queryApproveList(params));
    }

    /**
     * 查询接口申请状态
     * @param params
     * @return
     */
    @PostMapping("/queryApproveStatus")
    public JsonResult queryApproveStatus(@RequestBody Map params) {
        String transId= (String) params.get("transId");
        String serviceCalling= (String) params.get("serviceCalling");
        String serviceId= (String) params.get("serviceId");
        if(StringUtils.isEmpty(transId)||StringUtils.isEmpty(serviceCalling)||StringUtils.isEmpty(serviceId)){
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        return JsonResultUtil.buildSuccess(interfaceApplicationService.queryStatus(transId,serviceCalling,serviceId));
    }

    /**
     * 申请接口调用
     * @param params
     * @return
     */
    @OperateRecord(operateDiscribe="接口模块-申请接口调用")
    @PostMapping("/interfaceCallRequest")
    public JsonResult interfaceCallRequest(@RequestBody Map params) throws Exception{
        if(StringUtils.isEmpty(params.get("transId"))||StringUtils.isEmpty(params.get("serviceCalling"))
                ||StringUtils.isEmpty(params.get("serviceId"))||StringUtils.isEmpty(params.get(Dict.APPLICANT))
                ||StringUtils.isEmpty(params.get(Dict.REASON))){
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        List<String> list=interfaceApplicationService.insertApproveRecord(params);
        Map map=new HashMap();
        map.put("provideManagers",list);
        return JsonResultUtil.buildSuccess(map);
    }

    /**
     * 审批接口申请
     * @param params
     * @return
     */
    @OperateRecord(operateDiscribe="接口模块-接口申请审批")
    @PostMapping("/updateApplicationStatus")
    public JsonResult updateApplicationStatus(@RequestBody Map params) throws Exception{
        if(StringUtils.isEmpty(params.get("id"))||StringUtils.isEmpty(params.get("status"))|| StringUtils.isEmpty(params.get("approver"))){
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        interfaceApplicationService.updateApproveStatus(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询应用中是否有调用未申请接口
     * @param map
     * @return
     */
    @PostMapping("/queryIsNoApplyInterface")
    public JsonResult queryIsNoApplyInterface(@RequestBody Map map) throws Exception {
        String serviceCalling= (String) map.get("serviceCalling");
        String branch= (String) map.get("branch");
        if(StringUtils.isEmpty(serviceCalling)||StringUtils.isEmpty(branch)){
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        return JsonResultUtil.buildSuccess(interfaceApplicationService.isNoApplyInterface(serviceCalling,branch));
    }

    /**
     * 判断当前用户是否是应用负责人
     * @return
     */
    @PostMapping("/isManagers")
    public JsonResult isManagers(){
        Map map=new HashMap();
        if(interfaceApplicationService.isManagers()){
            map.put("limit",true);
        }else{
            map.put("limit",false);
        }
        return JsonResultUtil.buildSuccess(map);
    }

}