package com.fdev.notify.service.impl;

import com.fdev.notify.dao.MessageDao;
import com.fdev.notify.dict.Dict;
import com.fdev.notify.entity.Message;
import com.fdev.notify.service.MessageService;
import com.spdb.fdev.common.util.Util;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageDao messageDao;

    @Override
    public void addMessage(Message message) {
        messageDao.addMessage(message);
    }

    @Override
    public List<Message> queryMessage(Message message) throws Exception {
        List<Message> resList = messageDao.queryMessage(message);
        resList.addAll(queryNewMessage());
        return resList;
    }

    @Override
    public List<Message> queryNoReadMessage(Message message) throws Exception {
        message.setState(Dict.MESSAGE_UNREAD);
        return messageDao.queryMessage(message);
    }

    @Override
    public void updateMessageStatus(List<String> ids){
        messageDao.updateMessageStatus(ids);
    }


    @Override
    public List<Message> queryNewAnnounce() throws Exception {
        List<Message> messages = messageDao.queryAnnounce();
        List<Message> result = new ArrayList<>();
        if (messages.size() > 0) {
            for (Message message : messages) {
                Date date = Util.parseDate("yyyy-MM-dd HH:mm:ss", message.getCreate_time());
                long l = (new Date().getTime() - date.getTime()) / (24 * 60 * 60 * 1000);
                if (l < 3) {
                    if (message.getType().equals(Dict.ANNOUNCEUPDATE)) {
                        result.add(message);
                    } else if (!Util.isNullOrEmpty(message.getExpiry_time()) && message.getType().equals(Dict.ANNOUNCEHALT)) {
                        Date expiry_time = Util.parseDate("yyyy-MM-dd HH:mm:ss", message.getExpiry_time());
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
    public List<Message> queryAnnounce() throws ParseException {
        List<Message> messages = messageDao.queryAnnounce();
        if (messages.size() > 0) {
            for (Message message : messages) {
                Date date = Util.parseDate("yyyy-MM-dd HH:mm:ss", message.getCreate_time());
                long l = (new Date().getTime() - date.getTime()) / (24 * 60 * 60 * 1000);
                if (l < 3) {
                    if (message.getType().equals(Dict.ANNOUNCEUPDATE)) {
                        message.setIsNew("yes");
                    } else if (!Util.isNullOrEmpty(message.getExpiry_time()) && message.getType().equals(Dict.ANNOUNCEHALT)) {
                        Date expiry_time = Util.parseDate("yyyy-MM-dd HH:mm:ss", message.getExpiry_time());
                        if (expiry_time.after(new Date())) {
                            message.setIsNew("yes");
                        }
                    }
                }
            }
        }
        return messages;
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
    public List<Message> queryMessageByType(Message message) throws Exception {
        List<Message> resList = messageDao.queryMessageByType(message);
        return resList;
    }

}
