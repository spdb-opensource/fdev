package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.dto.UpdateFinalDateQueryDto;
import com.spdb.fdev.fdemand.spdb.entity.FdevFinalDateApprove;

import java.util.List;
import java.util.Map;

public interface IFdevFinalDateApproveDao {

    FdevFinalDateApprove save(FdevFinalDateApprove fdevFinalDateApprove);

    Map<String, Object> queryAll(UpdateFinalDateQueryDto queryDto, List<String> accessIds);

    Map<String, Object> queryMyApprove(String sectionId, UpdateFinalDateQueryDto queryDto);

    FdevFinalDateApprove queryById(String id);

    List<FdevFinalDateApprove> listByIds(List<String> ids);

    FdevFinalDateApprove update(FdevFinalDateApprove fdevFinalDateApprove) throws Exception;

    List<FdevFinalDateApprove> updateBatch(UpdateFinalDateQueryDto queryDto);

    Map<String, Long> queryCount(String sectionId);
}
