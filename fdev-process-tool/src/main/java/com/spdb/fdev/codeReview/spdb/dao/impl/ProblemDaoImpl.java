package com.spdb.fdev.codeReview.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.base.utils.CommonUtils;
import com.spdb.fdev.codeReview.base.utils.TimeUtil;
import com.spdb.fdev.codeReview.spdb.dao.IProblemDao;
import com.spdb.fdev.codeReview.spdb.dto.ProblemDto;
import com.spdb.fdev.codeReview.spdb.entity.CodeMeeting;
import com.spdb.fdev.codeReview.spdb.entity.CodeOrder;
import com.spdb.fdev.codeReview.spdb.entity.CodeProblem;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
@Repository
public class ProblemDaoImpl implements IProblemDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<CodeProblem> queryProblemsByMeetingId(String meetingId) {
        Query query = Query.query(Criteria.where(Dict.MEETING_ID).is(meetingId));
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        query.fields().exclude(Dict.OBJECT_ID);
        return mongoTemplate.find(query,CodeProblem.class);
    }

    @Override
    public CodeProblem queryProblemById(String id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        query.fields().exclude(Dict.OBJECT_ID);
        return mongoTemplate.findOne(query, CodeProblem.class);
    }

    @Override
    public void deleteProblemById(String id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        mongoTemplate.findAndRemove(query,CodeProblem.class);
    }

    @Override
    public CodeProblem add(CodeProblem problem) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        problem.setId(id);
        problem.setCreateTime(TimeUtil.formatTodayHs());
        problem.setCreateUser(CommonUtils.getSessionUser().getId());
        problem.setUpdateUser(CommonUtils.getSessionUser().getId());
        problem.setUpdateTime(TimeUtil.formatTodayHs());
        return mongoTemplate.save(problem);
    }

    @Override
    public void update(CodeProblem problem) throws Exception {
        //更新时间
        problem.setUpdateUser(CommonUtils.getSessionUser().getId());
        problem.setUpdateTime(TimeUtil.formatTodayHs());
        Query query = Query.query(Criteria.where(Dict.ID).is(problem.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(problem);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = new Update();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if(!Dict.OBJECT_ID.equals(key) && !Dict.ID.equals(key) && !Dict.MEETING_ID.equals(key) && !Dict.CREATE_USER.equals(key) && !Dict.CREATE_TIME.equals(key)){
                update.set(key,value);
            }
        }
        mongoTemplate.findAndModify(query,update, CodeProblem.class);
    }

    @Override
    public List<CodeProblem> queryProblemsByMeetingIds(Set<String> meetingIds, String startDate, String endDate) {
        Criteria criteria = Criteria.where(Dict.MEETING_ID).in(meetingIds);
        if(!CommonUtils.isNullOrEmpty(startDate) && !CommonUtils.isNullOrEmpty(endDate)){
            criteria.and(Dict.CREATE_TIME).gte(startDate).lte(endDate + "999");
        }
        if(!CommonUtils.isNullOrEmpty(startDate) && CommonUtils.isNullOrEmpty(endDate)){
            criteria.and(Dict.CREATE_TIME).gte(startDate);
        }
        if(CommonUtils.isNullOrEmpty(startDate) && !CommonUtils.isNullOrEmpty(endDate)){
            criteria.and(Dict.CREATE_TIME).lte(endDate + "999");
        }
        Query query = Query.query(criteria);
        return mongoTemplate.find(query,CodeProblem.class);
    }

    /**
     * 问题描述模糊搜索、按工单搜索、按照问题项搜索、
     * 按照工单申请时间applyTime、会议时间meetingTime、问题创建时间createTime的范围搜索
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> queryAll(Map param) {
        //1、按照工单的搜索条件筛选符合的工单
        Set<String> orderIds = new HashSet<>();
        Criteria orderCriteria = new Criteria();
        boolean orderCriteriaFlag = false;//记录是否有工单类的查询条件，默认无
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.ORDERID))){
            //按照工单搜索
            orderCriteria.and(Dict.ID).is(param.get(Dict.ORDERID));
            orderCriteriaFlag = true;
        }
        if(!CommonUtils.isNullOrEmpty(param.get("dateField")) && ((List<String>)param.get("dateField")).contains(Dict.APPLYTIME)){
            //按照工单申请时间搜索
            orderCriteria = queryByDateSection(orderCriteria,Dict.APPLY_TIME, param);
            orderCriteriaFlag = true;
        }
        if(orderCriteriaFlag){
            Query orderQuery = Query.query(orderCriteria);
            List<CodeOrder> orders = mongoTemplate.find(orderQuery, CodeOrder.class);
            for(CodeOrder codeOrder : orders){
                orderIds.add(codeOrder.getId());
            }
        }
        //2、根据会议的搜索条件和1中的结果，筛选符合的会议
        Set<String> meetingIds = new HashSet<>();
        Criteria meetingCriteria = new Criteria();
        boolean meetingCriteriaFlag = false;//记录是否存在会议的查询条件，默认无
        if(!CommonUtils.isNullOrEmpty(param.get("dateField")) && ((List<String>)param.get("dateField")).contains(Dict.MEETINGTIME)){
            //按照会议时间搜索
            meetingCriteria = queryByDateSection(meetingCriteria, Dict.MEETING_TIME, param);
            meetingCriteriaFlag = true;
        }
        if(orderCriteriaFlag){
            meetingCriteria.and(Dict.ORDER_ID).in(orderIds);
        }
        if(orderCriteriaFlag || meetingCriteriaFlag){
            Query meetingQuery = Query.query(meetingCriteria);
            List<CodeMeeting> codeMeetings = mongoTemplate.find(meetingQuery, CodeMeeting.class);
            for(CodeMeeting codeMeeting : codeMeetings){
                meetingIds.add(codeMeeting.getId());
            }
        }
        //3、查询问题
        Criteria criteria = new Criteria();
        if(orderCriteriaFlag || meetingCriteriaFlag){
            criteria.and(Dict.MEETING_ID).in(meetingIds);
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.PROBLEM))){
            //根据问题描述模糊搜索
            Pattern pattern = Pattern.compile("^.*" + param.get(Dict.PROBLEM) + ".*$");
            criteria.and(Dict.PROBLEM).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.ITEMTYPE))){
            //根据问题项搜索
            criteria.and(Dict.ITEM_TYPE).is(param.get(Dict.ITEMTYPE));
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.PROBLEMTYPE))){
            //根据问题类型搜索
            criteria.and(Dict.PROBLEM_TYPE).is(param.get(Dict.PROBLEMTYPE));
        }
        if(!CommonUtils.isNullOrEmpty(param.get("dateField")) && ((List<String>)param.get("dateField")).contains(Dict.CREATETIME)){
            //根据问题创建时间区间搜索
            criteria = queryByDateSection(criteria,Dict.CREATE_TIME,param);
        }
        Query query = Query.query(criteria);
        long count = mongoTemplate.count(query, CodeProblem.class);
        //分页
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.PAGESIZE)) && (int)param.get(Dict.PAGESIZE) > 0){
            if(!CommonUtils.isNullOrEmpty(param.get(Dict.INDEX)) && (int)param.get(Dict.INDEX) > 0){
                int index = (int)param.get(Dict.INDEX);
                int pageSize = (int)param.get(Dict.PAGESIZE);
                index = (index-1)*pageSize;
                query.limit(pageSize).skip(index);
            }
        }
        List<CodeProblem> list = mongoTemplate.find(query,CodeProblem.class);
        List<ProblemDto> problemDtoList = new ArrayList<>();
        for (CodeProblem problem : list) {
            ProblemDto problemDto = new ProblemDto();
            BeanUtils.copyProperties(problem,problemDto);
            problemDtoList.add(problemDto);
        }
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.COUNT, count);
        result.put("list", problemDtoList);
        return result;
    }

    private Criteria queryByDateSection(Criteria criteria,String field, Map param){
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.STARTDATE)) && !CommonUtils.isNullOrEmpty(param.get(Dict.ENDDATE))){
            criteria.and(field).gte(param.get(Dict.STARTDATE)).lte(param.get(Dict.ENDDATE) + "999");
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.STARTDATE)) && CommonUtils.isNullOrEmpty(param.get(Dict.ENDDATE))){
            criteria.and(field).gte(param.get(Dict.STARTDATE));
        }
        if(CommonUtils.isNullOrEmpty(param.get(Dict.STARTDATE)) && !CommonUtils.isNullOrEmpty(param.get(Dict.ENDDATE))){
            criteria.and(field).lte(param.get(Dict.ENDDATE) + "999");
        }
        return criteria;
    }
}
