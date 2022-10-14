package com.spdb.fdev.fdemand.spdb.service;

import java.util.List;

/**
 * 异步操作统一接口
 */
public interface AsyncService {
    /**
     * 异步发送暂缓或者取消暂缓邮件
     */
    void sendDeferEmail(String title, String prefixContent, String suffixContent, String id, String oaContactNo, String oaContactName, List<String> demandLeaderEmail) throws Exception;


    /**
     * 异步发送修改定稿日期审批申请邮件
     *
     * @param oaContactNo   需求编号
     * @param oaContactName 需求名称
     * @param sectionId     条线id
     * @throws Exception
     */
    void sendUpdateFinalDate(String oaContactNo, String oaContactName, String sectionId) throws Exception;

    /**
     * 异步发送同意审批邮件
     * @param oaContactNo 需求编号
     * @param oaContactName 需求名称
     * @param userName 审批人姓名
     * @param id 需求评估id
     * @param sendEmailList 邮件发送目标
     * @throws Exception
     */
    void agreeUpdateFinalDate(String oaContactNo, String oaContactName, String userName, String id, List<String> sendEmailList) throws Exception;

    /**
     * 异步发送拒绝审批邮件
     * @param oaContactNo 需求编号
     * @param oaContactName 需求名称
     * @param userName 审批人姓名
     * @param state 拒绝说明
     * @param id 需求评估id
     * @param sendEmailList 邮件发送目标
     * @throws Exception
     */
    void refuseUpdateFinalDate(String oaContactNo, String oaContactName, String userName, String state, String id, List<String> sendEmailList) throws Exception;
}
