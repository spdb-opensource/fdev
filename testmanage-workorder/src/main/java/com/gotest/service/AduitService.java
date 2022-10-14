package com.gotest.service;

import com.gotest.domain.WorkOrderUser;

import java.util.List;
import java.util.Map;

/**
 * 待审核 工单接口
 */
public interface AduitService {
    List<WorkOrderUser> queryAduitOrder(Map map) throws Exception;

    Integer passAduit(Map map) throws Exception;

    Integer refuseAduit(Map map) throws Exception;

    Integer queryAduitOrderCount(Map map) throws Exception;
}
