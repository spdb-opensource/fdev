package com.mantis.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.mantis.entity.ProIssue;


@Repository
public interface ProIssueDao {

	void addProIssueReleaseNode(String id, String release_node_name) throws  Exception;

    List<ProIssue> queryUserProIssues(@Param("user_name_en")String user_name_en, @Param("env")String env) throws Exception;

	List<ProIssue> queryTaskProIssues(@Param("task_id")String task_id,  @Param("env")String env) throws Exception;

	List<ProIssue> queryProIssues(@Param("start_page")Integer start_page, @Param("page_size")Integer page_size, @Param("start_time")String start_time,
			@Param("end_time")String end_time, @Param("module")List<String> module,
			@Param("responsible_type")String responsible_type, @Param("responsible_name_en")String responsible_name_en, 
			@Param("deal_status")String deal_status, @Param("issue_level")String issue_level, @Param("env")String env
			, @Param("reviewerStatus")String reviewerStatus, @Param("problem_type")String problem_type, @Param("sortParam")String sortParam, @Param("sortord")String sortord) throws Exception;

	List<Map> countProIssues(@Param("start_time")String start_time,
			@Param("end_time")String end_time, @Param("module")List<String> module,
			@Param("responsible_type")String responsible_type, @Param("responsible_name_en")String responsible_name_en, 
			@Param("deal_status")String deal_status, @Param("issue_level")String issue_level, @Param("env")String env
			, @Param("reviewerStatus")String reviewerStatus,  @Param("problem_type")String problem_type) throws Exception;

	ProIssue queryProIssueById(@Param("id")String id) throws Exception;

	List<Map> queryProByTeam(Map map) throws Exception;

	List<ProIssue> queryProIssueByReleaseNode(@Param("relaseNodeName")String relaseNodeName, @Param("env")String env)throws Exception;

	List<ProIssue> queryProIssueByReleaseNodeList(@Param("relaseNodeName")Set<String> relaseNodeName, @Param("env")String env)throws Exception;
}
