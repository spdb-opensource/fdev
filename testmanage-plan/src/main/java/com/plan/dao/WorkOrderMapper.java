package com.plan.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface WorkOrderMapper {

    String queryMainTaskNoByWorkNo(@Param("workNo") String workNo) throws Exception;

    String queryWorkFlagByWorkNo(@Param("workNo") String workNo) throws Exception;

	void updateIssueTime(@Param("fnlIssueTm")long fnlIssueTm , @Param("id")String id) throws Exception;

	String queryWorkManagerByWorkNo(@Param("workNo")String workNo) throws  Exception;

    String queryMainTaskNameByWorkNo(@Param("workNo") String workNo) throws Exception;

    Map<String, String> queryFtmsPeopleByWorkNo(@Param("workNo")String workNo) throws Exception;

    String queryFtmsGroupId(@Param("fdevGroupId")String fdevGroupId) throws Exception;

    List<String> queryTaskNoByWorkNos(@Param("workNos")List<String> workNos);

    List<String> queryTaskNosByWorkNo(@Param("workNo")String workNo);

    String queryDemandNameByWorkNo(@Param("workNo")String workNo);

    List<String> queryTaskNamesByWorkNo(@Param("workNo")String workNo);

    String queryTaskNameByTaskNo(@Param("taskNo")String taskNo);

    String queryUnitNoByWorkNo(@Param("workNo")String workNo);

    Map<String, String> queryWorkOrderByTaskNo(@Param("taskNo") String taskNo);

    String queryFdevNewByWorkNo(@Param("workNo") String workNo);

    Map<String, String> queryWorkOrderByNo(String workNo);
}
