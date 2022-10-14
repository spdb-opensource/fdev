package com.gotest.service;

import com.gotest.domain.WorkOrder;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface RollbacksInfoService {

    void workOrderRollback(WorkOrder workOrder, String userName, String reason, String detailInfo,String taskId) throws  Exception;

    List queryRollbackReport(String mainTaskName, String groupId, String startDate, String endDate, String childGroupFlag, String developer, String reason) throws  Exception;

    void exportRollbackReport(String mainTaskName, String groupId, String startDate, String endDate, String childGroupFlag, HttpServletResponse resp, String developer, String reason) throws  Exception;

    void refuseTask(List<String> taskIds, String workNo) throws Exception;
}
