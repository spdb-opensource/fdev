package com.plan.service.impl;

import com.plan.dict.Constants;
import com.plan.dict.ErrorConstants;
import com.plan.service.INotifyApi;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class INotifyApiImpl implements INotifyApi {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;


    @Override
    public void sendMail(String content, String subject, String[] to) {
        Map param = new HashMap<>();
        param.put(Constants.CONTENT,content);
        param.put(Constants.SUBJECT,subject);
        param.put(Constants.TO,to);
        try {
            restTransport.submitSourceBack(restTransport.getUrl("fnotify.host"), param);
        } catch (Exception e) {
            logger.error("发送邮件失败"+e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"发送邮件失败"});
        }
    }



}
