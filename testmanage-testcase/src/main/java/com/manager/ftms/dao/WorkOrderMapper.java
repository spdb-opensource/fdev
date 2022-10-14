package com.manager.ftms.dao;

import com.manager.ftms.entity.WorkOrder;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface WorkOrderMapper {

    WorkOrder queryWorkOrderByNo(@Param("workOrderNo") String workOrderNo);

    List<String> queryTaskByNo(@Param("workNo")String workNo) throws Exception;

    List<Map<String, String>> queryPlanByOrderNo(@Param("workNo") String workNo) throws Exception;
}


