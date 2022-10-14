package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.service.IMailService;
import com.spdb.fdev.transport.RestTransport;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.HashMap;

@Service
public class MailServiceImpl implements IMailService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
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

    /**
     * 将邮件模版内容发送给前端进行确认
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @Override
    public String getMailContent(HashMap hashMap) throws Exception {
        //返回邮件模版内容
        return getContent("fcomponent_update.ftl", hashMap);
    }

    /**
     * mpass组件废弃优化内容
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @Override
    public String mpassMailDestroyContent(HashMap hashMap) throws Exception {
        //返回邮件模版内容
        return getContent("fcomponent_mpassdestroy.ftl", hashMap);
    }

    /**
     * 骨架使用组件版本是否最新版本检测提醒
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @Override
    public String archetypeNotifyContent(HashMap hashMap) throws Exception {
        //返回邮件模版内容
        return getContent("fcomponent_archetype_notify.ftl", hashMap);
    }

    /**
     * Mpass拉取开发分支提醒邮件
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @Override
    public String mpassMailDevNotifyContent(HashMap hashMap) throws Exception {
        //返回邮件模版内容
        return getContent("fcomponent_mpassdev_notify.ftl", hashMap);
    }


    @Override
    public void sendEmail(String subject, String templateName, HashMap<String, String> model, String... to) throws Exception {
        model.put("subject", subject);
        model.put("template_name", templateName);
        model.put("path", Dict.FCOMPONENT);
        String content = getContent("index.ftl", model);
        HashMap<String, Object> mailParams = new HashMap<>();
        mailParams.put("subject", subject);
        mailParams.put("content", content);
        mailParams.put("to", to);
        mailParams.put(Dict.REST_CODE, Dict.SEND_EAMIL);
        restTransport.submit(mailParams);
    }


}
