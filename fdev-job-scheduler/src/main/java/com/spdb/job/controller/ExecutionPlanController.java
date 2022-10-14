package com.spdb.job.controller;

import com.spdb.common.dict.Dict;
import com.spdb.common.util.JsonResult;
import com.spdb.job.service.IElasticSearchService;
import com.spdb.job.service.IExecutionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 执行计划管理
 * @author: lizz
 * @since: 2019/10/18 17:57
 **/
@RestController
@RequestMapping("/api/admin")
public class ExecutionPlanController {

    @Autowired
    IExecutionPlanService executionPlanService;
    @Autowired
    IElasticSearchService elasticSearchService;

    @PostMapping("/selectJobInfo")
    public JsonResult selectJobInfo(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = executionPlanService.selectJobInfo(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/generateFutureJobList")
    public JsonResult generateFutureJobList() {
        executionPlanService.generateFutureJobList();
        return JsonResult.buildSuccess();
    }

    @PostMapping("/selectExecutorPlanDetail")
    public JsonResult selectExecutorPlanDetail(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = executionPlanService.selectExecutorPlanDetail(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/selectExecutorPlanLog")
    public JsonResult selectExecutorPlanLog(@RequestBody Map<String, String> param) {
        //id 为执行记录的流水号
        String schedulerSeqNo = param.get(Dict.ID);
        String date = param.get(Dict.SCHEDULERDATE);
        List resultMap = elasticSearchService.queryJobLog(schedulerSeqNo);
        return JsonResult.buildSuccess(resultMap);
    }


    @PostMapping("/cancelJob")
    public JsonResult cancelJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        executionPlanService.cancelJob(param);
        return JsonResult.buildSuccess();
    }

    @PostMapping("/postponeJob")
    public JsonResult postponeJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        executionPlanService.postponeJob(param);
        return JsonResult.buildSuccess();
    }

    @PostMapping("/triggerJob")
    public JsonResult triggerJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        executionPlanService.triggerJob(param);
        return JsonResult.buildSuccess();
    }

}
