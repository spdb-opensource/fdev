package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IHistoryDetailsDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.HistoryDetails;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Repository
public class HistoryDetailsDaoImpl implements IHistoryDetailsDao {
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public HistoryDetails add(HistoryDetails historyDetails) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        historyDetails.set_id(objectId);
        historyDetails.setId(id);
        historyDetails.setCtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_ISO_FORMAT));
        return this.mongoTemplate.save(historyDetails);
    }

    @Override
    public Map<String, Object> getHistoryList(String modelId, String envId, String type, Integer page, Integer perPage) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotEmpty(modelId)) {
            criteria.and(Dict.BEFORE + "." + Constants.MODEL_ID).is(modelId);
        }
        if (StringUtils.isNotEmpty(envId)) {
            criteria.and(Dict.BEFORE + "." + Constants.ENV_ID).is(envId);
        }
        if (StringUtils.isNotEmpty(type)) {
            criteria.and(Dict.TYPE).is(type);
        }
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Constants.CTIME));
        Long total = mongoTemplate.count(query, HistoryDetails.class);
        query.skip((page - 1L) * perPage).limit(perPage);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(Dict.TOTAL, total);
        responseMap.put(Dict.LIST, mongoTemplate.find(query, HistoryDetails.class));
        return responseMap;
    }

    @Override
    public HistoryDetails getHistory(String id) {
        return mongoTemplate.findOne(new Query(Criteria.where(Dict.ID).is(id)), HistoryDetails.class);
    }

}
