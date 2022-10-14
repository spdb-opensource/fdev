package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IDemandAssessDao;
import com.spdb.fdev.fdemand.spdb.dto.DemandAssessDto;
import com.spdb.fdev.fdemand.spdb.entity.DemandAssess;
import com.spdb.fdev.fdemand.spdb.entity.DictEntity;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhanghp4
 */
@Repository
public class DemandAssessDaoImpl implements IDemandAssessDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public DemandAssess queryByIdOrNo(String id, String oaContactNo, Integer demandStatus) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.DEMAND_STATUS).ne(demandStatus));
        if (!CommonUtils.isNullOrEmpty(id)) {
            query.addCriteria(Criteria.where(Dict.ID).is(id));
        }
        if (!CommonUtils.isNullOrEmpty(oaContactNo)) {
            query.addCriteria(Criteria.where(Dict.OA_CONTACT_NO).is(oaContactNo));
        }
        return mongoTemplate.findOne(query, DemandAssess.class);
    }

    @Override
    public DemandAssess save(DemandAssess demandAssess) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        demandAssess.setId(id);
        return mongoTemplate.save(demandAssess);
    }

    @Override
    public DemandAssess queryById(String id) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findOne(query, DemandAssess.class);
    }

    @Override
    public DemandAssess queryByNo(String no) {
        return mongoTemplate.findOne(new Query(Criteria.where(Dict.OA_CONTACT_NO).is(no)), DemandAssess.class);
    }

    @Override
    public DemandAssess queryByConfUrl(String url) {
        return mongoTemplate.findOne(new Query(Criteria.where(Dict.CONF_URL).is(url)), DemandAssess.class);
    }

    @Override
    public Map<String, Object> query(DemandAssessDto dto) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        if (!CommonUtils.isNullOrEmpty(dto.getOaContactNameNo())) {
            query.addCriteria(criteria.orOperator(Criteria.where(Dict.OA_CONTACT_NO).regex(dto.getOaContactNameNo()), Criteria.where(Dict.OA_CONTACT_NAME).regex(dto.getOaContactNameNo())));
        }
        if (!CommonUtils.isNullOrEmpty(dto.getDemandLeaderGroups())) {
            query.addCriteria(Criteria.where(Dict.DEMAND_LEADER_GROUP).in(dto.getDemandLeaderGroups()));
        }
        if (!CommonUtils.isNullOrEmpty(dto.getDemandLeader())) {
            query.addCriteria(Criteria.where(Dict.DEMAND_LEADER).in(dto.getDemandLeader()));
        }
        if (!CommonUtils.isNullOrEmpty(dto.getPriority())) {
            query.addCriteria(Criteria.where(Dict.PRIORITY).is(dto.getPriority()));
        }
        if (!CommonUtils.isNullOrEmpty(dto.getOverdueType())) {
            query.addCriteria(Criteria.where(Dict.OVERDUE_TYPE).is(dto.getOverdueType()));
        }
        if (!CommonUtils.isNullOrEmpty(dto.getGteAssessDays())) {
            query.addCriteria(Criteria.where(Dict.ASSESS_DAYS).gte(dto.getGteAssessDays()));
        }
        if (!CommonUtils.isNullOrEmpty(dto.getDemandStatus())) {
            query.addCriteria(Criteria.where(Dict.DEMAND_STATUS).is(dto.getDemandStatus()));
        }
        if(!CommonUtils.isNullOrEmpty(dto.getConfState())){
            query.addCriteria(Criteria.where(Dict.CONF_STATE).is(dto.getConfState()));
        }
        if(!CommonUtils.isNullOrEmpty(dto.getGoingDays())){
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_WEEK, -dto.getGoingDays());
            query.addCriteria(Criteria.where(Dict.START_ASSESS_DATE).lte(new SimpleDateFormat("yyyy-MM-dd").format(c.getTime())));
        }
        if(!CommonUtils.isNullOrEmpty(dto.getFinalDays())){
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_WEEK, -dto.getFinalDays());
            query.addCriteria(Criteria.where(Dict.FINAL_DATE).lte(new SimpleDateFormat("yyyy-MM-dd").format(c.getTime())));
        }
        return new HashMap<String, Object>() {{
            put(Dict.TOTAL, mongoTemplate.count(query, DemandAssess.class));
            if (!CommonUtils.isNullOrEmpty(dto.getPageNum()) && !CommonUtils.isNullOrEmpty(dto.getPageSize())) {
                query.skip((dto.getPageNum() - 1) * dto.getPageSize()).limit(dto.getPageSize());
            }
            query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
            put(Dict.DATA, mongoTemplate.find(query, DemandAssess.class));
        }};
    }

	 @Override
    public DemandAssess update(DemandAssess demandAssess) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(demandAssess.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(demandAssess);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, demandAssess.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)) {
                continue;
            }
            update.set(key, value);
        }
        return this.mongoTemplate.findAndModify(query, update, DemandAssess.class);
    }

    @Override
    public List<DemandAssess> queryByStatus(Integer demandStatus) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.DEMAND_STATUS).is(demandStatus));
        return mongoTemplate.find(query, DemandAssess.class);
    }

    @Override
    public List<DictEntity> queryOverdueTypeById(String id) {
        return mongoTemplate.find(new Query().addCriteria(Criteria.where(Dict.ID).is(id)), DictEntity.class);
    }
}
