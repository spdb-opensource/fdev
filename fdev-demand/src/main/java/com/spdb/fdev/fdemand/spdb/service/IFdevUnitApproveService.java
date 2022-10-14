package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.OtherDemandTask;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;

public interface IFdevUnitApproveService {
    /**
     * 查询评估完成时间和XY值
     *
     * @throws Exception
     */
    Map<String, String> queryDemandAssessDate(Map<String, Object> params) throws Exception;
    /**
     * 申请超期审批（发送研发单元超期申请审批邮件）
     *
     * @param params
     * @throws Exception
     */
    void applyApprove(Map<String, Object> params) throws Exception;
    /**
     * 查询我的审批
     *
     * @throws Exception
     */
    Map<String,Object> queryMyApproveList(Map<String, Object> params) throws Exception;
    /**
     * 审批通过（支持批量审批）
     *
     * @throws Exception
     */
    void approvePass(Map<String, Object> params) throws Exception;
    /**
     * 审批拒绝（发送邮件，可填写拒绝原因）
     *
     * @throws Exception
     */
    void approveReject(Map<String, Object> params) throws Exception;
    /**
     * 查询审批列表
     *
     * @throws Exception
     */
    Map<String, Object> queryApproveList(Map<String, Object> params) throws Exception;
    /**
     * 导出审批列表
     *
     * @throws Exception
     */
    void exportApproveList(Map<String, Object> params, HttpServletResponse resp) throws Exception;

}
