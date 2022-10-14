package com.spdb.fdev.gitlabwork.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.dao.IStatisticCodeLineDao;
import com.spdb.fdev.gitlabwork.entiy.StatisticCodeLine;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;

/**
 * @Author: c-jiangjk
 * @Date: 2021/3/1 15:26
 */
@Service
public class StatisticCodeLineDaoImpl implements IStatisticCodeLineDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public List<StatisticCodeLine> query(StatisticCodeLine statisticCodeLine) {
        Query query = new Query();
        return mongoTemplate.find(query, StatisticCodeLine.class);
    }

    @Override
    public void saveAll(List<StatisticCodeLine> statistics) {
        BulkOperations operations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, StatisticCodeLine.class);
        operations.insert(statistics);
        operations.execute();
    }

    @Override
    public void save(StatisticCodeLine statisticCodeLine) {
        mongoTemplate.save(statisticCodeLine);
    }

    @Override
    public void update(StatisticCodeLine statisticCodeLine) throws Exception {
        Query query = Query.query(Criteria.where(Dict.MONTH).is(statisticCodeLine.getMonth()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(statisticCodeLine);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.MONTH, statisticCodeLine.getMonth());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, StatisticCodeLine.class);
    }

    @Override
    public List<StatisticCodeLine> queryByDate(String startDate, String endDate) {

        Query query = new Query();
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            query.addCriteria(Criteria.where(Dict.MONTH).lte(startDate).lte(endDate));
        } else {
            if (StringUtils.isNotBlank(startDate)) {
                query.addCriteria(Criteria.where(Dict.MONTH).lte(startDate));
            }
            if (StringUtils.isNotBlank(endDate)) {
                query.addCriteria(Criteria.where(Dict.MONTH).gte(endDate));
            }
        }
        return mongoTemplate.find(query, StatisticCodeLine.class);
    }
}
