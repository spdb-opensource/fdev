package com.spdb.fdev.fdevtask.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.spdb.service.TaskManageService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author lsw
 * @description task manage
 * @date 2020/8/10
 */
@Api(tags = "task manage interface")
@RestController
@RequestMapping("/api/manage")
@RefreshScope
public class TaskManageController {

    private final Logger log = LoggerFactory.getLogger(TaskManageController.class);

    @Autowired
    private TaskManageService taskManageService;

    @OperateRecord(operateDiscribe = "任务模块-任务管理\"出测试包\"")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.REF, Dict.ISREINFORCE, Dict.ENV_NAME, Dict.USERNAME, Dict.DESC})
    @PostMapping(value = "/generate/package")
    public JsonResult generateTestPackage(@RequestBody Map<String, Object> requestParam) throws Exception {
        taskManageService.checkAuthentication(requestParam);
        return taskManageService.generateTestPackage(requestParam);
    }
    @OperateRecord(operateDiscribe = "挡板卡点需求")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_PROJECT_ID,Dict.BRANCH_NAME})
    @PostMapping(value = "/check/iam/properties")
    public JsonResult checkIamsProperties(@RequestBody Map<String, Object> requestParam) throws Exception {
        return taskManageService.checkIamsProperties(requestParam);
    }

    @OperateRecord(operateDiscribe = "任务模块-任务管理\"查询原生参数文件\"")
    @PostMapping(value = "/queryParamFile")
    public JsonResult queryParamFile(@RequestBody Map<String, Object> requestParam) throws Exception {
        taskManageService.checkAuthentication(requestParam);
        return taskManageService.queryParamFile();
    }

    @OperateRecord(operateDiscribe = "任务模块-任务管理\"通过配置文件出测试包\"")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping(value = "/generate/packageFile")
    public JsonResult generatePackageFile(@RequestBody Map<String, Object> requestParam) throws Exception {
        taskManageService.checkAuthentication(requestParam);
        taskManageService.generatePackageFile(requestParam);
        return JsonResultUtil.buildSuccess();
    }
}
