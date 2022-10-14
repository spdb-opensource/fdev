package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.EsfConfiguration;
import com.spdb.fdev.release.entity.EsfRegistration;
import com.spdb.fdev.release.service.impl.EsfRegistrationServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api(tags = "esf注册信息")
@RequestMapping("/api/esf")
@RestController
@RefreshScope
public class EsfRegistrationController {

    @Autowired
    private EsfRegistrationServiceImpl registrationService;

    private static Logger logger = LoggerFactory.getLogger(EsfRegistrationController.class);

    @OperateRecord(operateDiscribe = "查询应用是否投过产")
    @PostMapping(value = "/queryAppStatus")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID})
    public JsonResult queryAppStatus(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        Map<String,Object> result = registrationService.queryAppStatus(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }

    @OperateRecord(operateDiscribe = "esf注册用户信息添加")
    @PostMapping(value = "/addEsfRegistration")
    public JsonResult addEsfRegistration(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        registrationService.addEsfRegistration(requestParam);
        return JsonResultUtil.buildSuccess(null);
    }

    @OperateRecord(operateDiscribe = "esf注册用户信息修改")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping(value = "/updateEsf")
    public JsonResult updateEsf(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        registrationService.updateEsf(requestParam);
        return JsonResultUtil.buildSuccess(null);
    }

    @OperateRecord(operateDiscribe = "esf注册用户信息删除")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping(value = "/delEsf")
    public JsonResult delEsf(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        registrationService.delEsf(requestParam);
        return JsonResultUtil.buildSuccess(null);
    }

    @OperateRecord(operateDiscribe = "查询esf用户注册信息")
    @RequestValidate(NotEmptyFields = {Dict.PROD_ID})
    @PostMapping(value = "/queryEsfRegistration")
    public JsonResult queryEsfRegistration(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        List<EsfRegistration> esfRegistrationList = registrationService.queryEsfRegistration(requestParam);
        return JsonResultUtil.buildSuccess(esfRegistrationList);
    }

    @OperateRecord(operateDiscribe = "查询esf配置中心信息")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, "env_name"})
    @PostMapping(value = "/queryEsfConfiguration")
    public JsonResult queryEsfConfiguration(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        List<EsfConfiguration> esfConfigurationList = registrationService.queryEsfConfig(requestParam);
        return JsonResultUtil.buildSuccess(esfConfigurationList);
    }

    @OperateRecord(operateDiscribe = "批量录入esf配置中心ip")
    @PostMapping(value = "/batchAdd")
    public JsonResult batchAdd(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        registrationService.batchAdd(requestParam);
        return JsonResultUtil.buildSuccess(null);
    }

    @OperateRecord(operateDiscribe = "查询全量应用")
    @PostMapping(value = "/queryApps")
    @RequestValidate(NotEmptyFields = {Dict.PROD_ID})
    public JsonResult queryApps(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        List<Map<String,Object>> result = registrationService.queryApps(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }


}
