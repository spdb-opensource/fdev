package com.fdev.notify.service;

import java.util.Set;

public interface IAlarmService {

    /**
     * 去除 环境显示 的 发送 告警邮件
     * @param toAlarmEmail
     * @param subject
     * @param content
     */
    void sendAlarmEmail(Set<String> toAlarmEmail, String subject, String content);
}
