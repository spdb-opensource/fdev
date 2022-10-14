package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.service.IMailService;
import com.spdb.fdev.fdevenvconfig.spdb.service.ISendEmailService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RefreshScope
public class ISendEmailServiceImpl implements ISendEmailService {
    @Autowired
    private IMailService mailService;
    @Value("${sendEmailAppSize}")
    private Integer sendEmailAppSize;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    @Async
    public void sendSubjectModelUpdateEmail(Map model){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(Dict.SENDEMAILAPP_SIZE,sendEmailAppSize);
        //附件模板
        map.put(Dict.ENCLOSURE_TEMPLETE,"enclosure.ftl");
        map.put(Dict.MODEL_NAME_EN,model.get(Dict.MODEL_NAME_EN));
        map.put(Dict.MODEL_NAME_CN,model.get(Dict.MODEL_NAME_CN));
        map.put(Dict.DELETE, model.get(Dict.DELETE));
        map.put(Dict.VERSION, StringUtils.isBlank((String) model.get(Dict.VERSION))? "无":model.get(Dict.VERSION));
        map.put(Dict.APP, model.get(Dict.APP));
        List<String> email = new ArrayList<String>();
        Set<String> emailTo = (Set<String>) model.get(Dict.EMAIL);
        email.addAll(emailTo);
        try {
            mailService.sendEmail(Constants.EMAIL_SUBJECT_MODEL_UPDATE, Constants.TEMPLATE_SUBJECT_MODEL_UPDATE,map, email);
        } catch (Exception e) {
            logger.info("发送邮件失败");
        }
    }

    @Override
    @Async
    public void sendSubjectModelDeleteEmail(Map model){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(Dict.SENDEMAILAPP_SIZE,sendEmailAppSize);
        //附件模板
        map.put(Dict.ENCLOSURE_TEMPLETE,"enclosure.ftl");
        map.put(Dict.MODEL_NAME_EN,model.get(Dict.MODEL_NAME_EN));
        map.put(Dict.MODEL_NAME_CN,model.get(Dict.MODEL_NAME_CN));
        map.put(Dict.DELETE, model.get(Dict.DELETE));
        map.put(Dict.VERSION, StringUtils.isBlank((String) model.get(Dict.VERSION))? "无":model.get(Dict.VERSION));
        map.put(Dict.APP, model.get(Dict.APP));
        List<String> email = new ArrayList<String>();
        Set<String> emailTo = (Set<String>) model.get(Dict.EMAIL);
        email.addAll(emailTo);
        try {
            mailService.sendEmail(Constants.EMAIL_SUBJECT_MODEL_DELETE, Constants.TEMPLATE_SUBJECT_MODEL_DELETE,map, email);
        } catch (Exception e) {
            logger.info("发送邮件失败");
        }
    }

    @Override
    @Async
    public void sendSubjectModelEnvUpdateEmail(Map model){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(Dict.SENDEMAILAPP_SIZE,sendEmailAppSize);
        //附件模板
        map.put(Dict.ENCLOSURE_TEMPLETE,"enclosure.ftl");
        map.put(Dict.MODEL_NAME_EN,model.get(Dict.MODEL_NAME_EN));
        map.put(Dict.MODEL_NAME_CN,model.get(Dict.MODEL_NAME_CN));
        map.put(Dict.ENV_NAME_EN,model.get(Dict.ENV_NAME_EN));
        map.put(Dict.ENV_NAME_CN,model.get(Dict.ENV_NAME_CN));
        map.put(Dict.VERSION,  StringUtils.isBlank((String) model.get(Dict.VERSION))? "无":model.get(Dict.VERSION));
        List<Map<String, Object>> upDate = (List<Map<String, Object>>) model.get(Dict.UPDATE);
        map.put(Dict.UPDATED, upDate);
        map.put(Dict.APP, model.get(Dict.APP));
        map.put(Dict.MODIFY_REASON,model.get(Dict.MODIFY_REASON));
        map.put(Dict.APPLY_USERNAME,model.get(Dict.APPLY_USERNAME));
        List<String> email = new ArrayList<String>();
        Set<String> emailTo = (Set<String>) model.get(Dict.EMAIL);
        email.addAll(emailTo);
        try {
            mailService.sendEmail(Constants.EMAIL_SUBJECT_MODEL_ENV_UPDATE, Constants.TEMPLATE_SUBJECT_MODEL_ENV_UPDATE,map, email);
        } catch (Exception e) {
            logger.info("发送邮件失败");
        }
    }

    @Override
    @Async
    public void sendSubjectModelEnvDeleteEmail(Map model){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(Dict.SENDEMAILAPP_SIZE,sendEmailAppSize);
        //附件模板
        map.put(Dict.ENCLOSURE_TEMPLETE,"enclosure.ftl");
        map.put(Dict.MODEL_NAME_EN,model.get(Dict.MODEL_NAME_EN));
        map.put(Dict.MODEL_NAME_CN,model.get(Dict.MODEL_NAME_CN));
        map.put(Dict.ENV_NAME_EN,model.get(Dict.ENV_NAME_EN));
        map.put(Dict.ENV_NAME_CN,model.get(Dict.ENV_NAME_CN));
        map.put(Dict.VERSION, StringUtils.isBlank((String) model.get(Dict.VERSION))? "无":model.get(Dict.VERSION));
        List<Map<String, Object>> upDate = (List<Map<String, Object>>) model.get(Dict.UPDATE);
        map.put(Dict.UPDATED, upDate);
        map.put(Dict.APP, model.get(Dict.APP));
        List<String> email = new ArrayList<String>();
        Set<String> emailTo = (Set<String>) model.get(Dict.EMAIL);
        email.addAll(emailTo);
        try {
            mailService.sendEmail(Constants.EMAIL_SUBJECT_MODEL_ENV_DELETE, Constants.TEMPLATE_SUBJECT_MODEL_ENV_DELETE,map, email);
        } catch (Exception e) {
            logger.info("发送邮件失败");
        }
    }

    @Override
    @Async
    public void sendModelEnvUpdateappEmail(Map model){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(Dict.SENDEMAILAPP_SIZE,sendEmailAppSize);
        //附件模板
        map.put(Dict.ENCLOSURE_TEMPLETE,"enclosure.ftl");
        map.put(Dict.MODEL_NAME_EN,model.get(Dict.MODEL_NAME_EN));
        map.put(Dict.MODEL_NAME_CN,model.get(Dict.MODEL_NAME_CN));
        map.put(Dict.ENV_NAME_CN,model.get(Dict.ENV_NAME_CN));
        map.put(Dict.APPLY_USERNAME,model.get(Dict.APPLY_USERNAME));
        map.put(Dict.URL,model.get(Dict.URL));
        List<Map<String, Object>> upDate = (List<Map<String, Object>>) model.get(Dict.UPDATE);
        map.put(Dict.UPDATED, upDate);
        map.put(Dict.APP, model.get(Dict.APP));
        List<String> email = new ArrayList<String>();
        Set<String> emailTo = (Set<String>) model.get(Dict.EMAIL);
        email.addAll(emailTo);
        try {
            mailService.sendEmail(Constants.EMAIL_MODEL_ENV_UPDATE_APP, Constants.TEMPLATE_MODEL_ENV_UPDATE_APP,map, email);
        } catch (Exception e) {
            logger.info("发送邮件失败");
        }
    }
}
