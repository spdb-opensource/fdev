package com.spdb.fdev.pipeline.dao.impl;

import com.mongodb.client.result.UpdateResult;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.dao.IPipelineCronDao;
import com.spdb.fdev.pipeline.dao.IPipelineDao;
import com.spdb.fdev.pipeline.dao.IPluginDao;
import com.spdb.fdev.pipeline.entity.*;
import com.spdb.fdev.pipeline.service.IUserService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PipelineDaoImpl implements IPipelineDao {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IPluginDao pluginDao;
    @Autowired
    private IPipelineCronDao pipelineCronDao;
    @Autowired
    private IUserService userService;

    @Override
    public Pipeline queryById(String id) {
        Query query = new Query(Criteria.where(Dict.PIPELINEID).is(id));
        return mongoTemplate.findOne(query, Pipeline.class, "pipeline");
    }

    @Override
    public List<Pipeline> queryDetailByProjectId(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.BINDPROJECTPROJECTID)
                .is(id).and(Dict.TRIGGERRULES_PUSH_SWITCH).is(true).and(Dict.STATUS).is("1"));
        return mongoTemplate.find(query, Pipeline.class);
    }

    @Override
    public Pipeline queryOneByProjectId(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.BINDPROJECTPROJECTID)
                .is(id).and(Dict.STATUS).is("1"));
        return mongoTemplate.findOne(query, Pipeline.class);
    }

    @Override
    public String add(Pipeline pipeline) throws Exception{
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        pipeline.setId(id);
        if(CommonUtils.isNullOrEmpty(pipeline.getNameId())){
            pipeline.setNameId(id);
        }
        pipeline.setStatus(Constants.STATUS_OPEN);
        //开始新增流水线时创建时间和更新时间一致
        pipeline.setCreateTime(TimeUtils.getDate(TimeUtils.FORMAT_DATE_TIME));
        pipeline.setUpdateTime(pipeline.getCreateTime());
        return this.mongoTemplate.save(pipeline).getId();
    }

    @Override
    public void delete(String pipelineId) {
        Query query = new Query(Criteria.where(Dict.PIPELINEID).is(pipelineId));
//        Pipeline pipeline = mongoTemplate.findOne(query, Pipeline.class);
        mongoTemplate.findAndRemove(query, Pipeline.class);
    }

    @Override
    public void updateStatusClose(String id , User user){
        Query query = Query.query(Criteria.where(Dict.PIPELINEID).is(id));
        Update update = Update.update(Dict.STATUS, Constants.STATUS_CLOSE);
        mongoTemplate.findAndModify(query, update, Pipeline.class);
    }

    /**
     * 查询流水线列表
     *
     * @param skip
     * @param limit
     * @param applicationId 应用id
     * @param userId    用户id
     * @param apps  应用集合
     * @param user  当前用户可以看得到的流水线
     * @param searchContent     搜索内容
     * @return
     */
    @Override
    public Map<String, Object> queryPipelineList(long skip, int limit, String applicationId, String userId, List<String> apps, User user, String searchContent) {
        Map<String, Object> map = new HashMap<>();
        Criteria baseCri = new Criteria();
        baseCri.and(Dict.STATUS).ne(Constants.ZERO);
//        Query query = Query.query(Criteria.where(Dict.STATUS).ne(Constants.ZERO));
        if(!CommonUtils.isNullOrEmpty(applicationId)) {
//            Criteria appCriteria = Criteria.where(Dict.BINDPROJECTPROJECTID).is(applicationId);
//            query.addCriteria(appCriteria);
            baseCri.and(Dict.BINDPROJECTPROJECTID).is(applicationId);
        }
        if(!CommonUtils.isNullOrEmpty(userId)) {
//            Criteria collectedCriteria = Criteria.where(Dict.COLLECTED).in(userId);
//            query.addCriteria(collectedCriteria);
            baseCri.and(Dict.COLLECTED).in(userId);
        }
        if(!CommonUtils.isNullOrEmpty(apps)) {
//            Criteria collectedCriteria = Criteria.where(Dict.BINDPROJECTPROJECTID).in(apps);
//            query.addCriteria(collectedCriteria);
            baseCri.and(Dict.BINDPROJECTPROJECTID).in(apps);
        }
        if(!CommonUtils.isNullOrEmpty(searchContent)) {
            searchContent = ".*?" + searchContent + ".*";
            Criteria nameCriteria = Criteria.where(Dict.NAME).regex(searchContent);
            Criteria projectNameEnCriteria = Criteria.where(Dict.BINDPROJECT_NAMEEN).regex(searchContent);
            Criteria projectNameCnCriteria = Criteria.where(Dict.BINDPROJECT_NAMECN).regex(searchContent);
            Criteria descCriteria = Criteria.where(Dict.DESC).regex(searchContent);
            Criteria authorCriteria = Criteria.where(Dict.AUTHOR_NAMECN).regex(searchContent);
            Criteria criteria = new Criteria().orOperator(nameCriteria, projectNameEnCriteria,projectNameCnCriteria, descCriteria, authorCriteria);
            baseCri.orOperator(nameCriteria, projectNameEnCriteria,projectNameCnCriteria, descCriteria, authorCriteria);
//            query.addCriteria(criteria);
        }
        //先获取当前登录用户的所属条线列表（调用户模块接口）（普通人只会有一个条线，领导可以有多个条线）
//        if(!CommonUtils.isNullOrEmpty(user)) {
//            String groupId = user.getGroup_id();
//            List<String> groupIds = userService.getLineIdsByGroupId(groupId);
//            if (!CommonUtils.isNullOrEmpty(groupIds)) {
////                query.addCriteria(Criteria.where(Dict.GROUPLINEID).in(groupIds));
//                baseCri.and(Dict.GROUPLINEID).in(groupIds);
//            }
//        }
        List<AggregationOperation> aggOperationList = new ArrayList<>();
        MatchOperation match = Aggregation.match(baseCri);
        LookupOperation lookup = Aggregation.lookup(Dict.PIPELINEDIGITALRATE, Dict.NAMEID, Dict.NAMEID, "digitalRate");
        UnwindOperation unwind = Aggregation.unwind("digitalRate", true);
        aggOperationList.add(match);
        aggOperationList.add(lookup);
        aggOperationList.add(unwind);

        List<String> sortList = new ArrayList<>();
        //sortList.add("build_time");
        sortList.add(Dict.UPDATETIME);
        Query query = new Query(baseCri);
        query.with(new Sort(Sort.Direction.DESC, sortList));
        long total = mongoTemplate.count(query, Pipeline.class);
        map.put(Dict.TOTAL, total);
        if(limit != 0) {
            query.skip(skip).limit(limit);
            Sort orders = new Sort(Sort.Direction.DESC, sortList);
            SortOperation sort = Aggregation.sort(orders);
            SkipOperation skipOperation = Aggregation.skip(skip);
            LimitOperation limitOperation = Aggregation.limit(limit);
            aggOperationList.add(sort);
            aggOperationList.add(skipOperation);
            aggOperationList.add(limitOperation);
        }
        Map addFieldsMap = new HashMap();
        addFieldsMap.put(Dict.AVEERRORTIME, "$digitalRate.aveErrorTime");
        addFieldsMap.put(Dict.ERROREXETOTAL, "$digitalRate.errorExeTotal");
        addFieldsMap.put(Dict.EXETOTAL, "$digitalRate.exeTotal");
        addFieldsMap.put(Dict.SUCCESSEXETOTAL, "$digitalRate.successExeTotal");
        addFieldsMap.put(Dict.SUCCESSRATE, "$digitalRate.successRate");
        addFieldsMap.put(Dict.ID, "$" + Dict.PIPELINEID);
        aggOperationList.add(getAddFieldsAggregationOper(addFieldsMap));
        Map projectMap = new HashMap();
        projectMap.put("digitalRate", 0);
        projectMap.put(Dict.PIPELINEID, 0);
        aggOperationList.add(getProjectFiterFields(projectMap));

        Aggregation aggregation = Aggregation.newAggregation(aggOperationList);
        AggregationResults<Map> results = this.mongoTemplate.aggregate(aggregation, Dict.PIPELINE, Map.class);
        List<Map> list = results.getMappedResults();
//        List<Pipeline> list = mongoTemplate.find(query, Pipeline.class);

        map.put(Dict.PIPELINELIST, list);
        return map;
    }

    /**
     * 由于spring没有实现mongo的addFields需要自己写，于是加了该方法
     * 传入的map格式需要
     *  key 为聚合增加的字段名，value为值对应的$字段
     *  对应的mongo命令为
     *      {
     *         $addFields:{
     *             "aveErrorTime":"$digitalRate.aveErrorTime",
     *             "errorExeTotal":"$digitalRate.errorExeTotal",
     *             "exeTotal":"$digitalRate.exeTotal",
     *             "successExeTotal":"$digitalRate.successExeTotal",
     *             "successRate":"$digitalRate.successRate"
     *         }
     *     }
     * @param data
     * @return
     */
    private AggregationOperation getAddFieldsAggregationOper(Map<String, Object> data) {
        AggregationOperation aggregationOperation = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext aggregationOperationContext) {
                return new Document("$addFields", new Document(data));
            }
        };
        return aggregationOperation;
    }

    /**
     * 实现project的过滤字段 即mongo命令为
     *     {
     *         $project:{
     *             "digitalRate":0
     *         }
     *     }
     * @param data
     * @return
     */
    private AggregationOperation getProjectFiterFields(Map<String, Object> data) {
        AggregationOperation project = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext aggregationOperationContext) {
                return new Document("$project", new Document(data));
            }
        };
        return project;
    }

    @Override
    public String saveDraft(PipelineDraft draft) throws Exception{
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        draft.setId(id);
        //1. 将前草稿删除
        String authorID = userService.getUserFromRedis().getId();
        deleteDraft(authorID);
        //2. 保存作者和时间
        draft.setAuthorId(authorID);
        draft.setUpdateTime(TimeUtils.getDate(TimeUtils.FORMAT_DATE_TIME));
        return this.mongoTemplate.save(draft).getId();
    }

    @Override
    public void deleteDraft(String authorId) throws Exception{
        Query query = new Query(Criteria.where(Dict.AUTHORID).is(authorId));
        if(!CommonUtils.isNullOrEmpty(mongoTemplate.findOne(query, PipelineDraft.class))){
            mongoTemplate.findAndRemove(query, PipelineDraft.class);
        }
    }

    @Override
    public Images findImageById(String id) {
        Query query = Query.query(Criteria.where(Dict.IMAGEID).is(id));
        return  mongoTemplate.findOne(query, Images.class);
    }

    @Override
    public Images findDefaultImage() {
        Query query = Query.query(Criteria.where(Dict.NAME).is(Constants.IMAGE_BASE));
        return  mongoTemplate.findOne(query, Images.class);
    }

    @Override
    public PipelineDraft readDraftByAuthor(String authorID) {
        Query query = Query.query(Criteria.where(Dict.AUTHORID).is(authorID));
        return mongoTemplate.findOne(query, PipelineDraft.class);
    }

    @Override
    public long updateFollowStatus(String pipelineId, User user) {
        Query query = Query.query(Criteria.where(Dict.PIPELINEID).is(pipelineId));
        Pipeline pipeline = mongoTemplate.findOne(query,Pipeline.class);
        List<String>  users = null;
        if(!CommonUtils.isNullOrEmpty(pipeline)) {
            users = pipeline.getCollected(); //获取已收藏该流水线的用户集合
            if(CommonUtils.isNullOrEmpty(users)){ //没有人关注过该流水线
                users = new ArrayList<>();
                users.add(user.getId());
            }else{
                if(users.contains(user.getId())){
                    users.remove(user.getId());
                }else {
                    users.add(user.getId());
                }
            }
        }
        Update update = new Update().set(Dict.COLLECTED,users);
        UpdateResult updateResult = mongoTemplate.updateFirst(query,update,Pipeline.class);
        return Optional.ofNullable(updateResult.getMatchedCount()).orElse(0l);
    }


    @Override
    public List<Pipeline> queryAllPipeline() {
        Query query = new Query();
        List<Pipeline> pipelineList = mongoTemplate.find(query, Pipeline.class);
        return pipelineList;
    }

    @Override
    public Pipeline findActiveVersion(String nameId) {
        Query query = Query.query(Criteria.where(Dict.NAMEID).is(nameId).and(Dict.STATUS).is(Constants.STATUS_OPEN));
        query.with(new Sort(Sort.Direction.DESC, Dict.UPDATETIME));
        return mongoTemplate.findOne(query, Pipeline.class);
    }

    @Override
    public void updateBuildTime(Pipeline pipeline){
        Query query = Query.query(Criteria.where(Dict.PIPELINEID).is(pipeline.getId()));
        Update update = Update.update(Dict.BUILDTIME, pipeline.getBuildTime());
        update.set(Dict.UPDATETIME,pipeline.getUpdateTime());
        mongoTemplate.findAndModify(query, update, Pipeline.class);
    }

    @Override
    public Map<String, Object> findHistoryPipelineList(long skip, int limit, String nameId) {
        Map<String, Object> map = new HashMap<>();
        Query query = Query.query(Criteria.where(Dict.NAMEID).is(nameId).and(Dict.STATUS).is(Constants.STATUS_CLOSE));
        query.with(new Sort(Sort.Direction.DESC, Dict.UPDATETIME));
        long total = mongoTemplate.count(query, Pipeline.class);
        map.put(Dict.TOTAL, total);
        if(limit != 0) {
            query.skip(skip).limit(limit);
        }
        List<Pipeline> list = mongoTemplate.find(query, Pipeline.class);
        map.put(Dict.PIPELINELIST, Dict.LIST);
        return map;
    }

    /**
     * 查询有pipeline使用该插件的记录
     *
     * @param pluginId
     * @return
     */
    @Override
    public List<Pipeline> queryByPluginId(String pluginId) {
        //查询stages下的jobs下的steps下的plugin_info.id  is  pluginId
        Criteria pluginCriteria = new Criteria();
        //pluginCriteria.and("plugin_info.id").is(pluginId);
        pluginCriteria.and(Dict.PLUGININFO_PLUGINCODE).is(pluginId);
        Criteria stepCriteria = new Criteria();
        stepCriteria.and(Dict.STEPS).elemMatch(pluginCriteria);

        Criteria jobsCriteria = new Criteria();
        jobsCriteria.and(Dict.JOBS).elemMatch(stepCriteria);

        Criteria criteria = new Criteria();
        criteria.and(Dict.STAGES).elemMatch(jobsCriteria);

        Query query = new Query(criteria);
        List<Pipeline> pipeline = this.mongoTemplate.find(query, Pipeline.class, Dict.PIPELINE);
        return pipeline;
    }

    /**
     *
     * 查询定时触发的流水线
     * @return
     */
    @Override
    public List<Pipeline> querySchedulePipelines() {
        Criteria criteria = new Criteria();
        criteria.and(Dict.TRIGGERRULES_SCHEDULE_SWITCHFLAG).is(true);
        criteria.and(Dict.STATUS).is(Constants.ONE);
        Query query = new Query(criteria);
        List<Pipeline> pipelines = this.mongoTemplate.find(query, Pipeline.class, Dict.PIPELINE);
        return pipelines;
    }

    @Override
    public String updateTriggerRules(String pipelineId, TriggerRules triggerRules) throws Exception {
        Query query = Query.query(Criteria.where(Dict.PIPELINEID).is(pipelineId));
        Update update = Update.update(Dict.PIPELINEID, pipelineId);
        update.set(Dict.TRIGGERRULES, triggerRules);
        UpdateResult updateResult = mongoTemplate.updateFirst(query,update,Pipeline.class);
        long modifiedCount = updateResult.getModifiedCount();
        return modifiedCount>0 ? Constants.SUCCESS:Constants.FAIL;
    }

    /**
     * id查询不需要判断status
     * nameId查询取status为1的取出来
     *
     * @param param
     * @return
     */
    @Override
    public Pipeline queryPipelineByIdOrNameId(Map param) {
        String id = (String) param.get(Dict.ID);
        String nameId = (String) param.get(Dict.NAMEID);
        if (CommonUtils.isNullOrEmpty(id)) {
            if (CommonUtils.isNullOrEmpty(nameId)) {
                throw new FdevException(ErrorConstants.PARAMS_IS_ILLEGAL, new String[]{"nameId and id is null"});
            }
            return findActiveVersion(nameId);
        }else {
            return queryById(id);
        }
    }

    @Override
    public List queryByEntityId(Map request) {
        List<String> entities = (List) request.get(Dict.ENTITYIDS);
        //分页
        Integer pageSize = (Integer) request.get(Dict.PAGESIZE);
        Integer pageNum = (Integer) request.get(Dict.PAGE_NUM);
        List allList = new ArrayList();
        for (String entity : entities) {
            Criteria yamlConfigCriteria = new Criteria();
            yamlConfigCriteria.and(Dict.PARAMS+"."+Dict.ENTITY+".entityId").in(entity);
            Query yamlConfigQuery = new Query(yamlConfigCriteria);
            List<String> configIds = mongoTemplate.findDistinct(yamlConfigQuery, Dict.CONFIGID, YamlConfig.class, String.class);

            Map pipelineInfo = new HashMap();
            Criteria entityTemplateParamsMatch = new Criteria();
            entityTemplateParamsMatch.and(Dict.ENTITY__ID).in(entity);

            Criteria paramsMatch = new Criteria();
            paramsMatch.orOperator(
                    new Criteria().and(Dict.ENTITYTEMPLATEPARAMS).elemMatch(entityTemplateParamsMatch),
                    new Criteria().and(Dict.VALUE).in(configIds)
            );

            Criteria stepsMatch = new Criteria();
            stepsMatch.and(Dict.PLUGININFO__PARAMS).elemMatch(paramsMatch);

            Criteria jobsMatch = new Criteria();
            jobsMatch.and(Dict.STEPS).elemMatch(stepsMatch);

            Criteria stageMatch = new Criteria();
            stageMatch.and(Dict.JOBS).elemMatch(jobsMatch);

            Criteria criteria = new Criteria();
            criteria.and(Dict.STAGES).elemMatch(stageMatch);

            criteria.and(Dict.STATUS).is(Constants.ONE);

            Query query = new Query(criteria);
            long count = this.mongoTemplate.count(query, Pipeline.class);
            if (pageNum != null && pageNum > 0) {
                //第0页或没有的情况，默认查所有
                if ((pageSize == null || pageSize <= 0)) {
                    //若有页数 但是没有页寸，用默认，默认5
                    pageSize = 5;
                }
                query.skip((pageNum - 1) * pageSize).limit(pageSize);
            }
            List<Pipeline> pipelines = this.mongoTemplate.find(query, Pipeline.class, Dict.PIPELINE);
            List resultList = new ArrayList();
            for (Pipeline pipeline : pipelines) {
                Map resultMap = new HashMap();
                String pipelineId = pipeline.getId();
                String nameId = pipeline.getNameId();
                BindProject bindProject = pipeline.getBindProject();
                String pipelineName = pipeline.getName();
                Author author = pipeline.getAuthor();
                resultMap.put(Dict.PIPELINEID, pipelineId);
                resultMap.put(Dict.NAMEID, nameId);
                resultMap.put(Dict.BINDPROJECT, bindProject);
                resultMap.put(Dict.PIPELINENAME, pipelineName);
                resultMap.put(Dict.AUTHOR, author);
                resultList.add(resultMap);
            }
            pipelineInfo.put("pipelineList", resultList);
            pipelineInfo.put(Dict.COUNT, count);
            allList.add(pipelineInfo);
        }
        return allList;
    }

    /**
     * 根据nameId查询pipeline列表
     *
     * @param nameId
     * @return
     */
    @Override
    public List<Pipeline> queryPipelinesByNameId(String nameId) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.NAMEID).is(nameId);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.PIPELINEID));
        return this.mongoTemplate.find(query, Pipeline.class, Dict.PIPELINE);
    }

    @Override
    public List<Map> queryPipLookDigital() {
        Criteria criteria = new Criteria();
        criteria.and(Dict.STATUS).is("1");
        MatchOperation match = Aggregation.match(criteria);
        LookupOperation lookup = Aggregation.lookup("pipeline_exe", "pipelineId", "pipelineId", "exeInfo");
        AggregationOperation addOperation = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext aggregationOperationContext) {
                return new Document("$addFields", new Document("exeTotal", "$exeInfo.length"));
            }
        };

        Aggregation aggregation = Aggregation.newAggregation(match, lookup, addOperation);
        AggregationResults<Map> results = this.mongoTemplate.aggregate(aggregation,  Dict.PIPELINE, Map.class);
        List<Map> mappedResults = results.getMappedResults();
        return mappedResults;
    }
}
