package com.mantis.service;

import com.mantis.entity.MantisIssue;

import java.util.List;
import java.util.Map;

public interface MantisFdevService {

	List<MantisIssue> queryFuserMantis(String currentPage, String pageSize, List<Map<String, String>> userList) throws Exception;

	Integer queryFuserMantisCount(List<Map<String, String>> userList) throws Exception;

    List<MantisIssue> queryFtaskMantis(String currentPage, String pageSize, List<String> taskList) throws Exception;

	Integer queryFtaskMantisCount(List<String> taskList) throws Exception;

	String updateFdevMantis(Map map) throws Exception;

	List<MantisIssue> queryFuserMantisAll(List<Map<String, String>> userList, String includeCloseFlag) throws  Exception;

	List<MantisIssue> queryFtaskMantisAll(List<String> taskList, String includeCloseFlag) throws Exception;
	
	Map<String,Object> queryUser(String username_en)throws Exception;

	void deleteTaskIssue(String task_id) throws Exception;

	List<Map> queryMantisTask(Map map);

	/**
	 * 提交缺陷时设置缺陷的环境字段
	 * @param id
	 */
    void setMantisEnv(String id);

    void updateAllMantisGroup();

    void updateMantisGroupByWorkNo(String workNo, String groupId);
}
