package com.spdb.fdev.fdemand.spdb.controller;


import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdemand.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.entity.OtherDemandTask;
import com.spdb.fdev.fdemand.spdb.service.IOtherDemandTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/other")
public class OtherDemandTaskController {

    @Autowired
    private IOtherDemandTaskService otherDemandTaskService;

    /**
     * 新增其他需求任务
     *
     * @param otherDemandTask
     * @return
     * @throws Exception
     */
    @PostMapping("/addOtherDemandTask")
    @NoNull(require = {Dict.TASKNAME,Dict.TASKTYPE, Dict.TASKCLASSIFY, Dict.PRJNUM,Dict.PLANPRJNAME,
            Dict.HEADERUNITNAME,Dict.HEADERTEAMNAME, Dict.LEADERGROUP,Dict.LEADERGROUPNAME,
            Dict.TASKLEADERID,Dict.TASKLEADERNAME, Dict.PLANSTARTDATE,Dict.PLANDONEDATE,Dict.DEMANDID}, parameter = OtherDemandTask.class)
    public JsonResult addOtherDemandTask(@RequestBody OtherDemandTask otherDemandTask) throws Exception {
        return JsonResultUtil.buildSuccess(otherDemandTaskService.addOtherDemandTask(otherDemandTask));
    }

    /**
     * 编辑其他需求任务
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/updateOtherDemandTask")
    @NoNull(require = {Dict.TASKNAME,Dict.TASKTYPE, Dict.TASKCLASSIFY, Dict.PRJNUM,Dict.PLANPRJNAME,
            Dict.HEADERUNITNAME,Dict.HEADERTEAMNAME, Dict.LEADERGROUP,Dict.LEADERGROUPNAME,
            Dict.TASKLEADERID,Dict.TASKLEADERNAME, Dict.PLANSTARTDATE,Dict.PLANDONEDATE,Dict.DEMANDID
            ,Dict.TASKNUM,Dict.ID}, parameter = OtherDemandTask.class)
    public JsonResult updateOtherDemandTask(@RequestBody OtherDemandTask otherDemandTask) throws Exception {
        return JsonResultUtil.buildSuccess(otherDemandTaskService.updateOtherDemandTask(otherDemandTask));
    }

    /**
     * 删除其他需求任务
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/deleteOtherDemandTask")
    @RequestValidate(NotEmptyFields = {Dict.TASKNUM})
    public JsonResult deleteOtherDemandTask(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(otherDemandTaskService.deleteOtherDemandTask(params));
    }

    /**
     * 查询其他需求任务列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryOtherDemandTaskList")
    @RequestValidate(NotEmptyFields = {Dict.DEMANDID})
    public JsonResult queryOtherDemandTaskList(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(otherDemandTaskService.queryOtherDemandTaskList(params));
    }

    /**
     * 查询其他需求任务详情
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryOtherDemandTask")
    @RequestValidate(NotEmptyFields = {Dict.TASKNUM})
    public JsonResult queryOtherDemandTask(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(otherDemandTaskService.queryOtherDemandTask(params));
    }

}
