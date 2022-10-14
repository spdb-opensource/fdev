package com.spdb.fdev.fdevinterface.spdb.dao.impl;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.spdb.dao.TransRelationDao;
import com.spdb.fdev.fdevinterface.spdb.entity.RestRelation;
import com.spdb.fdev.fdevinterface.spdb.entity.TransRelation;
import com.spdb.fdev.fdevinterface.spdb.vo.TransParamShow;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Repository
public class TransRelationDaoImpl implements TransRelationDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void save(List<TransRelation> transRelationList) {
        mongoTemplate.insert(transRelationList, TransRelation.class);
    }

    @Override
    public void delete(String serviceCalling, String branch, String channel) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_CALLING).is(serviceCalling);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.CHANNEL).is(channel);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, TransRelation.class);
    }

    @Override
    public Map showTransRelation(TransParamShow paramShow) {
        Criteria criteria = new Criteria();
        // 设置分支
        String branchDefault = paramShow.getBranchDefault();
        if (Dict.OTHER.equals(branchDefault)) {
            Pattern pattern = Pattern.compile("^(?!.*" + Dict.MASTER + "|" + Dict.SIT + ")" + "^.*" + paramShow.getBranch() + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.BRANCH).regex(pattern);
        } else {
            criteria.and(Dict.BRANCH).is(branchDefault);
        }
        List<String> transIdList = paramShow.getTransId();
        if (CollectionUtils.isNotEmpty(transIdList)) {
            if (transIdList.size() == 1) {
                Pattern pattern = Pattern.compile("^.*" + transIdList.get(0) + ".*$", Pattern.CASE_INSENSITIVE);
                criteria.and(Dict.TRANS_ID).regex(pattern);
            } else {
                criteria.and(Dict.TRANS_ID).in(transIdList);
            }
        }
        String serviceCalling = paramShow.getServiceCalling();
        if (!StringUtils.isEmpty(serviceCalling)) {
            Pattern pattern = Pattern.compile("^.*" + serviceCalling + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.SERVICE_CALLING).regex(pattern);
        }
        String serviceId = paramShow.getServiceId();
        if (!StringUtils.isEmpty(serviceId)) {
            Pattern pattern = Pattern.compile("^.*" + serviceId + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.SERVICE_ID).regex(pattern);
        } else {
            criteria.and(Dict.SERVICE_ID).nin("");
        }
        String channel = paramShow.getChannel();
        if (!StringUtils.isEmpty(channel)) {
            criteria.and(Dict.CHANNEL).is(channel);
        }
        Query query = new Query(criteria);
        Long total = mongoTemplate.count(query, TransRelation.class);
        Integer page = paramShow.getPage();
        Integer pageNum = paramShow.getPageNum();
        if (page == null) {
            page = 1;
        }
        if (pageNum == null) {
            pageNum = 10;
        }
        query.skip((page - 1L) * pageNum).limit(pageNum);
        List<TransRelation> list = mongoTemplate.find(query, TransRelation.class);
        Map<String, Object> restMap = new HashMap<>();
        restMap.put(Dict.TOTAL, total);
        restMap.put(Dict.LIST, list);
        return restMap;
    }

    @Override
    public void deleteTransRelation(Map params) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(params.get("serviceId"));
        if(!FileUtil.isNullOrEmpty(params.get("branch"))){
            criteria.and(Dict.BRANCH).is(params.get("branch"));
        }
        Query query = new Query(criteria);
        mongoTemplate.remove(query, TransRelation.class);
        Criteria criteria1 = new Criteria();
        criteria1.and(Dict.SERVICE_CALLING).is(params.get("serviceId"));
        if(!FileUtil.isNullOrEmpty(params.get("branch"))){
            criteria1.and(Dict.BRANCH).is(params.get("branch"));
        }
        Query query1 = new Query(criteria1);
        mongoTemplate.remove(query1, TransRelation.class);
    }

}
