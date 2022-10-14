package com.fdev.notify.dao.impl;

import com.fdev.notify.dao.MessageDao;
import com.fdev.notify.dict.Dict;
import com.fdev.notify.entity.Message;
import com.fdev.notify.util.CommonUtils;
import com.fdev.notify.util.NotifyUtil;
import net.sf.json.JSONObject;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;

@Repository
public class MessageDaoImpl implements MessageDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void addMessage(Message message) {
        mongoTemplate.save(message);
    }

    @Override
    public List<Message> queryMessage(Message message) throws Exception {

        JSONObject pJson = NotifyUtil.objectToJsonObject(message);
        Iterator<Object> it = pJson.keys();
        Query query = new Query();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECT_ID.equals(key)) {
                continue;
            }
            Criteria criteria = new Criteria(key);
            criteria.is(value);
            query.addCriteria(criteria);
        }
        Sort sort = new Sort(Sort.Direction.DESC, "create_time");
        query.with(sort);
        List<Message> messages = mongoTemplate.find(query, Message.class);
        return messages;
    }

    @Override
    public void updateMessageStatus(List<String> ids){
        Query query = Query.query(Criteria.where(Dict.ID).in(ids));
        int count = (int) mongoTemplate.count(query, Message.class);
        System.out.println("更新消息状态的数量为:---->" + count);
        Update update = Update.update(Dict.STATE, Dict.MESSAGE_READ);
        mongoTemplate.updateMulti(query, update, Message.class);
    }

    @Override
    public List<Message> queryAnnounce() {
        Query query = new Query();
        Criteria criteria = new Criteria(Dict.TYPE);
        criteria.regex(Dict.ANNOUNCE + "*");
        query.addCriteria(criteria);
        Sort sort = new Sort(Sort.Direction.DESC, Dict.CREATE_TIME);
        query.with(sort);
        List<Message> messages = mongoTemplate.find(query, Message.class);
        return messages;
    }

    @Override
    public List<Message> queryNewMessage() {
        Query query = new Query();
        Criteria criteria = new Criteria(Dict.DESC).is("版本更新");
        query.addCriteria(criteria);
        Sort sort = new Sort(Sort.Direction.DESC, Dict.CREATE_TIME);
        query.with(sort);
        List<Message> messages = mongoTemplate.find(query, Message.class);
        return messages;
    }
    
    @Override
    public List<Message> queryMessageByType(Message message) throws Exception {
        if(CommonUtils.isNullOrEmpty(message.getState())){
            message.setState(Dict.MESSAGE_UNREAD);
        }
        JSONObject pJson = NotifyUtil.objectToJsonObject(message);
        Iterator<Object> it = pJson.keys();
        Query query = new Query();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECT_ID.equals(key)) {
                continue;
            }
            Criteria criteria = new Criteria(key);
            criteria.is(value);
            query.addCriteria(criteria);
        }
        Sort sort = new Sort(Sort.Direction.DESC, "create_time");
        query.with(sort);
        List<Message> messages = mongoTemplate.find(query, Message.class);
        return messages;
    }

}
