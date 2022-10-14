package com.spdb.fdev.fdevtask.base.utils;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.SpringContextUtil;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.spdb.service.INotifyApi;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
public class MailUtil {
    @Autowired
    WebApplicationContext applicationContext;

    @Autowired
    private INotifyApi iNotifyApi;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

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

    @Async
    public void sendTaskMail(String confKey, HashMap model, String taskName, String[] to) throws Exception {
        Properties ret = PropertiesLoaderUtils
                .loadProperties(new ClassPathResource("application-email.properties"));
        String emailConf = ret.getProperty(confKey);
        String[] split = emailConf.split("[|]");
        String subject = "【" + split[0] + "】" + "(" + taskName + ")";
        String templateName = split[1];
        if (CommonUtils.isNullOrEmpty(to)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"发件人为空"});
        }
        String path = applicationContext.getServletContext().getContextPath();
        path = path.substring(1);
        model.put("subject", subject);
        model.put("template_name", templateName);
        model.put("path", path);
        String content = getContent("index.ftl", model);
        send(to, subject, content);
    }

    @Async()
    public void sendReviewRecordEmail(String confKey, Map keys, List to, String db_type) throws Exception {
        Properties ret = PropertiesLoaderUtils
                .loadProperties(new ClassPathResource("application-email.properties"));
        String emailConf = ret.getProperty(confKey);
        String[] split = emailConf.split("[|]");
        String taskName = (String) keys.get("taskName");
        String rqrmntNum = (String) keys.get("rqrmntNum");
        String subject = "[" + db_type + "]" + split[0] + "(" + rqrmntNum + taskName + ")";
        String activeProfile = SpringContextUtil.getActiveProfile();
        if (!Util.isNullOrEmpty(subject) && !"pro".equals(activeProfile))
            subject = "【" + activeProfile + "】" + subject;
        String templateName = split[1];
        if (CommonUtils.isNullOrEmpty(to)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"发件人为空"});
        }
        String path = applicationContext.getServletContext().getContextPath();
        path = path.substring(1);
        keys.put("subject", subject);
        keys.put("template_name", templateName);
        keys.put("path", path);
        String content = getContent("index.ftl", (HashMap) keys);
        send((String[]) to.toArray(new String[to.size()]), subject, content);
    }


    public void sendNocodeTaskMail(String confKey,HashMap map, String[] to) throws Exception {
        Properties ret = PropertiesLoaderUtils
                .loadProperties(new ClassPathResource("application-email.properties"));
        String emailConf = ret.getProperty(confKey);
        String[] split = emailConf.split("[|]");
        String subject = split[0] + "(" + map.get(Dict.TASKNAME) + ")";
        subject = "【" + map.get("stage") + "】" + subject;
        String templateName = split[1];
        if (CommonUtils.isNullOrEmpty(to)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"发件人为空"});
        }
        String path = applicationContext.getServletContext().getContextPath();
        path = path.substring(1);
        map.put("subject", subject);
        map.put("template_name", templateName);
        map.put("path", path);
        String content = getContent("index.ftl", map);
        send(to, subject, content);
    }

    /**
     * 发送邮件
     */
    private void send(String[] to, String subject, String content) {
        iNotifyApi.sendMail(content, subject, to);
    }

    @Async
    public void sendTaskReviewMail(String confKey, HashMap model, String title, String taskName, String[] to) throws Exception {
        Properties ret = PropertiesLoaderUtils
                .loadProperties(new ClassPathResource("application-email.properties"));
        String emailConf = ret.getProperty(confKey);
        String[] split = emailConf.split("[|]");
        String subject = title + split[0];
        String activeProfile = SpringContextUtil.getActiveProfile();
        if (!Util.isNullOrEmpty(subject) && !"pro".equals(activeProfile))
            subject = "【" + activeProfile + "】" + subject;
        String templateName = split[1];
        if (CommonUtils.isNullOrEmpty(to)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"收件人为空"});
        }
        String path = applicationContext.getServletContext().getContextPath();
        path = path.substring(1);
        model.put("subject", subject);
        model.put("template_name", templateName);
        model.put("path", path);
        String content = getContent("index.ftl", model);
        send(to, subject, content);
    }

}