package com.spdb.fdev.fdevinterface.spdb.dao.impl;


import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dao.InterfaceDao;
import com.spdb.fdev.fdevinterface.spdb.dao.util.BathUpdateOption;
import com.spdb.fdev.fdevinterface.spdb.dao.util.BathUpdateUtil;
import com.spdb.fdev.fdevinterface.spdb.dao.util.QueryUtil;
import com.spdb.fdev.fdevinterface.spdb.dto.Param;
import com.spdb.fdev.fdevinterface.spdb.entity.ParamDesciption;
import com.spdb.fdev.fdevinterface.spdb.entity.RestApi;
import com.spdb.fdev.fdevinterface.spdb.entity.SoapApi;
import com.spdb.fdev.fdevinterface.spdb.entity.TinyURL;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceDetailShow;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceParamShow;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceShow;
import org.apache.commons.collections.CollectionUtils;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InterfaceDaoImpl implements InterfaceDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void saveRestApiList(List<RestApi> restApiList) {
        mongoTemplate.insert(restApiList, RestApi.class);
    }

    @Override
    public List<RestApi> getRestApiList(String appServiceId, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(appServiceId);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.IS_NEW).is(1);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, RestApi.class);
    }

    @Override
    public void updateRestApiIsNewByIds(List<String> id) {
        Query query = new Query(Criteria.where(Dict.L_ID).in(id));
        Update update = Update.update(Dict.IS_NEW, 0).set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        mongoTemplate.updateMulti(query, update, RestApi.class);
    }

    @Override
    public void updateRestApiRegister(List<RestApi> restApiList) {
        List<BathUpdateOption> list = new ArrayList<>();
        for (RestApi restApi : restApiList) {
            Query query = new Query(Criteria.where(Dict.TRANS_ID).is(restApi.getTransId())
                    .and(Dict.SERVICE_ID).is(restApi.getServiceId())
                    .and(Dict.BRANCH).is(restApi.getBranch()));
            Update update = Update.update(Dict.REGISTER, restApi.getRegister())
                    .set(Dict.SCHEMA_CONFIG_REPOSITORY_ID, restApi.getSchemaConfigRepositoryId())
                    .set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            list.add(new BathUpdateOption(query, update, false, true));
        }
        if (CollectionUtils.isNotEmpty(list)) {
            BathUpdateUtil.bathUpdate(mongoTemplate, Dict.INTERFACE_REST_API, list);
        }
    }

    @Override
    public void saveSoapApiList(List<SoapApi> soapApiList) {
        mongoTemplate.insert(soapApiList, SoapApi.class);
    }

    @Override
    public List<SoapApi> getSoapApiList(String appServiceId, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(appServiceId);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.IS_NEW).is(1);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, SoapApi.class);
    }

    @Override
    public void updateSoapApiIsNewByIds(List<String> id) {
        Query query = new Query(Criteria.where(Dict.L_ID).in(id));
        Update update = Update.update(Dict.IS_NEW, 0).set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        mongoTemplate.updateMulti(query, update, SoapApi.class);
    }

    @Override
    public Map<String, Object> showRestApi(InterfaceParamShow interfaceParamShow) {
        Criteria criteria = QueryUtil.getCriteria(interfaceParamShow);
        Query query = new Query(criteria);
        Long total = mongoTemplate.count(query, RestApi.class);
        Integer page = interfaceParamShow.getPage();
        Integer pageNum = interfaceParamShow.getPageNum();
        query.skip((page - 1L) * pageNum).limit(pageNum);
        List<RestApi> list = mongoTemplate.find(query, RestApi.class);
        Map<String, Object> restMap = new HashMap<>();
        restMap.put(Dict.TOTAL, total);
        restMap.put(Dict.LIST, list);
        return restMap;
    }

    @Override
    public List<RestApi> getRestApi() {
        Criteria criteria=new Criteria();
        Query query=new Query(criteria);
        return mongoTemplate.find(query,RestApi.class);
    }

    @Override
    public RestApi getRestApiName(String transId, String serviceId, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.TRANS_ID).is(transId);
        criteria.and(Dict.SERVICE_ID).is(serviceId);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.IS_NEW).is(1);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, RestApi.class);
    }

    @Override
    public RestApi getRestApiById(String id) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.L_ID).is(id);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, RestApi.class);
    }

    @Override
    public Map<String, Object> showSoapApi(InterfaceParamShow interfaceParamShow) {
        Criteria criteria = QueryUtil.getCriteria(interfaceParamShow);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.lookup(Dict.INTERFACE_ESB_RELATION, Dict.TRANS_ID, Dict.SERVICE_AND_OPERATION_ID, Dict.ESB)
        );
        List<Map> mappedResults = mongoTemplate.aggregate(aggregation, Dict.INTERFACE_SOAP_API, Map.class).getMappedResults();
      
        Map<String, Object> soapMap = new HashMap<>();
        soapMap.put(Dict.LIST, mappedResults);
        return soapMap;
    }

    @Override
    public SoapApi getSoapApi(String transId, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.TRANS_ID).is(transId);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.IS_NEW).is(1);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, SoapApi.class);
    }

    @Override
    public SoapApi getSoapApiById(String id) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.L_ID).is(id);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, SoapApi.class);
    }

    @Override
    public List<RestApi> getRestApiVer(String serviceId, String transId, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(serviceId);
        criteria.and(Dict.TRANS_ID).is(transId);
        criteria.and(Dict.BRANCH).is(branch);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        query.skip(0).limit(Constants.FIVE);
        return mongoTemplate.find(query, RestApi.class);
    }

    @Override
    public List<RestApi> getNotRegister(String serviceId, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(serviceId);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.REGISTER).is(0);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, RestApi.class);
    }

    @Override
    public void deleteRestApi(Map params) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(params.get("serviceId"));
        if(!FileUtil.isNullOrEmpty(params.get("branch"))){
            criteria.and(Dict.BRANCH).is(params.get("branch"));
        }
        Query query = new Query(criteria);
        mongoTemplate.remove(query,RestApi.class);
    }

    @Override
    public List<SoapApi> getSoapApiVer(String serviceId, String interfaceAlias, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(serviceId);
        criteria.and(Dict.INTERFACE_ALIAS).is(interfaceAlias);
        criteria.and(Dict.BRANCH).is(branch);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        query.skip(0).limit(Constants.FIVE);
        return mongoTemplate.find(query, SoapApi.class);
    }

    @Override
    public void updateSoapApiInterfaceName(List<InterfaceShow> interfaceShowList) {
        List<BathUpdateOption> list = new ArrayList<>();
        for (InterfaceShow interfaceShow : interfaceShowList) {
            Query query = new Query(Criteria.where(Dict.TRANS_ID).is(interfaceShow.getServiceAndOperationId())
                    .and(Dict.SERVICE_ID).is(interfaceShow.getServiceId())
                    .and(Dict.BRANCH).is(interfaceShow.getBranch()));
            Update update = Update.update(Dict.INTERFACE_NAME, interfaceShow.getInterfaceName())
                    .set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            list.add(new BathUpdateOption(query, update, false, true));
        }
        if (CollectionUtils.isNotEmpty(list)) {
            BathUpdateUtil.bathUpdate(mongoTemplate, Dict.INTERFACE_SOAP_API, list);
        }
    }

    @Override
    public void deleteSoapApi(Map params) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(params.get("serviceId"));
        if(!FileUtil.isNullOrEmpty(params.get("branch"))){
            criteria.and(Dict.BRANCH).is(params.get("branch"));
        }
        Query query = new Query(criteria);
        mongoTemplate.remove(query,SoapApi.class);
    }

    @Override
    public String saveTinyUrlKey(TinyURL tinyUrl) {
        return mongoTemplate.save(tinyUrl, Dict.INTERFACE_TINY_URL).getId();
    }

    @Override
    public Map getTinyUrlKeyById(String id) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.L_ID).is(id);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, TinyURL.class).getTinyUrlkey();
    }


    @Override
    public void saveParamDescription(List<ParamDesciption> paramDesciption){
        mongoTemplate.insert(paramDesciption, ParamDesciption.class);
    }

    @Override
    public ParamDesciption getParamDescription(String transId,String serviceId,String interfaceType){
        Criteria criteria = new Criteria();
        criteria.and("transId").is(transId);
        criteria.and("serviceId").is(serviceId);
        criteria.and("interfaceType").is(interfaceType);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, ParamDesciption.class);
    }

    @Override
    public void updateParamDescription(InterfaceDetailShow interfaceDetailShow){
        Criteria criteria = new Criteria();
        criteria.and("transId").is(interfaceDetailShow.getTransId());
        criteria.and("serviceId").is(interfaceDetailShow.getServiceId());
        criteria.and("interfaceType").is(interfaceDetailShow.getInterfaceType());
        Query query = new Query(criteria);
        List<Param> requestParam=interfaceDetailShow.getRequest();
        List<Param> responseParam=interfaceDetailShow.getResponse();
        Document doc=new Document();
        if(!FileUtil.isNullOrEmpty(requestParam)){
            Update update=Update.update("request",requestParam);
            mongoTemplate.updateMulti(query,update,ParamDesciption.class);
        }
        if(!FileUtil.isNullOrEmpty(responseParam)){
            Update update=Update.update("response",responseParam);
            mongoTemplate.updateMulti(query,update,ParamDesciption.class);
        }


    }

    @Override
    public List<RestApi> getRestApi(String transId, String serviceId) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(serviceId);
        criteria.and(Dict.TRANS_ID).is(transId);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, RestApi.class);
    }

}
