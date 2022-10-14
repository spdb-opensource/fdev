package com.gotest.dao;

import com.gotest.domain.WorkOrderStatus;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderDimensionMapper {

    List queryOrderDimension(@Param("startTime") String start, @Param("endTime") String end, @Param("groupIds") List<String> groupIds,
                             @Param("orderNameOrNo") String orderNameOrNo, @Param("tester") String tester,
                             @Param("stage") String stage, String orderType);

	List<WorkOrderStatus> queryWorkOrderStatus(@Param("startTime") String start, @Param("endTime") String end, @Param("groupIds") List<String> groupIds);

	List<Map<String, String>> queryPlanStatusCount(@Param("workNo") String workNo);

	Map<String, BigDecimal> queryTestResult(String workNo);

	List<Map<String, String>> exportReport(@Param("startDate") String startDate, @Param("endDate") String endDate) throws Exception;

	List<Map<String, Object>> queryDayGroupReport(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("groupId") String groupId) throws Exception;

	//查询该工单 案例信息
	Map queryOrderCaseInfo(@Param("workStr") String workNo, @Param("startDate") String startDate, @Param("endDate") String endDate) throws Exception;

	//查询工单列表
	List<Map<String, Object>> queryDayTotalWorks(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("groupFilter")String groupFilter) throws Exception;

	//查询已完成工单数（2020/7/3添加查询开发中工单数）
	List<Map<String, Object>> queryAlreadyOrder(@Param("startDate") String startDate, @Param("endDate") String endDate) throws Exception;

	//新工单完成报表
	List<Map<String, Object>> queryNewOrderDimension(@Param("startDate") String startDate, @Param("endDate") String endDate) throws Exception;

	Map<String, Object> queryDayGroupAllCase(@Param("workNo") String workNo) throws Exception;

	//查询选定时间段内特定测试人员的工单号（执行案例或新建案例）
	List<String> queryOrderNoByUser(@Param("startDate") String startDate,
									@Param("endDate") String endDate,
									@Param("userEnName") String userEnName) throws Exception;

	//查询选定时间段内特定测试人员的特定工单信息
	Map<String, String> queryOrderInfoByNo(@Param("workNo")String workNo,
										   @Param("startDate")String startDate,
										   @Param("endDate")String endDate,
										   @Param("userEnName")String userEnName) throws Exception;

	//根据组id和日期查当前完成内测的工单数
	Integer queryDayOrderDone(@Param("groupId")String groupId,
							  @Param("startDate")String startDate,
							  @Param("endDate")String endDate) throws Exception;

	//根据组id和日期查当前提交内测的工单数
	Integer queryDayOrderSubmit(@Param("groupId")String groupId,
							  @Param("startDate")String startDate,
							  @Param("endDate")String endDate) throws Exception;

	//查询待启动和待分配工单
	Map<String, Object> queryUndoOrder(@Param("groupId")String groupId) throws Exception;

	//趋势图
	Map<String, Integer> querytendencyChart(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("groupIds") List<String> groupIds) throws Exception;

	// 折线图 新建工单
	Integer queryNewWorkOrder(@Param("startDate")String startDate, @Param("endDate")String endDate, @Param("groupId")String groupId);

	// 折线图 进行中的工单
    Integer queryProcessingWorkOrder(@Param("startDate")String startDate, @Param("endDate")String endDate, @Param("groupId")String groupId);

    // 提测准时率
	List<Map<String, String>> sitSubmitTimely(@Param("fdevGroupId") String fdevGroupId,
											  @Param("startDate") String startDate,
											  @Param("endDate") String endDate) throws Exception;

	//冒烟测试通过率
	List<Map<String, String>> smokePass(@Param("fdevGroupId")String fdevGroupId,
										@Param("startDate") String startDate,
										@Param("endDate") String endDate) throws Exception;

	//根据fdev组查案例执行数
    List<Map<String, String>> queryExeTimeByFdevGroup(@Param("fdevGroupId") String fdevGroupId,
													  @Param("startDate") String startDate,
													  @Param("endDate") String endDate) throws Exception;

}
