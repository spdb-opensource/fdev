package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.IFdevUnitApproveDao;
import com.spdb.fdev.fdemand.spdb.dao.IOtherDemandTaskDao;
import com.spdb.fdev.fdemand.spdb.entity.FdevUnitApprove;
import com.spdb.fdev.fdemand.spdb.entity.OtherDemandTask;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RefreshScope
@Repository
public class FdevUnitApproveDaoImpl implements IFdevUnitApproveDao {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public void saveFdevUnitApprove(FdevUnitApprove fdevUnitApprove) throws Exception {
        mongoTemplate.save(fdevUnitApprove);
    }

    @Override
    public Map<String, Object> queryMyApproveList(String type ,String code,String sectionId,List<String> fdevUnitNos,String keyword,Integer pageNum,Integer pageSize) throws Exception {
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.SECTIONID).is(sectionId);
        //待审批总数
        Long waitCount = mongoTemplate.count(new Query(Criteria.where(Dict.SECTIONID).
                is(sectionId).and(Dict.APPROVESTATE).is(Constants.WAIT)), FdevUnitApprove.class);
        //已完成总数
        Long doneCount = mongoTemplate.count(new Query(Criteria.where(Dict.SECTIONID).
                is(sectionId).and(Dict.APPROVESTATE).ne(Constants.WAIT)), FdevUnitApprove.class);

        // 0 = 开发审批 1 = 超期 2 = 提前开发&超期
        if( !CommonUtils.isNullOrEmpty(type) ){
            if( "0".equals(type) ){
                criteria.and(Dict.APPROVETYPE).in(Constants.DEVAPPROVE,Constants.DEVOVERDUE);
            } else if( "1".equals(type) ){
                criteria.and(Dict.APPROVETYPE).in(Constants.OVERDUEAPPROVE,Constants.DEVOVERDUE);
            } else if( "2".equals(type) ){
                criteria.and(Dict.APPROVETYPE).is(Constants.DEVOVERDUE);
            }
        }

        //按状态筛选
        if(Constants.WAIT.equals(code)){
            criteria.and(Dict.APPROVESTATE).is(Constants.WAIT);
        } else {
            criteria.and(Dict.APPROVESTATE).ne(Constants.WAIT);
        }
        //研发单元编号 搜索条件不为空
        if( !CommonUtils.isNullOrEmpty(keyword) ){
            criteria.and(Dict.FDEVUNITNO).in(fdevUnitNos);
        }

        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        //计数
        Long count = mongoTemplate.count(query, FdevUnitApprove.class);
        query.with(new Sort(Sort.Direction.DESC, Dict.APPLYTIME));
        //分页
        if( !CommonUtils.isNullOrEmpty(pageNum) && pageNum != 0 ) {
            query.skip((pageNum - 1L) * pageSize).limit(pageSize);
        }
        List<FdevUnitApprove> data = mongoTemplate.find(query, FdevUnitApprove.class);
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.COUNT,count);
        map.put(Dict.WAITCOUNT,waitCount);
        map.put(Dict.DONECOUNT,doneCount);
        map.put(Dict.APPROVELIST,data);
        return map;
    }

    @Override
    public List<FdevUnitApprove> passFdevUnitApproveByIds(List<String> ids,String approverId) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).in(ids));
        Update update = new Update();
        update.set(Dict.APPROVETIME, TimeUtil.formatTodayHs());//通过时间
        update.set(Dict.APPROVESTATE, Constants.PASS);//通过状态
        update.set(Dict.APPROVERID, approverId);//审批人id
        mongoTemplate.updateMulti(query, update, FdevUnitApprove.class);
        return mongoTemplate.find(query,FdevUnitApprove.class);

    }

    @Override
    public FdevUnitApprove rejectFdevUnitApproveById(String id,String approveRejectReason,String approverId) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        Update update = new Update();
        update.set(Dict.APPROVETIME, TimeUtil.formatTodayHs());//拒绝时间
        update.set(Dict.APPROVESTATE, Constants.REJECT);//拒绝状态
        update.set(Dict.APPROVEREASON, approveRejectReason);//拒绝原因
        update.set(Dict.APPROVERID, approverId);//审批人id
        return mongoTemplate.findAndModify(query,update,FdevUnitApprove.class);
    }

    @Override
    public Map<String, Object> queryApproveList(String fdevUnitKey,List<String> fdevUnitLeaderIds,List<String> groupIds
            , List<String> fdevUnitNos, List<String> demandIds, String demandKey, List<String> approveStates,
            List<String> approverIds, Integer pageNum, Integer pageSize,String type) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        // 0 = 开发审批 1 = 超期 2 = 提前开发&超期
        if( !CommonUtils.isNullOrEmpty(type) ){
            if( "0".equals(type) ){
                criteria.and(Dict.APPROVETYPE).in(Constants.DEVAPPROVE,Constants.DEVOVERDUE);
            } else if( "1".equals(type) ){
                criteria.and(Dict.APPROVETYPE).in(Constants.OVERDUEAPPROVE,Constants.DEVOVERDUE);
            } else if( "2".equals(type) ){
                criteria.and(Dict.APPROVETYPE).is(Constants.DEVOVERDUE);
            }
        }

        //研发单元编号
        if(!CommonUtils.isNullOrEmpty(fdevUnitKey) || !CommonUtils.isNullOrEmpty(fdevUnitLeaderIds)
                || !CommonUtils.isNullOrEmpty(groupIds)){
            criteria.and(Dict.FDEVUNITNO).in(fdevUnitNos);
        }
        //需求id
        if(!CommonUtils.isNullOrEmpty(demandKey)){
            criteria.and(Dict.DEMANDID).in(demandIds);
        }
        //审批状态
        if(!CommonUtils.isNullOrEmpty(approveStates)){
            criteria.and(Dict.APPROVESTATE).in(approveStates);
        }
        //审批人id
        if(!CommonUtils.isNullOrEmpty(approverIds)){
            criteria.and(Dict.APPROVERID).in(approverIds);
        }
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        //计数
        Long count = mongoTemplate.count(query, FdevUnitApprove.class);
        query.with(new Sort(Sort.Direction.DESC, Dict.APPLYTIME));
        //分页
        if( !CommonUtils.isNullOrEmpty(pageNum) && pageNum != 0 ) {
            query.skip((pageNum - 1L) * pageSize).limit(pageSize);
        }

        List<FdevUnitApprove> data = mongoTemplate.find(query, FdevUnitApprove.class);
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.COUNT,count);
        map.put(Dict.APPROVELIST,data);
        return map;
    }

}
