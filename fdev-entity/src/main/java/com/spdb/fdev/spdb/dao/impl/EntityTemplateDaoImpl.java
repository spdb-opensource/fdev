package com.spdb.fdev.spdb.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.spdb.dao.IEntityTemplateDao;
import com.spdb.fdev.spdb.entity.EntityTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName EntityTemplateDaoImpl
 * @DESCRIPTION
 * @Author xxx
 * @Date 2021/5/8 15:40
 * @Version 1.0
 */
@Repository
public class EntityTemplateDaoImpl implements IEntityTemplateDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(List<EntityTemplate> templates) throws Exception {
        mongoTemplate.insert(templates, EntityTemplate.class);
    }

    @Override
    public List<EntityTemplate> queryList(String likeKey) throws Exception {
        Query query = new Query();
        if (!CommonUtil.isNullOrEmpty(likeKey)) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where(Dict.NAMECN).regex(likeKey, "i"),
                    Criteria.where(Dict.NAMEEN).regex(likeKey, "i")
            ));
        }
        return mongoTemplate.find(query, EntityTemplate.class);
    }

    @Override
    public EntityTemplate queryById(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findOne(query,EntityTemplate.class);
    }
}
