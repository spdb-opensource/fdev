package com.spdb.fdev.fdevapp.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevapp.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.spdb.entity.AutoTestEnv;
import com.spdb.fdev.fdevapp.spdb.service.IAutoTestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "自动测试实体接口")
@RequestMapping("/api/autotest")
@RestController
public class AutoTestController {
    @Autowired
    IAutoTestService autoTestService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @ApiOperation(value = "查询应用自动化测试开关")
    @PostMapping("/query")
    @NoNull(require = {Dict.GITLAB_PROJECT_ID}, parameter = AutoTestEnv.class)
    public JsonResult query(@RequestBody AutoTestEnv autoTest) throws Exception {
        return JsonResultUtil.buildSuccess(this.autoTestService.query(autoTest));
    }

    @OperateRecord(operateDiscribe = "应用模块-应用详情\"自动化测试\"")
    @ApiOperation(value = "修改应用自动化测试开关")
    @PostMapping("/update")
    @NoNull(require = {Dict.GITLAB_PROJECT_ID}, parameter = AutoTestEnv.class)
    public JsonResult update(@RequestBody AutoTestEnv autoTest) throws Exception {
        return JsonResultUtil.buildSuccess(this.autoTestService.update(autoTest));
    }

    @ApiOperation(value = "对接自动测试平台")
    @PostMapping("/autoTestEnv")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_PROJECT_ID, Dict.BRANCH})
    public JsonResult autoTestEnv(@RequestBody Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.autoTestService.autoTestEnv(requestParam));
    }
}
