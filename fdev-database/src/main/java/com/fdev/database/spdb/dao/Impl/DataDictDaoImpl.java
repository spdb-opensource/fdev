package com.fdev.database.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.dao.DataDictDao;
import com.fdev.database.spdb.entity.DataDict;
import com.fdev.database.spdb.entity.Database;
import net.sf.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;


@Repository
public class DataDictDaoImpl implements DataDictDao {

    @Resource
    private MongoTemplate mongoTemplate;


    @Override
    public DataDict add(DataDict dataDict) {
        dataDict.initId();
       return mongoTemplate.save(dataDict);
    }

    @Override
    public List<DataDict> query(DataDict dataDict) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(dataDict);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)){
                continue;
            } else if (Dict.FIELD_EN_NAME.equals(key)) {
                c.and(key).regex((String) value);
            } else {
                c.and(key).is(value);
            }
        }
        Query query = Query.query(c);
        return mongoTemplate.find(query, DataDict.class);
    }

    @Override
    public List<DataDict> queryByKey(DataDict dataDict) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(dataDict);
        Query query =new BasicQuery(json);
        return mongoTemplate.find(query, DataDict.class);
    }

    @Override
    public void update(DataDict dataDict) {
        Query query =Query.query(Criteria.where(Dict.DICT_ID).is(dataDict.getDict_id()));
        Update update = Update.update(Dict.FIELD_CH_NAME, dataDict.getField_ch_name()).set(Dict.FIELD_LENGTH, dataDict.getField_length());
        mongoTemplate.findAndModify(query, update, DataDict.class);
    }

    @Override
    public List<DataDict> queryIdByFields(List<String> all_field) {
        Query query =Query.query(Criteria.where(Dict.FIELD_EN_NAME).in(all_field));
        return mongoTemplate.find(query, DataDict.class);
    }


}
