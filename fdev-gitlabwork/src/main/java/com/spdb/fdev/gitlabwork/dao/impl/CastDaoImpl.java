package com.spdb.fdev.gitlabwork.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.dao.CastDao;
import com.spdb.fdev.gitlabwork.entiy.SitMergedInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CastDaoImpl implements CastDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(SitMergedInfo obj) {
        mongoTemplate.save(obj, "sitmerged");
    }

    @Override
    public List<SitMergedInfo> query(Map query) {
        Criteria c = new Criteria();
        Object in = query.remove("in");
        Object nin = query.remove("nin");
        query.forEach((k, v) -> c.and((String) k).is(v));
        if (in != null) {
            String key = (String) ((Map) in).get(Dict.KEY);
            List value = (List) ((Map) in).get(Dict.VALUE);
            c.andOperator(Criteria.where(key).in(value));
        }
        if (nin != null) {
            String key = (String) ((Map) nin).get(Dict.KEY);
            List value = (List) ((Map) nin).get(Dict.VALUE);
            c.andOperator(Criteria.where(key).nin(value));
        }
        Query q = new Query(c);
        return mongoTemplate.find(q, SitMergedInfo.class);
    }

}
