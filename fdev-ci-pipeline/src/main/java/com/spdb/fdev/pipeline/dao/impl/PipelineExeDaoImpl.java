package com.spdb.fdev.pipeline.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.pipeline.dao.IPipelineExeDao;
import com.spdb.fdev.pipeline.entity.PipelineExe;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PipelineExeDaoImpl implements IPipelineExeDao {

    private static final Logger logger = LoggerFactory.getLogger(PipelineExeDaoImpl.class);
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public PipelineExe savePipelineExe(PipelineExe pipelineExe) throws Exception {
        return mongoTemplate.insert(pipelineExe);
    }
public void queryPipelineLog(){
}
    /**
     * 2021年3月17日 09:46:21 修改查询运行历史的时候应该根据pipelineId和nameId两个条件来查询获取pipelineExe
     *
     * @param pipelineId
     * @param skip
     * @param limit
     * @param nameId
     * @return
     */
    @Override
    public Map<String, Object> queryListByPipelineIdSort(String pipelineId, long skip, int limit, String nameId) {
        Criteria criteria = new Criteria();
        SkipOperation skipOperation = Aggregation.skip(0L);
        LimitOperation limitOperation = Aggregation.limit(100L);
        if(!CommonUtils.isNullOrEmpty(pipelineId) && !CommonUtils.isNullOrEmpty(nameId)) {
            criteria.orOperator(Criteria.where(Dict.PIPELINEID).is(pipelineId), Criteria.where(Dict.PIPELINENAMEID).is(nameId));
        }
        if(limit != 0) {
             skipOperation = Aggregation.skip(skip);
             limitOperation = Aggregation.limit(limit);
        }
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.lookup("pipeline", "pipelineId", "pipelineId", "pipeline"),
                Aggregation.unwind("pipeline", true),
                Aggregation.replaceRoot(ObjectOperators.MergeObjects.merge("$$ROOT", new Document("version", "$$ROOT.pipeline.version"))),
                Aggregation.project().andExclude("pipeline",
                        Dict._ID,
                        Dict.PROJECTINFO,
                        Dict.GITINFO,
                        Dict.VARIABLES,
                        Dict.VOLUMES),
                Aggregation.facet(
                        Aggregation.sort(new Sort(Sort.Direction.DESC, Dict.STARTTIME)),
                        skipOperation,
                        limitOperation
                ).as("pipelineExeList")
                        .and(Aggregation.count().as("count")).as("total"),
                Aggregation.unwind("total", true),
                aggregationOperationContext -> Document.parse("{$addFields: {\"total\":\"$total.count\"}}")
        );
        AggregationResults<Document> pipeline_exe = mongoTemplate.aggregate(aggregation, "pipeline_exe", Document.class);
        List<Document> mappedResults = pipeline_exe.getMappedResults();
        return mappedResults.get(0);
    }

    @Override
    public Map<String, Object> queryListRegexSort(String commitId,String bracnch,String searchContent, long skip, int limit) {
        Criteria criteria = new Criteria();
        if (!CommonUtils.isNullOrEmpty(searchContent)) {
            Criteria bindProjectNameEnCnCriteria = Criteria.where(Dict.BINDPROJECT_NAMEEN).regex(searchContent);
            Criteria bindProjectNameCnCnCriteria = Criteria.where(Dict.BINDPROJECT_NAMECN).regex(searchContent);

            Criteria userNameEnCriteria = Criteria.where("user." + Dict.NAMEEN).regex(searchContent);
            Criteria userNameCnCriteria = Criteria.where("user." + Dict.NAMECN).regex(searchContent);

            Criteria pipelineNameCriteria = Criteria.where(Dict.PIPELINENAME).regex(searchContent);
            criteria = new Criteria().orOperator(bindProjectNameEnCnCriteria, bindProjectNameCnCnCriteria, userNameEnCriteria, userNameCnCriteria, pipelineNameCriteria);
        }
        if (!CommonUtils.isNullOrEmpty(bracnch)) {
            criteria = Criteria.where(Dict.BRANCH).is(bracnch);
        }
        if (!CommonUtils.isNullOrEmpty(commitId)) {
            criteria = Criteria.where(Dict.COMMITID).is(commitId);
        }
        SkipOperation skipOperation = Aggregation.skip(0L);
        LimitOperation limitOperation = Aggregation.limit(100L);
        if (limit != 0) {
            skipOperation = Aggregation.skip(skip);
            limitOperation = Aggregation.limit(limit);
        }
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.lookup("pipeline", "pipelineId", "pipelineId", "pipeline"),
                Aggregation.unwind("pipeline", true),
                Aggregation.replaceRoot(ObjectOperators.MergeObjects.merge("$$ROOT", new Document("version", "$$ROOT.pipeline.version"))),
                Aggregation.project().andExclude("pipeline",
                        Dict._ID,
                        Dict.PROJECTINFO,
                        Dict.GITINFO,
                        Dict.VARIABLES,
                        Dict.VOLUMES),
                Aggregation.facet(
                        Aggregation.sort(new Sort(Sort.Direction.DESC, Dict.STARTTIME)),
                        skipOperation,
                        limitOperation
                ).as("pipelineExeList")
                        .and(Aggregation.count().as("count")).as("total"),
                Aggregation.unwind("total", true),
                aggregationOperationContext -> Document.parse("{$addFields: {\"total\":\"$total.count\"}}")
        );
        AggregationResults<Document> pipeline_exe = mongoTemplate.aggregate(aggregation, "pipeline_exe", Document.class);
        List<Document> mappedResults = pipeline_exe.getMappedResults();
        return mappedResults.get(0);
    }

    @Override
    public void save(PipelineExe pi) {
        mongoTemplate.save(pi);
    }

    @Override
    public PipelineExe queryPipelineExeByExeId(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.EXEID).is(id));
        return mongoTemplate.findOne(query, PipelineExe.class);
    }

    @Override
    public void updateStagesAndStatus(PipelineExe pipelineExe) throws Exception {
        Query query = new Query(Criteria.where(Dict.EXEID).is(pipelineExe.getExeId()));
        Update update = Update.update(Dict.EXEID, pipelineExe.getExeId());
        update.set(Dict.STARTTIME, pipelineExe.getStartTime());
        update.set(Dict.ENDTIME, pipelineExe.getEndTime());
        update.set(Dict.COSTTIME, pipelineExe.getCostTime());
        update.set(Dict.STATUS, pipelineExe.getStatus());
        update.set(Dict.STAGES, pipelineExe.getStages());
        mongoTemplate.findAndModify(query, update, PipelineExe.class);
        PipelineExe pipelineExeAfter = queryPipelineExeByExeId(pipelineExe.getExeId());
        logger.info("$$$$$$  query pipelineExe from DB after update, pipelineExe :" + JSONObject.toJSON(pipelineExeAfter));
    }

    @Override
    public void updateStagesAndStatusAndUser(PipelineExe pipelineExe) throws Exception {
        Query query = new Query(Criteria.where(Dict.EXEID).is(pipelineExe.getExeId()));
        Update update = Update.update(Dict.EXEID, pipelineExe.getExeId());
        update.set(Dict.STARTTIME, pipelineExe.getStartTime());
        update.set(Dict.ENDTIME, pipelineExe.getEndTime());
        update.set(Dict.COSTTIME, pipelineExe.getCostTime());
        update.set(Dict.STATUS, pipelineExe.getStatus());
        update.set(Dict.STAGES, pipelineExe.getStages());
        Map userMap = new HashMap();
        userMap.put(Dict.USER_ID, pipelineExe.getUser().getId());
        userMap.put(Dict.NAMEEN, pipelineExe.getUser().getNameEn());
        userMap.put(Dict.NAMECN, pipelineExe.getUser().getNameCn());
        update.set(Dict.USER, userMap);
        mongoTemplate.findAndModify(query, update, PipelineExe.class);
        PipelineExe pipelineExeAfter = queryPipelineExeByExeId(pipelineExe.getExeId());
        logger.info("$$$$$$  query pipelineExe from DB after update, pipelineExe :" + JSONObject.toJSON(pipelineExeAfter));
    }

    @Override
    public void updateArtifacts(PipelineExe pipelineExe) throws Exception {
        Query query = new Query(Criteria.where(Dict.EXEID).is(pipelineExe.getExeId()));
        Update update = Update.update(Dict.EXEID, pipelineExe.getExeId());
        update.set(Dict.ARTIFACTS, pipelineExe.getArtifacts());
        mongoTemplate.findAndModify(query, update, PipelineExe.class);
    }

    @Override
    public PipelineExe queryOneByPipelineIdSort(String pipelineId) {
        Query query = new Query(Criteria.where(Dict.PIPELINEID).is(pipelineId));
        query.with(new Sort(Sort.Direction.DESC, Dict.STARTTIME));
        return mongoTemplate.findOne(query, PipelineExe.class);
    }

    @Override
    public List<PipelineExe> queryExeByPipeLineNumberOrCommitId(String pipleNumber, String commitId) {
        List<Criteria> orList = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(pipleNumber)) {
            Criteria numberCriteria = Criteria.where(Dict.PIPELINENUMBER).is(Integer.valueOf(pipleNumber));
            orList.add(numberCriteria);
        }
        if (!CommonUtils.isNullOrEmpty(commitId)) {
            Criteria commitIdCriteria = Criteria.where(Dict.COMMITID).is(commitId);
            orList.add(commitIdCriteria);
        }
        Criteria orCriteria = new Criteria().orOperator(orList.toArray(new Criteria[orList.size()]));
        Query query = new Query();
        query.addCriteria(orCriteria);
        query.with(Sort.by(Sort.Order.desc(Dict.ENDTIME)));
        return mongoTemplate.find(query,PipelineExe.class);
    }

    /**
     * 查询存在有running的job的exe列表
     *
     * @return
     */
    @Override
    public List<PipelineExe> findRunningJobPipeline() {

        Criteria jobExesCriteria = new Criteria();
        jobExesCriteria.and(Dict.JOBEXESTATUS).is(Dict.RUNNING);

        Criteria jobsCriteria = new Criteria();
        jobsCriteria.and(Dict.JOBEXES).elemMatch(jobExesCriteria);

        Criteria stagesCriteria = new Criteria();
        stagesCriteria.and(Dict.JOBS).elemMatch(jobsCriteria);

        Criteria criteria = new Criteria();
        criteria.and(Dict.STAGES).elemMatch(stagesCriteria);

        Query query = new Query(criteria);
        return this.mongoTemplate.find(query, PipelineExe.class, "pipeline_exe");
    }

    /**
     * 查询流水线exe通过流水线的nameId
     *
     * @param pipelineNameId
     * @return
     */
    @Override
    public List<PipelineExe> queryExeByPipelineNameId(String pipelineNameId) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PIPELINENAMEID).is(pipelineNameId);
        Query query = new Query(criteria);
        return this.mongoTemplate.find(query, PipelineExe.class, "pipeline_exe");
    }
}
