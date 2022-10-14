package com.spdb.fdev.pipeline.web;

import com.spdb.fdev.base.annotation.nonull.NoNull;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.pipeline.entity.PipelineTemplate;
import com.spdb.fdev.pipeline.service.IPipelineTemplateService;
import com.spdb.fdev.pipeline.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pipelineTemplate")
public class PipelineTemplateController {
    @Autowired
    private IPipelineTemplateService pipelineTemplateService;

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/delTemplate", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.PIPELINETEMPLATEID})
    public JsonResult delete(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.PIPELINETEMPLATEID);
        pipelineTemplateService.delTemplate(id);
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/queryMinePipelineTemplateList", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.PAGE_NUM, Dict.PAGE_SIZE})
    public JsonResult queryMinePipelineTemplateList(@RequestBody Map<String, String> requestParam) throws Exception {
        String pageNum = requestParam.get(Dict.PAGE_NUM);
        String pageSize = requestParam.get(Dict.PAGE_SIZE);
        String searchContent = requestParam.get(Dict.SEARCHCONTENT);
        User user = userService.getUserFromRedis();
        Map<String, Object> map = pipelineTemplateService.queryMinePipelineTemplateList(pageNum, pageSize, user, searchContent);
        return JsonResultUtil.buildSuccess(map);
    }

    @PostMapping("/add")
    @NoNull(require = {Dict.NAME, Dict.STAGES}, parameter = PipelineTemplate.class)
    public JsonResult add(@RequestBody PipelineTemplate template) throws Exception {
        return JsonResultUtil.buildSuccess(pipelineTemplateService.add(template));
    }

    @PostMapping("/queryById")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryById(@RequestBody Map request) throws Exception {
        String id = (String) request.get(Dict.ID);
        return JsonResultUtil.buildSuccess(pipelineTemplateService.queryById(id));
    }

    @PostMapping("/update")
    @NoNull(require = {Dict.NAME, Dict.ID, Dict.NAMEID, Dict.STAGES}, parameter = PipelineTemplate.class)
    public JsonResult update(@RequestBody PipelineTemplate template) throws Exception {
        return JsonResultUtil.buildSuccess(pipelineTemplateService.update(template));
    }


    /**
     * 查询历史流水线模版
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryPipelineTemplateHistory", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.NAMEID, Dict.PAGE_NUM, Dict.PAGE_SIZE})
    public JsonResult queryPipelineTemplateHistory(@RequestBody Map<String, String> requestParam) throws Exception {
        String nameId = requestParam.get(Dict.NAMEID);
        String pageNum = requestParam.get(Dict.PAGE_NUM);
        String pageSize = requestParam.get(Dict.PAGE_SIZE);
        Map<String, Object> resultMap = pipelineTemplateService.queryPipelineTemplateHistory(nameId, pageNum, pageSize);
        return JsonResultUtil.buildSuccess(resultMap);
    }

    @PostMapping("/copy")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult copy(@RequestBody Map request) throws Exception {
        String id = (String) request.get(Dict.ID);
        return JsonResultUtil.buildSuccess(pipelineTemplateService.copy(id));
    }

    /**
     * 修改模版可见范围
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/updateVisibleRange")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.VISIBLERANGE})
    public JsonResult updateVisibleRange(@RequestBody Map request) throws Exception {
        String id = (String) request.get(Dict.ID);
        String visibleRange = (String) request.get(Dict.VISIBLERANGE);
        PipelineTemplate pipelineTemplate = pipelineTemplateService.updateVisibleRange(id, visibleRange);
        return JsonResultUtil.buildSuccess(pipelineTemplate);
    }
}
