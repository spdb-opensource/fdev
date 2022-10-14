package com.gotest.dao;

import com.gotest.domain.WorkOrder;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BatchMapper {

    List<Map<String, String>> queryOrder() throws Exception;

    void setFdevGroupId(@Param("workNo") String workNo, @Param("fdevGroupId") String fdevGroupId) throws Exception;

    void setWorkManager(@Param("workNo") String workNo, @Param("workManager") String workManager) throws Exception;

    void setWorkLeader(@Param("workNo") String workNo, @Param("workLeader") String workLeader) throws Exception;

    List<String> queryproblemRqrmntNos() throws Exception;

    void setRqrNo(@Param("oaNoName") String oaNoName, @Param("problemRqrmntNo") String problemRqrmntNo) throws Exception;

    List<WorkOrder> queryFdevAllOrder() throws Exception;

    void setOrderDemandNo(@Param("workNo")String workNo, @Param("rqrmntNo")String rqrmntNo) throws Exception;

    void setOrderDemandName(@Param("workNo")String workNo,@Param("rqrmntName")String rqrmntName) throws Exception;

}
