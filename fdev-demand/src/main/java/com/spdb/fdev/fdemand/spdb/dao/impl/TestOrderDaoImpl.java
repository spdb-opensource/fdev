package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.ITestOrderDao;
import com.spdb.fdev.fdemand.spdb.dto.testorder.TestOrderQueryDto;
import com.spdb.fdev.fdemand.spdb.entity.TestOrder;
import com.spdb.fdev.fdemand.spdb.vo.PageVo;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@RefreshScope
@Repository
public class TestOrderDaoImpl implements ITestOrderDao {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public TestOrder queryTestOrderById(String id) throws Exception {
        return mongoTemplate.findOne(new Query(Criteria.where(Dict.ID).is(id)), TestOrder.class);
    }

    @Override
    public List<TestOrder> queryTestOrderByFdevUnitNo(String fdev_implement_unit_no) throws Exception {
        Query query = new Query();
        Criteria criteria = new Criteria();
        if (!CommonUtils.isNullOrEmpty(fdev_implement_unit_no)) {
            Pattern pattern = Pattern.compile("^.*" + fdev_implement_unit_no + ".*$");
            criteria.and(Dict.FDEV_IMPLEMENT_UNIT_NO).regex(pattern);
        }
        criteria.and(Dict.STATUS).ne(Constants.DELETED);//排除已撤销
        query.addCriteria(criteria);
        return mongoTemplate.find(query, TestOrder.class);
    }

    @Override
    public List<TestOrder> queryTestOrderByImplUnitNo(String implUnitNum) throws Exception {
        Query query = new Query();
        Criteria criteria = new Criteria();
        if(!CommonUtils.isNullOrEmpty(implUnitNum)){
            Pattern pattern = Pattern.compile("^.*" + implUnitNum + ".*$");
            criteria.and(Dict.IMPL_UNIT_NUM).regex(pattern);
        }
        criteria.and(Dict.STATUS).is(Constants.FDEV_TEST);//提测状态的提测单
        query.addCriteria(criteria);
        return mongoTemplate.find(query, TestOrder.class);
    }

    @Override
    public void addTestOrder(TestOrder testOrder) throws Exception {
        mongoTemplate.save(testOrder);
    }

    @Override
    public TestOrder deleteTestOrder(String id,String userId,String date) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        Update update = new Update();
        update.set(Dict.DELETED_TIME, date);//撤销时间
        update.set(Dict.UPDATE_TIME, date);//修改时间
        update.set(Dict.STATUS, Constants.DELETED);//已撤销状态
        update.set(Dict.UPDATE_USER_ID, userId);//更新人id
        return mongoTemplate.findAndModify(query, update, TestOrder.class);
    }

    @Override
    public TestOrder fileTestOrder(String id,String date) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        Update update = new Update();
        update.set(Dict.FILE_TIME, date);//归档时间
        update.set(Dict.STATUS, Constants.FILE);//归档状态
        return mongoTemplate.findAndModify(query, update, TestOrder.class);
    }

    @Override
    public void updateTestOrder(TestOrder testOrder) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(testOrder.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(testOrder);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, testOrder.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)) {
                continue;
            }
            if (Dict.WORKNO.equals(key)) {
                continue;
            }
            if (Dict.OA_CONTACT_NO.equals(key)) {
                if (CommonUtils.isNullOrEmpty(value))
                    continue;
                else
                    update.set(key, value);
            }
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, TestOrder.class);
    }

    @Override
    public TestOrder queryByCreateTime(String todayDate) {
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*" + todayDate + ".*$");
        Criteria criteria = Criteria.where(Dict.CREATE_TIME).regex(pattern);
        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        return mongoTemplate.findOne(query, TestOrder.class);
    }

    @Override
    public PageVo<TestOrder> queryTestOrder(TestOrderQueryDto dto) throws Exception {
        if (null != dto.getImplNos() && dto.getImplNos().isEmpty())
            return new PageVo<>(new ArrayList<>(), 0L);//我TM懒得解释 为什么要用MongoDB？？？
        Query query = new Query() {{
            if (!CommonUtils.isNullOrEmpty(dto.getTestOrder()))
                addCriteria(Criteria.where(Dict.TEST_ORDER).regex(dto.getTestOrder()));
            if (!CommonUtils.isNullOrEmpty(dto.getGroup()))
                addCriteria(Criteria.where(Dict.GROUP).is(dto.getGroup()));
            if (null != dto.getDemandIds()) addCriteria(Criteria.where(Dict.DEMAND_ID).in(dto.getDemandIds()));
            if (null != dto.getImplNos()) {
                addCriteria(new Criteria().orOperator(new ArrayList<Criteria>() {{
                    for (String implNo : dto.getImplNos())
                        add(Criteria.where(Dict.FDEV_IMPLEMENT_UNIT_NO).regex(implNo));
                }}.toArray(new Criteria[]{})));
            }
            if (null != dto.getIpmpNos()) addCriteria(Criteria.where(Dict.IMPL_UNIT_NUM).in(dto.getIpmpNos()));
            if (!CommonUtils.isNullOrEmpty(dto.getStatus()))
                addCriteria(Criteria.where(Dict.STATUS).in(dto.getStatus()));
            if (!CommonUtils.isNullOrEmpty(dto.getCreatorId()))
                addCriteria(Criteria.where("create_user_id").is(dto.getCreatorId()));
        }};
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        Pageable pageable = dto.getPageable();
        if (pageable != null) {//不为null表示分页
            long count = mongoTemplate.count(query, TestOrder.class);
            query.with(pageable);
            return new PageVo<>(mongoTemplate.find(query, TestOrder.class), count);
        } else {//支持不分页查全量
            return new PageVo<>(mongoTemplate.find(query, TestOrder.class));
        }
    }

    @Override
    public List<TestOrder> queryTechTestOrder() throws Exception {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(Dict.STATUS).is(Constants.FDEV_TEST);//提测状态
        criteria.and(Dict.DEMAND_TYPE).is(Constants.TECH);//科技需求
        query.addCriteria(criteria);
        return mongoTemplate.find(query, TestOrder.class);
    }

    @Override
    public List<TestOrder> queryTestOrderByDemandId(String demandId) throws Exception {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(Dict.DEMAND_ID).is(demandId);//需求id
        criteria.and(Dict.STATUS).is(Constants.FDEV_TEST);//提测状态
        query.addCriteria(criteria);
        return mongoTemplate.find(query, TestOrder.class);
    }
}
