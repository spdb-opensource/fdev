package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.Constant;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.IComponentInfoDao;
import com.spdb.fdev.component.entity.ComponentInfo;
import com.spdb.fdev.component.entity.ComponentIssue;
import com.spdb.fdev.component.entity.ComponentRecord;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class ComponentInfoDaoImpl implements IComponentInfoDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List query(ComponentInfo componentInfo) throws Exception {
        List<ComponentInfo> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = componentInfo == null ? "{}" : objectMapper.writeValueAsString(componentInfo);
        BasicDBObject queryJson = BasicDBObject.parse(json);
        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude(Dict.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<ComponentInfo> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.COMPONENT_INFO, ComponentInfo.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public ComponentInfo save(ComponentInfo componentInfo) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        componentInfo.set_id(objectId);
        componentInfo.setId(id);
        return mongoTemplate.save(componentInfo);
    }

    @Override
    public void delete(ComponentInfo componentInfo) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(componentInfo.getId()));
        mongoTemplate.findAndRemove(query, ComponentInfo.class);
    }

    @Override
    public ComponentInfo queryById(ComponentInfo componentInfo) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(componentInfo.getId()));
        return mongoTemplate.findOne(query, ComponentInfo.class);
    }

    @Override
    public ComponentInfo queryById(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findOne(query, ComponentInfo.class);
    }

    @Override
    public ComponentInfo queryByNameEn(String name_en) throws Exception {
        Query query = new Query(Criteria.where(Dict.NAME_EN).is(name_en));
        return mongoTemplate.findOne(query, ComponentInfo.class);
    }

    @Override
    public ComponentInfo queryByGroupIdAndArtifact(String groupId, String artifactId) {
        Query query = new Query(Criteria.where(Dict.GROUPID).is(groupId).and(Dict.ARTIFACTID).is(artifactId));
        return mongoTemplate.findOne(query, ComponentInfo.class);
    }

    @Override
    public ComponentInfo queryByWebUrl(String web_url) {
        Query query = new Query(Criteria.where(Dict.GITLAB_URL).is(web_url));
        return mongoTemplate.findOne(query, ComponentInfo.class);
    }

    @Override
    public List<ComponentInfo> queryByType(String type) throws Exception {
        Query query = new Query(Criteria.where(Dict.TYPE).is(type));
        return mongoTemplate.find(query, ComponentInfo.class);
    }

    @Override
    public ComponentInfo update(ComponentInfo componentInfo) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(componentInfo.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(componentInfo);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, componentInfo.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, ComponentInfo.class);
        return this.mongoTemplate.findOne(query, ComponentInfo.class);
    }

    @Override
    public List<ComponentInfo> queryByUserId(String user_id) {
        Criteria criteria = new Criteria();
        if (!StringUtils.isEmpty(user_id)) {
            criteria.and(Dict.ID).in(user_id);
        }
        Query query = new Query(Criteria.where(Dict.MANAGER_ID).elemMatch(criteria));
        return mongoTemplate.find(query, ComponentInfo.class);
    }

    @Override
    public Integer queryDataByType(String startTime, String endTime) {
        Query query = new Query(Criteria.where(Dict.CREATE_DATE).lte(endTime));
        List<ComponentInfo> ComponentInfo = mongoTemplate.find(query, ComponentInfo.class);
        return ComponentInfo.size();
    }

    @Override
    public List<ComponentInfo> getComponentByIds(List params) throws Exception {
        Criteria c = Criteria.where(Dict.ID).in(params);
        List<ComponentInfo> componentInfoList = this.mongoTemplate.find(Query.query(c), ComponentInfo.class);
        return componentInfoList;
    }

}
