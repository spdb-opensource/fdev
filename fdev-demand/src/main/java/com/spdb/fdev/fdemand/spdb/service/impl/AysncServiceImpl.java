package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.service.AsyncService;
import com.spdb.fdev.fdemand.spdb.service.IMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RefreshScope
@Service
public class AysncServiceImpl implements AsyncService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Value("${assess.demand.url}")
    private String assessDemandUrl;

    @Autowired
    private IMailService mailService;

    @Value("${demand.email.filePath}")
    private String filePath;

    @Value("${section.info}")
    private String sectionInfo;

    @Value("${my.updateFinalDate.approve.url}")
    private String updateFinalDateApproveUrl;


    @Override
    @Async
    public void sendDeferEmail(String title, String prefixContent, String suffixContent, String id, String oaContactNo, String oaContactName, List<String> demandLeaderEmail) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(title);
        String template = "demand_access_defer";
        String subject = stringBuffer.toString();
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.OA_CONTACT_NO, oaContactNo);
        model.put(Dict.OA_CONTACT_NAME, oaContactName);
        model.put("prefixContent", prefixContent);
        model.put("suffixContent", suffixContent);
        model.put(Dict.ASSESS_DEMAND_URL, assessDemandUrl + id);//需求链接地址
        String mailContent = mailService.getOverDueMsg(template, model);
        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailContent);
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);

        try {
            mailService.sendEmail(subject, template, sendMap, demandLeaderEmail, filePaths);
        } catch (Exception e) {
            logger.error("------" + title + "邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendUpdateFinalDate(String oaContactNo, String oaContactName, String sectionId) throws Exception {
        String subject = "修改定稿日期审批申请";
        String template = "apply_update_final_date";
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.OA_CONTACT_NO, oaContactNo);
        model.put(Dict.OA_CONTACT_NAME, oaContactName);
        model.put(Dict.MYAPPROVEURL, updateFinalDateApproveUrl);//我的审批Url
        // 获取收件人
        List<String> emailList = new ArrayList<>();
        emailList.addAll(CommonUtils.appendGroupReceiverNew(sectionInfo, sectionId));
        String mailContent = mailService.getOverDueMsg(template, model);
        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailContent);
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(subject, template, sendMap, emailList, filePaths);
        } catch (Exception e) {
            logger.error("------" + "【" + oaContactNo + "】-【" + oaContactName + "】" + subject + "邮件发送失败------" + e.getMessage());
        }

    }

    @Override
    @Async
    public void agreeUpdateFinalDate(String oaContactNo, String oaContactName, String userName, String id, List<String> sendEmailList) throws Exception {
        String subject = "同意修改定稿日期审批";
        String template = "agree_update_final_date";
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.OA_CONTACT_NO, oaContactNo);
        model.put(Dict.OA_CONTACT_NAME, oaContactName);
        model.put(Dict.OPERATE_USER_ID, userName);
        model.put(Dict.ASSESS_DEMAND_URL, assessDemandUrl + id);//需求链接地址
        String mailContent = mailService.getOverDueMsg(template, model);
        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailContent);
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(subject, template, sendMap, sendEmailList, filePaths);
        } catch (Exception e) {
            logger.error("------" + "【" + oaContactNo + "】-【" + oaContactName + "】" + subject + "邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    @Async
    public void refuseUpdateFinalDate(String oaContactNo, String oaContactName, String userName, String state, String id, List<String> sendEmailList) throws Exception {
        String subject = "拒绝修改定稿日期审批";
        String template = "refuse_update_final_date";
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.OA_CONTACT_NO, oaContactNo);
        model.put(Dict.OA_CONTACT_NAME, oaContactName);
        model.put(Dict.OPERATE_USER_ID, userName);
        model.put(Dict.STATE, state);
        model.put(Dict.ASSESS_DEMAND_URL, assessDemandUrl + id);//需求链接地址
        String mailContent = mailService.getOverDueMsg(template, model);
        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailContent);
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(subject, template, sendMap, sendEmailList, filePaths);
        } catch (Exception e) {
            logger.error("------" + "【" + oaContactNo + "】-【" + oaContactName + "】" + subject + "邮件发送失败------" + e.getMessage());
        }
    }
}
