package com.spdb.job.controller;

import com.spdb.common.aop.validate.RequestValidate;
import com.spdb.common.util.JsonResult;
import com.spdb.common.dict.Dict;
import com.spdb.job.service.IElasticSearchService;
import com.spdb.job.service.ITaskManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 定时任务管理
 * @author: lizz
 * @since: 2019/10/18 17:57
 **/
@RestController
@RequestMapping("/api/admin")
public class TaskManagerController {
    @Autowired
    ITaskManagerService taskManagerService;

    @PostMapping("/addJob")
    @RequestValidate(NotEmptyFields = {Dict.JOBNAME})
    public JsonResult addJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        param.put("schedulerType", "cron");
        taskManagerService.addJob(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/addTempJob")
    @RequestValidate(NotEmptyFields = {Dict.JOBNAME})
    public JsonResult addTempJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        param.put("schedulerType", "simple");
        taskManagerService.addJob(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/selectTrigger")
    public JsonResult selectTrigger(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = taskManagerService.selectTrigger(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/selectJobDetail")
    public JsonResult selectJobDetail(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = taskManagerService.selectJobDetail(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/deleteJob")
    public JsonResult deleteJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        taskManagerService.deleteJob(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/updateJob")
    public JsonResult updateJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        taskManagerService.updateJob(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/pauseJob")
    public JsonResult pauseJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        taskManagerService.pauseJob(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/resumeJob")
    public JsonResult resumeJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        taskManagerService.resumeJob(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/getServiceList")
    public JsonResult getServiceList(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = taskManagerService.getServiceList();
        return JsonResult.buildSuccess(resultMap);
    }

    @Autowired
    private IElasticSearchService elasticSearchService;

    @PostMapping("/esTest")
    public JsonResult esTest(@RequestBody Map<String, Object> param) {
        List list = elasticSearchService.queryJobLog("");
        return JsonResult.buildSuccess(list);
    }
}
