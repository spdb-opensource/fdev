package com.spdb.fdev.fdevtask.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevtask.spdb.service.TaskDashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @program: fdev-task
 * @description:新任务大屏数据
 * @author: c-jiangl2
 * @create: 2021-04-06 10:14
 **/
@RestController
@RequestMapping("/api/taskDashBoard")
public class TaskDashBoardController {

    @Autowired
    private TaskDashBoardService taskDashBoardService;

    @RequestMapping(value = "/getGroupTaskInfo", method = RequestMethod.POST)
    public JsonResult getTaskNum(@RequestBody Map params) throws Exception {
        String groupId = String.valueOf(params.get("groupId"));
        return JsonResultUtil.buildSuccess(taskDashBoardService.statisticsTaskInfo(groupId));
    }

    @RequestMapping(value = "/getAllTaskNum", method = RequestMethod.POST)
    public JsonResult getAllTaskNum() throws Exception {
        return JsonResultUtil.buildSuccess(taskDashBoardService.getAllTaskNum());
    }

    @RequestMapping(value = "/getDelayTaskByGroup", method = RequestMethod.POST)
    public JsonResult getDelayTaskByGroup(@RequestBody Map params) throws Exception {
        return JsonResultUtil.buildSuccess(taskDashBoardService.getDelayTaskByGroup(params));
    }
}
