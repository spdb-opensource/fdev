package com.test.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public interface TestCaseService {
	List<Map> countUserTestCaseByTime(String groupId,String startDate, String endDate, String username, boolean isParent) throws Exception;

	void exportExcelUser(String startDate, String endDate, String groupId, String username, boolean isParent, HttpServletResponse resp) throws Exception;
}