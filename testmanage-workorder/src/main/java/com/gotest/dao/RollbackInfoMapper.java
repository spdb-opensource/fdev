package com.gotest.dao;

import com.gotest.domain.RollbackInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RollbackInfoMapper {

    void addRollbackInfo(RollbackInfo rollbackInfo) throws Exception;

    List<Map<String, Object>> countOrderRollback(String mainTaskName, String groupId, String startDate, String endDate, String reason) throws Exception;

    List<RollbackInfo> queryByTaskNo(@Param("taskNo") String taskNo, String orderType);

    Integer updateWorkNoById(@Param("workNo") String workNo, @Param("id") String id);

    Integer queryRollbackCount(@Param("taskNo") String taskNo, @Param("workNo") String workNo);

    Integer updateWorkNoByTaskNos(@Param("taskIds") List<String> taskIds, @Param("newWorkNo") String newWorkNo);

    Integer updateRollBackInfoByTaskNos(@Param("taskIds") List<String> taskIds, @Param("newWorkNo") String newWorkNo,
                                        @Param("mainTaskName") String mainTaskName, @Param("fdevGroupId") String fdevGroupId);
}
