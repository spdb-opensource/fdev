package com.spdb.fdev.fdevtask.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.dao.ReviewRecordDao;
import com.spdb.fdev.fdevtask.spdb.entity.ReviewRecord;
import com.spdb.fdev.fdevtask.spdb.entity.ReviewRecordHistory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReviewRecordDaoImpl implements ReviewRecordDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 新建审核项 补充审核字段
     *
     * @param _id
     * @param id
     * @param taskName
     * @param taskId
     * @param appName
     * @param appId
     * @param master
     * @param group
     * @param groupName
     * @param status
     * @return
     */
    @Override
    public ReviewRecord save(String _id, String id, String taskName, String taskId, String appName, String appId, List master, String group, String groupName, String status, String reviewType) {
        ObjectId oid = new ObjectId(id);
        ReviewRecord rr = new ReviewRecord(oid, id, taskName, taskId, appName, appId, master, "", "", new ArrayList(),
                group, "", groupName, reviewType, "", Dict.INIT, CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN), status);
        ReviewRecord reviewRecord = queryById(id);
        if (!CommonUtils.isNullOrEmpty(reviewRecord)) {
            rr.setCreateDate(reviewRecord.getCreateDate());
            rr.setReviewStatus(reviewRecord.getReviewStatus());
        }
        return mongoTemplate.save(rr, "review_record");
    }

    /**
     * 审核状态变更  审核归档 必须有id
     *
     * @param reviewRecord
     * @return
     */
    @Override
    public ReviewRecord update(ReviewRecord reviewRecord) {
        String id = reviewRecord.getId();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = null;
        try {
            json = objectMapper.writeValueAsString(reviewRecord);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        BasicDBObject taskJson = BasicDBObject.parse(json);
        Iterator<String> it = taskJson.keySet().iterator();
        Query query = Query.query(Criteria.where("id").is(id));
        Update update = Update.update("id", id);
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = taskJson.get(key);
            if ("_id".equals(key) || "serialVersionUID".equals(key)) {
                continue;
            }
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, ReviewRecord.class);
        return mongoTemplate.findOne(query, ReviewRecord.class);
    }

    /**
     * 任务名 应用名 任务负责人
     *
     * @param regex
     * @return
     */
    @Override
    public Map fuzzySearch(String regex, int page, int pageSize, Map param) {
        Query query = new Query();
        //查询数据库审核列表，全部查询时，已归档信息不展示。
        //全部查询：reviewStatus 不上送
        if(CommonUtils.isNullOrEmpty(param.get("reviewStatus"))){
            query.addCriteria(Criteria.where("reviewStatus").nin("已归档"));
        }
        if (!CommonUtils.isNullOrEmpty(param)) {
            Iterator<String> it = param.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                Object value = param.get(key);
                if ("_id".equals(key) || "serialVersionUID".equals(key)) {
                    continue;
                }
                query.addCriteria(Criteria.where(key).is(value));
            }
        }
        if (!CommonUtils.isNullOrEmpty(regex)) {
            query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.TASKNAME).regex(regex, "i"),
                    Criteria.where("appName").regex(regex, "i"),
                    Criteria.where("master.name").regex(regex, "i"),
                    Criteria.where("reviewers.name").regex(regex, "i")
            ));
        }
        Map result = new HashMap();
        int count = (int) mongoTemplate.count(query, ReviewRecord.class);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("sum", count);
        if (pageSize != 0) {
            query.with(new Sort(Sort.Direction.DESC, "createDate")).with(PageRequest.of(page - 1, pageSize));
        }
        query.with(new Sort(Sort.Direction.DESC, "createDate"));
        result.put("record", mongoTemplate.find(query, ReviewRecord.class));
        return result;
    }

    @Override
    public Map queryPageable(Map param, int page, int pageSize) {
//        Iterator<String> it = param.keySet().iterator();
//        while (it.hasNext()) {
//            String key = (String) it.next();
//            Object value = param.get(key);
//            if ("_id".equals(key) || "serialVersionUID".equals(key)) {
//                continue;
//            }
//            query.addCriteria(Criteria.where(key).is(value));
//        }
//        Map result = new HashMap();
//        int count = (int) mongoTemplate.count(Query.query(Criteria.where("")),ReviewRecord.class);
//        result.put("page",page);
//        result.put("pageSize",pageSize);
//        result.put("sum",count);
//        result.put("record", mongoTemplate.find(query, ReviewRecord.class));
        return null;
    }

    @Override
    public List<ReviewRecord> query(ReviewRecord reviewRecord) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = null;
        try {
            json = objectMapper.writeValueAsString(reviewRecord);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        BasicDBObject taskJson = BasicDBObject.parse(json);
        Iterator<String> it = taskJson.keySet().iterator();
        Query query = new Query();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = taskJson.get(key);
            if ("_id".equals(key) || "serialVersionUID".equals(key)) {
                continue;
            }
            query.addCriteria(Criteria.where(key).is(value));
        }
        return mongoTemplate.find(query, ReviewRecord.class);
    }

    /**
     * 关联项id
     *
     * @param id
     * @return
     */
    @Override
    //查询出来的 reviewuser  对象不完整 id 为null
    public ReviewRecord queryById(String id) {
        Query q = Query.query(Criteria.where("_id").is(id));
        return mongoTemplate.findOne(q, ReviewRecord.class);
    }

    @Override
    public void deleteReviewRecord(String id) {
        mongoTemplate.findAndRemove(Query.query(Criteria.where("_id").is(id)), ReviewRecord.class);
    }

    @Override
    public void deleteByTaskId(String task_id) {
        mongoTemplate.remove(Query.query(Criteria.where("taskId").is(task_id)), ReviewRecord.class);
    }

    @Override
    public List<ReviewRecordHistory> queryReviewRecordHistory(ReviewRecordHistory reviewRecordHistory) {
        Query q = Query.query(Criteria.where(Dict.TASK_ID).is(reviewRecordHistory.getTask_id())
                .and(Dict.STATUS).is("1"));
        q.with(new Sort(Sort.Direction.DESC, Dict.REVIEW_TIME));
        return mongoTemplate.find(q, ReviewRecordHistory.class);
    }

    @Override
    public ReviewRecordHistory saveReviewRecord(ReviewRecordHistory reviewRecordHistory) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        reviewRecordHistory.setStatus("1");
        reviewRecordHistory.set_id(objectId);
        reviewRecordHistory.setId(id);
        reviewRecordHistory.setReview_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));
        return mongoTemplate.save(reviewRecordHistory);
    }

    @Override
    public ReviewRecord queryByTaskId(String taskId) {
        return mongoTemplate.findOne(Query.query(Criteria.where("taskId").is(taskId)), ReviewRecord.class);
    }

    @Override
    public void deleteByTask_Id(String task_id) {
        mongoTemplate.remove(Query.query(Criteria.where("task_id").is(task_id)), ReviewRecordHistory.class);
    }

    @Override
    public ReviewRecord queryTaskReviewByTaskId(String task_id) {
        Query q = Query.query(Criteria.where("taskId").is(task_id));
        return mongoTemplate.findOne(q, ReviewRecord.class);
    }

    @Override
    public ReviewRecord queryRecordByTaskId(String task_id) {
        Query q = Query.query(Criteria.where("taskId").is(task_id).and("reviewStatus").is("通过"));
        return mongoTemplate.findOne(q, ReviewRecord.class);
    }

    @Override
    public void updateById(ReviewRecord reviewRecord) {
        Query q = Query.query(Criteria.where("taskId").is(reviewRecord.getId()));
        Update update = Update.update("reviewStatus", reviewRecord.getReviewStatus());
        mongoTemplate.findAndModify(q, update,ReviewRecord.class);
    }

    @Override
    public ReviewRecord saveNoCodeReview(ReviewRecord reviewRecord){
        return mongoTemplate.save(reviewRecord, "review_record");
    }
}
