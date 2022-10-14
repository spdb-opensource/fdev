package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IOutsideTemplateDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.OutSideTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class OutsideTemplateDaoImpl implements IOutsideTemplateDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public OutSideTemplate outsideTemplateSave(OutSideTemplate outSideTemplate) {
        Query query = new Query(Criteria.where(Dict.PROJECT_ID).is(outSideTemplate.getProject_id()));
        Update update = Update.update(Dict.PROJECT_ID, outSideTemplate.getProject_id())
                .set(Constants.VARIABLES, outSideTemplate.getVariables())
                .set(Constants.OPNO, outSideTemplate.getOpno())
                .set(Constants.CTIME, outSideTemplate.getCtime())
                .set(Constants.UTIME, outSideTemplate.getUtime());
        this.mongoTemplate.upsert(query, update, OutSideTemplate.class);
        return this.mongoTemplate.findOne(query, OutSideTemplate.class);
    }

    @Override
    public List<OutSideTemplate> query(OutSideTemplate outSideTemplate) throws Exception {
        List<OutSideTemplate> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = outSideTemplate == null ? "{}" : objectMapper.writeValueAsString(outSideTemplate);
        BasicDBObject queryJson = BasicDBObject.parse(json);

        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude(Constants.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<OutSideTemplate> docs = this.mongoTemplate.aggregate(
                Aggregation.newAggregation(project, match), Constants.OUT_SIDE_TEMPLATE, OutSideTemplate.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public OutSideTemplate update(OutSideTemplate outSideTemplate) {
        Query query = new Query(Criteria.where(Dict.PROJECT_ID).is(outSideTemplate.getProject_id()));
        Update update = Update.update(Dict.PROJECT_ID, outSideTemplate.getProject_id())
                .set(Constants.VARIABLES, outSideTemplate.getVariables()).set(Constants.OPNO, outSideTemplate.getOpno())
                .set(Constants.UTIME, outSideTemplate.getUtime());
        this.mongoTemplate.findAndModify(query, update, OutSideTemplate.class);
        return this.mongoTemplate.findOne(query, OutSideTemplate.class);
    }

    @Override
    public OutSideTemplate queryByProjectId(String projectId) {
        return this.mongoTemplate.findOne(new Query(Criteria.where(Dict.PROJECT_ID).is(projectId)), OutSideTemplate.class);
    }

}
