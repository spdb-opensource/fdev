package com.spdb.fdev.codeReview.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.base.utils.CommonUtils;
import com.spdb.fdev.codeReview.base.utils.TimeUtil;
import com.spdb.fdev.codeReview.spdb.dao.IMeetingDao;
import com.spdb.fdev.codeReview.spdb.entity.CodeMeeting;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
@Component
public class MeetingDaoImpl implements IMeetingDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<CodeMeeting> queryMeetingByOrderId(String orderId) {
        Query query = Query.query(Criteria.where(Dict.ORDER_ID).is(orderId));
        query.with(new Sort(Sort.Direction.DESC, Dict.MEETING_TIME));
        query.fields().exclude(Dict.OBJECT_ID);
        return mongoTemplate.find(query, CodeMeeting.class);
    }

    @Override
    public CodeMeeting queryMeetingById(String id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        query.fields().exclude(Dict.OBJECT_ID);
        return mongoTemplate.findOne(query, CodeMeeting.class);
    }

    @Override
    public CodeMeeting deleteMeetingById(String id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findAndRemove(query, CodeMeeting.class);
    }

    @Override
    public CodeMeeting add(CodeMeeting codeMeeting) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        codeMeeting.setId(id);
        codeMeeting.setCreateTime(TimeUtil.formatTodayHs());
        codeMeeting.setCreateUser(CommonUtils.getSessionUser().getId());
        codeMeeting.setUpdateUser(CommonUtils.getSessionUser().getId());
        codeMeeting.setUpdateTime(TimeUtil.formatTodayHs());
        return mongoTemplate.save(codeMeeting);
    }

    @Override
    public void update(CodeMeeting codeMeeting) throws Exception {
        //更新时间
        codeMeeting.setUpdateUser(CommonUtils.getSessionUser().getId());
        codeMeeting.setUpdateTime(TimeUtil.formatTodayHs());
        Query query = Query.query(Criteria.where(Dict.ID).is(codeMeeting.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(codeMeeting);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = new Update();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if(!Dict.OBJECT_ID.equals(key) && !Dict.ID.equals(key) && !Dict.ORDER_ID.equals(key) && !Dict.CREATE_USER.equals(key) && !Dict.CREATE_TIME.equals(key)){
                update.set(key,value);
            }
        }
        mongoTemplate.findAndModify(query,update, CodeMeeting.class);
    }

    @Override
    public List<CodeMeeting> queryMeetingByIds(Set<String> meetingIds) {
        Query query = new Query(Criteria.where(Dict.ID).in(meetingIds));
        return mongoTemplate.find(query, CodeMeeting.class);
    }
}
