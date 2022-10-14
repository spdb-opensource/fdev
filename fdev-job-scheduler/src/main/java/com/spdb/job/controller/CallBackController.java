package com.spdb.job.controller;

import com.spdb.common.dict.Dict;
import com.spdb.common.util.JsonResult;
import com.spdb.job.service.IExecutionPlanService;
import com.spdb.job.service.impl.ExecutionPlanServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/callback")
public class CallBackController {
    private static final Logger logger = LoggerFactory.getLogger(CallBackController.class);

    @Autowired
    IExecutionPlanService executionPlanService;

    @PostMapping("/updateExecutorResult")
    public JsonResult updateExecutorResult(@RequestBody Map<String, Object> param) {
        logger.info("callback updateExecutorResult begin");
        Map<String, Object> resultMap = new HashMap<>();
        param.put(Dict.ENDTIME, System.currentTimeMillis());
        executionPlanService.updateJobInfo(param);
        logger.info("callback updateExecutorResult end");
        return JsonResult.buildSuccess(resultMap);
    }
}
