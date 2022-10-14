package com.fdev.notify.config;

import com.fdev.notify.util.CommonUtils;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 获取 发送邮件 相关内容
 *
 * @author xxx
 * @date 2019/9/9 15:41
 */
@Component
public class AlarmEmailContext {

    @Autowired
    private AlarmEmailConfig emailConfig;

    /**
     * 根据邮件内容 获取邮件发送人
     * @param params
     * @return
     */
    public Set<String> getAlarmEmail(Map<String, Object> params) {
        JSONArray jsonArray = JSONArray.fromObject(params.get("alerts"));
        Set<String> result = new LinkedHashSet<>();
        for (Object alarmObj : jsonArray) {
            if (alarmObj instanceof Map) {
                Map alarmMap = (Map) alarmObj;
                if (alarmMap.containsKey("labels")) {
                    Map labels = (Map) alarmMap.get("labels");
                    String alertName = (String) labels.get("alertname");
                    Object alertNameList = CommonUtils.getMethodByGet(emailConfig, alertName);
                    if (alertNameList instanceof List) {
                        result.addAll((List<String>) alertNameList);
                    } else {
                        result.addAll(emailConfig.getOthers());
                    }
                }

            }
        }
       return result;
    }
}
