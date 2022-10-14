package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.IDemandAssessDao;
import com.spdb.fdev.fdemand.spdb.dao.IFdevFinalDateApproveDao;
import com.spdb.fdev.fdemand.spdb.dto.DemandAssessDto;
import com.spdb.fdev.fdemand.spdb.dto.UpdateFinalDateQueryDto;
import com.spdb.fdev.fdemand.spdb.entity.DemandAssess;
import com.spdb.fdev.fdemand.spdb.entity.FdevFinalDateApprove;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.spdb.fdev.fdemand.base.dict.Dict.*;

@Repository
public class FdevFinalDateApproveDaoImpl implements IFdevFinalDateApproveDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IDemandAssessDao demandAssessDao;

    @Override
    public FdevFinalDateApprove save(FdevFinalDateApprove fdevFinalDateApprove) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        fdevFinalDateApprove.setId(id);
        return mongoTemplate.save(fdevFinalDateApprove);
    }

    @Override
    public Map<String, Object> queryAll(UpdateFinalDateQueryDto queryDto, List<String> accessIds) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        // 获取操作人id
        List<String> operateUserId = queryDto.getOperateUserId();
        // 获取审批状态
        List<String> operateStatus = queryDto.getOperateStatus();
        // 获取申请人id
        List<String> applyUserId = queryDto.getApplyUserId();
        // 获取需求编号或者名称
        String oaContactNameNo = queryDto.getOaContactNameNo();
        Integer pageNum = queryDto.getPageNum();
        Integer pageSize = queryDto.getPageSize();
        // 获取牵头组
        List<String> demandLeaderGroups = queryDto.getDemandLeaderGroups();
        if (!CommonUtils.isNullOrEmpty(demandLeaderGroups)) {
            criteria.and(Dict.ACCESS_ID).in(accessIds);
        }
        if (!CommonUtils.isNullOrEmpty(oaContactNameNo)) {
            List<String> contactNos = new ArrayList<>();
            DemandAssessDto dto = new DemandAssessDto();
            dto.setOaContactNameNo(oaContactNameNo);
            Map<String, Object> queryDtoList = demandAssessDao.query(dto);
            List<DemandAssess> data = (List<DemandAssess>) queryDtoList.get(Dict.DATA);
            for (DemandAssess demandAssess : data) {
                contactNos.add(demandAssess.getOa_contact_no());
            }
            criteria.and(Dict.OA_CONTACT_NO).in(contactNos);
        }
        //审批状态
        if (!CommonUtils.isNullOrEmpty(operateStatus)) {
            criteria.and(Dict.OPERATE_STATUS).in(operateStatus);
        }
        // 申请人
        if (!CommonUtils.isNullOrEmpty(operateUserId)) {
            criteria.and(Dict.OPERATE_USER_ID).in(operateUserId);
        }
        // 审批人
        if (!CommonUtils.isNullOrEmpty(applyUserId)) {
            criteria.and(Dict.APPLY_USER_ID).in(applyUserId);
        }
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        //计数
        Long count = mongoTemplate.count(query, FdevFinalDateApprove.class);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        //分页
        if (!CommonUtils.isNullOrEmpty(pageNum) && pageNum != 0) {
            query.skip((pageNum - 1L) * pageSize).limit(pageSize);
        }
        List<FdevFinalDateApprove> data = mongoTemplate.find(query, FdevFinalDateApprove.class);
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.COUNT, count);
        map.put(Dict.APPROVELIST, data);
        return map;
    }

    @Override
    public Map<String, Object> queryMyApprove(String sectionId, UpdateFinalDateQueryDto queryDto) {
        Integer pageNum = queryDto.getPageNum();
        Integer pageSize = queryDto.getPageSize();
        Query query = new Query();
        Criteria criteria = new Criteria();
        String status = queryDto.getStatus();
        if (!status.equals(UNDETERMINED) && !status.equals("approve")) {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        if (status.equals(UNDETERMINED)) {
            criteria.and(Dict.OPERATE_STATUS).is(status);
        }
        if (status.equals("approve")) {
            criteria.and(Dict.OPERATE_STATUS).ne(UNDETERMINED);
        }

        String oaContactNameNo = queryDto.getOaContactNameNo();
        if (!CommonUtils.isNullOrEmpty(oaContactNameNo)) {
            List<String> contactNos = new ArrayList<>();
            DemandAssessDto dto = new DemandAssessDto();
            dto.setOaContactNameNo(oaContactNameNo);
            Map<String, Object> queryDtoList = demandAssessDao.query(dto);
            List<DemandAssess> data = (List<DemandAssess>) queryDtoList.get(Dict.DATA);
            for (DemandAssess demandAssess : data) {
                contactNos.add(demandAssess.getOa_contact_no());
            }
            criteria.and(Dict.OA_CONTACT_NO).in(contactNos);
        }
        criteria.and(Dict.SECTION_ID).is(sectionId);
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        //计数
        Long count = mongoTemplate.count(query, FdevFinalDateApprove.class);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        //分页
        if (!CommonUtils.isNullOrEmpty(pageNum) && pageNum != 0) {
            query.skip((pageNum - 1L) * pageSize).limit(pageSize);
        }
        List<FdevFinalDateApprove> data = mongoTemplate.find(query, FdevFinalDateApprove.class);
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.COUNT, count);
        map.put(Dict.APPROVELIST, data);
        return map;
    }

    @Override
    public FdevFinalDateApprove queryById(String id) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findOne(query, FdevFinalDateApprove.class);
    }

    @Override
    public List<FdevFinalDateApprove> listByIds(List<String> ids) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ID).in(ids);
        query.addCriteria(criteria);
        return mongoTemplate.find(query, FdevFinalDateApprove.class);
    }

    @Override
    public FdevFinalDateApprove update(FdevFinalDateApprove fdevFinalDateApprove) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(fdevFinalDateApprove.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(fdevFinalDateApprove);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, fdevFinalDateApprove.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)) {
                continue;
            }
            update.set(key, value);
        }
        return this.mongoTemplate.findAndModify(query, update, FdevFinalDateApprove.class);
    }

    @Override
    public List<FdevFinalDateApprove> updateBatch(UpdateFinalDateQueryDto updateDto) {
        Query query = Query.query(Criteria.where(Dict.ID).in(updateDto.getIds()));
        Update update = new Update();
        update.set(Dict.OPERATE_TIME, TimeUtil.formatTodayHs());//通过时间
        update.set(Dict.OPERATE_STATUS, updateDto.getStatus());//通过状态
        update.set(Dict.OPERATE_USER_ID, updateDto.getOperateUser());//审批人id
        mongoTemplate.updateMulti(query, update, FdevFinalDateApprove.class);
        query.with(new Sort(Sort.Direction.ASC, CREATE_TIME));
        return mongoTemplate.find(query, FdevFinalDateApprove.class);
    }

    @Override
    public Map<String, Long> queryCount(String sectionId) {
        Map<String, Long> map = new HashMap<>();
        Query queryWait = new Query();
        Criteria criteriaWait = new Criteria();
        criteriaWait.and(Dict.OPERATE_STATUS).is(UNDETERMINED);
        criteriaWait.and(Dict.SECTION_ID).is(sectionId);
        queryWait.addCriteria(criteriaWait);
        queryWait.fields().exclude(Dict.OBJECTID);
        //待审批计数
        Long waitCount = mongoTemplate.count(queryWait, FdevFinalDateApprove.class);
        map.put(WAITCOUNT, waitCount);
        Query queryApprove = new Query();
        Criteria criteriaApprove = new Criteria();
        criteriaApprove.and(Dict.OPERATE_STATUS).ne(UNDETERMINED);
        criteriaApprove.and(Dict.SECTION_ID).is(sectionId);
        queryApprove.addCriteria(criteriaApprove);
        queryApprove.fields().exclude(Dict.OBJECTID);
        //已完成计数
        Long approveCount = mongoTemplate.count(queryApprove, FdevFinalDateApprove.class);
        map.put(DONECOUNT, approveCount);
        return map;
    }

}
