package com.test.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.test.entity.Message;

@Repository
public interface MessageDao {

	/**
	 * 新增一条消息
	 */
	void addMessage(Message message) throws Exception;

	/**
	 * 查询消息
	 */
	List<Message> queryMessage(Message message) throws Exception;
	
	/**
	 * 查询用户消息
	 */
	List<Message> queryMessageUser(@Param("target") String target, @Param("type") String type) throws Exception;
	
	/**
	 * 更新消息状态
	 */
	Integer updateNotifyStatus(@Param("id") String id) throws Exception;
	
	/**
	 * 新增用户消息
	 */
	void addUserMessage(Message message) throws Exception;
	
	List<Message> queryNewMessage();

}
