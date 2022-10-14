package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.entity.DemandDoc;

import java.util.List;

public interface IDemandDocDao {


    public List queryDemandDocPagination(Integer start, Integer pageSize, String demand_id, String demand_doc_type, String demand_kind);

    public Long queryCountDemandDoc(String demand_id, String demand_doc_type, String demand_kind);

    List<DemandDoc> queryAll(DemandDoc demand) throws Exception;

    public List<DemandDoc> query(DemandDoc demand) throws Exception;

    DemandDoc save(DemandDoc demandDoc);

    DemandDoc queryById(String id);

    DemandDoc updateById(DemandDoc demandDoc) throws Exception;

    void deleteById(String id);

    void saveBatch(List<DemandDoc> demandDocList) throws Exception;

}
