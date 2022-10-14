package com.spdb.fdev.fuser.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.dao.ISectionDao;
import com.spdb.fdev.fuser.spdb.entity.user.Section;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;

@Repository
public class SectionDaoImpl implements ISectionDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public void addSection(Section section) {
        mongoTemplate.save(section);
    }

    public void querySection(Section section) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(section);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Criteria c = new Criteria();
        while(it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if(Dict.OBJECTID.equals(key))continue;
            c.and(key).is(value);
        }
        Query query = new Query(c);
        mongoTemplate.find(query,Section.class);
    }


    public List<Section> querySectionByNameEn(String sectionNameEn) {
        Query query = new Query(Criteria.where("sectionNameEn").is(sectionNameEn));
        return mongoTemplate.find(query,Section.class);
    }

    public List<Section> querySectionByNameCn(String sectionNameCn) {
        Query query = new Query(Criteria.where("sectionNameCn").is(sectionNameCn));
        return mongoTemplate.find(query,Section.class);
    }

    @Override
    public List<Section> queryAllSection() {
        return mongoTemplate.findAll(Section.class);
    }
}
