package com.spdb.fdev.fuser.spdb.service.Impl;

import java.util.HashMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.service.MailService;
import com.spdb.fdev.transport.RestTransport;

import freemarker.template.Template;

@Service
@RefreshScope
public class MailServiceImpl implements MailService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Resource
    private RestTransport restTransport;
    @Value("${send.email.from}")
    private String sendBaseEmail;           //xxx


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

    @Override
    public void sendEmail(String subject, String templateName, HashMap<String, String> model, String... to) throws Exception {
        model.put("subject", subject);
        model.put("template_name", templateName);
        model.put("path", Dict.FUSER);
        model.put("send_email", sendBaseEmail);
        String content = getContent("index.ftl", model);
        HashMap<String, Object> mailParams = new HashMap<>();
        mailParams.put("subject", subject);
        mailParams.put("content", content);
        mailParams.put("to", to);
        mailParams.put(Dict.REST_CODE, "sendEmail");
        restTransport.submit(mailParams);
    }
}
