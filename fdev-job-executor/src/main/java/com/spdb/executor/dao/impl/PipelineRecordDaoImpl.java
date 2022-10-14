package com.spdb.executor.dao.impl;

import com.csii.pe.pojo.PipelineRecord;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.DateUtils;
import com.spdb.executor.dao.PipelineRecordDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author xxx
 * @date 2020/5/18 14:36
 */
@Repository
public class PipelineRecordDaoImpl implements PipelineRecordDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void savePipelineRecord(PipelineRecord pipelineRecord) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        pipelineRecord.set_id(objectId);
        pipelineRecord.setId(id);
        pipelineRecord.setCreateTime(DateUtils.getDate(DateUtils.FORMAT_DATE_TIME));
        pipelineRecord.setUpdateTime(DateUtils.getDate(DateUtils.FORMAT_DATE_TIME));
        mongoTemplate.save(pipelineRecord);
    }

    @Override
    public void updatePipelineRecord(String id, Set<Integer> pipelineIds) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.ID).is(id);
        Update update = Update.update(Dict.PIPELINE_IDS, pipelineIds).set(Dict.UPDATE_TIME, DateUtils.getDate(DateUtils.FORMAT_DATE_TIME));
        mongoTemplate.findAndModify(new Query(criteria), update, PipelineRecord.class);
    }

    @Override
    public PipelineRecord getPipelineRecord(Integer projectId, String branch, String startTime, String endTime) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PROJECT_ID).is(projectId);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.CREATE_TIME).gte(startTime).lt(endTime);
        return mongoTemplate.findOne(new Query(criteria), PipelineRecord.class);
    }

    @Override
    public List<PipelineRecord> listPipelineRecords(String startTime, String endTime) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.CREATE_TIME).gte(startTime).lt(endTime);
        return mongoTemplate.find(new Query(criteria), PipelineRecord.class);
    }
}
