package com.spdb.fdev.pipeline.dao.impl;


import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.pipeline.dao.IPipelineUpdateDiffDao;
import com.spdb.fdev.pipeline.entity.PipelineUpdateDiff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;


@Service
public class PipelineUpdateDiffDaoImpl implements IPipelineUpdateDiffDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public PipelineUpdateDiff queryDiffBySourceId(String sourcePipelineId) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SOURCEPIPELINEID).is(sourcePipelineId);
        Query query = new Query(criteria);
        return this.mongoTemplate.findOne(query, PipelineUpdateDiff.class, Dict.PIPELINEUPDATEDIFF);
    }

    @Override
    public void saveDiff(PipelineUpdateDiff entity) {
        this.mongoTemplate.save(entity, Dict.PIPELINEUPDATEDIFF);
    }

    @Override
    public PipelineUpdateDiff queryDiffById(String id) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.ID).is(id);
        Query query = new Query(criteria);
        return this.mongoTemplate.findOne(query, PipelineUpdateDiff.class, Dict.PIPELINEUPDATEDIFF);
    }
}