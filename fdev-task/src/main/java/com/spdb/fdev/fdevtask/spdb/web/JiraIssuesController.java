package com.spdb.fdev.fdevtask.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.spdb.service.JiraIssuesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author huyz
 * @description task jira
 * @date 2021/1/19
 */
@RestController
@RequestMapping("/api/jira")
public class JiraIssuesController {

    private final Logger log = LoggerFactory.getLogger(JiraIssuesController.class);

    @Autowired
    private JiraIssuesService jiraIssuesService;

    //查询自己名下jira缺陷
    @PostMapping(value = "queryJiraIssues")
    public JsonResult queryJiraIssues(@RequestBody Map<String, Object> requestParam) throws Exception {
        return jiraIssuesService.queryJiraIssues(requestParam);
    }
    
    //修改缺陷其他信息
    @RequestValidate(NotEmptyFields = {Dict.ISSUEKEY,Dict.ID})
    @PostMapping(value = "updateJiraIssues")
    public JsonResult updateJiraIssues(@RequestBody Map<String, Object> requestParam) throws Exception {
        return jiraIssuesService.updateJiraIssues(requestParam);
    }
    
    //查询测试中的任务
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping(value = "queryTestTask")
    public JsonResult queryTestTask(@RequestBody Map<String, Object> requestParam) throws Exception {
        return jiraIssuesService.queryTestTask(requestParam);
    }
    
    //保存任务与jira缺陷
    @RequestValidate(NotEmptyFields = {Dict.ISSUEKEY,Dict.ISSUEID,Dict.TASKID,Dict.TASKNAME,Dict.RQRMNT_NO})
    @PostMapping(value = "saveTaskAndJiraIssues")
    public JsonResult saveTaskAndJiraIssues(@RequestBody Map<String, Object> requestParam) throws Exception {
        return jiraIssuesService.saveTaskAndJiraIssues(requestParam);
    }

    @RequestValidate(NotEmptyFields = {Dict.STORY_ID})
    @RequestMapping(value = "/queryJiraStoryByKey", method = RequestMethod.POST)
    public JsonResult queryJiraStoryByKey(@RequestBody Map params) throws Exception {
        return JsonResultUtil.buildSuccess(jiraIssuesService.queryJiraStory((String) params.get(Dict.STORY_ID)));
    }

}
