package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.release.dao.ITemplateDocumentDao;
import com.spdb.fdev.release.entity.TemplateDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class TemplateDocumentDaoImpl implements ITemplateDocumentDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public TemplateDocument getDocument(String sysname_cn, String template_type) {
        Criteria criteria = Criteria.where(Dict.SYSNAME_CN).is(sysname_cn);
        if(!CommonUtils.isNullOrEmpty(template_type)) {
            criteria.and(Dict.TEMPLATE_TYPE).is(template_type);
        }
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, TemplateDocument.class);
    }

    @Override
    public TemplateDocument save(TemplateDocument templateDocument) {
        templateDocument.setCreate_time(TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        return mongoTemplate.save(templateDocument);
    }

    @Override
    public TemplateDocument editDocumentList(TemplateDocument templateDocument) {
        Query query = new Query(Criteria.where(Dict.ID).is(templateDocument.getId()));
        Update update = Update.update(Dict.DOCUMENTLIST, templateDocument.getDocument_list())
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update, TemplateDocument.class);
        return mongoTemplate.findOne(query, TemplateDocument.class);
    }
}
