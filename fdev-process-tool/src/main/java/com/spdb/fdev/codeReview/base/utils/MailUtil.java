package com.spdb.fdev.codeReview.base.utils;

import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.transport.RestTransport;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.config.xml.DefaultInboundChannelAdapterParser;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @Author liux81
 * @DATE 2021/11/15
 */
@Component
public class MailUtil {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Resource
    private RestTransport restTransport;
    @Value("${send.email.from}")
    private String sendBaseEmail;


    /**
     * @param templatePath 模版文件名
     * @param replace      替换的字段和value的map集合
     * @return 邮件正文
     * @throws Exception
     */
    public String getContent(String templatePath, HashMap replace) throws Exception {
        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(templatePath);
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, replace);
    }

    public void sendEmail(String subject, String templateName, HashMap<String, String> model, String... to) throws Exception {
        model.put(Dict.SUBJECT, subject);
        model.put(Dict.TEMPLATE_NAME, templateName);
        model.put(Dict.PATH, "fprocesstool");
        model.put(Dict.SEND_EMAIL, sendBaseEmail);
        String content = getContent("index.ftl", model);
        HashMap<String, Object> mailParams = new HashMap<>();
        mailParams.put(Dict.SUBJECT, subject);
        mailParams.put(Dict.CONTENT, content);
        mailParams.put(Dict.TO, to);
        mailParams.put(Dict.REST_CODE, Dict.SENDEMAIL);
        restTransport.submit(mailParams);
    }
}
