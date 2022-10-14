package com.spdb.fdev.pipeline.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.pipeline.service.IPluginResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RequestMapping("/api/pluginResult")
@RestController
public class PluginResultController {

    @Autowired
    private IPluginResultService pluginResultService;


    @RequestMapping(value = "/queryPluginResultData")
    @RequestValidate(NotEmptyFields = {Dict.PIPELINEEXEID, Dict.STAGEINDEX, Dict.JOBINDEX, Dict.STEPINDEX})
    public JsonResult getPluginResultData(@RequestBody Map request) throws Exception {
        String pipelineExeId = (String) request.get(Dict.PIPELINEEXEID);
        Integer stageIndex = (Integer) request.get(Dict.STAGEINDEX);
        Integer jobIndex = (Integer) request.get(Dict.JOBINDEX);
        Integer stepIndex = (Integer) request.get(Dict.STEPINDEX);
        return JsonResultUtil.buildSuccess(this.pluginResultService.getPluginResultData(pipelineExeId, stageIndex, jobIndex, stepIndex));
    }

}
