package com.spdb.fdev.fdevtask.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.dao.TaskDao;
import com.spdb.fdev.fdevtask.spdb.entity.Task;
import com.spdb.fdev.fdevtask.spdb.service.DemandManageApi;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;
import com.spdb.fdev.fdevtask.spdb.service.ProcessApi;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @program: fdev-process
 * @description:
 * @author: c-jiangl2
 * @create: 2021-01-25 16:11
 **/
@Repository
public class TaskDaoImpl implements TaskDao {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private IUserApi userApi;

    @Resource
    private ProcessApi processApi;

    @Autowired
    private DemandManageApi demandManageApi;

    @Override
    public List<Task> query(Task task) throws Exception {
        List<Task> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        String json = task == null ? "{}" : objectMapper.writeValueAsString(task);
        BasicDBObject taskJson = BasicDBObject.parse(json);
        Iterator<String> it = taskJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = taskJson.get(key);
            if(value instanceof HashMap) {
                ((HashMap)value).forEach((k,v) ->
                c.and(key+"."+k).is(v)
            );
            }else{
                c.and(key).is(value);
            }
        }
        AggregationOperation project = Aggregation.project().andExclude("_id");
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<Task> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), "process_task", Task.class);
        docs.forEach(doc -> {
            result.add(doc);
        });
        return result;
    }

    @Override
    public Task save(Task task) throws Exception {
        return mongoTemplate.save(task);
    }

    @Override
    public Map getTaskList(String userId, Map params) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        Query query = new Query();
        Object pageSizeObject = params.get(Dict.PAGE_SIZE);
        Object pageNumObject = params.get(Dict.PAGE_NUM);
        String startDate = (String)params.get(Dict.STARTDATE);
        String endDate = (String) params.get(Dict.ENDDATE);
        String statusValue = (String) params.get(Dict.STATUS);
        Object assigneeIdObject = params.get(Dict.ASSIGNEE_ID);
        Object completeObject = params.get(Dict.COMPLETE);
        Object tagListObject = params.get(Dict.TAG_LIST);
        Object createUserIdObject = params.get(Dict.CREATEUSERID);
        Object planEndDateObject = params.get(Dict.PLAN_END_DATE);
        Object nameObject = params.get(Dict.NAME);
        Object priorityObject = params.get(Dict.PRIORITY);
        Object createTimeObject = params.get(Dict.CREATE_TIME);
        Object requirementPlanObject = params.get(Dict.REQUIREMENT_PLAN);
        Object requirementIdObject = params.get(Dict.REQUIREMENT_ID);
        //关于当前用户
        Criteria criteria = new Criteria();
        if (!CommonUtils.isNullOrEmpty(userId)){
            criteria.orOperator(Criteria.where(Dict.CREATEUSERID).is(userId),Criteria.where(Dict.ASSIGNEE_LIST).is(userId));
        }
        //筛选条件
        Criteria criteria2 = new Criteria();
        if(!CommonUtils.isNullOrEmpty(createTimeObject)){
            criteria2.and(Dict.CREATE_TIME).gte(createTimeObject + " 00:00:00").lte(createTimeObject + " 23:59:59");
        }
        if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
            criteria2.and(Dict.CREATE_TIME).gte(startDate + " 00:00:00").lte(endDate + " 23:59:59");
        }
        if(StringUtils.isNotBlank(startDate) && StringUtils.isBlank(endDate)){
            criteria2.and(Dict.CREATE_TIME).gte(startDate + " 00:00:00");
        }
        if(StringUtils.isBlank(startDate) && StringUtils.isNotBlank(endDate)){
            criteria2.and(Dict.CREATE_TIME).lte(endDate + " 23:59:59");
        }
        if(StringUtils.isNotBlank(statusValue)){
            criteria2.and(Dict.STATUS).is(statusValue);
        }
        if(!CommonUtils.isNullOrEmpty(assigneeIdObject)){
            String assigneeId = (String) assigneeIdObject;
            criteria2.and(Dict.ASSIGNEE_LIST).is(assigneeId);
        }
        if(!CommonUtils.isNullOrEmpty(completeObject)){
            String complete = (String) completeObject;
            ArrayList<Object> testList = new ArrayList<>();
            testList.add(null);
            testList.add("");
            if ("0".equals(complete + "")){
                criteria2.and(Dict.ENDDATE).in(testList);
            }else {
                criteria2.and(Dict.ENDDATE).nin(testList);
            }
        }
        if(!CommonUtils.isNullOrEmpty(tagListObject)){
            List<String> tagList = CommonUtils.castList(tagListObject,String.class);
            criteria2.and(Dict.TAG_LIST).is(tagList);
        }
        if(!CommonUtils.isNullOrEmpty(createUserIdObject)){
            String createUserId = (String) createUserIdObject;
            criteria2.and(Dict.CREATEUSERID).is(createUserId);
        }
        if(!CommonUtils.isNullOrEmpty(planEndDateObject)){
            String planEndDate = (String) planEndDateObject;
            criteria2.and(Dict.PLAN_END_DATE).is(planEndDate);
        }
        if(!CommonUtils.isNullOrEmpty(nameObject)){
            String name = (String) nameObject;
            Pattern compile = Pattern.compile(name,Pattern.CASE_INSENSITIVE);
            criteria2.and(Dict.NAME).regex(compile);
        }
        if(!CommonUtils.isNullOrEmpty(priorityObject)){
            Integer priority = (Integer) priorityObject;
            criteria2.and(Dict.PRIORITY).is(priority);
        }
        if(!CommonUtils.isNullOrEmpty(requirementPlanObject)){
            String requirementPlan = (String) requirementPlanObject;
            criteria2.and(Dict.REQUIREMENT_PLAN).is(requirementPlan);
        }
        if(!CommonUtils.isNullOrEmpty(requirementIdObject)){
            String requirementID = (String) requirementIdObject;
            criteria2.and(Dict.REQUIREMENT_ID).is(requirementID);
        }
        Criteria criteria3 = new Criteria();
        criteria3.andOperator(criteria,criteria2,Criteria.where(Dict.DELETE).is(0));
        query.addCriteria(criteria3);
        long count = mongoTemplate.count(query, Task.class);
        if (!CommonUtils.isNullOrEmpty(pageNumObject) && !CommonUtils.isNullOrEmpty(pageSizeObject)){
            String pageSize = (String) pageSizeObject;
            String pageNum = (String)pageNumObject ;
            query.skip((Integer.parseInt(pageNum) - 1) * Integer.parseInt(pageSize)).limit(Integer.parseInt(pageSize));
        }
        List<Task> tasks = mongoTemplate.find(query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME)), Task.class);
        result.put(Dict.TASK,tasks);
        result.put(Dict.COUNT,count);
        return result;
    }

    @Override
    public List<Task> isRepeat(String fieldName) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        criteria1.and(Dict.NAME).is(fieldName);
        criteria.and(Dict.USER_DEF_FIELD_LIST).elemMatch(criteria1);
        query.addCriteria(criteria);
        return mongoTemplate.find(query, Task.class);
    }

    @Override
    public Task updateTask(Task task) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(task);
        BasicDBObject taskJson = BasicDBObject.parse(json);
        Iterator<String> it = taskJson.keySet().iterator();
        Query query = Query.query(Criteria.where(Dict.ID).is(task.getId()));
        Update update = Update.update(Dict.ID, task.getId());
        while (it.hasNext()) {
            String key =  it.next();
            Object value = taskJson.get(key);
            if ("_id".equals(key) || "serialVersionUID".equals(key)) {
                continue;
            }
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, Task.class);
        return mongoTemplate.findOne(query, Task.class);
    }

    @Override
    public Task updateTaskInfo(Task task) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        String json = objectMapper.writeValueAsString(task);
        BasicDBObject taskJson = BasicDBObject.parse(json);
        Iterator<String> it = taskJson.keySet().iterator();
        Query query = Query.query(Criteria.where(Dict.ID).is(task.getId()));
        Update update = Update.update(Dict.ID, task.getId());
        while (it.hasNext()) {
            String key =  it.next();
            Object value = taskJson.get(key);
            if ("_id".equals(key) || "serialVersionUID".equals(key) || "status".equals(key)) {
                continue;
            }
            if(CommonUtils.isNullOrEmpty(value)){
                value = "";
                if ("bkInfo".equals(key)){
                    value = new HashMap<String,Object>();
                }
            }
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, Task.class);
        return mongoTemplate.findOne(query, Task.class);
    }

    @Override
    public List<Task> getProcessList(String userId,Map request) {
        Criteria criteria1 = new Criteria();
        criteria1.and(Dict.CREATEUSERID).is(userId);
        Criteria criteria2 = new Criteria();
        criteria2.and(Dict.ASSIGNEE_LIST).is(userId);
        Criteria criteria3 = new Criteria().orOperator(criteria1, criteria2);
        Criteria criteria4 = new Criteria();
        criteria4.and(Dict.DELETE).is(0);
        Criteria criteria5 = new Criteria();
        if(!CommonUtils.isNullOrEmpty(request.get(Dict.REQUIREMENT_ID))){
            criteria5.and(Dict.REQUIREMENT_ID).is(request.get(Dict.REQUIREMENT_ID));
        }
        return mongoTemplate.find(Query.query(criteria5.andOperator(criteria3,criteria4)),Task.class);
    }

    @Override
    public void updateEndTime(String taskId,String planEndDate) {
        Query query = Query.query(Criteria.where(Dict.ID).is(taskId));
        Update update = Update.update(Dict.ENDDATE, planEndDate);
        mongoTemplate.findAndModify(query, update, Task.class);
    }

    @Override
    public Task deleteTask(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.DELETE, 1);
        return mongoTemplate.findAndModify(query, update, Task.class);
    }

    @Override
    public List<Task> queryIds(List<String> ids) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).in(ids));
        return mongoTemplate.find(query,Task.class);
    }

    @Override
    public Map queryTaskByApplicationId(String applicationId,Integer type,int pageNum,int page,String name) throws Exception {
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        Criteria criteria2 = new Criteria();
        Criteria criteria3 = new Criteria();
        criteria.and(Dict.RELATED_APPLICATION).is(applicationId);
        if (!CommonUtils.isNullOrEmpty(type)){
            criteria1.and(Dict.DELETE).is(type);
        }
        if (!CommonUtils.isNullOrEmpty(name)){
            name = escapeExprSpeciaWord(name);
            criteria2.and(Dict.NAME).regex(name);
        }
        Query query = new Query(criteria3.andOperator(criteria,criteria1,criteria2));
        long count = mongoTemplate.count(query, Task.class);
        if (CommonUtils.isNotNullOrEmpty(pageNum+"",page+"")){
            query.limit(pageNum).skip((page - 1) * pageNum );
        }
        List<Task> tasks = mongoTemplate.find(query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME)), Task.class);
        HashMap<String, Object> returnMap = new HashMap<>();
        returnMap.put(Dict.COUNT,count);
        returnMap.put(Dict.TASKLIST,tasks);
        return returnMap;
    }

    @Override
    public Map queryTaskByFdevUnitId(String fdevUnitId, int pageNum, int page,Integer delete) throws Exception {
        Criteria criteria = new Criteria();
        if (!CommonUtils.isNullOrEmpty(delete)){
            criteria.and(Dict.DELETE).is(delete);
        }
        Query query = new Query(criteria.andOperator(Criteria.where(Dict.IMPL_UNIT_ID).is(fdevUnitId)));
        List<Task> taskList = mongoTemplate.find(query, Task.class);
        long count = taskList.stream().filter(taskTemp -> !CommonUtils.isNullOrEmpty(taskTemp.getEndDate())).count();
        if (CommonUtils.isNotNullOrEmpty(pageNum+"",page+"")){
            query.limit(pageNum).skip((page - 1) * pageNum );
        }
        List<Task> tasks = mongoTemplate.find(query, Task.class);
        HashMap<String, Object> returnMap = new HashMap<>();
        returnMap.put(Dict.COMPLETE_TASK,count);
        returnMap.put(Dict.COUNT,taskList.size());
        returnMap.put(Dict.TASKLIST,tasks);
        return returnMap;
    }

    @Override
    public List<Task> querTaskListByImplUnitIds(List<String> implUnitIds, Integer delete) throws Exception {
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        if (!CommonUtils.isNullOrEmpty(delete)){
            criteria.and(Dict.DELETE).is(delete);
        }
        criteria1.and(Dict.IMPL_UNIT_ID).in(implUnitIds);
        return mongoTemplate.find(Query.query(criteria.andOperator(criteria1)),Task.class);
    }

    @Override
    public List<Task> queryTaskByVersionIds(List<String> versionIds,Integer mountStatus,Integer delete) throws Exception {
        Query query = new Query();
        if(!CommonUtils.isNullOrEmpty(mountStatus)){
            query.addCriteria(Criteria.where(Dict.MOUNT_STATUS).is(mountStatus));
        }
        if(!CommonUtils.isNullOrEmpty(delete)){
            query.addCriteria(Criteria.where(Dict.DELETE).is(delete));
        }
        query.addCriteria(Criteria.where(Dict.VERSION_ID).in(versionIds));
        return mongoTemplate.find(query,Task.class);
    }

    @Override
    public List<String> updateMountStatus(Integer mountStatus,String versionId,List<Map> sourceIds) throws Exception {
        List<Map> successCollect = sourceIds.stream().filter(sourceId -> sourceId.get(Dict.STATUS).equals("1")).collect(Collectors.toList());
        List<Map> failCollect = sourceIds.stream().filter(sourceId -> sourceId.get(Dict.STATUS).equals("0")).collect(Collectors.toList());
        ArrayList<String> successTaskIds = new ArrayList<>();
        successCollect.stream().forEach( task ->successTaskIds.add((String)task.get(Dict.TASKID)));
        Query query = Query.query(Criteria.where(Dict.ID).in(successTaskIds));
        Update update = Update.update(Dict.MOUNT_STATUS,mountStatus);
        update.set(Dict.VERSION_ID,versionId);
        update.set(Dict.CONFIRM_RELEASE,mountStatus+"");
        update.set(Dict.ENDDATE,"");
        mongoTemplate.updateMulti(query, update, Task.class);
        ArrayList<String> taskIds = new ArrayList<>();
        failCollect.stream().forEach( task ->taskIds.add((String)task.get(Dict.TASKID)));
        return taskIds;
    }

    public String escapeExprSpeciaWord(String keyWord) {
        String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "{", "}", "|"};
        for (String key : fbsArr) {
            if (keyWord.contains(key)) {
                keyWord = keyWord.replace(key, "\\" + key);
            }
        }
        return keyWord;
    }

    @Override
    public Integer queryNotEndTask(String processId, String groupId) {
        Query query = Query.query(Criteria.where(Dict.PROCESS_ID).is(processId)
                .and(Dict.ENDDATE).exists(false)
                .and(Dict.DELETE).is(0));
        if(!CommonUtils.isNullOrEmpty(groupId)){
            query.addCriteria(Criteria.where(Dict.ASSIGNEE_GROUP_ID).is(groupId));
        }
        return (int)mongoTemplate.count(query,Task.class);
    }

    @Override
    public List<Task> queryByGroupId(List groupIds) {
        Criteria c = Criteria.where(Dict.ASSIGNEE_GROUP_ID).in(groupIds).and(Dict.DELETE).is(0);
        Query query = new Query(c);
        return mongoTemplate.find(query, Task.class);
    }

    @Override
    public long queryTaskNum() {
        Criteria c = Criteria.where(Dict.DELETE).is(0);
        Query query = new Query(c);
        return mongoTemplate.count(query,"process_task");
    }

    @Override
    public <T> List<T> queryByQuery(Query query, Class<T> clazz) {
        return mongoTemplate.find(query,clazz);
    }

    @Override
    public BulkWriteResult updateMountSameFBConfirms(List<Map> maps) {
        BulkOperations process_task = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "process_task");
        ArrayList<Pair<Query,Update>> updateList = new ArrayList<>();
        maps.forEach(map -> {
            Query query = new Query();
            Update update = new Update();
            query.addCriteria(Criteria.where(Dict.ID).is(map.get(Dict.ID)));
            update.set("mountSameFBConfirm",map.get("mountSameFBConfirm"));
            Pair<Query, Update> pair = Pair.of(query, update);
            updateList.add(pair);
        });
        process_task.updateMulti(updateList);
        return process_task.execute();
    }
}
