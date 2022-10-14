package com.spdb.fdev.codeReview.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.base.utils.CommonUtils;
import com.spdb.fdev.codeReview.base.utils.TimeUtil;
import com.spdb.fdev.codeReview.spdb.dao.IOrderDao;
import com.spdb.fdev.codeReview.spdb.entity.CodeMeeting;
import com.spdb.fdev.codeReview.spdb.entity.CodeOrder;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.swing.plaf.DesktopIconUI;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
@Repository
public class OrderDaoImpl implements IOrderDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public CodeOrder add(CodeOrder codeOrder) {
        return mongoTemplate.save(codeOrder);
    }

    @Override
    public CodeOrder queryOrderById(String orderId) {
        Query query = new Query(Criteria.where(Dict.ID).is(orderId));
        query.fields().exclude(Dict.OBJECT_ID);
        return mongoTemplate.findOne(query, CodeOrder.class);
    }

    @Override
    public void update(CodeOrder codeOrder) throws Exception {
        //更新时间
        codeOrder.setUpdateUser(CommonUtils.getSessionUser().getId());
        codeOrder.setUpdateTime(TimeUtil.formatTodayHs());
        Query query = Query.query(Criteria.where(Dict.ID).is(codeOrder.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(codeOrder);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = new Update();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if(!Dict.OBJECT_ID.equals(key) && !Dict.ID.equals(key) && !Dict.DEMAND_ID.equals(key)
                    && !Dict.CREATE_USER.equals(key) && !Dict.CREATE_TIME.equals(key) && !Dict.ORDER_NO.equals(key)){
                update.set(key,value);
            }
        }
        mongoTemplate.findAndModify(query,update, CodeOrder.class);
    }

    /**
     * 查询工单列表，可根据牵头人/创建人id、工单编号（模糊搜索）、需求id、牵头小组id、工单状态、申请日期分页查询
     * @param param
     * @return
     */
    @Override
    public Map queryOrders(Map param, boolean isAuditRole, String userId) {
        Map resultMap = new HashMap();
        Query query = new Query();
        String fields[] = {Dict.ID,Dict.ORDER_NO,Dict.ORDER_STATUS,Dict.DEMAND_ID,Dict.SYSTEMS,Dict.LEADER_GROUP,
                Dict.LEADER,Dict.APPLY_TIME, Dict.AUDIT_FINISH_Time,Dict.PLAN_PRODUCT_DATE,Dict.EXPECTAUDITDATE,Dict.REAL_PRODUCT_DATE};
        for(String field:fields){
            query.fields().include(field);
        }
        query.fields().exclude(Dict.OBJECT_ID);
        Criteria criteria = new Criteria();
        if(CommonUtils.isNullOrEmpty(param)){
            //默认与我相关,工单创建人、牵头人、审核人。若为代码审核角色，则查全部
            if(!isAuditRole){
                Criteria c = new Criteria();
                query.addCriteria(c.orOperator(Criteria.where(Dict.CREATE_USER).is(userId),
                        Criteria.where(Dict.LEADER).is(userId),
                        Criteria.where(Dict.AUDITE_USERS).in(userId)));
            }
            //查询全量
            resultMap.put(Dict.COUNT,mongoTemplate.count(query, CodeOrder.class));
            resultMap.put(Dict.ORDERLIST , mongoTemplate.find(query, CodeOrder.class));
            return resultMap;
        }

        if(!isAuditRole && (CommonUtils.isNullOrEmpty(param.get(Dict.ABOUTME)) || (boolean)param.get(Dict.ABOUTME)) ){
            criteria.orOperator(Criteria.where(Dict.CREATE_USER).is(userId),
                    Criteria.where(Dict.LEADER).is(userId),
                    Criteria.where(Dict.AUDITE_USERS).in(userId));
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.LEADER))){
            criteria.and(Dict.LEADER).is(param.get(Dict.LEADER));
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.CREATEUSER))){
            criteria.and(Dict.CREATE_USER).is(param.get(Dict.CREATEUSER));
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.ORDERNO))){
            Pattern pattern = Pattern.compile("^.*" + param.get(Dict.ORDERNO) + ".*$");
            criteria.and(Dict.ORDER_NO).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.DEMANDID))){
            criteria.and(Dict.DEMAND_ID).is(param.get(Dict.DEMANDID));
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.LEADERGROUP))){
            criteria.and(Dict.LEADER_GROUP).is(param.get(Dict.LEADERGROUP));
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.ORDERSTATUS))){
            criteria.and(Dict.ORDER_STATUS).in((List<Integer>)param.get(Dict.ORDERSTATUS));
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.APPLYTIME))){
            Pattern pattern = Pattern.compile(param.get(Dict.APPLYTIME) + ".*$");
            criteria.and(Dict.APPLY_TIME).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.DATEFIELD))){
            List<String> dateFields = (List<String>)param.get(Dict.DATEFIELD);
            for(String dateField : dateFields){
                if(Dict.MEETINGTIME.equals(dateField)){
                    //根据会议时间区间搜索。先查找出符合条件的工单id，
                    Criteria meetingCriteria = new Criteria();
                    queryByDateSection(meetingCriteria,dateField,param);
                    Query meetingQuery = new Query(meetingCriteria);
                    List<CodeMeeting> codeMeetings = mongoTemplate.find(meetingQuery, CodeMeeting.class);
                    Set orderIds = new HashSet();
                    for(CodeMeeting codeMeeting : codeMeetings){
                        orderIds.add(codeMeeting.getOrderId());
                    }
                    criteria.and(Dict.ID).in(orderIds);
                }else if("realProductDate".equals(dateField)){
                    //将日期参数转化为yyyy/MM/dd，再进行比较
                    String startDate = "";
                    String endDate = "";
                    if(!CommonUtils.isNullOrEmpty(param.get(Dict.STARTDATE))){
                        startDate = TimeUtil.timeConvert2((String) param.get(Dict.STARTDATE));
                    }
                    if(!CommonUtils.isNullOrEmpty(param.get(Dict.ENDDATE))){
                        endDate = TimeUtil.timeConvert2((String) param.get(Dict.ENDDATE));
                    }
                    Map param2 = new HashMap();
                    param2.put(Dict.STARTDATE, startDate);
                    param2.put(Dict.ENDDATE, endDate);
                    queryByDateSection(criteria,dateField,param2);
                }else {
                    queryByDateSection(criteria,dateField,param);
                }
            }
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.PAGESIZE)) && (int)param.get(Dict.PAGESIZE) > 0){
            if(!CommonUtils.isNullOrEmpty(param.get(Dict.INDEX)) && (int)param.get(Dict.INDEX) > 0){
                int index = (int)param.get(Dict.INDEX);
                int pageSize = (int)param.get(Dict.PAGESIZE);
                index = (index-1)*pageSize;
                query.limit(pageSize).skip(index);
            }
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.SORTBY))){
            if(!CommonUtils.isNullOrEmpty(param.get(Dict.DESCENDING)) && (boolean)param.get(Dict.DESCENDING)){
                query.with(new Sort(Sort.Direction.DESC, (String) param.get(Dict.SORTBY)));
            }else {
                query.with(new Sort(Sort.Direction.ASC, (String) param.get(Dict.SORTBY)));
            }
        }else {
            query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        }
        query.addCriteria(criteria);
        resultMap.put(Dict.COUNT,mongoTemplate.count(query, CodeOrder.class));
        resultMap.put(Dict.ORDERLIST,mongoTemplate.find(query, CodeOrder.class));
        return resultMap;
    }

    @Override
    public void updateByTask(CodeOrder codeOrder){
        Query query = Query.query(Criteria.where(Dict.ORDER_NO).is(codeOrder.getOrderNo()));
        Update update = new Update();
        if(!(!CommonUtils.isNullOrEmpty(codeOrder.getRealProductDate()) && CommonUtils.isNullOrEmpty(codeOrder.getTaskIds()))){
            update.set(Dict.REAL_PRODUCT_DATE, codeOrder.getRealProductDate());
            update.set(Dict.TASK_IDS, codeOrder.getTaskIds());
            mongoTemplate.findAndModify(query,update, CodeOrder.class);
        }
    }

    @Override
    public CodeOrder queryByOrderNo(String orderNo) {
        Query query = Query.query(Criteria.where(Dict.ORDER_NO).is(orderNo));
        query.fields().exclude(Dict.OBJECT_ID);
        return mongoTemplate.findOne(query, CodeOrder.class);
    }

    @Override
    public long count() {
        Calendar calendar = Calendar.getInstance();
        Pattern pattern = Pattern.compile("FDEV-CODE-" + calendar.get(Calendar.YEAR) + ".*$");
        Query query = Query.query(Criteria.where(Dict.ORDER_NO).regex(pattern));
        return mongoTemplate.count(query, CodeOrder.class);
    }

    @Override
    public void deleteOrderById(String orderId) {
        Query query = Query.query(Criteria.where(Dict.ID).is(orderId));
        mongoTemplate.findAndRemove(query, CodeOrder.class);
    }

    @Override
    public CodeOrder queryMaxNoThisYear(String year) {
        Pattern pattern = Pattern.compile("FDEV-CODE-" + year + ".*$");
        Query query = Query.query(Criteria.where(Dict.ORDER_NO).regex(pattern));
        query.with(new Sort(Sort.Direction.DESC, Dict.ORDER_NO));
        return mongoTemplate.findOne(query,CodeOrder.class);
    }

    @Override
    public List<CodeOrder> queryOrderByIds(Set<String> orderIds) {
        Query query = Query.query(Criteria.where(Dict.ID).in(orderIds));
        return mongoTemplate.find(query, CodeOrder.class);
    }

    @Override
    public List<CodeOrder> queryAll() {
        return mongoTemplate.findAll(CodeOrder.class);
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
