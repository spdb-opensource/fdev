package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.util.SpringContextUtil;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.service.IMailService;
import com.spdb.fdev.transport.RestTransport;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.HashMap;
import java.util.List;
@RefreshScope
@Service
public class MailServiceImpl implements IMailService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private RestTransport restTransport;

    @Value("${isSendEmail}")
    private boolean isSendEmail;

    /**
     * @param templatePath 模版文件名
     * @param replace      替换的字段和value的map集合
     * @return 邮件正文
     * @throws Exception
     */
    private String getContent(String templatePath, HashMap replace) throws Exception {
        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(templatePath);
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, replace);
    }

    /**
     * 将邮件模版内容发送给前端进行确认
     * 暂缓
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @Override
    public String getDeferMailContent(HashMap hashMap) throws Exception {
        //返回邮件模版内容
        return getContent(Dict.FDEMAND_DEFER_FTL, hashMap);
    }

    /**
     * 将邮件模版内容发送给前端进行确认
     * 恢复
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @Override
    public String getRecoverMailContent(HashMap hashMap) throws Exception {
        //返回邮件模版内容
        return getContent(Dict.FDEMAND_RECOVER_FTL, hashMap);
    }
    
    /**
     * 将邮件模版内容发送给前端进行确认
     * 录入
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @Override
    public String getInfoMailContent(HashMap hashMap) throws Exception {
        //返回邮件模版内容
        return getContent(Dict.FDEMAND_INFO_FTL, hashMap);
    }
    
    /**
     * 将邮件模版内容发送给前端进行确认
     * 更新
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @Override
    public String getUpdateMailContent(HashMap hashMap) throws Exception {
        //返回邮件模版内容
        return getContent(Dict.FDEMAND_UPDATE_FTL, hashMap);
    }
    
    /**
     * 将邮件模版内容发送给前端进行确认
     * Ui设计稿已上传
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @Override
    public String getUiUploadedMailContent(HashMap hashMap) throws Exception {
        //返回邮件模版内容
        return getContent(Dict.FDEMAND_UIUPLOADED_FTL, hashMap);
    }
    
    /**
     * 将邮件模版内容发送给前端进行确认
     *Ui设计稿待审核
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @Override
    public String getUiAuditWaitMailContent(HashMap hashMap) throws Exception {
        //返回邮件模版内容
        return getContent(Dict.FDEMAND_UIAUDITWAIT_FTL, hashMap);
    }
    
    /**
     * 将邮件模版内容发送给前端进行确认
     * Ui设计稿审核通过
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @Override
    public String getUiAuditPassMailContent(HashMap hashMap) throws Exception {
        //返回邮件模版内容
        return getContent(Dict.FDEMAND_UIAUDITPASS_FTL, hashMap);
    }
    
    /**
     * 将邮件模版内容发送给前端进行确认
     * Ui设计稿审核未通过
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @Override
    public String getUiAuditPassNotMailContent(HashMap hashMap) throws Exception {
        //返回邮件模版内容
        return getContent(Dict.FDEMAND_UIAUDITPASSNOT_FTL, hashMap);
    }

    @Override
    public void sendEmail(String subject, String templateName, HashMap<String, Object> model, List<String> to, List<String> filePaths) throws Exception {
        model.put("subject", subject);
        model.put("template_name", templateName);
        model.put("path", Dict.FDEMAND);
        String content = getContent("index.ftl", model);
        HashMap<String, Object> mailParams = new HashMap<>();
        mailParams.put("subject", subject);
        mailParams.put("content", content);
        mailParams.put("to", to);
        mailParams.put("filePaths", filePaths);
        mailParams.put(Dict.REST_CODE, "sendEmail");
        restTransport.submit(mailParams);
    }

    @Override
    public void sendEmail(String subject, String templateName, HashMap<String, Object> model, List<String> to,List<String> cc,  List<String> filePaths) throws Exception {
        model.put("subject", subject);
        model.put("template_name", templateName);
        model.put("path", Dict.FDEMAND);
        String content = getContent("index.ftl", model);
        HashMap<String, Object> mailParams = new HashMap<>();
        mailParams.put("subject", subject);
        mailParams.put("content", content);
        mailParams.put("to", to);
        mailParams.put("cc", cc);
        mailParams.put("filePaths", filePaths);
        mailParams.put(Dict.REST_CODE, "sendEmail");
        restTransport.submit(mailParams);
    }

    @Override
    public String getOverDueMsg(String template,HashMap hashMap) throws Exception {
        return this.getContent(template+".ftl",hashMap);
    }

    @Override
    public void sendEmail(String subject, String templateName, HashMap<String, String> model, String... to) throws Exception {
        model.put("subject", subject);
        model.put("template_name", templateName);
        model.put("path", Dict.FDEMAND);
        String content = getContent("index.ftl", model);
        HashMap<String, Object> mailParams = new HashMap<>();
        mailParams.put("subject", subject);
        mailParams.put("content", content);
        mailParams.put("to", to);
        mailParams.put(Dict.REST_CODE, Dict.SEND_EAMIL);
        restTransport.submit(mailParams);
    }

    @Override
    public void sendEmailPath(String subject, String templateName, HashMap<String, Object> model, List<String> to, List<String> filePaths) throws Exception {
        String activeProfile = SpringContextUtil.getActiveProfile();
        if (!CommonUtils.isNullOrEmpty(subject) && !"pro".equals(activeProfile))
            subject = "【" + activeProfile + "】" + subject;
        model.put("subject", subject);
        model.put("template_name", templateName);
        String content = getContent("index.ftl", model);
        HashMap<String, Object> mailParams = new HashMap<>();
        mailParams.put("subject", subject);
        mailParams.put("content", content);
        mailParams.put("to", to);
        mailParams.put("filePaths", filePaths);
        mailParams.put(Dict.REST_CODE, Dict.SEND_EAMIL);
        //发送邮件
        if (isSendEmail) {
            restTransport.submit(mailParams);
        }
    }
}
