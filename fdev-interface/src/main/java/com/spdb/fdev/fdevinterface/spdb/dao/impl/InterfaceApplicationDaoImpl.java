package com.spdb.fdev.fdevinterface.spdb.dao.impl;


import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dao.InterfaceApplicationDao;
import com.spdb.fdev.fdevinterface.spdb.entity.ApproveStatus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InterfaceApplicationDaoImpl implements InterfaceApplicationDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void insertRecord(List<ApproveStatus> approveStatusList) {
        mongoTemplate.insert(approveStatusList,ApproveStatus.class);
    }

    @Override
    public Map<String, Object> getApproveList(Map map) {
        Criteria criteria = new Criteria();
        String serviceCalling=(String)map.get("serviceCalling");
        String serviceId=(String)map.get("serviceId");
        String transId=(String)map.get("transId");
        String applicant=(String)map.get("applicant");
        String approver= (String) map.get("approver");
        String status= (String) map.get("status");
        if(!StringUtils.isEmpty(serviceCalling)){
            criteria.and("service_calling").is(serviceCalling);
        }
        if(!StringUtils.isEmpty(transId)){
            criteria.and("trans_id").is(transId);
        }
        if(!StringUtils.isEmpty(applicant)){
            criteria.and("applicant").is(applicant);
        }
        if(!StringUtils.isEmpty(serviceId)){
            criteria.and("service_id").is(serviceId);
        }
        if(!StringUtils.isEmpty(approver)){
            criteria.and("approver").is(approver);
        }
        if(!StringUtils.isEmpty(status)){
            criteria.and("status").is(status);
        }
        Query query = new Query(criteria);
        Long total = mongoTemplate.count(query, ApproveStatus.class);
        Integer page =(Integer) map.get("page");
        Integer pageNum =(Integer) map.get("pageNum");
        if (page == null) {
            page = 1;
        }
        if (pageNum == null) {
            pageNum = 10;
        }
        query.skip((page - 1L) * pageNum).limit(pageNum);
        List<ApproveStatus> list = mongoTemplate.find(query, ApproveStatus.class);
        Map<String, Object> restMap = new HashMap<>();
        restMap.put(Dict.TOTAL, total);
        restMap.put(Dict.LIST, list);
        return restMap;
    }

    @Override
    public List<ApproveStatus> ApproveRecord(String transId, String serviceCalling,String serviceId) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.TRANS_ID).is(transId);
        criteria.and(Dict.SERVICE_CALLING).is(serviceCalling);
        criteria.and(Dict.SERVICE_ID).is(serviceId);
        Query query=new Query(criteria);
        List<ApproveStatus> approveStatus=mongoTemplate.find(query, ApproveStatus.class);
        return approveStatus;
    }

    @Override
    public void updateApproveStatus(Map map) {
        Query query = new Query(Criteria.where(Dict.L_ID).is(map.get("id")));
        ApproveStatus approveStatus=mongoTemplate.findOne(query,ApproveStatus.class);
        if(!FileUtil.isNullOrEmpty(approveStatus)){
            Update update = Update.update(Dict.APPROVER,map.get("approver")).set(Dict.REFUSEREASON,map.get("refuseReason")).set("status",map.get("status"))
                    .set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            mongoTemplate.updateMulti(query, update, ApproveStatus.class);
        }

    }

    @Override
    public void updateApproveRecord(ApproveStatus approveStatus) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.TRANS_ID).is(approveStatus.getTransId());
        criteria.and(Dict.SERVICE_CALLING).is(approveStatus.getServiceCalling());
        Query query=new Query(criteria);
        Update update = Update.update(Dict.APPLICANT,approveStatus.getApplicant()).set(Dict.REASON,approveStatus.getReason()).set("status",approveStatus.getStatus())
                .set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        mongoTemplate.updateMulti(query, update, ApproveStatus.class);

    }

    @Override
    public List<ApproveStatus> queryApproveList(String transId, String serviceCalling,String serviceId) {
        Criteria criteria = new Criteria();
        criteria.and("service_calling").is(serviceCalling);
        criteria.and("trans_id").is(transId);
        criteria.and("service_id").is(serviceId);
        Query query = new Query(criteria);
        List<ApproveStatus> list = mongoTemplate.find(query, ApproveStatus.class);
        return list;
    }

    @Override
    public ApproveStatus queryRecordById(String id) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.L_ID).is(id);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query,ApproveStatus.class);
    }

	@Override
	public void deleteApproveStatus(Map map) {
		Criteria criteria = new Criteria();
		criteria.and(Dict.L_ID).is(map.get("id"));
		Query query = new Query(criteria);
		mongoTemplate.remove(query, ApproveStatus.class);
	}

}
