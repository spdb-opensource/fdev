package com.fdev.notify.service;

import com.fdev.notify.entity.Message;

import java.text.ParseException;
import java.util.List;

public interface MessageService {

    /**
     * 新增一条消息
     */
    void addMessage(Message message);

    /**
     * 查询消息
     */
    List<Message> queryMessage(Message message) throws Exception;

    /**
     * 查询未读消息
     */
    List<Message> queryNoReadMessage(Message message) throws Exception;


    /**
     * 更新消息状态
     */
    void updateMessageStatus(List<String> ids);

    /**
     * 查询最新的公告
     */
    List<Message> queryNewAnnounce() throws Exception;

    /**
     * 查询最新的公告
     */
    List<Message> queryAnnounce() throws ParseException;

    /**
     * 查询刷新信息
     */
    List<Message> queryNewMessage();
    
    /**
     * 查询消息通过消息类型
     */
    List<Message> queryMessageByType(Message message) throws Exception;
}
