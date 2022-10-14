package com.spdb.fdev.codeReview.spdb.service;

import com.spdb.fdev.codeReview.spdb.entity.CodeOrder;
import com.spdb.fdev.common.User;

/**
 * @Author liux81
 * @DATE 2021/11/16
 */
public interface IMailService {
    /**
     * 工单状态为终态时，通知需求牵头人处理
     * @param codeOrder
     * @throws Exception
     */
    void orderFinishMail(CodeOrder codeOrder) throws Exception;

    /**
     * 工单创建时，通知代码审核角色和用户指定的人
     * @param codeOrder
     */
    void orderCreateMail(CodeOrder codeOrder, User user) throws Exception;

    /**
     * 复审提醒邮件，通知申请人、牵头人、当前登录用户
     * @param codeOrder
     */
    void recheckRemindMail(CodeOrder codeOrder) throws Exception;

    /**
     * 申请复审邮件，发送邮件给代码审核角色和当前登录用户
     * @param codeOrder
     */
    void applyRecheckMail(CodeOrder codeOrder) throws Exception;
}
