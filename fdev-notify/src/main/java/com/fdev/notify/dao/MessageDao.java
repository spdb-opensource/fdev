package com.fdev.notify.dao;

import com.fdev.notify.entity.Message;

import java.util.List;

public interface MessageDao {

    /**
     * 新增一条消息
     */
    void addMessage(Message message);

    /**
     * 查询消息
     */
    List<Message> queryMessage(Message message) throws Exception;

    /**
     * 修改消息状态为已读
     */
    void updateMessageStatus(List<String> ids);

    /**
     * 查询最新公告
     */
    List<Message> queryAnnounce();

    List<Message> queryNewMessage();
    
    /**
     * 通过消息类型查询消息
     */
    List<Message> queryMessageByType(Message message) throws Exception;
}
