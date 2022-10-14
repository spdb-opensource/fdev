package com.spdb.executor.service;

import com.csii.pe.spdb.common.dict.Constants;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.*;

import freemarker.template.Template;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@Service
public class SendEmailService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${is.wickedapp.sendmailPerson}")
    private String[] person;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private QueryUserService queryUserService;

    private String getContent(String templatePath, HashMap replace) throws Exception {
        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(templatePath);
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, replace);
    }

    public void sendMail(List<Map> apps) throws Exception {
        HashMap param1 = new HashMap<String, String>();
        Map param2 = new HashMap<>();
        Set<String> emails = new HashSet();
        String wickedAppInfo = "";
        param1.put(Dict.SUBJECT, Constants.EMAIL_WICKEDAPP);
        param1.put("template_name", Constants.TEMPLATE_WICKEDAPP);
        param1.put("path", Dict.FDEVJOBEXECUTOR);
        for (Map app : apps) {
            List<Map> map1 = (List<Map>) app.get("spdb_managers");
            for (Map map : map1) {
                String id = (String) map.get("id");
                Map person1 = queryUserService.queryUser(id);
                emails.add((String) person1.get("email"));
            }
            List<Map> map2 = (List<Map>) app.get("dev_managers");
            for (Map map : map2) {
                String id = (String) map.get("id");
                Map person2 = queryUserService.queryUser(id);
                emails.add((String) person2.get("email"));
            }
            wickedAppInfo += "fdev应用英文名:" + app.get("name_en") + "," + "对应gitlab项目地址:" + app.get("git") + ",    " + "gitlab项目id:" + app.get("gitlab_project_id") + "\n" + "    ";
        }
        for (String s : person) {
            emails.add(s);
        }
        String[] to = emails.toArray(new String[emails.size()]);
        param1.put("wickedAppInfo", wickedAppInfo);
        String content = getContent("index.ftl", param1);
        param2.put(Dict.SUBJECT, Constants.EMAIL_WICKEDAPP);
        param2.put(Dict.TO, to);
        param2.put(Dict.CONTENT, content);
        param2.put(Dict.REST_CODE, "sendEmail");
        try {
            restTransport.submit(param2);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("发送邮件失败" + e.getMessage());
        }
    }
}
