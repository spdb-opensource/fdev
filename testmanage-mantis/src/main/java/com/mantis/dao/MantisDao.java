package com.mantis.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.mantis.entity.MantisIssue;

@Repository
public interface MantisDao {

	List<MantisIssue> queryALLissue(@Param("start_page") Integer start_page
			, @Param("page_size") Integer page_size
			, @Param("reporter") String reporter
			, @Param("handler") String handler
			, @Param("status") String status
			, @Param("workNo") String workNo
			, @Param("startDate") String startDate
			, @Param("endDate") String endDate
			, @Param("env") String env
			, @Param("includeCloseFlag") String includeCloseFlag
			, @Param("id") String id
			, @Param("groupIds") List<String> groupIds
			, @Param("redmine_id") String redmine_id
			, @Param("app_name") String app_name
			, @Param("project_name") String project_name
			, @Param("task_no") String task_no
			, @Param("openTimes") String openTimes,
									String auditFlag) throws Exception;

	List<Map<String,String>> countReporterSum(@Param("startDate")String startDate, 
			@Param("endDate")String endDate
			,@Param("env")String env
	       ) throws Exception;
	
	int count(@Param("reporter") String reporter
            , @Param("handler") String handler
            , @Param("status") String status
            , @Param("workNo") String workNo
            , @Param("startDate") String startDate
            , @Param("endDate") String endDate
            , @Param("env") String env
            , @Param("includeCloseFlag") String includeCloseFlag
            , @Param("id") String id
            , @Param("groupIds") List<String> groupIds
            , @Param("redmine_id") String redmine_id
            , @Param("app_name") String app_name
            , @Param("project_name") String project_name
            , @Param("task_no") String task_no
            , @Param("openTimes") String openTimes,
              String auditFlag) throws Exception;

	MantisIssue queryIssueDetail(@Param("id")String id) throws Exception;

	List<MantisIssue> queryIssueByPlanResultId(@Param("id")String id,@Param("env")String env) throws Exception;

	List<MantisIssue> exportMantis(@Param("reporter") String reporter
			, @Param("handler") String handler
			, @Param("status") String status
			, @Param("workNo") String workNo
			, @Param("startDate") String startDate
			, @Param("endDate") String endDate
			, @Param("includeCloseFlag") String includeCloseFlag
			, @Param("env") String env
			, @Param("groupIds") List<String> groupIds
			, @Param("redmine_id") String redmine_id
			, @Param("app_name") String app_name
			, @Param("project_name") String project_name,
								   String auditFlag) throws Exception;


	List<MantisIssue> queryFuserMantis(@Param("start_page")Integer start_page
			,@Param("page_size")Integer page_size
			,@Param("handlers")String handlers
			,@Param("env")String env
	) throws Exception;

	Integer queryFuserMantisCount(@Param("handlers")String handlers,@Param("env")String env);

    List<MantisIssue> queryFuserMantisAll(@Param("handlers")String handlers,@Param("env")String env, @Param("includeCloseFlag")String includeCloseFlag) throws Exception;


	List<Map<String, String>> countWorkOrderSum(@Param("startDate")String startDate, 
			@Param("endDate")String endDate,@Param("env")String env) throws Exception;

	Map<String, Integer> queryWorkOrderIssues(@Param("workNo")String workNo, @Param("startDate")String startDate,
											  @Param("endDate")String endDate, @Param("env")String env) throws Exception;

	Map<String, Integer> queryOrderUnderwayIssues(String workNo) throws Exception;

	List<Map<String, String>> countMantisByWorkNo(@Param("workNo") String workNo) throws Exception;

	Map<String, Integer> queryOrderUnderwayIssues(String workNo,@Param("env")String env) throws Exception;

	List<Map<String, String>> countMantisByWorkNo(@Param("workNo") String workNo,@Param("env")String env) throws Exception;

	Map<String, String> queryIssueByTimeUser(@Param("workNo")String workNo,
											 @Param("userEnName")String userEnName,
											 @Param("startDate")String startDate,
											 @Param("endDate")String endDate,
											 @Param("env")String env
	                                         ) throws Exception;

	List<Map<String, Object>> countIssueDetailByOrderNos(@Param("workNos") String workNos,
														 @Param("startDate") String startDate,
														 @Param("endDate") String endDate,
														 @Param("env") String env,
														 @Param("groupIds") List<String> groupIds) throws Exception;

	List<Map<String, Object>> queryGroupIssueInfo(@Param("groupIds")List<String> groupIds,@Param("env")String env) throws  Exception;

    List<Map<String, String>> qualityReport(@Param("fdevGroupId") String fdevGroupId, @Param("env")String env) throws Exception;

    List<Map<String, String>> reopenIssue(@Param("fdevGroupId") String fdevGroupId, @Param("env")String env) throws Exception;

    List<Map<String, String>> solveTime(@Param("fdevGroupId") String fdevGroupId, @Param("env")String env) throws Exception;

	List<MantisIssue> queryTasksMantis(@Param("taskId")String taskId, @Param("env")String env, @Param("includeCloseFlag")String includeCloseFlag) throws Exception;

	List<MantisIssue> queryTasksMantisPage(@Param("taskId")String taskId, @Param("env")String env, @Param("start_page")Integer start_page, @Param("page_size")Integer page_size) throws Exception;

	Integer countTasksMantisPage(@Param("taskId")String taskId, @Param("env")String env) throws Exception;

    List<Map<String, String>> qualityReport(@Param("fdevGroupId") String fdevGroupId,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate,
                                            @Param("env")String env
											) throws Exception;

    List<Map<String, String>> reopenIssue(@Param("fdevGroupId") String fdevGroupId,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("env")String env
										  ) throws Exception;

    List<Map<String, String>> solveTime(@Param("fdevGroupId") String fdevGroupId,
                                        @Param("startDate") String startDate,
                                        @Param("endDate") String endDate,
                                        @Param("env")String env
										) throws Exception;

    List<Map<String, String>> qualityReportAll(@Param("env")String env) throws Exception;

    //查询缺陷总数
	int countTotal(@Param("workNo") String workNo,
				   @Param("userNameEn") String userNameEn,
				   @Param("env") String env) throws Exception;

	//查询缺陷
	List<MantisIssue> queryMantis(@Param("workNo") String workNo ,
								  @Param("userNameEn") String userNameEn,
								  @Param("status")   String status ,
								  @Param("env") String env
								 ) throws Exception;

	//工单查询缺陷
	List<MantisIssue> queryMantisByWorkNos(@Param("workNo") String workNo,@Param("env") String env);

	//修改缺陷状态为90，关闭
	int updateMantisStatus(@Param("ids") String ids);

	//根据任务ids关闭缺陷状态
	int updateMantisByTaskIds(@Param("taskId") String taskId,@Param("env") String env);

	List<Integer> queryMantisIdByTaskNo(String taskNo, String env);

	Map<String,Object> queryIssueById(String id);

	/**
	 * 修改缺陷审批标志
	 * @param id 缺陷编号
	 * @param auditFlag 0-待审核、1-审核完成
	 */
    void updateMantisAudit(String id, String auditFlag);

	/**
	 * 查询缺陷审批信息
	 * @param id
	 * @return
	 */
	Map<String, String> queryMantisAuditInfo(String id);

	/**
	 * 修改缺陷状态
	 * @param status
	 * @param id
	 */
	void updateMantisStatusById(String status, String id);

	/**
	 * 查询缺陷报告人英文名和缺陷摘要
	 * @param id
	 * @return
	 */
	Map<String, String> queryReportNameAndSummary(String id);

	/**
	 * 更新缺陷对应的字段表信息
	 * @param id
	 * @param wantStatus
	 * @param auditReason
	 * @param wantFlawSource
	 */
	void addFieldString(String id, String wantStatus, String auditReason, String wantFlawSource);

	/**
	 * 审核后修改缺陷信息
	 * @param id
	 * @param status
	 * @param reason
	 * @param flawSource
	 */
	void updateMantisByAudit(String id, String status, String reason, String flawSource);

	/**
	 * 更新历史记录表
	 * @param historyList
	 */
	void addMantisHistory(@Param("historyList") List<Map> historyList);

	/**
	 * 删除缺陷关联的字段表信息
	 * @param id
	 * @param fieldIds
	 */
    void deleteFieldString(String id, List<String> fieldIds);

	/**
	 * 查询关联字段是否已存在
	 * @param id
	 * @param fieldIds
	 * @return
	 */
	int queryFieldString(String id, List<String> fieldIds);

	/**
	 * 修改缺陷对应的字段表信息
	 * @param id
	 * @param wantStatus
	 * @param auditReason
	 * @param wantFlawSource
	 */
	void updateFieldString(String id, String wantStatus, String auditReason, String wantFlawSource);

	/**
	 * 提交缺陷时设置缺陷的环境字段
	 * @param id
	 * @param env
	 */
	void setMantisEnv(String id, String env);

	/**
	 * 查询一年内缺陷的工单号
	 * @param env
	 * @param time
	 * @return
	 */
	List<Map> queryMantisLastYear(String env, long time);

	/**
	 * 修改缺陷的小组信息
	 * @param id
	 * @param fdevGroupId
	 * @param fdevGroupName
	 */
    void updateMantisGroup(long id, String fdevGroupId, String fdevGroupName);

	/**
	 *
	 * @param startTime
	 * @param endTime
	 * @param groupIds
	 * @param env
	 * @return
	 */
    List<Map> countMantisByGroup(Long startTime, Long endTime, List<String> groupIds, String env);

	/**
	 * 查询测试人员在指定时间段内提出的有效缺陷
	 * @param startDate
	 * @param endDate
	 * @param userNameEnList
	 * @param env
	 * @return
	 */
	List<Map<String, String>> countReporterSumNew(String startDate, String endDate, List<String> userNameEnList, String env);

	/**
	 * 根据用户、工单集合和选择时间查缺陷总数，未修复缺陷数，当前该用户新建缺陷数
	 * @param workNoList
	 * @param userEnName
	 * @param startDate
	 * @param endDate
	 * @param env
	 * @return
	 */
	List<Map<String, String>> queryIssueByTimeUserNew(List<String> workNoList, String userEnName, String startDate, String endDate, String env);

	List<Long> queryMantisByWorkNo(String workNo);
}
