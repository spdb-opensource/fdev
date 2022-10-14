package com.fdev.notify.service.impl;


import com.fdev.notify.dict.ErrorConstants;
import com.fdev.notify.service.IAlarmService;
import com.fdev.notify.service.MailService;
import com.spdb.fdev.common.exception.FdevException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;

@Service
@EnableAsync
@RefreshScope
public class AlarmServiceImpl implements IAlarmService {

    @Autowired
    private MailService mailService;

    /**
     * 发送邮件
     */
    @Async
    public void sendAlarmEmail(Set<String> to, String subject, String content) {
        try {
            mailService.sendEmail(new ArrayList<>(to), null, subject, content, null);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.EMAIL_SEND_ERROR);
        }
    }

}