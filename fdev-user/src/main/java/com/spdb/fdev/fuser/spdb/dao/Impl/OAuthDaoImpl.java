package com.spdb.fdev.fuser.spdb.dao.Impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.dao.OAuthDao;
import com.spdb.fdev.fuser.spdb.entity.user.OAuth;

import net.sf.json.JSONObject;

@Repository
public class OAuthDaoImpl implements OAuthDao {
    @Resource
    private MongoTemplate mongoTemplate;
    @Override
    public List<OAuth> query(OAuth oAuth) throws Exception {
        List<OAuth> result = new ArrayList<>();
        ObjectMapper o=new ObjectMapper();
        o.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String s = oAuth==null?"{}":o.writeValueAsString(oAuth);
        JSONObject pJson = JSONObject.fromObject(s);
        Iterator<Object> it = pJson.keys();
        Criteria c = new Criteria();
        while(it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
          
            if(Dict.OBJECTID.equals(key)) {
				continue;
			}
            c.and(key).is(value);
        }
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<OAuth> docs = mongoTemplate.aggregate(Aggregation.newAggregation(match),"oAuth", OAuth.class);
        docs.forEach(doc->{
            result.add(doc);
        });
        return result;
    }

    @Override
    public OAuth add(OAuth oAuth) throws Exception {
        oAuth.initId();
        return mongoTemplate.save(oAuth);
    }
}
