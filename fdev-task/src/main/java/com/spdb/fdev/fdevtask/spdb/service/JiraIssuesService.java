package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;

import java.util.Map;

public interface JiraIssuesService {

	JsonResult queryJiraIssues(Map<String, Object> requestParam);

	JsonResult updateJiraIssues(Map<String, Object> requestParam) throws Exception;

	JsonResult queryTestTask(Map<String, Object> requestParam);

	JsonResult saveTaskAndJiraIssues(Map<String, Object> requestParam);

	Map<String,Object> queryJiraStoryByKey(String key);

	String createJiraSubTask(FdevTask task);

	void updateJiraTaskStatus(String key,String destStatusId);

	void deleteJiraSubTask(String key);

	void updateJiraSubTask(String jiraKey,String developerId);

	Map<String,Object> queryJiraStory(String key);

}
