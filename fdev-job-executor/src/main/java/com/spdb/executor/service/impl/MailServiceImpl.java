package com.spdb.executor.service.impl;

import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.dict.ErrorConstants;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.executor.service.MailService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.SpringContextUtil;
import com.spdb.fdev.transport.RestTransport;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Resource
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


    public void sendEmail(String subject, String templateName, HashMap<String, Object> model, List<String> to, List<String> filePaths) throws Exception {
        model.put(Dict.SUBJECT, subject);
        model.put(Dict.TEMPLATE_NAME, templateName);
        String content = getContent(Dict.INDEX_TEMPLATE, model);
        HashMap<String, Object> mailParams = new HashMap<>();
        mailParams.put(Dict.SUBJECT, subject); //主题
        mailParams.put(Dict.CONTENT, content); //模版
        mailParams.put(Dict.TO, to);       //发送给
        mailParams.put(Dict.FILEPATHS, filePaths); //附件地址
        mailParams.put(Dict.REST_CODE, Dict.SENDEMAIL);
        if (isSendEmail)
            restTransport.submit(mailParams);
    }


    @Async
    @Override
    public void sendEmail(String confKey, HashMap model, List<String> to, List<String> filePaths) throws Exception{
        Properties ret = PropertiesLoaderUtils
                .loadProperties(new ClassPathResource("email.properties"));
        String emailConf = ret.getProperty(confKey);
        String[] split = emailConf.split("[|]");
        String subject = split[0];
        String activeProfile = SpringContextUtil.getActiveProfile();
        if (!CommonUtils.isNullOrEmpty(subject) && !"pro".equals(activeProfile))
            subject = "【" + activeProfile + "】" + subject;
        String templateName = split[1];
        if (CommonUtils.isNullOrEmpty(to)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"发件人"});
        }
        sendEmail(subject, templateName, model, to, filePaths);
    }


}
