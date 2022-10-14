package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.executor.service.SendEmailService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

public class AppWickedNotifyScheduleAction extends BaseExecutableAction {

    @Value("${is.wickedapp.sendmail}")
    private boolean isSendMail;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private SendEmailService sendEmailService;

    @Override
    public void execute(Context context) {
        logger.info("execute AppWickedNotifySchedule begin");
        HashMap map1 = new HashMap();
        HashMap map2 = new HashMap();
        List<Map> wickedApps = new ArrayList<>();
        map1.put(Dict.REST_CODE, "queryAppGitlabId");
        try {
            //查询所有的应用
            List<Map> apps = (List<Map>) restTransport.submit(map1);
            for (Map app : apps) {
                map2.put(Dict.ID,app.get("id"));
                map2.put(Dict.REST_CODE, "queryProjectById");
                try {
                    //根据应用ID查询项目
                    restTransport.submit(map2);
                } catch (Exception e) {
                    if(e.getMessage().equals("无法获取相关数据!@@@@@404[fapp]")){
                        wickedApps.add(app);
                    }
                }
            }
            try {
                if(isSendMail&&!CommonUtils.isNullOrEmpty(wickedApps)){
                    sendEmailService.sendMail(wickedApps);
                }
            } catch (Exception e) {
                logger.error("异常应用通知邮件发送失败", e);
            }

        } catch (Exception e) {
            logger.error("查询应用失败", e);
            throw new FdevException("查询应用失败");
        }
        logger.info("execute AppWickedNotifySchedule end");
    }
}
