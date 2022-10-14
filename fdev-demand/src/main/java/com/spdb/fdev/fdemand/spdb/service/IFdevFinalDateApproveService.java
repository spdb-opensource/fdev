package com.spdb.fdev.fdemand.spdb.service;


import com.spdb.fdev.fdemand.spdb.dto.UpdateFinalDateQueryDto;
import com.spdb.fdev.fdemand.spdb.entity.FdevFinalDateApprove;

import java.util.Map;

public interface IFdevFinalDateApproveService {

    /**
     * 分页查询定稿日期审批功能
     *
     * @param queryDto
     */
    Map<String, Object> queryApproveList(UpdateFinalDateQueryDto queryDto) throws Exception;

    Map<String, Object> queryMyList(UpdateFinalDateQueryDto queryDto) throws Exception;

    String agree(UpdateFinalDateQueryDto updateDto) throws Exception;

    void refuse(FdevFinalDateApprove updateDto) throws Exception;

    Map<String, Long> queryCount() throws Exception;
}
