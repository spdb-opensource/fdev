package com.spdb.fdev.fdevtask.spdb.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DemandService {
    Map queryByFdevNo(String rqrmnt_no, String fdev_implement_unit_no);

    Map queryDemandInfoDetail(String demand_id);

    List showDemandDoc(String demand_id);

    List<Map> queryDemandList();

    Map<String, Object> queryFdevImplUnitDetail(String unitNo) throws Exception;

    List<Map> queryFdevImplUnitByUnitNos(List<String> unitNos) throws Exception;

    List<Map<String, String>> queryDemandByIds(Set<String> ids) throws Exception;
}
