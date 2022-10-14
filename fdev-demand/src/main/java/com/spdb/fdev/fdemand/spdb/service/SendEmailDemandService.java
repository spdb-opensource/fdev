package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.common.User;
import com.spdb.fdev.fdemand.spdb.entity.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SendEmailDemandService {

    /**
     * 批量发送邮件——第一次评估超过14个自然日
     *
     * @throws Exception
     */
    void sendEmailAssessDemand() throws Exception;

    /**
     * 发送实施单元延期邮件
     *
     * @throws Exception
     */
    void sendEmailImplDelay(IpmpUnit ipmpUnit, DemandBaseInfo demandBaseInfo, Map<String, Map> userMap) throws Exception;

    /**
     * 发送邮件提醒用户挂载研发单元
     *
     * @throws Exception
     */
    void sendUserMountDevUnit(FdevImplementUnit fdevUnit, DemandBaseInfo demandBaseInfo) throws Exception;

    /**
     * 发送邮件提醒用户补充需求信息
     *
     * @throws Exception
     */
    void sendEmailUpdateDemand(DemandBaseInfo demandBaseInfo) throws Exception;

    /**
     * 实施单元未核算提醒邮件
     *
     * @throws Exception
     */
    void sendEmailIpmpUnitCheck(String implUnitNum) throws Exception;

    /**
     * 新增需求时向分析中的需求发送邮件
     *
     * @throws Exception
     */
    void sendEmailAssessSave(String id, String oaContactNo, String oaContactName, List<String> demandLeaderEmail) throws Exception;

    /**
     * 评估时长为7天时，fdev邮件发送需求牵头人、吴磊、杨静：“需求编号-需求名称已评估7天，请及时推进。”
     *
     * @param id
     * @param oaContactNo
     * @param oaContactName
     * @param demandLeaderEmail
     */
    void sendEmailAssess7(String id, String oaContactNo, String oaContactName, List<String> demandLeaderEmail) throws Exception;

    void sendEmailAssess11(String id, String oaContactNo, String oaContactName, List<String> demandLeaderEmail) throws Exception;

    void sendEmailAssess14(String id, String oaContactNo, String oaContactName, Integer assessDays, List<String> demandLeaderEmail) throws Exception;

    void sendEmailConfUpdateAssess(String id, String oaContactNo, String oaContactName, String oldState, String newState, String[] target) throws Exception;

    void sendEmailConfRemind(String confTitle, String confUrl, String[] target) throws Exception;

    /**
     * 申请研发单元审批邮件
     *
     * @throws Exception
     */
    void sendEmailApplyApprove(FdevImplementUnit fdevImplementUnit, DemandBaseInfo demandBaseInfo, FdevUnitApprove fdevUnitApprove, List<String> ccEmailList) throws Exception;

    /**
     * 研发单元审批拒绝邮件
     *
     * @throws Exception
     */
    void sendEmailApproveReject(FdevImplementUnit fdevImplementUnit, DemandBaseInfo demandBaseInfo, String approveRejectReason, List<String> emailList) throws Exception;

    /**
     * 研发单元延期邮件
     *
     * @throws Exception
     */
    void sendEmailFdevUnitOverdue(List<String> emailList, String titleHead, String fdevUnitNo, String fdevUnitName
            , String demandNo, String demandName, String demandId, String emailContent) throws Exception;

    /**
     * 研发单元审批通过邮件
     *
     * @throws Exception
     */
    void sendEmailApprovePass(FdevImplementUnit fdevImplementUnit, DemandBaseInfo demandBaseInfo, List<String> emailList) throws Exception;

    /**
     * 发送实施单元投产延期提醒邮件
     *
     * @param ipmpUnit
     */
    void sendEmailImplProDelay(IpmpUnit ipmpUnit, DemandBaseInfo demandBaseInfo, Map<String, Map> userMap) throws Exception;

    /**
     * 提交用户测试邮件
     *
     * @throws Exception
     */
    void sendSubmitTestOrder(HashMap<String,Object> testOrder, List<String> testOrderFileList, List<String> toEmailList , List<String> ccEmailList) throws Exception;

    /**
     * 撤销提测单邮件
     *
     * @throws Exception
     */
    void sendDeletedTestOrder(TestOrder testOrder, List<String> toEmailList , List<String> ccEmailList , User user ) throws Exception;

    /**
     * 编辑提测单邮件
     *
     * @throws Exception
     */
    void sendUpdateTestOrder(HashMap<String,Object> testOrder,List<String> testOrderFileList, List<String> toEmailList , List<String> ccEmailList , User user) throws Exception;

    /**
     * 归档提测单邮件
     *
     * @throws Exception
     */
    void sendFileTestOrder(TestOrder testOrder, List<String> toEmailList , List<String> ccEmailList ) throws Exception;

    /**
     * 研发单元提测提醒
     *
     * @throws Exception
     */
    void sendFdevUnitWarnDelay(FdevImplementUnit fdevImplementUnit, DemandBaseInfo demandBaseInfo, List<String> emailList) throws Exception;
}
