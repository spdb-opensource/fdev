package com.spdb.fdev.pipeline.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.pipeline.dao.IJobExeDao;
import com.spdb.fdev.pipeline.entity.JobExe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Repository
public class JobExeDaoImpl implements IJobExeDao {
    private static final Logger logger = LoggerFactory.getLogger(JobExeDaoImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public JobExe saveJobExe(JobExe jobExe) throws Exception {
        return mongoTemplate.insert(jobExe);
    }

    @Override
    public JobExe queryPendingJob() throws Exception {
        Query query = new Query(Criteria.where(Dict.STATUS).is(Dict.PENDING));
        return mongoTemplate.findOne(query, JobExe.class);
    }

    @Override
    public void updateJobStart(JobExe jobExe) throws Exception {
        Query query = new Query(Criteria.where(Dict.EXEID).is(jobExe.getExeId()));
        Update update = Update.update(Dict.EXEID, jobExe.getExeId());
        update.set(Dict.TOKEN,jobExe.getExeId());
        update.set(Dict.STATUS,jobExe.getStatus());
        update.set(Dict.INFO,jobExe.getInfo());
        update.set(Dict.JOBSTARTTIME,jobExe.getJobStartTime());
        mongoTemplate.findAndModify(query, update, JobExe.class);
        JobExe jobExeAfter = queryJobExeByExeId(jobExe.getExeId());
        logger.info("$$$$$$  query jobExe from DB after update, jobExe :" + JSONObject.toJSON(jobExeAfter));
    }

    @Override
    public void updateSteps(JobExe jobExe) throws Exception {
        Query query = new Query(Criteria.where(Dict.EXEID).is(jobExe.getExeId()));
        Update update = Update.update(Dict.EXEID, jobExe.getExeId());
        update.set(Dict.STEPS,jobExe.getSteps());
        mongoTemplate.findAndModify(query, update, JobExe.class);
        JobExe jobExeAfter = queryJobExeByExeId(jobExe.getExeId());
        logger.info("$$$$$$  query jobExe from DB after update, jobExe :" + JSONObject.toJSON(jobExeAfter));
    }

    @Override
    public void updateJobFinish(JobExe jobExe) throws Exception {
        Query query = new Query(Criteria.where(Dict.EXEID).is(jobExe.getExeId()));
        Update update = Update.update(Dict.EXEID, jobExe.getExeId());
        update.set(Dict.JOBENDTIME,jobExe.getJobEndTime());
        update.set(Dict.JOBCOSTTIME,jobExe.getJobCostTime());
        update.set(Dict.MINIOLOGURL,jobExe.getMinioLogUrl());
        update.set(Dict.STATUS,jobExe.getStatus());
        mongoTemplate.findAndModify(query, update, JobExe.class);
        JobExe jobExeAfter = queryJobExeByExeId(jobExe.getExeId());
        logger.info("$$$$$$  query jobExe from DB after update, jobExe :" + JSONObject.toJSON(jobExeAfter));
    }

    @Override
    public JobExe queryJobExeByIndex(String pipeline, Integer stageIndex, Integer jobIndex) throws Exception {
        Query query = new Query(Criteria.where(Dict.PIPELINEEXEID)
                .is(pipeline).and(Dict.STAGEINDEX)
                .is(stageIndex)
                .and(Dict.JOBINDEX)
                .is(jobIndex));
        List<JobExe> jobExes = mongoTemplate.find(query, JobExe.class, Dict.JOB_EXE);


        //若job实例被执行多次，则取最新的一个
        return jobExes.get(jobExes.size() - 1);
    }

    @Override
    public JobExe queryJobExeByExeId(String jobExeId) throws Exception {
        Query query = new Query(Criteria.where(Dict.EXEID).is(jobExeId));
        return mongoTemplate.findOne(query, JobExe.class);
    }

    @Override
    public void updateStatusByExeId(String jobExeId, String status) throws Exception {
        Query query = new Query(Criteria.where(Dict.EXEID).is(jobExeId));
        Update update = Update.update(Dict.EXEID, jobExeId).set(Dict.STATUS, status);
        mongoTemplate.findAndModify(query, update, JobExe.class);
    }

    @Override
    public List<JobExe> queryByPipelineExeId(String pipelineExeId) throws Exception {
        Query query = new Query(Criteria.where(Dict.PIPELINEEXEID).is(pipelineExeId));
        return mongoTemplate.find(query, JobExe.class);
    }
}
