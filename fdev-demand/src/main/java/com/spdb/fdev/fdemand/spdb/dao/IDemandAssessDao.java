package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.dto.DemandAssessDto;
import com.spdb.fdev.fdemand.spdb.entity.DemandAssess;
import com.spdb.fdev.fdemand.spdb.entity.DictEntity;

import java.util.List;
import java.util.Map;

public interface IDemandAssessDao {

    /**
     * 通过需求id或需求编号进行查询
     *
     * @param id
     * @param oaContactNo
     * @param demandStatus
     * @return
     */
    DemandAssess queryByIdOrNo(String id, String oaContactNo, Integer demandStatus);

    DemandAssess save(DemandAssess demandAssess);

    DemandAssess queryById(String id);

    DemandAssess queryByNo(String no);

    DemandAssess queryByConfUrl(String url);

    Map<String, Object> query(DemandAssessDto dto);

    DemandAssess update(DemandAssess demandAssess) throws Exception;

    List<DemandAssess> queryByStatus(Integer demandStatus);

    List<DictEntity> queryOverdueTypeById(String id);
}
