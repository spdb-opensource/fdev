package com.gotest.utils;

import com.gotest.controller.InformController;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.service.INotifyApi;
import com.gotest.service.IUserService;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.HashMap;
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
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private OrderUtil orderUtil;
    @Autowired
    private IUserService userService;

    private Logger logger = LoggerFactory.getLogger(InformController.class);

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
        //获取组名拼接主题
        String groupName = "";
        if(!Util.isNullOrEmpty(model.get(Dict.GROUPID))){
            try {
                Map<String, Object> group = userService.queryGroupDetailById(String.valueOf(model.get(Dict.GROUPID)));
                if(!Util.isNullOrEmpty(group)){
                    groupName = "【"+ String.valueOf(group.get(Dict.NAME)) + "】" ;
                }
            } catch (Exception e) {
                logger.error("fail to query fdev parent group info");
            }
        }
        String subject = groupName + split[0] + "(" + taskName + ")";
        String activeProfile = SpringContextUtil.getActiveProfile();
        if (!Util.isNullOrEmpty(subject) && !"pro".equals(activeProfile))
            subject = "【" + activeProfile + "】" + subject;
        String templateName = split[1];
        if (CommonUtils.isNullOrEmpty(to)) {
            logger.error("empty mail receiver");
            throw new FtmsException(ErrorConstants.PARAM_ERROR, new String[]{"发件人为空"});
        }
        model.put(Dict.SUBJECT, subject);
        model.put(Dict.TEMPLATE_NAME, templateName);
        String content = getContent("index.ftl", model);
        send(to, subject, content);
    }

    @Async
    public void sendSitReportMail(String subject, String templateName, HashMap model, String[] to) throws Exception {
        if (CommonUtils.isNullOrEmpty(to)) {
            throw new FtmsException(ErrorConstants.PARAM_ERROR, new String[]{"发件人为空"});
        }
        String activeProfile = SpringContextUtil.getActiveProfile();
        if (!Util.isNullOrEmpty(subject) && !"pro".equals(activeProfile))
            subject = "【" + activeProfile + "】" + subject;
        model.put("subject", subject);
        model.put("template_name", templateName);
        String content = getContent("index.ftl", model);
        send(to, subject, content);
    }

    @Async
    public void sendInnerTestMail(String subject, String templateName, HashMap model, String[] to) throws Exception {
        if (CommonUtils.isNullOrEmpty(to)) {
            throw new FtmsException(ErrorConstants.PARAM_ERROR, new String[]{"发件人为空"});
        }
        model.put("subject", subject);
        model.put("template_name", templateName);
        String content = getContent("index.ftl", model);
        send(to, subject, content);
    }

    /**
     * 发送邮件
     */
    private void send(String[] to, String subject, String content) {
        iNotifyApi.sendMail(content,subject,to);
    }



}