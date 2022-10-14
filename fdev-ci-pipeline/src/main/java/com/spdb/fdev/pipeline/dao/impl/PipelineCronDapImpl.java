package com.spdb.fdev.pipeline.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.pipeline.dao.IPipelineCronDao;
import com.spdb.fdev.pipeline.entity.PipelineCronBatch;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PipelineCronDapImpl implements IPipelineCronDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void upsertCron(PipelineCronBatch pipelineCronBatch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.CRONID).is(pipelineCronBatch.getCronId());
        Update update = new Update();
        update.set(Dict.CRONID, pipelineCronBatch.getCronId());
        update.set(Dict.BATCHNO, pipelineCronBatch.getBatchNo());
        update.set(Dict.STATUS, pipelineCronBatch.getBatchStatus());
        update.set(Dict.COUNT, pipelineCronBatch.getCount());
        Query query = new Query(criteria);
        this.mongoTemplate.upsert(query, update, PipelineCronBatch.class);
    }

    @Override
    public List<PipelineCronBatch> queryAllCron() {
        return this.mongoTemplate.findAll(PipelineCronBatch.class);
    }

    /**
     * 获取上一次的批次号
     *
     * @return
     */
    @Override
    public PipelineCronBatch getLastBatch() {
        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, Dict.BATCHNO));
        PipelineCronBatch pipelineCronBatches = this.mongoTemplate.findOne(query, PipelineCronBatch.class);
        if (CommonUtils.isNullOrEmpty(pipelineCronBatches)) {
            return null;
        }else
            return pipelineCronBatches;
    }
}
