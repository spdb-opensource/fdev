package com.spdb.fdev.pipeline.web;

import com.spdb.fdev.base.annotation.nonull.NoNull;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.pipeline.dao.IJobEntityDao;
import com.spdb.fdev.pipeline.entity.JobEntity;
import com.spdb.fdev.pipeline.service.IJobEntityService;
import com.spdb.fdev.pipeline.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobEntityController {

    @Autowired
    private IJobEntityService jobEntityService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IJobEntityDao jobEntityDao;

    @PostMapping("/getAllJobs")
    @RequestValidate(NotEmptyFields = {Dict.PAGE_NUM, Dict.PAGE_SIZE})
    public JsonResult getAllJobs(@RequestBody Map<String, String> requestParam) throws Exception {
        String pageNum = requestParam.get(Dict.PAGE_NUM);
        String pageSize = requestParam.get(Dict.PAGE_SIZE);
        String searchContent = requestParam.get(Dict.SEARCHCONTENT);
        User user = userService.getUserFromRedis();
        List<Map> result = jobEntityService.getAllJobs(pageNum, pageSize, user, searchContent);
        return JsonResultUtil.buildSuccess(result);
    }

    @PostMapping("/getJobTemplateInfo")
    public JsonResult getJobTemplateInfo(@RequestBody Map<String, Object> param) throws Exception {
        String id = (String) param.get(Dict.ID);
        return JsonResultUtil.buildSuccess(jobEntityService.getJobTemplateInfo(id));
    }

    @PostMapping("/add")
    @NoNull(require = {Dict.NAME, Dict.STEPS}, parameter = JobEntity.class)
    public JsonResult add(@RequestBody JobEntity jobEntity) throws Exception {
        return JsonResultUtil.buildSuccess(jobEntityService.add(jobEntity));
    }

    @PostMapping("/update")
    @NoNull(require = {Dict.JOBID, Dict.NAMEID, Dict.NAME, Dict.STEPS}, parameter = JobEntity.class)
    public JsonResult update(@RequestBody JobEntity jobEntity) throws Exception {
        return JsonResultUtil.buildSuccess(jobEntityService.update(jobEntity));
    }

    @PostMapping("/delTemplate")
    @RequestValidate(NotEmptyFields = {Dict.JOBID})
    public JsonResult delete(@RequestBody Map requestParam) throws Exception {
        String jobId = (String) requestParam.get(Dict.JOBID);
        jobEntityService.delTemplate(jobId);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/updateVisibleRange")
    @RequestValidate(NotEmptyFields = {Dict.JOBID, Dict.VISIBLERANGE})
    public JsonResult updateVisibleRange(@RequestBody Map request) throws Exception {
        String jobId = (String) request.get(Dict.JOBID);
        String visibleRange = (String) request.get(Dict.VISIBLERANGE);
        JobEntity jobEntity = jobEntityService.updateVisibleRange(jobId, visibleRange);
        return JsonResultUtil.buildSuccess(jobEntity);
    }

    //当插件更新时，更新任务组模板中用到了该插件的任务组，重新组装其中的steps
    @PostMapping("/updateStepsInJobEntity")
    @RequestValidate(NotEmptyFields = {Dict.NAMEID,Dict.TYPE})
    public JsonResult updateStepsInJobEntity(@RequestBody Map request) throws Exception {
        String nameId = (String) request.get(Dict.NAMEID);
        String type = (String) request.get(Dict.TYPE);
        jobEntityService.updateStepsInJobEntity(nameId,type);
        return JsonResultUtil.buildSuccess();
    }

}
