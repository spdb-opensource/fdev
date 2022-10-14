package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IOtherDemandTaskDao;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import com.spdb.fdev.fdemand.spdb.entity.OtherDemandTask;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RefreshScope
@Repository
public class OtherDemandTaskDaoImpl implements IOtherDemandTaskDao {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public void addOtherDemandTask(OtherDemandTask otherDemandTask) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        otherDemandTask.set_id(objectId);
        otherDemandTask.setId(id);
        mongoTemplate.save(otherDemandTask);
    }

    @Override
    public void updateOtherDemandTask(OtherDemandTask otherDemandTask) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(otherDemandTask.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(otherDemandTask);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, otherDemandTask.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)) {
                continue;
            }
            if (Dict.ID.equals(key)) {
                continue;
            }
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, OtherDemandTask.class);
    }

    @Override
    public void deleteOtherDemandTask(OtherDemandTask otherDemandTask) throws Exception {
        Query query = new Query(Criteria.where(Dict.TASKNUM).is(otherDemandTask.getTaskNum()));
        Update update = new Update();
        update.set(Dict.STATUS, Constants.DELETE);//置为删除状态
        mongoTemplate.findAndModify(query, update,OtherDemandTask.class);
    }

    @Override
    public Map<String, Object> queryOtherDemandTaskList(Map<String, Object> params) throws Exception {
        String demandId = (String)params.get(Dict.DEMANDID);//需求ID
        Integer size = (Integer)params.get(Dict.SIZE);//每页条数，不传或o默认查全部
        Integer index = (Integer)params.get(Dict.INDEX);//页码
        String leaderGroup = (String)params.get(Dict.LEADERGROUP);//牵头小组
        String status = (String)params.get(Dict.STATUS);//实施状态
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.DEMANDID).is(demandId);
        if(!CommonUtils.isNullOrEmpty(leaderGroup)){
            criteria.and(Dict.LEADERGROUP).is(leaderGroup);
        }
        //状态为空查询全部状态
        if(!CommonUtils.isNullOrEmpty(status)){
            criteria.and(Dict.STATUS).is(status);
        } else {
            criteria.and(Dict.STATUS).ne(Constants.DELETE);
        }
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        Long count = mongoTemplate.count(query, OtherDemandTask.class);
        if( !CommonUtils.isNullOrEmpty(size) && size != 0 ) {
            query.skip((index - 1L) * size).limit(size);
        }
        List<OtherDemandTask> data = mongoTemplate.find(query, OtherDemandTask.class);
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.COUNT,count);
        map.put(Dict.DATA,data);
        return map;
    }

    @Override
    public OtherDemandTask queryOtherDemandTask(Map<String, Object> params) throws Exception {
        return null;
    }

    @Override
    public OtherDemandTask queryByTaskNum(String taskNum) {
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*" + taskNum + ".*$");
        Criteria criteria = Criteria.where(Dict.TASKNUM).regex(pattern);
        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATETIME));
        return mongoTemplate.findOne(query, OtherDemandTask.class);
    }
}
