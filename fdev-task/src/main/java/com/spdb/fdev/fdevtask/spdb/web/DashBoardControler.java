package com.spdb.fdev.fdevtask.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevtask.spdb.service.IDashBaordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashBoardControler {

    @Autowired
    private IDashBaordService iDashBaordService;

    @RequestMapping(value = "/getGroupTaskInfo", method = RequestMethod.POST)
    public JsonResult getTaskNum(@RequestBody Map params) throws Exception {
        String groupId = String.valueOf(params.get("groupId"));
        return JsonResultUtil.buildSuccess(iDashBaordService.statisticsTaskInfo(groupId));
    }

    @RequestMapping(value = "/getAllTaskNum", method = RequestMethod.POST)
    public JsonResult getAllTaskNum() throws Exception {
        return JsonResultUtil.buildSuccess(iDashBaordService.getAllTaskNum());
    }

    @RequestMapping(value = "/getDelayTaskByGroup", method = RequestMethod.POST)
    public JsonResult getDelayTaskByGroup(@RequestBody Map params) throws Exception {
        String queryMonth = String.valueOf(params.get("queryMonth"));
        return JsonResultUtil.buildSuccess(iDashBaordService.getDelayTaskByGroup(queryMonth));
    }
}
