package com.spdb.fdev.fdevinterface.spdb.dao.impl;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dao.TransDao;
import com.spdb.fdev.fdevinterface.spdb.entity.RestRelation;
import com.spdb.fdev.fdevinterface.spdb.entity.Trans;
import com.spdb.fdev.fdevinterface.spdb.entity.TransParamDesciption;
import com.spdb.fdev.fdevinterface.spdb.vo.TransParamShow;
import org.apache.commons.collections.CollectionUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Repository
public class TransDaoImpl implements TransDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void saveTrans(List<Trans> transList) {
        mongoTemplate.insert(transList, Trans.class);
    }

    @Override
    public void deleteTrans(String appServiceId, String branchName) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).in(appServiceId);
        criteria.and(Dict.BRANCH).in(branchName);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, Trans.class);
    }

    @Override
    public void updateTransTags(List<String> ids, List<String> tags) {
        Query query = new Query(Criteria.where(Dict.L_ID).in(ids));
        Update update = Update.update(Dict.TAGS, tags).set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        mongoTemplate.updateMulti(query, update, Trans.class);
    }

    @Override
    public List<Trans> getTransList(@NotNull String serviceId, @NotNull String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(serviceId);
        criteria.and(Dict.BRANCH).is(branch);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Trans.class);
    }

    @Override
    public Map showTrans(TransParamShow paramShow) {
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
        String transName = paramShow.getTransName();
        if (!StringUtils.isEmpty(transName)) {
            Pattern pattern = Pattern.compile("^.*" + transName + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.TRANS_NAME).regex(pattern);
        }
        String serviceId = paramShow.getServiceId();
        if (!StringUtils.isEmpty(serviceId)) {
            Pattern pattern = Pattern.compile("^.*" + serviceId + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.SERVICE_ID).regex(pattern);
        }
        String channel = paramShow.getChannel();
        if (!StringUtils.isEmpty(channel)) {
            criteria.and(Dict.CHANNEL_LIST).in(channel);
        }
        String needLogin = paramShow.getNeedLogin();
        if (!StringUtils.isEmpty(needLogin)) {
            criteria.and(Dict.NEED_LOGIN).is(Integer.valueOf(needLogin));
        }
        String writeJnl = paramShow.getWriteJnl();
        if (!StringUtils.isEmpty(writeJnl)) {
            criteria.and(Dict.WRITE_JNL).is(Integer.valueOf(writeJnl));
        }
        String verCode = paramShow.getVerCodeType();
        if (!StringUtils.isEmpty(verCode) && !Dict.ALL.equals(verCode)) {
            Pattern pattern = Pattern.compile("^.*" + verCode + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.VER_CODE_TYPE).regex(pattern);
        }
        String tags = paramShow.getTags();
        if (!StringUtils.isEmpty(tags)) {
            Pattern pattern = Pattern.compile("^.*" + tags + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.TAGS).regex(pattern);
        }
        int page = paramShow.getPage();
        if (Objects.isNull(page)) {
			page = 1;
		}
        int pageNum = paramShow.getPageNum();
        if (Objects.isNull(pageNum)) {
			pageNum = 5;
		}
        Query query = new Query(criteria);
        long total = mongoTemplate.count(query, Trans.class);
        query.skip((page - 1L) * pageNum).limit(pageNum);
        List<Trans> list = mongoTemplate.find(query, Trans.class);
        Map<String, Object> restMap = new HashMap<>();
        restMap.put(Dict.TOTAL, total);
        restMap.put(Dict.LIST, list);
        return restMap;
    }

    @Override
    public List<Trans> showAllTrans() {
        Query query = new Query(Criteria.where(Dict.BRANCH).is(Dict.MASTER));
        return mongoTemplate.find(query, Trans.class);
    }

    @Override
    public Trans getTransDetailById(String id) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.L_ID).is(id);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, Trans.class);
    }

    @Override
    public Trans getTrans(String transId, String serviceId, String branch, String channel) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.TRANS_ID).is(transId);
        criteria.and(Dict.SERVICE_ID).is(serviceId);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.CHANNEL).is(channel);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, Trans.class);
    }

    @Override
    public List<Trans> getTrans(Trans trans) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.TRANS_ID).is(trans.getTransId());
        criteria.and(Dict.SERVICE_ID).is(trans.getServiceId());
        criteria.and(Dict.BRANCH).is(trans.getBranch());
        criteria.and(Dict.CHANNEL_LIST).in(trans.getChannelList());
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Trans.class);
    }

    @Override
    public void saveParamDescription(List<TransParamDesciption> transParamDesciptionList){
        mongoTemplate.insert(transParamDesciptionList, TransParamDesciption.class);
    }

    @Override
    public TransParamDesciption getParamDescription(String transId, String serviceId, String channel){
        Criteria criteria = new Criteria();
        criteria.and("transId").is(transId);
        criteria.and("serviceId").is(serviceId);
        criteria.and("channel").is(channel);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, TransParamDesciption.class);
    }

    @Override
    public void updateParamDescription(Trans trans){
        Criteria criteria = new Criteria();
        criteria.and("transId").is(trans.getTransId());
        criteria.and("serviceId").is(trans.getServiceId());
        criteria.and("channel").is(trans.getChannel());
        Query query = new Query(criteria);
        List requestParam=trans.getRequest();
        List responseParam=trans.getResponse();
        Document doc=new Document();
        if(!FileUtil.isNullOrEmpty(requestParam)){
            Update update=Update.update("request",requestParam);
            mongoTemplate.updateMulti(query,update, TransParamDesciption.class);
        }
        if(!FileUtil.isNullOrEmpty(responseParam)){
            Update update=Update.update("response",responseParam);
            mongoTemplate.updateMulti(query,update,TransParamDesciption.class);
        }


    }

    @Override
    public void deleteTrans(Map params) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(params.get("serviceId"));
        if(!FileUtil.isNullOrEmpty(params.get("branch"))){
            criteria.and(Dict.BRANCH).is(params.get("branch"));
        }
        Query query = new Query(criteria);
        mongoTemplate.remove(query, Trans.class);
    }
    
    
    @Override
    public Map queryTrans(TransParamShow paramShow) {
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
        String transName = paramShow.getTransName();
        if (!StringUtils.isEmpty(transName)) {
            Pattern pattern = Pattern.compile("^.*" + transName + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.TRANS_NAME).regex(pattern);
        }
        String serviceId = paramShow.getServiceId();
        if (!StringUtils.isEmpty(serviceId)) {
            Pattern pattern = Pattern.compile("^.*" + serviceId + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.SERVICE_ID).regex(pattern);
        }
        String channel = paramShow.getChannel();
        if (!StringUtils.isEmpty(channel)) {
            criteria.and(Dict.CHANNEL_LIST).in(channel);
        }
        String needLogin = paramShow.getNeedLogin();
        if (!StringUtils.isEmpty(needLogin)) {
            criteria.and(Dict.NEED_LOGIN).is(Integer.valueOf(needLogin));
        }
        String writeJnl = paramShow.getWriteJnl();
        if (!StringUtils.isEmpty(writeJnl)) {
            criteria.and(Dict.WRITE_JNL).is(Integer.valueOf(writeJnl));
        }
        String verCode = paramShow.getVerCodeType();
        if (!StringUtils.isEmpty(verCode) && !Dict.ALL.equals(verCode)) {
            Pattern pattern = Pattern.compile("^.*" + verCode + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.VER_CODE_TYPE).regex(pattern);
        }
        String tags = paramShow.getTags();
        if (!StringUtils.isEmpty(tags)) {
            Pattern pattern = Pattern.compile("^.*" + tags + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.TAGS).regex(pattern);
        }
        Query query = new Query(criteria);
        List<Trans> list = mongoTemplate.find(query, Trans.class);
        Map<String, Object> restMap = new HashMap<>();
        restMap.put(Dict.LIST, list);
        return restMap;
    }

}
