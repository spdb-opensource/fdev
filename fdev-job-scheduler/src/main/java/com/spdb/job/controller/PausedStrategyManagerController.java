package com.spdb.job.controller;

import com.spdb.common.util.JsonResult;
import com.spdb.job.service.IPausedStrategyManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 暂停策略管理
 * @author: lizz
 * @since: 2019/10/18 17:57
 **/
@RestController
@RequestMapping("/api/admin")
public class PausedStrategyManagerController {

    @Autowired
    IPausedStrategyManagerService pausedStrategyManagerService;

    @PostMapping("/insertPauseDateJob")
    public JsonResult insertPauseDateJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        pausedStrategyManagerService.insertPauseDateJob(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/selectPausedTriggerDateAndJob")
    public JsonResult selectPausedTriggerDateAndJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = pausedStrategyManagerService.selectPausedTriggerDateAndJob(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/selectJobAndpausedDate")
    public JsonResult selectJobAndpausedDate(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = pausedStrategyManagerService.selectJobAndpausedDate(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/deletePausedStrategy")
    public JsonResult deletePausedStrategy(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        pausedStrategyManagerService.deletePausedStrategy(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/deletePausedStrategyJob")
    public JsonResult deletePausedStrategyJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        pausedStrategyManagerService.deletePausedStrategyJob(param);
        return JsonResult.buildSuccess(resultMap);
    }

    @PostMapping("/recoverPausedStrategyJob")
    public JsonResult recoverPausedStrategyJob(@RequestBody Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        pausedStrategyManagerService.recoverPausedStrategyJob(param);
        return JsonResult.buildSuccess(resultMap);
    }
}
