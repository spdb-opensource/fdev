package com.spdb.fdev.fdevtask.spdb.service;

import java.util.List;
import java.util.Map;

public interface RequirementApi {

    boolean updateRqrmntState(String rqrmntId,String stage,Map time);

    //{"type":"","state":"","keyword":"","relevant":false,"userid":"","groupid":"","datetype":"","stateNum":"","featureType":"","featureNum":"","size":-1,"index":-1,"sortBy":"","descending":false}
    Object query(boolean priority);

    Map queryRqrmntInfo(String rqrmnt_no);

    List<Map> queryRqrmntsByRqrmntNo(String rqrmntNo);

    Map queryRqrmntById(String rqrmntId);

    Boolean updateImplUnitState(String unitNo, String stage, Map time);

    Boolean updateDemandState(String unitNo, String stage, Map time);

}
