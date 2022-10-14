package com.mantis.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.mantis.entity.MantisIssue;

public interface MantisService {

	Map query(String currentPage, String pageSize, String reporter, String handler, String status, String workNo,
			  String startDate, String endDate, String includeCloseFlag, String id, List<String> groupIds, String redmine_id,
			  String app_name, String project_name, String taskNo, String openTimes, String auditFlag, Boolean isIncludeChildren) throws Exception;

	void update(Map<String, Object> map) throws Exception;

	List<Map<String, String>> countReporterSum(String startDate, String endDate) throws Exception;

	Map<String, Object> queryIssueDetail(String id) throws Exception;
	
	void delete(Map<String, String> map) throws Exception;

	List<MantisIssue> queryIssueByPlanResultId(String id) throws Exception;

	void exportMantis(String reporter, String handler, String status, String workNo, String startDate, String endDate,
					  String includeCloseFlag, List<String> groupIds, String redmine_id, String app_name, String project_name, String auditFlag, Boolean isIncludeChildren, HttpServletResponse resp) throws Exception;

	List<Map<String, String>> countWorkOrderSum(String startDate, String endDate) throws Exception;

	Map<String,Integer> queryWorkOrderIssues(String workNo, String startDate, String endDate) throws Exception;

	Map<String, Integer> queryOrderUnderwayIssues(String workNo) throws Exception;

	List<Map<String, String>> countMantisByWorkNo(String workNo) throws Exception;

	Map<String, String> queryIssueByTimeUser(Map map) throws Exception;

	void batchGroupInfo() throws Exception;

	Map<String, Object> countIssueDetailByOrderNos(List<String> workNo, String startDate, String endDate, List<String> groupIds) throws Exception;

	Map<String, Integer> queryGroupIssueInfo(List<String> groupIds) throws Exception;

	void updateMantis(Map map);

	Map<String, Object> qualityReport(String fdevGroup, String startDate, String endDate) throws Exception;

	List<Map<String, String>> qualityReportAll() throws Exception;

    List<Integer> queryMantisIdByTaskNo(String taskNo);

	/**
	 * 审批缺陷
	 * @param id
	 */
    void auditMantis(String id, String auditFlag) throws Exception;

	/**
	 * 校验是否能审核
	 * @param id
	 * @param handlerId
	 * @return
	 */
	Boolean checkAuditAuthority(String id, String handlerId) throws Exception;

	/**
	 * 查询待审核缺陷信息
	 * @param id
	 * @return
	 */
	Map<String, String> queryAuditMantisInfo(String id);

	List<Map> countMantisByGroup(String startDate, String endDate, List<String> groupIds);

    Map countReporterSumNew(String startDate, String endDate, List<String> userNameEnList);

	List<Map<String, String>> queryIssueByTimeUserNew(List<String> workNoList, String userEnName, String startDate, String endDate);
}
