package com.gotest.service;

import com.gotest.domain.OrderDimension;
import com.gotest.domain.WorkOrderStatus;
import com.gotest.domain.WorkOrderUser;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface OrderDimensionService {
    List<OrderDimension> queryOrderDimension(Map<String, Object> map) throws Exception;

    List<WorkOrderStatus> queryWorkOrderStatus(Map map) throws Exception;

    List<WorkOrderUser> queryUpSitOrder(Integer currentPage, Integer pageSize, String taskName, String workOrderNo, String userEnName, String done, String sortManager, String orderType) throws Exception;

    Integer queryUpSitOrderCount(String taskName, String workOrderNo, String userEnName, String done, String orderType);

    Map exportSitReportData(String workNo, Map map);

	List<Map<String, Object>> queryDayTotalReport(String startDate, String endDate, List<String> group, boolean isParent) throws Exception;

	void exportGroupStatement(Map map, HttpServletResponse resp) throws Exception;

	Object queryDayGroupReport(String startDate, String endDate, String groupId) throws Exception;

	List<Map<String,String>> exportReport(String startDate, String endDate) throws Exception;

    void exportDayGroupReport(Map<String,String> requestMap, HttpServletResponse resp) throws Exception;

    List<Map<String, String>> queryOrderInfoByUser(Map map) throws Exception;

    Map<String, Integer> queryGroupInfo(Map map) throws Exception;

    void exportPersonalDimensionReport(Map<String,Object> requestMap, HttpServletResponse resp) throws Exception;

    void exportOrderDimension(Map map, HttpServletResponse resp) throws Exception;

    Map<String, Object> tendencyChart(String dateType, String startDate, String endDate, List<String> groupIds) throws Exception;

    Map<String, Object> queryDiscountChart(Map map);

    void exportMantisStatement(Map map, HttpServletResponse resp) throws Exception;

    List<Map<String, String>> qualityReport(Map map) throws Exception;

    Map<String, Object> cacheQualityReport() throws Exception;

    List<Map<String, Object>> qualityReportNew(Map map, Map<String, Object> data) throws Exception;

    Map<String, Object> qualityReportNewUnit(Map map, Map<String, Object> data) throws Exception;
}
