package com.plan.service;

import java.util.List;
import java.util.Map;


public interface MantisFlawService {

	void addMantisFlaw(Map<String,Object> map) throws Exception;

	List<Map<String,Object>> queryMantisProjects() throws Exception;

	boolean isTestcaseAddIssue(String planlist_testcase_id) throws Exception;

    Object updateMantisEmail(Map map) throws Exception;

	void addProIssue(Map<String, Object> map) throws Exception;

	List<String> queryTaskNoByWorkNos(List<String> workNos);

	List<Map> queryAppByWorkNo(String workNo) throws Exception;

    String querymainTaskNoByWorkNo(String workNo) throws Exception;
}
