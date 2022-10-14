package com.spdb.fdev.fdevtask.spdb.dao;

import java.util.List;

import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.TaskJira;


public interface JiraIssuesDao {

	String queryJiraIssues(String requestJson, String queryUrl, String jiraUser);
	
	List queryTaskAndJiraIssues(String taskId);

	String updateJiraIssues(String jsonString, String updateUrl, String jiraUserAndPwd);

	List<FdevTask> queryTestTask(String userId);

	void saveTaskAndJiraIssues(TaskJira taskJira);

	TaskJira queryTaskName(String key);
	
	void putJiraIssues(String jsonString, String updateUrl, String jiraUserAndPwd);

	String createJiraSubTask(String jsonString, String updateUrl, String jiraUserAndPwd);

	void deleteJiraSubTask(String key, String updateUrl, String jiraUserAndPwd);
}
