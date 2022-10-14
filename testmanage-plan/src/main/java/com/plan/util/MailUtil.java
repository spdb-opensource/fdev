package com.plan.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plan.dict.Constants;
import com.plan.dict.Dict;
import com.plan.dict.ErrorConstants;
import com.plan.service.INotifyApi;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
@RefreshScope
public class MailUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    WebApplicationContext applicationContext;

    @Autowired
    private INotifyApi iNotifyApi;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;


    @Value("${fdev.transport.log.data.enabled:true}")
    private boolean logDataEnabled = true;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private MyUtil myUtil;


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
        //获取组信息
        String mainTaskNo = String.valueOf(model.get(Dict.MAINTASKNO));
        String groupName = "";
        if(!Util.isNullOrEmpty(mainTaskNo)){
            try {
                Map sendFdev = new HashMap();
                sendFdev.put(Dict.ID, mainTaskNo);
                sendFdev.put(Dict.REST_CODE, "fdev.ftask.queryTaskDetail");
                sendFdev = (Map)restTransport.submitSourceBack(sendFdev);
                Map result = myUtil.getFtmsGroupId(String.valueOf(((Map)sendFdev.get(Dict.GROUP)).get(Dict.ID)));
                Map<String, String> sendMap = new HashMap<>();
                sendMap.put(Dict.ID, String.valueOf(result.get(Dict.FDEVGROUPID)));
                sendMap.put(Dict.REST_CODE, "queryChildGroupById");
                List<Map<String,String>> resultList = (List<Map<String, String>>) restTransport.submit(sendMap);
                groupName = "【" + resultList.get(0).get(Dict.NAME) + "】";
            } catch (Exception e) {
                logger.error("fail to query fdev parent group info");
            }
        }
        String subject = groupName + split[0];
        String activeProfile = SpringContextUtil.getActiveProfile();
        if (!Util.isNullOrEmpty(subject) && !"pro".equals(activeProfile))
            subject = "【" + activeProfile + "】" + subject;
        String templateName = split[1];
        if (Util.isNullOrEmpty(to)) {
            throw new FtmsException(ErrorConstants.PARAM_ERROR, new String[]{"发件人为空"});
        }
        model.put(Constants.SUBJECT, subject);
        model.put(Constants.TEMPLATE_NAME, templateName);
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