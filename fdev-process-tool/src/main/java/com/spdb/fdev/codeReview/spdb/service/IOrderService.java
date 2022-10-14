package com.spdb.fdev.codeReview.spdb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.codeReview.spdb.entity.CodeOrder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
public interface IOrderService {

    CodeOrder add(CodeOrder codeOrder, @RequestParam MultipartFile[] requirementSpecification,
                  @RequestParam MultipartFile[] codeReviewTable, @RequestParam MultipartFile[] prototypeFigure,
                  @RequestParam MultipartFile[] demandInstructionBook) throws Exception;

    void update(CodeOrder codeOrder) throws Exception;

    void deleteOrderById(String orderId) throws Exception;

    /**
     *查询工单列表，可根据牵头人id、工单编号（模糊搜索）、需求id、牵头小组id、工单状态、申请日期分页查询
     * @return
     */
    Map queryOrders(Map param) throws Exception;

    /**
     * 查询工单详情
     * @param orderId
     * @return
     */
    CodeOrder queryOrderById(String orderId) throws Exception;

    /**
     * 任务模块发此接口更新关联的任务和工单的实际投产日期
     * @param codeOrder
     */
    void updateByTask(CodeOrder codeOrder) throws JsonProcessingException;

    /**
     * 导出工单列表
     * @param param
     */
    void exportOrderExcel(Map param, HttpServletResponse resp) throws Exception;

    /**
     *更新工单的审核人字段（工单下会议的审核人集合）
     * @param orderId
     */
    void updateAuditUser(String orderId) throws Exception;

    List<Map> queryListSimple() throws Exception;

    /**
     * 工单复审提醒，仅工单状态为“需线下复审”、“需会议复审” 且 当前登录用户为 代码审核角色可点击
     * 发送提醒邮件给工单创建人、牵头人、当前登录用户
     * @param orderId
     */
    void recheckRemind(String orderId) throws Exception;

    /**
     * 申请复审邮件，仅工单状态为“需线下复审”、“需会议复审”时可点击
     * @param orderId
     */
    void applyRecheck(String orderId, String recheckContent) throws Exception;
}
