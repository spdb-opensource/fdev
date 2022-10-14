package com.fdev.database.spdb.service.Impl;

import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.service.MailService;
import com.spdb.fdev.transport.RestTransport;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.*;

@Service
public class MailServiceImpl implements MailService {
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Resource
    private RestTransport restTransport;

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
    public void sendEmail(String subject, String templateName, HashMap<String, String> model, List<String> to) throws Exception {
        model.put(Dict.SUBJECT, subject);
        model.put(Dict.TEMPLATE_NAME, templateName);
        model.put(Dict.PATH, Dict.FDATABASE);
        String content = getContent("index.ftl", model);
        HashMap<String, Object> mailParams = new HashMap<>();
        mailParams.put(Dict.SUBJECT, subject);
        mailParams.put(Dict.CONTENT, content);
        mailParams.put(Dict.TO, to);
        mailParams.put(Dict.REST_CODE,"sendEmail");
        restTransport.submit(mailParams);
    }

    @Override
    public void sendUserNotify(String content, List<String> target, String desc, String type, String hyperlink) throws Exception {
        HashMap<String, Object> notifyParams = new HashMap<>();
        notifyParams.put(Dict.CONTENT, content);
        notifyParams.put(Dict.TARGET, target);
        notifyParams.put(Dict.DESC, desc);
        notifyParams.put(Dict.HYPERLINK, hyperlink);
        notifyParams.put(Dict.TYPE, type);
        notifyParams.put(Dict.REST_CODE,"sendUserNotify");
        restTransport.submit(notifyParams);
    }
    
}
