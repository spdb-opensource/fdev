package com.test.service.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import javax.annotation.Resource;

import com.test.dict.Constants;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.test.dao.MessageDao;
import com.test.dict.Dict;
import com.test.entity.Message;
import com.test.service.MessageService;
import com.test.utils.MyUtils;
@Service
public class MessageServiceImpl implements MessageService {

    private final static Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

	@Resource
	private MessageDao messageDao;

	@Override
	public void addMessage(Message message) throws Exception {
		message.setId(new ObjectId().toString());
		message.setCreate_time(MyUtils.formatDate(MyUtils.STANDARDDATEPATTERN));
		messageDao.addMessage(message);
	}

	@Override
	public List<Message> queryMessage(Message message) throws Exception {
		return messageDao.queryMessage(message);
	}

	@Override
	public List<Message> queryNewAnnounce() throws Exception {
		List<Message> messages = messageDao.queryMessage(null);
        List<Message> result = new ArrayList<>();
		if (messages.size() > 0) {
            for (Message message : messages) {
                Date date = MyUtils.parseDate("yyyy-MM-dd HH:mm:ss", message.getCreate_time());
                long l = (new Date().getTime() - date.getTime()) / (24 * 60 * 60 * 1000);
                if (l < 3) {
                    if (message.getType().equals(Dict.ANNOUNCEUPDATE)) {
                        result.add(message);
                    } else if (!MyUtils.isEmpty(message.getExpiry_time()) && message.getType().equals(Dict.ANNOUNCEHALT)) {
                        Date expiry_time = MyUtils.parseDate("yyyy-MM-dd HH:mm:ss", message.getExpiry_time());
                        if (expiry_time.after(new Date())) {
                            result.add(message);
                        }
                    }
                }
            }
        }
        return result;
	}
	
	@Override
	public List<Message> queryMessageUser(Message message) throws Exception {
		List<Message> resList = messageDao.queryMessageUser(message.getTarget(), message.getType());
        resList.addAll(queryNewMessage());
        return resList;
	}

	@Override
	public Integer updateNotifyStatus(String id) throws Exception {	
		return messageDao.updateNotifyStatus(id);
			
	}
	
	@Override
	public void addUserMessage(Message message) throws Exception {
		message.setId(new ObjectId().toString());
		message.setCreate_time(MyUtils.formatDate(MyUtils.STANDARDDATEPATTERN));
		messageDao.addUserMessage(message);
	}

	@Override
    public List<Message> queryNewMessage() {
        List<Message> resList = messageDao.queryNewMessage();
        if (resList.size() > 0) {
            String tempDate = "";
            Message resMsg = null;
            for (Message message : resList) {   //获取最大时间
                if (tempDate.compareTo(message.getCreate_time()) < 1) {
                    tempDate = message.getCreate_time();
                    resMsg = message;
                }
            }
            resList.clear();
            resList.add(resMsg);
        }
        return resList;
    }

    @Override
    public Set queryMessageTypeList() {
        Properties fp = new Properties();
        try (InputStream in = ClassLoader.getSystemResourceAsStream(Constants.MESSAGE_TYPE_FILE_NAME);
             InputStreamReader reader = new InputStreamReader(in, "UTF-8");){
             fp.load(reader);
        } catch (Exception e) {
            logger.info(e.toString());
        }
        Set<Object> set = fp.keySet();
        return set;
    }

    @Override
    public void batchUpdateNotiftStatus(List<String> ids) throws Exception {
        for (String id: ids){
            try {
                messageDao.updateNotifyStatus(id);
            }catch (Exception e){
                logger.error("update messer error, id="+id);
            }
        }
    }
}
