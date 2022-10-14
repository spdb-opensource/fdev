package com.spdb.fdev.fdevtask.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.dao.IFdevTaskDao;
import com.spdb.fdev.fdevtask.spdb.dao.IReleaseApproveDao;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.ReleaseApprove;
import net.sf.json.JSONObject;
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
import java.util.regex.Pattern;

/**
 * @Author liux81
 * @DATE 2021/12/30
 */
@Repository
public class ReleaseApproveDaoImpl implements IReleaseApproveDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    IFdevTaskDao fdevTaskDao;

    @Override
    public void save(ReleaseApprove releaseApprove) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        releaseApprove.setId(id);
        if(!CommonUtils.isNullOrEmpty(releaseApprove.getAudit_time())){
            //首次合并，审核时间=申请时间
            releaseApprove.setApply_time(releaseApprove.getAudit_time());
        }else {
            releaseApprove.setApply_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));
        }
        mongoTemplate.save(releaseApprove);
    }

    /**
     * 可根据审核状态(多选)、任务所属小组（多选）、申请人（多选）、任务名称（模糊搜索）、任务id（单选精准匹配）分页查询
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> queryReleaseApproveList(Map param) {
        Criteria criteria = new Criteria();
        Criteria taskCriteria = new Criteria();
        //任务信息查询，先查询出符合的任务id集合
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.GROUP))){
            taskCriteria.and(Dict.GROUP).in((List<String>) param.get(Dict.GROUP));
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.TASKNAME))){
            Pattern pattern = Pattern.compile("^.*" + param.get(Dict.TASKNAME) + ".*$");
            taskCriteria.and(Dict.NAME).regex(pattern);
        }
        Set<String> taskIds = new HashSet<>();
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.GROUP)) || !CommonUtils.isNullOrEmpty(param.get(Dict.TASKNAME))){
            //查询符合条件的任务,获取任务id集合
            Query taskquery = Query.query(taskCriteria);
            List<FdevTask> fdevTasks = mongoTemplate.find(taskquery, FdevTask.class);
            if(!CommonUtils.isNullOrEmpty(fdevTasks)){
                for(FdevTask fdevTask : fdevTasks){
                    taskIds.add(fdevTask.getId());
                }
            }
        }

        //查询申请记录
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.STATUS))){
            //按审核状态搜索
            criteria.and(Dict.STATUS).in((List<Integer>) param.get(Dict.STATUS));
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.TASKID))){
            //按任务id搜索
            criteria.and(Dict.TASK_ID).is((String) param.get(Dict.TASKID));
        }else {
            if(!CommonUtils.isNullOrEmpty(param.get(Dict.GROUP)) || !CommonUtils.isNullOrEmpty(param.get(Dict.TASKNAME))){
                criteria.and(Dict.TASK_ID).in(taskIds);
            }
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.APPLICANT))){
            //按申请人搜索
            criteria.and(Dict.APPLICANT).in((List<String>) param.get(Dict.APPLICANT));
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.AUDITOR))){
            //按审核人搜索
            criteria.and(Dict.AUDITOR).in((List<String>) param.get(Dict.AUDITOR));
        }

        //按环境过滤
        criteria.and(Dict.ENV).is( param.get(Dict.ENV) );

        //按时间区间过滤
        if( !CommonUtils.isNullOrEmpty(param.get(Dict.START_DATE)) && !CommonUtils.isNullOrEmpty(param.get(Dict.END_DATE))){
            criteria.and(Dict.APPLY_TIME).gt(param.get(Dict.START_DATE)).lt( param.get(Dict.END_DATE) );

        }
        //任务详情中的申请记录始终展示，工作台-审核列表中的记录不展示删除的和初次合并的
        if(CommonUtils.isNullOrEmpty(param.get(Dict.REQUESTSOURCE)) || !"1".equals(param.get(Dict.REQUESTSOURCE))){
            criteria.and(Dict.DELETE).ne("del");
            //只查询需要审核的数据（需要审核的数据申请描述字段不为空，自动合并的没有此字段）
            criteria.and("apply_desc").ne(null);
        }
        Query query = Query.query(criteria);
        long count = mongoTemplate.count(query, ReleaseApprove.class);
        LookupOperation lookup = Aggregation.lookup(Dict.TASK, Dict.TASK_ID, Dict.ID, Dict.TASKINFO);
        MatchOperation match = Aggregation.match(criteria);
        SortOperation sort = Aggregation.sort(new Sort(Sort.Direction.DESC, Dict.APPLY_TIME));
        //分页
        Aggregation aggregation;
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.PAGE_SIZE)) && (int)param.get(Dict.PAGE_SIZE) > 0){
            long index = 0;
            if(!CommonUtils.isNullOrEmpty(param.get(Dict.CURRENTPAGE)) && (int)param.get(Dict.CURRENTPAGE) > 1){
                index = ((int)param.get(Dict.CURRENTPAGE) - 1) * (int)param.get(Dict.PAGE_SIZE);
            }
            aggregation = Aggregation.newAggregation(lookup, match, sort, Aggregation.skip(index),Aggregation.limit((int) param.get(Dict.PAGE_SIZE)));
        }else {
            aggregation = Aggregation.newAggregation(lookup, match, sort);
        }
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, ReleaseApprove.class, Map.class);

        for(Map map : results){
            if(!CommonUtils.isNullOrEmpty(map.get(Dict.TASKINFO))){
                map.put(Dict.TASKINFO,((List)map.get(Dict.TASKINFO)).get(0));
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.COUNT,count);
        result.put("list",results.getMappedResults());
        return result;
    }

    @Override
    public ReleaseApprove queryById(String id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        query.fields().exclude("_id");
        return mongoTemplate.findOne(query, ReleaseApprove.class);
    }

    @Override
    public ReleaseApprove update(ReleaseApprove releaseApprove) throws JsonProcessingException {
        Query query = Query.query(Criteria.where(Dict.ID).is(releaseApprove.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(releaseApprove);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, releaseApprove.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if ("_id".equals(key)) {
                continue;
            }
            if(Dict.TASK_ID.equals(key)){
                if(CommonUtils.isNullOrEmpty(value))
                    continue;
                else
                    update.set(key, value);
            }
            update.set(key, value);
        }
        return mongoTemplate.findAndModify(query,update,ReleaseApprove.class);
    }

    @Override
    public long count(List status) {
        Query query = Query.query(Criteria.where(Dict.STATUS).in(status).and(Dict.ENV).is("uat").and(Dict.DELETE).ne("del").and("apply_desc").ne(null));
        return mongoTemplate.count(query,ReleaseApprove.class);
    }

    @Override
    public void deleteByTaskId(String taskId) {
        Query query = Query.query(Criteria.where(Dict.TASK_ID).is(taskId));
        Update update = Update.update(Dict.DELETE,"del");
        mongoTemplate.updateMulti(query,update,ReleaseApprove.class);
    }

    @Override
    public long getCountByTaskId(String taskId) {
        Query query = Query.query(Criteria.where(Dict.TASK_ID).is(taskId).and(Dict.ENV).is(Dict.UAT));
        return mongoTemplate.count(query,ReleaseApprove.class);
    }

    //批量查询某个任务的合并申请次数
    @Override
    public List<Map> getCountByTaskIds(Set<String> taskIds) {
        MatchOperation match = Aggregation.match(Criteria.where(Dict.TASK_ID).in(taskIds));
        GroupOperation taskGroup = Aggregation.group(Dict.TASK_ID).count().as("num");
        Aggregation aggregation = Aggregation.newAggregation(match, taskGroup);
        List<Map> result = mongoTemplate.aggregate(aggregation, "release_approve", Map.class).getMappedResults();
        return result;
    }


}
