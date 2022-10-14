package com.gotest.service.serviceImpl;

import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.service.INotifyApi;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RefreshScope
public class INotifyApiImpl implements INotifyApi {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;
    @Value("${ftms.tui.sitMsg.page}")
    private String sitMsgPage;

    @Override
    public void sendMail(String content, String subject, String[] to) {
        Map param = new HashMap<>();
        param.put(Dict.CONTENT, content);
        param.put(Dict.SUBJECT, subject);
        param.put(Dict.TO, to);
        param.put(Dict.REST_CODE, "fnotify.send.email");
        try {
            restTransport.submitSourceBack(param);
        } catch (Exception e) {
            logger.error("发送邮件失败" + e.getMessage());
            throw new FtmsException(com.gotest.dict.ErrorConstants.DATA_QUERY_ERROR, new String[]{"发送邮件失败"});
        }
    }

    @Override
    public void sendUserNotify(Map map) throws Exception {
        Map param = new HashMap<>();

        param.put(Dict.CONTENT, map.get("content"));
        param.put(Dict.TYPE, map.get("type"));
        param.put(Dict.TARGET, map.get("target"));
        param.put(Dict.REST_CODE, "ftms.send.message");
        param.put(Dict.LINKURI, map.get(Dict.LINKURI));
        if (!Util.isNullOrEmpty(map.get(Dict.ID))) {
            String id = String.valueOf(map.get(Dict.ID));
            StringBuilder linkUri = new StringBuilder(sitMsgPage).append(id);
            param.put(Dict.LINKURI, linkUri);
        }
        try {
            restTransport.submitSourceBack(param);
        } catch (Exception e) {
            logger.error("发送通知失败" + e.getMessage());
            throw new FtmsException(com.gotest.dict.ErrorConstants.DATA_QUERY_ERROR, new String[]{"发送通知失败"});
        }

    }

    @Override
    public void sendUserNotifyForFdev(Map map, String fdevNew) throws Exception {
        Map param = new HashMap<>();
        param.put(Dict.CONTENT, map.get("content"));
        param.put(Dict.TYPE, map.get("type"));
        param.put(Dict.DESC, map.get(Dict.DESC));
        param.put(Dict.TARGET, map.get("target"));
        param.put(Dict.REST_CODE, Constants.NUMBER_1.equals(fdevNew) ? "new.fnotify.send.message" : "fnotify.send.message");
        try {
            restTransport.submitSourceBack(param);
        } catch (Exception e) {
            logger.error("发送通知失败" + e.getMessage());
            throw new FtmsException(com.gotest.dict.ErrorConstants.DATA_QUERY_ERROR, new String[]{"发送通知失败"});
        }

    }

}
