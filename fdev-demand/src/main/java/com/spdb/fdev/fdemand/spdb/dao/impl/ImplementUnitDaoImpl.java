package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.dict.ImplementUnitEnum;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.IImplementUnitDao;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import net.sf.json.JSONObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Pattern;

@Repository
@RefreshScope
public class ImplementUnitDaoImpl implements IImplementUnitDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${fdev.unit.stock.date}")
    private String stockDate;

    @Override
    public List queryPaginationByDemandId(Integer start, Integer pageSize, String demand_id) {
        Query query = new Query(Criteria.where(Dict.DEMAND_ID).is(demand_id)
                .and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0));

        if (!pageSize.toString().equals("0")) {
            query.limit(pageSize).skip(start);  //分页
        }

        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public Long queryCountByDemandId(String demand_id) {
        Query query = new Query(Criteria.where(Dict.DEMAND_ID).is(demand_id).
                and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0));
        return mongoTemplate.count(query, FdevImplementUnit.class);
    }

    @Override
    public List queryPaginationByIpmpUnitNo(String demandId, Integer start, Integer pageSize, String ipmp_unit_no , String group) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0);
        if(!CommonUtils.isNullOrEmpty(demandId)){
            criteria.and(Dict.DEMAND_ID).is(demandId);
        }
        if(!CommonUtils.isNullOrEmpty(group)){
            criteria.and(Dict.GROUP).is(group);
        }
        if(!CommonUtils.isNullOrEmpty(ipmp_unit_no)){
            criteria.and(Dict.IPMP_IMPLEMENT_UNIT_NO).is(ipmp_unit_no);
        }
        Query query = new Query(criteria);

        if (!pageSize.toString().equals("0")) {
            query.limit(pageSize).skip(start);  //分页
        }
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public Long queryPaginationByIpmpUnitNo(String demandId, String ipmp_unit_no, String group) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0);
        if(!CommonUtils.isNullOrEmpty(demandId)){
            criteria.and(Dict.DEMAND_ID).is(demandId);
        }
        if(!CommonUtils.isNullOrEmpty(group)){
            criteria.and(Dict.GROUP).is(group);
        }
        if(!CommonUtils.isNullOrEmpty(ipmp_unit_no)){
            criteria.and(Dict.IPMP_IMPLEMENT_UNIT_NO).is(ipmp_unit_no);
        }
        Query query = new Query(criteria);
        return mongoTemplate.count(query, FdevImplementUnit.class);
    }

    @Override
    public FdevImplementUnit save(FdevImplementUnit fdevImplementUnit) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        fdevImplementUnit.set_id(objectId);
        fdevImplementUnit.setId(id);
        return mongoTemplate.save(fdevImplementUnit);
    }

    @Override
    public long queryCountAll() {
        Query query = new Query(Criteria.where(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0));
        return mongoTemplate.count(query, FdevImplementUnit.class);
    }

    @Override
    public FdevImplementUnit queryByCreatetime(String todayDate) {
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*" + todayDate + ".*$");
        Criteria criteria = Criteria.where(Dict.CREATE_TIME).regex(pattern);
        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.FDEV_IMPLEMENT_UNIT_NO));
        return mongoTemplate.findOne(query, FdevImplementUnit.class);
    }

    @Override
    public void update(FdevImplementUnit fdevImplementUnit) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(fdevImplementUnit.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(fdevImplementUnit);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, fdevImplementUnit.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if(!"is_new".equals(key) && !"is_fourStatus".equals(key) && !"mount_flag".equals(key)){
                update.set(key, value);
            }
        }
        this.mongoTemplate.findAndModify(query, update, FdevImplementUnit.class);
    }

    @Override
    public FdevImplementUnit queryById(String id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id).and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, FdevImplementUnit.class);
    }

    @Override
    public void deleteById(String id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        Update update = new Update();
        update.set(Dict.IMPLEMENT_UNIT_STATUS_NORMAL, 0);//删除
        mongoTemplate.findAndModify(query,update, FdevImplementUnit.class);
    }

    @Override
    public FdevImplementUnit queryByDemandIdAndGroupOne(String demandId, String group) {
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.DEMAND_ID).is(demandId).and(Dict.GROUP).is(group)
                .and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0);
        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, FdevImplementUnit.class);
    }

    @Override
    public List<FdevImplementUnit> queryByDemandId(String demandId) {
        Query query = new Query(Criteria.where(Dict.DEMAND_ID).is(demandId)
                .and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public FdevImplementUnit queryByFdevNo(String fdevNo){
        Query query = Query.query(Criteria.where(Dict.FDEV_IMPLEMENT_UNIT_NO).is(fdevNo));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, FdevImplementUnit.class);
    }

    public FdevImplementUnit queryByUnitNo(String unitNo) {
        Query query = new Query(Criteria.where(Dict.FDEV_IMPLEMENT_UNIT_NO).is(unitNo));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, FdevImplementUnit.class);
    }

    @Override
    public List<FdevImplementUnit> queryByDemandIdAndGroup(String demandId, String group) {
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.DEMAND_ID).is(demandId).and(Dict.GROUP).is(group)
                .and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0);
        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }


    public List<FdevImplementUnit> queryImplUnitByGroupId(Set<String> groupIds) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.GROUP).in(groupIds));
        query.addCriteria(Criteria.where(Dict.IS_CANCELED).ne(true));
        query.addCriteria(Criteria.where(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0));
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        List<FdevImplementUnit> resList;
        try {
            resList = mongoTemplate.find(query, FdevImplementUnit.class);
        }catch (Exception e){
            throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return resList;
    }

    @Override
    public List<FdevImplementUnit> queryImplUnitByipmpImplementUnitNo(String ipmpImplementUnitNo) {
        Query query = new Query();
        Criteria criteria = Criteria.where("ipmp_implement_unit_no").is(ipmpImplementUnitNo);
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public String queryRealStart(String demandId, Integer status) {
        Query query = new Query(Criteria.where(Dict.DEMAND_ID).is(demandId)
                .and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).gt(status));
        query.with(new Sort(Sort.Direction.ASC, Dict.REAL_START_DATE));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, FdevImplementUnit.class).getReal_start_date();
    }

    @Override
    public List<FdevImplementUnit> queryImplPreImplmentByDemandId(String demandId) {
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).gte(ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue());
        criteria.andOperator(Criteria.where(Dict.DEMAND_ID).is(demandId));
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public FdevImplementUnit queryByDemandIdAndGroupMax(String demandId, String partId) {
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.DEMAND_ID).is(demandId).and(Dict.GROUP).is(partId).
                and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0);
        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.ADD_FLAG));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, FdevImplementUnit.class);
    }

    @Override
    public List<FdevImplementUnit> queryFdevImplementUnitByDemandId(String demandId) {
        Query query = new Query(Criteria.where(Dict.DEMAND_ID).is(demandId));
        Criteria criteria = new Criteria();
        //除去暂缓和撤销 删除
        criteria.and(Dict.IMPLEMENT_UNIT_STATUS_SPECIAL).nin(ImplementUnitEnum.ImplementUnitDeferStatus.DEFER.getValue(),
                ImplementUnitEnum.ImplementUnitDeferStatus.RECOVER.getValue());
        criteria.and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).nin(ImplementUnitEnum.ImplementUnitStatusEnum.IS_CANCELED.getValue()
        ,0);
        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.ASC, Dict.IMPLEMENT_UNIT_STATUS_NORMAL));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public List<FdevImplementUnit> queryRecoverFdevUnitByDemandId(String demandId) {
        Query query = new Query(Criteria.where(Dict.DEMAND_ID).is(demandId));
        Criteria criteria = new Criteria();
        //恢复中
        criteria.and(Dict.IMPLEMENT_UNIT_STATUS_SPECIAL).in(ImplementUnitEnum.ImplementUnitDeferStatus.RECOVER.getValue());
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public List<FdevImplementUnit> queryDeferFdevUnitByDemandId(String demandId) {
        Query query = new Query(Criteria.where(Dict.DEMAND_ID).is(demandId));
        Criteria criteria = new Criteria();
        //暂缓和暂存
        criteria.and(Dict.IMPLEMENT_UNIT_STATUS_SPECIAL).in(ImplementUnitEnum.ImplementUnitDeferStatus.DEFER.getValue());
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public List<FdevImplementUnit> queryCanceledFdevUnitByDemandId(String demandId) {
        Query query = new Query(Criteria.where(Dict.DEMAND_ID).is(demandId));
        Criteria criteria = new Criteria();
        //撤销
        criteria.and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).is(ImplementUnitEnum.ImplementUnitStatusEnum.IS_CANCELED.getValue());

        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public List<FdevImplementUnit> queryByImplUnitNum(String implUnitNum,String taskNum) {
        Query query = new Query();
        if(!CommonUtils.isNullOrEmpty(implUnitNum)){
            query = new Query(Criteria.where(Dict.IPMP_IMPLEMENT_UNIT_NO).is(implUnitNum).
                    and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0));
        }else {
            query = new Query(Criteria.where(Dict.OTHER_DEMAND_TASK_NUM).is(taskNum).
                    and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0));
        }

        query.with(new Sort(Sort.Direction.ASC, Dict.IMPLEMENT_UNIT_STATUS_NORMAL));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public List<FdevImplementUnit> queryByOtherDemandTaskNum(String taskNum) {
        Query query = new Query(Criteria.where(Dict.OTHER_DEMAND_TASK_NUM).is(taskNum)
                .and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0));
        query.with(new Sort(Sort.Direction.ASC, Dict.IMPLEMENT_UNIT_STATUS_NORMAL));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }


    @Override
    public List<FdevImplementUnit> queryDailyFdevUnitList(String demandId, String group,String other_demand_task_num) {
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.DEMAND_ID).is(demandId).and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0);
        if(!CommonUtils.isNullOrEmpty(group)){
            criteria.and(Dict.GROUP).is(group);
        }
        if(!CommonUtils.isNullOrEmpty(other_demand_task_num)){
            criteria.and(Dict.OTHER_DEMAND_TASK_NUM).is(other_demand_task_num);
        }

        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public List<FdevImplementUnit> queryFdevImplementUnit() {
        Query query = new Query();
        Criteria criteria = new Criteria();
        //未挂载实施单元的研发单元
        criteria.and(Dict.IPMP_IMPLEMENT_UNIT_NO).in("",null);
        //除去暂缓和撤销 删除
        criteria.and(Dict.IMPLEMENT_UNIT_STATUS_SPECIAL).nin(ImplementUnitEnum.ImplementUnitDeferStatus.DEFER.getValue(),
                ImplementUnitEnum.ImplementUnitDeferStatus.RECOVER.getValue());
        criteria.and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).nin(ImplementUnitEnum.ImplementUnitStatusEnum.IS_CANCELED.getValue(),0);
        query.addCriteria(criteria);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public List<Map> queryFdevUnitByUnitNos(List<String> unitNos) {
        Criteria criteria = Criteria.where(Dict.FDEV_IMPLEMENT_UNIT_NO).in(unitNos);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.lookup(Dict.DEMAND_BASEINFO, Dict.DEMAND_ID, Dict.ID, Dict.DEMAND_BASEINFO),
                Aggregation.match(criteria),
                Aggregation.unwind(Dict.DEMAND_BASEINFO),
                Aggregation.project().andExclude(Dict.OBJECTID)
        );
        return mongoTemplate.aggregate(aggregation, Dict.FDEV_IMPLEMENT_UNIT, Map.class).getMappedResults();
    }

    @Override
    public List<FdevImplementUnit> queryAllFdevUnitByDemandId(String demandId) {
        Query query = new Query(Criteria.where(Dict.DEMAND_ID).is(demandId));
        Criteria criteria = new Criteria();
        //撤销
        criteria.and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).nin(ImplementUnitEnum.ImplementUnitStatusEnum.IS_CANCELED.getValue(),0);
        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public Map<String, Object> queryFdevUnitList(String dateTypePlan, String dateTypeReal, List<String> groupIds, String keyword,
                                                 String demandType, String demandKey, List<String> demandIds, String ipmpUnitNo,
                                                 String otherDemandTaskNum, List<String> userIds, List<String> states, Integer size, Integer index) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        if( !CommonUtils.isNullOrEmpty(dateTypePlan) && !CommonUtils.isNullOrEmpty(dateTypeReal) ){
            criteria1.orOperator(new Criteria(){
                @Override
                public Document getCriteriaObject(){
                    Document obj = new Document();
                    obj.put("$where","this." + dateTypePlan + " < " + "this." + dateTypeReal);
                    return obj;
                }

            },Criteria.where(dateTypeReal).in("",null).and(dateTypePlan).nin("",null).lt(TimeUtil.formatToday()));
        }
        if(!CommonUtils.isNullOrEmpty(groupIds)){
            query.addCriteria(Criteria.where(Dict.GROUP).in(groupIds));
        }
        Criteria criteria3 = Criteria.where("_id").ne(null);
        if(!CommonUtils.isNullOrEmpty(keyword)){
            Pattern pattern = Pattern.compile("^.*" + keyword + ".*$");
            criteria3.orOperator(Criteria.where(Dict.FDEV_IMPLEMENT_UNIT_NO).regex(pattern),
                    Criteria.where(Dict.IMPLEMENT_UNIT_CONTENT).regex(pattern));
        }
        if(!CommonUtils.isNullOrEmpty(demandType)){
            query.addCriteria(Criteria.where(Dict.DEMAND_TYPE).is(demandType));
        }
        if(!CommonUtils.isNullOrEmpty(demandKey)){
            query.addCriteria(Criteria.where(Dict.DEMAND_ID).in(demandIds));
        }
        if(!CommonUtils.isNullOrEmpty(ipmpUnitNo)){
            Pattern pattern = Pattern.compile("^.*" + ipmpUnitNo + ".*$");
            query.addCriteria(Criteria.where(Dict.IPMP_IMPLEMENT_UNIT_NO).regex(pattern));
        }
        if(!CommonUtils.isNullOrEmpty(otherDemandTaskNum)){
            Pattern pattern = Pattern.compile("^.*" + otherDemandTaskNum + ".*$");
            query.addCriteria(Criteria.where(Dict.OTHER_DEMAND_TASK_NUM).regex(pattern));
        }
        if(!CommonUtils.isNullOrEmpty(userIds)){
            query.addCriteria(Criteria.where(Dict.IMPLEMENT_LEADER).in(userIds));
        }
        Criteria criteria2 = Criteria.where("_id").ne(null);
        if(!CommonUtils.isNullOrEmpty(states)){
            if( states.contains("defer") ){
                List<Integer> statesInt = new ArrayList<>();
                for (String state : states) {
                    if(!"defer".equals(state)){
                        statesInt.add(Integer.valueOf(state));
                    }
                }
                //状态为空查询 未删除 并且暂缓
                if(CommonUtils.isNullOrEmpty(statesInt)){
                    query.addCriteria(Criteria.where(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0).and
                            (Dict.IMPLEMENT_UNIT_STATUS_SPECIAL).in(1,2));
                }else{
                    criteria2.orOperator(Criteria.where(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).in(statesInt),
                            Criteria.where(Dict.IMPLEMENT_UNIT_STATUS_SPECIAL).in(1,2));
                }

            }else{
                List<Integer> statesInt = new ArrayList<>();
                for (String state : states) {
                    statesInt.add(Integer.valueOf(state));
                }
                query.addCriteria(Criteria.where(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).in(statesInt).
                        and(Dict.IMPLEMENT_UNIT_STATUS_SPECIAL).nin(1,2));

            }

        }else{
            query.addCriteria(Criteria.where(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0)) ;
        }
        criteria.andOperator(criteria1,criteria2,criteria3);
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        Long count = mongoTemplate.count(query, FdevImplementUnit.class);

        if ( !CommonUtils.isNullOrEmpty(size) && 0 != size ) {
            Integer start = size * (index - 1);   //起始
            query.limit(size).skip(start);  //分页
        }
        List<FdevImplementUnit> data = mongoTemplate.find(query, FdevImplementUnit.class);
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.COUNT,count);
        map.put(Dict.DATA,data);
        return map;
    }

    @Override
    public List<FdevImplementUnit> queryFdevUnitList(String fdevUnitKey,List<String> fdevUnitLeaderIds,
                                                     List<String> fdevUnitNos,List<String> groupIds){
        Query query = new Query();
        Criteria criteria = new Criteria();
        if(!CommonUtils.isNullOrEmpty(fdevUnitKey)){
            Pattern pattern = Pattern.compile("^.*" + fdevUnitKey + ".*$");
            criteria.orOperator(Criteria.where(Dict.FDEV_IMPLEMENT_UNIT_NO).regex(pattern),
                    Criteria.where(Dict.IMPLEMENT_UNIT_CONTENT).regex(pattern));
        }
        if(!CommonUtils.isNullOrEmpty(fdevUnitLeaderIds)){
            query.addCriteria(Criteria.where(Dict.IMPLEMENT_LEADER).in(fdevUnitLeaderIds));
        }
        if(!CommonUtils.isNullOrEmpty(fdevUnitNos)){
            query.addCriteria(Criteria.where(Dict.FDEV_IMPLEMENT_UNIT_NO).in(fdevUnitNos));
        }
        if(!CommonUtils.isNullOrEmpty(groupIds)){
            query.addCriteria(Criteria.where(Dict.GROUP).in(groupIds));
        }
        criteria.and(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).ne(0);
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public List<FdevImplementUnit> queryFdevUnitOverdueList(String date,String type,String gt) {
        Query query = new Query();
        //去除特殊状态 只要业务需求
        Criteria criteria = Criteria.where(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).nin(9,0).
                and(Dict.IMPLEMENT_UNIT_STATUS_SPECIAL).nin(1,2).and(Dict.DEMAND_TYPE).is(Constants.BUSINESS)
                .and(Dict.CREATE_TIME).gt(stockDate);
        if(Constants.FDEV_TEST.equals(type)){
            if(Constants.GT.equals(gt)){
                criteria.and(Dict.PLAN_TEST_DATE).lt(date)
                        .and(Dict.REAL_TEST_DATE).in("",null);
            }else{
                criteria.and(Dict.PLAN_TEST_DATE).is(date)
                        .and(Dict.REAL_TEST_DATE).in("",null);
            }
        } else if (Constants.START.equals(type)){
            if(Constants.GT.equals(gt)){
                criteria.and(Dict.PLAN_START_DATE).lt(date)
                        .and(Dict.REAL_START_DATE).in("",null);
            }else{
                criteria.and(Dict.PLAN_START_DATE).is(date)
                        .and(Dict.REAL_START_DATE).in("",null);
            }
        }
        query.fields().exclude(Dict.OBJECTID);
        query.addCriteria(criteria);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    @Override
    public List<FdevImplementUnit> queryFdevUnitWarnOverdueList() {
        Query query = new Query();
        //去除特殊状态 只要业务需求
        Criteria criteria = Criteria.where(Dict.IMPLEMENT_UNIT_STATUS_NORMAL).nin(9,0).
                and(Dict.IMPLEMENT_UNIT_STATUS_SPECIAL).nin(1,2);
                criteria.and(Dict.PLAN_TEST_DATE).is(TimeUtil.formatToday())
                        .and(Dict.REAL_TEST_DATE).in("",null);
        query.fields().exclude(Dict.OBJECTID);
        query.addCriteria(criteria);
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

}
