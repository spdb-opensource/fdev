package com.fdev.notify.web;

import com.fdev.notify.config.AlarmEmailContext;
import com.fdev.notify.dict.ErrorConstants;
import com.fdev.notify.service.IAlarmService;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * @author xxx
 * @date 2019/9/5 14:13
 */
@RestController
public class AlarmController {

    @Resource
    private IAlarmService alarmService;
    @Autowired
    private AlarmEmailContext emailContext;

    private static StringBuilder contentBuilder;

    static {
        InputStream stream = AlarmController.class.getClassLoader().getResourceAsStream("alarm.html");
        assert stream != null;
        try (Reader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            contentBuilder = new StringBuilder(350);
            br.lines().forEach(contentBuilder::append);
        } catch (IOException e) {
            throw new FdevException(ErrorConstants.EMAIL_SEND_ERROR, new String[]{"告警邮件模板不存在!"});
        }
    }

    /**
     * 监控 告警 邮件
     *
     * @param params
     * @return
     */
    @PostMapping("/alarmEmail")
    public JsonResult alarmEmail(@RequestBody Map<String, Object> params) throws Exception {
        Set<String> toAlarmEmail = this.emailContext.getAlarmEmail(params);
        String content = getFileContextAndReplace(params);
        String subject = getSubject(params, "[fdev k8s] ${} 告警邮件");
        this.alarmService.sendAlarmEmail(toAlarmEmail, subject, content);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 获取邮件主题
     * @param params
     * @return
     */
    private String getSubject(Map<String, Object> params, String oldSubject) {
        final String newSubject;
        JSONArray jsonArray = JSONArray.fromObject(params.get("alerts"));
        boolean flag = false;
        for (Object alarmObj : jsonArray) {
            if (alarmObj instanceof  Map) {
                Map alarmMap = (Map) alarmObj;
                String branch = (String) alarmMap.get("branch");
                if ("master".equalsIgnoreCase(branch)) {
                    flag = true;
                }
            }
        }
        if (flag) {
            newSubject = oldSubject.replace("${}", "master");
        } else {
            newSubject = oldSubject.replace("${}", "");
        }
        return newSubject;
    }

    /**
     * 获得文件模板 并替换其内容
     *
     * @param params
     * @return
     */
    private String getFileContextAndReplace(Map<String, Object> params) throws Exception {
        JSONArray jsonArray = JSONArray.fromObject(params.get("alerts"));
        StringBuilder labelsBuilder = new StringBuilder();
        StringBuilder trtdHtmlBuilders = new StringBuilder();
        for (Object alarmObj : jsonArray) {
            if (alarmObj instanceof Map) {
                Map alarmMap = (Map) alarmObj;
                Map labels = (Map) alarmMap.get("labels");
                Map annotations = (Map) alarmMap.get("annotations");
                String alertname = (String) labels.get("alertname");
                String instance = (String) labels.get("instance");
                String team = (String) labels.get("team");
                String summary = (String) annotations.get("summary");
                String description = (String) annotations.get("description");

                String generatorURL = (String) alarmMap.get("generatorURL");
                int start = generatorURL.indexOf("/") + 2;
                int end = generatorURL.indexOf("/", start);
                String substring = generatorURL.substring(start, end);
                String result = generatorURL.replace(substring, "xxx:9002");

                labelsBuilder.append(alertname).append(", ");
                StringBuilder trtdHtmlBuilder = new StringBuilder();
                trtdHtmlBuilder.append("<tr>")
                        .append("<td class=\"content-block\">")
                        .append("<strong>Labels</strong><br/>")
                        .append("alertname : ").append(alertname).append(" <br/>")
                        .append("instance : ").append(instance).append(" <br/>")
                        .append("team : ").append(team).append(" <br/>")
                        .append("summary : ").append(summary).append(" <br/>")
                        .append("description : ").append(description).append(" <br/>")
                        .append("<a href=\"").append(result).append("\">Source</a><br/>")
                        .append("</td>")
                        .append("</tr>");
                trtdHtmlBuilders.append(trtdHtmlBuilder);
            }
        }
        int lastd = labelsBuilder.lastIndexOf(",");
        String title = jsonArray.size() + " alert for Alertname = " + labelsBuilder.replace(lastd, lastd + 1, "");

        String email = contentBuilder.toString();
        return email.replace("{{ title }}", title)
                .replace("{{ fireCount }}", String.valueOf(jsonArray.size()))
                .replace("{{ trtd }}", trtdHtmlBuilders.toString());
    }
}