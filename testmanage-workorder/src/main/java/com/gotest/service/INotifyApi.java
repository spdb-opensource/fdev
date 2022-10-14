package com.gotest.service;

import java.util.Map;

public interface INotifyApi {

    void sendMail(String content, String subject, String[] to);

    /***
     * 发送用户通知
     * @param content    通知内容
     * @param target    通知对象集合
     * @param type        消息类型
     */
    void sendUserNotify(Map map) throws Exception;

    /**
     * 发送用户通知（fdev）
     *
     * @param map
     * @param fdevNew
     * @throws Exception
     */
    void sendUserNotifyForFdev(Map map, String fdevNew) throws Exception;

}
