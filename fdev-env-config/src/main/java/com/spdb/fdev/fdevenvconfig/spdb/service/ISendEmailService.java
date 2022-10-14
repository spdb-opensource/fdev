package com.spdb.fdev.fdevenvconfig.spdb.service;

import java.util.Map;

public interface ISendEmailService {
    /**
     * 更新实体属性事件，邮件通知
     *
     * @param model
     * @throws Exception
     */
    void sendSubjectModelUpdateEmail(Map model);

    /**
     * 实体删除/实体映射删除，邮件通知
     *
     * @param model
     * @throws Exception
     */
    void sendSubjectModelDeleteEmail(Map model);

    /**
     * 实体删除/实体映射删除，邮件通知
     *
     * @param model
     * @throws Exception
     */
    void sendSubjectModelEnvUpdateEmail(Map model);

    /**
     * 删除整个实体与环境映射事件，邮件通知
     *
     * @param model
     * @throws Exception
     */
    void sendSubjectModelEnvDeleteEmail(Map model);

    /**
     * 审核卡点邮件通知
     *
     * @param model
     */
    void sendModelEnvUpdateappEmail(Map model);
}
