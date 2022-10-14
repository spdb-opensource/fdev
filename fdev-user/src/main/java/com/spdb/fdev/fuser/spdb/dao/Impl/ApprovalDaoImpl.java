package com.spdb.fdev.fuser.spdb.dao.Impl;

import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.mongodb.client.result.UpdateResult;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.dao.ApprovalDao;
import com.spdb.fdev.fuser.spdb.dao.UserDao;
import com.spdb.fdev.fuser.spdb.entity.user.NetApproval;
import com.spdb.fdev.fuser.spdb.entity.user.User;


@Repository
public class ApprovalDaoImpl implements ApprovalDao {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalDaoImpl.class);// 控制台日志打印

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private UserDao userDao;

    @Override
    public Map queryApprovalList(Map params) {
        Criteria criteria = new Criteria();
        if (!CommonUtils.isNullOrEmpty(params.get(Dict.IDS))) {
            List<String> ids = (List<String>) params.get(Dict.IDS);
            criteria.and("user_id").in(ids);
        }
        if (!CommonUtils.isNullOrEmpty(params.get("applicant_id"))) {
            List<String> applicant = (List<String>) params.get("applicant_id");
            criteria.and("applicant_id").in(applicant);
        }
        if (!CommonUtils.isNullOrEmpty(params.get("user_id"))) {
            List<String> users = (List<String>) params.get("user_id");
            criteria.and("user_id").in(users);
        }
        if (!CommonUtils.isNullOrEmpty(params.get("vm_user_name"))) {
            String userName = (String) params.get("vm_user_name");
            Pattern pattern = Pattern.compile("^.*" + userName + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and("vm_user_name").regex(pattern);
        }
        if (!StringUtils.isEmpty(params.get(Dict.STATUS))) {
            String status = (String) params.get(Dict.STATUS);
            criteria.and(Dict.STATUS).is(status);
        }
        if (!StringUtils.isEmpty(params.get("type"))) {
            String type = (String) params.get("type");
            criteria.and("type").is(type);
        }
        List<Criteria> criterias = new ArrayList<>();
        if (!StringUtils.isEmpty(params.get("startTime"))) {
            String startTime = (String) params.get("startTime");
            criterias.add(Criteria.where("create_time").gte(startTime));
        }
        if (!StringUtils.isEmpty(params.get("endTime"))) {
            String endTime = (String) params.get("endTime");
            criterias.add(Criteria.where("create_time").lte(endTime));
        }
        if (!CommonUtils.isNullOrEmpty(criterias)) {
            criteria.andOperator(criterias.toArray(new Criteria[criterias.size()]));
        }
        int page = 1;
        int pageNum = 10;
        if (!StringUtils.isEmpty(params.get(Dict.PAGE))) {
            page = (int) params.get(Dict.PAGE);
        }
        if (!StringUtils.isEmpty(params.get("pageNum"))) {
            pageNum = (int) params.get("pageNum");
        }
        Query query = new Query(criteria);
        Map map = new HashMap<>();
        long total = mongoTemplate.count(query, NetApproval.class);
        query.skip((page > 0 ? page - 1 : 0) * pageNum).limit(pageNum);
        List<HashMap> approvals = mongoTemplate.find(query, HashMap.class, "netApproval");
        map.put(Dict.TOTAL, total);
        map.put("list", approvals);
        return map;
    }

    @Override
    public List<String> queryUserIdsByCompany(Map param) {
        if (CommonUtils.isNullOrEmpty(param.get(Dict.COMPANY_ID))) {
            return null;
        }
        List<Map> list = new ArrayList<>();
        Query query = new Query();
        Criteria criteria = new Criteria();
        String companyId = (String) param.get(Dict.COMPANY_ID);
        criteria.and(Dict.COMPANY_ID).is(companyId);
        query.addCriteria(criteria);
        query.fields().include(Dict.ID);
        List<User> lists = mongoTemplate.find(query, User.class);
        if (CollectionUtils.isNotEmpty(lists)) {
            List<String> ids = new ArrayList<>();
            for (User user : lists) {
                ids.add(user.getId());
            }
            return ids;
        }
        return null;
    }


    @Override
    public void addApprovalByUser(NetApproval approval) {
        mongoTemplate.insert(approval);
    }

    @Override
    public List<Map> queryApproval(NetApproval approval) {
        Criteria c = new Criteria();
        if (!CommonUtils.isNullOrEmpty(approval.getUser_id())) {
            c.and("user_id").is(approval.getUser_id());
        }
        if (!CommonUtils.isNullOrEmpty(approval.getId())) {
            c.and("id").is(approval.getId());
        }
        if (!CommonUtils.isNullOrEmpty(approval.getApplicant_id())) {
            c.and("applicant_id").is(approval.getApplicant_id());
        }
        if (!CommonUtils.isNullOrEmpty(approval.getType())) {
            c.and("type").is(approval.getType());
        }
        if (!CommonUtils.isNullOrEmpty(approval.getPhone_type())) {
            c.and("phone_type").is(approval.getPhone_type());
        }
        if (!CommonUtils.isNullOrEmpty(approval.getPhone_mac())) {
            c.and("phone_mac").is(approval.getPhone_mac());
        }
        if (!CommonUtils.isNullOrEmpty(approval.getVm_ip())) {
            c.and("vm_ip").is(approval.getVm_ip());
        }
        if (!CommonUtils.isNullOrEmpty(approval.getVm_name())) {
            c.and("vm_name").is(approval.getVm_name());
        }
        if (!CommonUtils.isNullOrEmpty(approval.getVm_user_name())) {
            c.and("vm_user_name").is(approval.getVm_user_name());
        }
        if (!CommonUtils.isNullOrEmpty(approval.getStatus())) {
            c.and("status").is(approval.getStatus());
        }
        if (!CommonUtils.isNullOrEmpty(approval.getCreate_time())) {
            c.and("create_time").gte(approval.getCreate_time());
        }
        if (!CommonUtils.isNullOrEmpty(approval.getOff_flag()) && approval.getOff_flag() != 0) {
            c.and("off_flag").is(approval.getOff_flag());
        }
        MatchOperation match = Aggregation.match(c);
        return mongoTemplate.aggregate(Aggregation.newAggregation(match), "netApproval", Map.class).getMappedResults();
    }

    @Override
    public void updateApprovalStatus(NetApproval approval) throws Exception {
        Criteria c = new Criteria();
        Update up = new Update();
        //其中id为查找的记录，剩下的字段为更新字段
        if (!CommonUtils.isNullOrEmpty(approval)) {
            if (!CommonUtils.isNullOrEmpty(approval.getId())) {
                c.and("id").is(approval.getId());
            } else {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"提交更新查询的id字段为空"});
            }
            if (!CommonUtils.isNullOrEmpty(approval.getType())) {
                up.set("type", approval.getType());
            }
            if (!CommonUtils.isNullOrEmpty(approval.getApplicant_id())) {
                up.set("applicant_id", approval.getApplicant_id());
            }
            if (!CommonUtils.isNullOrEmpty(approval.getUser_id())) {
                up.set("user_id", approval.getUser_id());
            }
            if (!CommonUtils.isNullOrEmpty(approval.getPhone_type())) {
                up.set("phone_type", approval.getPhone_type());
            }
            if (!CommonUtils.isNullOrEmpty(approval.getPhone_mac())) {
                up.set("phone_mac", approval.getPhone_mac());
            }
            if (!CommonUtils.isNullOrEmpty(approval.getVm_ip())) {
                up.set("vm_ip", approval.getVm_ip());
            }
            if (!CommonUtils.isNullOrEmpty(approval.getVm_name())) {
                up.set("vm_name", approval.getVm_name());
            }
            if (!CommonUtils.isNullOrEmpty(approval.getVm_user_name())) {
                up.set("vm_user_name", approval.getVm_user_name());
            }
            if (!CommonUtils.isNullOrEmpty(approval.getStatus())) {
                up.set("status", approval.getStatus());
            }
            if (!CommonUtils.isNullOrEmpty(approval.getOff_flag()) && approval.getOff_flag() != 0) {
                up.set("off_flag", approval.getOff_flag());
            }
            if (!CommonUtils.isNullOrEmpty(approval.getCreate_time())) {
                up.set("create_time", approval.getCreate_time());
            }
            if (!CommonUtils.isNullOrEmpty(approval.getUpdate_time())) {
                up.set("update_time", approval.getUpdate_time());
            }
        } else {
            //传入的参数为空时
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"提交更新的字段为空"});
        }
        Query query = new Query(c);
        this.mongoTemplate.updateMulti(query, up, "netApproval");

    }

    @Override
    public int updateApproval(Map param) {
        Criteria queryCrieria = getQueryCrieria(param);
        Update update = new Update();
        update.set(Dict.STATUS, (String) param.get(Dict.STATUS));
        update.set("update_time", CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        Query query = new Query(queryCrieria);
        UpdateResult result = mongoTemplate.updateMulti(query, update, "netApproval");
        return (int) result.getModifiedCount();
    }

    /**
     * 非分页查询
     */
    @Override
    public List<Map> queryWaitApproveList(Map param) {
        Criteria queryCrieria = getQueryCrieria(param);
        Query query = new Query(queryCrieria);
        long a = mongoTemplate.count(query, NetApproval.class);
        MatchOperation match = Aggregation.match(queryCrieria);
        List<Map> result = mongoTemplate.aggregate(Aggregation.newAggregation(match), "netApproval", Map.class).getMappedResults();
        return result;
    }

    private Criteria getQueryCrieria(Map param) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.STATUS).is(Dict.WAIT_APPROVE);
        //判断map内容ids和type
        criteria.and(Dict.ID).in((List<String>) param.get(Dict.IDS));
        criteria.and("type").is((String) param.get("type"));
        return criteria;
    }

    @Override
    public int addAllOffFlag() {
        Criteria criteria = new Criteria();
        criteria.and("type").is(Dict.KF_APPROVAL);
        criteria.and("off_flag").gte(2);
        Update update = new Update();
        update.inc("off_flag", 1);
        UpdateResult updateMulti = mongoTemplate.updateMulti(new Query(criteria), update, NetApproval.class);
        return (int) updateMulti.getModifiedCount();
    }

}
