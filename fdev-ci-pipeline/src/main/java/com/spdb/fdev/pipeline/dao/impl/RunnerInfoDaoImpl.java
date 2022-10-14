package com.spdb.fdev.pipeline.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.pipeline.dao.IRunnerInfoDao;
import com.spdb.fdev.pipeline.entity.RunnerInfo;
import com.spdb.fdev.pipeline.service.impl.RunnerServiceImpl;
import com.sun.org.apache.bcel.internal.generic.I2F;
import org.bson.types.ObjectId;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Author: c-jiangjk
 * @Date: 2021/4/2 14:32
 */
@Repository
public class RunnerInfoDaoImpl implements IRunnerInfoDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RunnerInfoDaoImpl.class);

    @Override
    public RunnerInfo query(RunnerInfo runnerInfo) throws Exception{
        Criteria c = new Criteria();
        if (!CommonUtils.isNullOrEmpty(runnerInfo.getToken()))
            c.and(Dict.TOKEN).is(runnerInfo.getToken());
        if (!CommonUtils.isNullOrEmpty(runnerInfo.getRunnerId()))
            c.and(Dict.RUNNERID).is(runnerInfo.getRunnerId());
        if (!CommonUtils.isNullOrEmpty(runnerInfo.getName()))
            c.and(Dict.NAME).is(runnerInfo.getName());
        c.and(Dict.STATUS).is(Constants.ONE);
        Query query = new Query(c);
        return mongoTemplate.findOne(query, RunnerInfo.class);
    }

    @Override
    public void saveRunner(RunnerInfo runnerInfo) throws Exception {
        ObjectId id = new ObjectId();
        runnerInfo.setRunnerId(id.toString());
        runnerInfo.setRevision(Constants.RUNNER_REVISION_FIRST);
        runnerInfo.setStatus(Constants.ONE);
        mongoTemplate.insert(runnerInfo);
    }

    @Override
    public void updateRunner(RunnerInfo runnerInfo) throws ParseException {
        String token = runnerInfo.getToken();
        Criteria c = new Criteria();
        Update up = new Update();
        up.set(Constants.RUNNER_REVISION, setNextRevision(runnerInfo.getRevision()));
        c.and(Dict.RUNNERID).is(runnerInfo.getRunnerId());
        Query query = new Query(c);
        if (!CommonUtils.isNullOrEmpty(token)) {
            up.set(Dict.TOKEN, token);
        }else {
            return;
        }
        this.mongoTemplate.updateFirst(query, up, Dict.RUNNERINFO);
    }

    /**
     * 计算设置下一个revision  大于六位数的不能转换
     *
     * @param currentRevision
     * @return
     * @throws ParseException
     */
    private static String setNextRevision(String currentRevision) throws ParseException {
        DecimalFormat df = new DecimalFormat(Constants.DC_PATTEN);
        int curNum = df.parse(currentRevision).intValue();
        curNum = curNum + 1;
        String nextRevison = df.format(curNum);
        return nextRevison;
    }


    @Override
    public List<RunnerInfo> queryAllRunnerInfo(Map<String, Object> param) {
        String platForm =(String) param.get(Dict.PLATFORM);
        Query query = new Query();
        if (!CommonUtils.isNullOrEmpty(platForm)){
            query.addCriteria(Criteria.where(Dict.PLATFORM).is(platForm));
        }
        return this.mongoTemplate.find(query,RunnerInfo.class);
    }

    /**
     * 根据id查询runnerInfo
     *
     * @param id
     * @return
     */
    @Override
    public RunnerInfo queryById(String id) {
        Criteria c = new Criteria();
        c.and(Dict.RUNNERID).is(id);
        Query query = new Query(c);
        return this.mongoTemplate.findOne(query, RunnerInfo.class, Dict.RUNNERINFO);
    }
}
