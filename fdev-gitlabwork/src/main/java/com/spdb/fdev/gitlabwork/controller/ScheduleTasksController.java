package com.spdb.fdev.gitlabwork.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.gitlabwork.dto.CommitUpdate;
import com.spdb.fdev.gitlabwork.schedule.ScheduleTasks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: zhangxt
 * @Date: 2021/4/12 10:13
 **/

@RestController
@RequestMapping("/api")
public class ScheduleTasksController {

    @Autowired
    private ScheduleTasks scheduleTasks;

    @PostMapping("/batchCodeStatistics")
    public JsonResult scheduleTasks() {
        scheduleTasks.updateGitlabUser();
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/commitStatistics")
    public JsonResult commitStatistics(@RequestBody CommitUpdate commitUpdate) {
        scheduleTasks.commitStatistics(commitUpdate);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/commitStats")
    public JsonResult commitStats() {
        scheduleTasks.commitStats();
        return JsonResultUtil.buildSuccess();
    }

}
