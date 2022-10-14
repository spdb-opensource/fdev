package com.spdb.fdev.fdevapp.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.service.IVipChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/vip")
@RestController
public class VipChannelController {

    @Autowired
    private IVipChannelService vipChannelService;

    /**
     * 部署的接口，根据前端传的应用id及分支，获取channel数据模型中的信息，存入数据库，并组装环境变量发送kafka消息
     *
     * @param request 应用id，分支ref，持续集成变量variables
     * @return
     */
    @PostMapping("/runPipeline")
    public JsonResult analysisInfoAndSaveChannel(@RequestBody Map request) throws Exception {
        this.vipChannelService.analysisInfo(request);
        return JsonResultUtil.buildSuccess(request);
    }


    /**
     * 根据前端传的查询条件，返回部署列表，后端分页，根据触发时间倒序返回
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/pipelines")
    public JsonResult getPipelines(@RequestBody Map request) {
        return JsonResultUtil.buildSuccess(this.vipChannelService.getChannelListByPagination(request));
    }

    /**
     * 取消当前部署
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/cancel")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult cacelPipelines(@RequestBody Map request) {
        String id = (String) request.get(Dict.ID);
        return JsonResultUtil.buildSuccess(this.vipChannelService.cancelPipelines(id));
    }

    ///getLog
    @PostMapping("/getLog")
    @RequestValidate(NotEmptyFields = {Dict.ID}, NotBothEmptyFields = {Dict.STAGES})
    public JsonResult getLog(@RequestBody Map request) {
        String id = (String) request.get(Dict.ID);
        String stage = (String) request.get(Dict.STAGES);
        return JsonResultUtil.buildSuccess(this.vipChannelService.getChannelLogContent(id, stage));
    }


    //根据id来获得pipeline信息
    @PostMapping("/getPipelineById")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult getPipelineById(@RequestBody Map request) {
        String id = (String) request.get(Dict.ID);
        return JsonResultUtil.buildSuccess(this.vipChannelService.getPipelineById(id));
    }


}
