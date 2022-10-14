package com.spdb.fdev.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.spdb.dao.OutsideTemplateDao;
import com.spdb.fdev.spdb.entity.OutSideTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class OutsideTemplateDaoImpl implements OutsideTemplateDao {

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 查询外部参数配置模版
     * @param outSideTemplate
     * @return
     * @throws Exception
     */
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
        AggregationOperation project = Aggregation.project().andExclude(Dict.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<OutSideTemplate> docs = this.mongoTemplate.aggregate(
                Aggregation.newAggregation(project, match), Dict.OUT_SIDE_TEMPLATE, OutSideTemplate.class);
        docs.forEach(result::add);
        return result;
    }
}
