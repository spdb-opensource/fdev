package com.spdb.fdev.codeReview.spdb.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.codeReview.spdb.entity.CodeOrder;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
public interface IOrderDao {
    CodeOrder add(CodeOrder codeOrder);

    CodeOrder queryOrderById(String orderId);

    void update(CodeOrder codeOrder) throws Exception;

    /**
     * 查询工单列表，可根据牵头人id、工单编号（模糊搜索）、需求id、牵头小组id、工单状态、申请日期分页查询
     * @param param
     * @return
     */
    Map queryOrders(Map param, boolean isAuditRole, String userId);

    /**
     * 任务模块更新工单关联任务和实际投产日期
     * @param codeOrder
     */
    void updateByTask(CodeOrder codeOrder) throws JsonProcessingException;

    CodeOrder queryByOrderNo(String orderNo);

    long count();

    void deleteOrderById(String orderId);

    CodeOrder queryMaxNoThisYear(String year);

    List<CodeOrder> queryOrderByIds(Set<String> orderIds);

    List<CodeOrder> queryAll();
}
