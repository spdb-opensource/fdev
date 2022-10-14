package com.test.service;

import java.util.List;
import java.util.Set;

import com.test.entity.Message;

public interface MessageService {
	

    /**
     * 新增一条消息
     */
    void addMessage(Message message) throws Exception;

    /**
     * 查询消息
     */
    List<Message> queryMessage(Message message) throws Exception;
    
    /**
     * 查询最新的公告
     */
    List<Message> queryNewAnnounce() throws Exception;
    
    /**
     * 查询用户消息
     */
    List<Message> queryMessageUser(Message message) throws Exception;
    
    /**
     * 更新消息状态
     */
    Integer updateNotifyStatus(String id) throws Exception;
    
    /**
     * 新增用户消息
     */
    void addUserMessage(Message message) throws Exception;
    
    /**
     * 查询刷新信息
     */
    List<Message> queryNewMessage();

    /**
     * 查询消息类型列表
     * @return
     */
    Set queryMessageTypeList();

    /**
     * 一件已读
     * @param ids
     * @throws Exception
     */
    void batchUpdateNotiftStatus(List<String> ids) throws Exception;
}
