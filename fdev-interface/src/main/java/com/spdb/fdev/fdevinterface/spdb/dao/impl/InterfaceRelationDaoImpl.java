package com.spdb.fdev.fdevinterface.spdb.dao.impl;

import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dao.InterfaceRelationDao;
import com.spdb.fdev.fdevinterface.spdb.dao.util.BathUpdateOption;
import com.spdb.fdev.fdevinterface.spdb.dao.util.BathUpdateUtil;
import com.spdb.fdev.fdevinterface.spdb.dao.util.QueryUtil;
import com.spdb.fdev.fdevinterface.spdb.entity.RestRelation;
import com.spdb.fdev.fdevinterface.spdb.entity.SoapApi;
import com.spdb.fdev.fdevinterface.spdb.entity.SoapRelation;
import com.spdb.fdev.fdevinterface.spdb.entity.SopRelation;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceParamShow;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceRelationShow;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.management.relation.Relation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Repository
public class InterfaceRelationDaoImpl implements InterfaceRelationDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void saveRestRelation(List<RestRelation> restRelationList) {
        mongoTemplate.insert(restRelationList, RestRelation.class);
    }

    @Override
    public void deleteRestRelation(String appServiceId, String branchName) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_CALLING).in(appServiceId);
        criteria.and(Dict.BRANCH).in(branchName);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, RestRelation.class);
    }

    @Override
    public List<RestRelation> showRestRelation(InterfaceParamShow interfaceParamShow) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.IS_NEW).is(1);
        if(!"other".equals(interfaceParamShow.getBranchDefault())){
            criteria.and(Dict.BRANCH).is(interfaceParamShow.getBranchDefault());
        }else if(!FileUtil.isNullOrEmpty(interfaceParamShow.getBranch())){
        	criteria.and(Dict.BRANCH).is(interfaceParamShow.getBranch());
        }
        if(!FileUtil.isNullOrEmpty(interfaceParamShow.getServiceId())){
            criteria.and(Dict.SERVICE_ID).is(interfaceParamShow.getServiceId());
        }
        if(!FileUtil.isNullOrEmpty(interfaceParamShow.getServiceCalling())){
            criteria.and(Dict.SERVICE_CALLING).is(interfaceParamShow.getServiceCalling());
        }
        List<String> seachList=interfaceParamShow.getTransOrServiceOrOperation();
        if(!FileUtil.isNullOrEmpty(seachList)){
            criteria.and(Dict.TRANS_ID).in(seachList);
        }
        Query query = new Query(criteria);
        List<RestRelation> list = mongoTemplate.find(query, RestRelation.class);
        return list;
    }

    @Override
    public List<RestRelation> getRestRelation(String serviceCalling) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_CALLING).is(serviceCalling);
        Query query = new Query(criteria);
        List<RestRelation> restRelationList=mongoTemplate.find(query,RestRelation.class);
        return restRelationList;
    }

    @Override
    public List<RestRelation> queryRestRelation() {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria);
        List<RestRelation> restRelationList=mongoTemplate.find(query,RestRelation.class);
        return restRelationList;
    }

    @Override
    public void deleteRestRelation(Map params) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(params.get("serviceId"));
        if(!FileUtil.isNullOrEmpty(params.get("branch"))){
            criteria.and(Dict.BRANCH).is(params.get("branch"));
        }
        Query query = new Query(criteria);
        mongoTemplate.remove(query, RestRelation.class);
        Criteria criteria1 = new Criteria();
        criteria1.and(Dict.SERVICE_CALLING).is(params.get("serviceId"));
        if(!FileUtil.isNullOrEmpty(params.get("branch"))){
            criteria1.and(Dict.BRANCH).is(params.get("branch"));
        }
        Query query1 = new Query(criteria1);
        mongoTemplate.remove(query1, RestRelation.class);
    }

    @Override
    public List<SoapRelation> showSoapRelation(InterfaceParamShow interfaceParamShow) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.IS_NEW).is(1);
        if(!"other".equals(interfaceParamShow.getBranchDefault())){
            criteria.and(Dict.BRANCH).is(interfaceParamShow.getBranchDefault());
        }else if(!FileUtil.isNullOrEmpty(interfaceParamShow.getBranch())){
        	criteria.and(Dict.BRANCH).is(interfaceParamShow.getBranch());
        }
        if(!FileUtil.isNullOrEmpty(interfaceParamShow.getServiceId())){
            criteria.and(Dict.SERVICE_ID).is(interfaceParamShow.getServiceId());
        }
        if(!FileUtil.isNullOrEmpty(interfaceParamShow.getServiceCalling())){
            criteria.and(Dict.SERVICE_CALLING).is(interfaceParamShow.getServiceCalling());
        }
        if (!FileUtil.isNullOrEmpty(interfaceParamShow.getTransOrServiceOrOperation())){
        	List<String> searchList = interfaceParamShow.getTransOrServiceOrOperation();
        	Criteria criteria1 = new Criteria().and(Dict.ESB_TRANS_ID).in(searchList);
        	Criteria criteria2 = new Criteria().and(Dict.ESB_SERVICE_ID).in(searchList);
        	Criteria criteria3 = new Criteria().and(Dict.ESB_OPERATION_ID).in(searchList);
        	Criteria criteria4 = new Criteria().and(Dict.INTERFACE_ALIAS).in(searchList);
        	criteria.orOperator(criteria1,criteria2,criteria3,criteria4);
		}
        Query query=new Query(criteria);
        List<SoapRelation> mappedResults = mongoTemplate.find(query,SoapRelation.class);
        return mappedResults;
    }

    @Override
    public SoapRelation getSoapRelationById(String id) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.L_ID).in(id);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, SoapRelation.class);
    }

    @Override
    public SoapRelation getSoapRelation(String serviceId, String interfaceAlias, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_CALLING).is(serviceId);
        criteria.and(Dict.INTERFACE_ALIAS).is(interfaceAlias);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.IS_NEW).is(1);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, SoapRelation.class);
    }

    @Override
    public List<SoapRelation> getSoapRelationList(String appServiceId, String branchName) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_CALLING).in(appServiceId);
        criteria.and(Dict.BRANCH).in(branchName);
        criteria.and(Dict.IS_NEW).is(1);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, SoapRelation.class);
    }

    @Override
    public void updateSoapRelationIsNewByIds(List<String> idList) {
        Query query = new Query(Criteria.where(Dict.L_ID).in(idList));
        Update update = Update.update(Dict.IS_NEW, 0).set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        mongoTemplate.updateMulti(query, update, SoapRelation.class);
    }

    @Override
    public void saveSoapRelationList(List<SoapRelation> soapRelationList) {
        mongoTemplate.insert(soapRelationList, SoapRelation.class);
    }

    @Override
    public List<SoapRelation> getSoapRelationVer(String serviceId, String interfaceAlias, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_CALLING).is(serviceId);
        criteria.and(Dict.INTERFACE_ALIAS).is(interfaceAlias);
        criteria.and(Dict.BRANCH).is(branch);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        query.skip(0).limit(Constants.FIVE);
        return mongoTemplate.find(query, SoapRelation.class);
    }

    @Override
    public void updateSoapRelationServiceId(List<InterfaceRelationShow> updateServiceIdList) {
        List<BathUpdateOption> list = new ArrayList<>();
        for (InterfaceRelationShow soapRelation : updateServiceIdList) {
            Query query = new Query(Criteria.where(Dict.TRANS_ID).is(soapRelation.getServiceAndOperationId())
                    .and(Dict.SERVICE_CALLING).is(soapRelation.getServiceCalling())
                    .and(Dict.BRANCH).is(soapRelation.getBranch()));
            Update update = Update.update(Dict.INTERFACE_NAME, soapRelation.getInterfaceName())
                    .set(Dict.SERVICE_ID, soapRelation.getServiceId())
                    .set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            list.add(new BathUpdateOption(query, update, false, true));
        }
        if (CollectionUtils.isNotEmpty(list)) {
            BathUpdateUtil.bathUpdate(mongoTemplate, Dict.INTERFACE_SOAP_RELATION, list);
        }
    }

    @Override
    public void updateSoapRelationInterfaceName(List<InterfaceRelationShow> interfaceRelationShowList) {
        List<BathUpdateOption> updateOptionList = new ArrayList<>();
        for (InterfaceRelationShow soapRelation : interfaceRelationShowList) {
            Query query = new Query(Criteria.where(Dict.TRANS_ID).is(soapRelation.getServiceAndOperationId())
                    .and(Dict.SERVICE_CALLING).is(soapRelation.getServiceCalling())
                    .and(Dict.BRANCH).is(soapRelation.getBranch()));
            Update update = Update.update(Dict.INTERFACE_NAME, soapRelation.getInterfaceName())
                    .set(Dict.SERVICE_ID, soapRelation.getServiceId())
                    .set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            updateOptionList.add(new BathUpdateOption(query, update, false, true));
        }
        if (CollectionUtils.isNotEmpty(updateOptionList)) {
            BathUpdateUtil.bathUpdate(mongoTemplate, Dict.INTERFACE_SOAP_RELATION, updateOptionList);
        }
    }

    @Override
    public void deleteSoapRelation(Map params) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(params.get("serviceId"));
        if(!FileUtil.isNullOrEmpty(params.get("branch"))){
            criteria.and(Dict.BRANCH).is(params.get("branch"));
        }
        Query query = new Query(criteria);
        mongoTemplate.remove(query, SoapRelation.class);
        Criteria criteria1 = new Criteria();
        criteria1.and(Dict.SERVICE_CALLING).is(params.get("serviceId"));
        if(!FileUtil.isNullOrEmpty(params.get("branch"))){
            criteria1.and(Dict.BRANCH).is(params.get("branch"));
        }
        Query query1 = new Query(criteria1);
        mongoTemplate.remove(query1, SoapRelation.class);
    }

    @Override
    public List<SopRelation> showSopRelation(InterfaceParamShow interfaceParamShow) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.IS_NEW).is(1);
        if(!"other".equals(interfaceParamShow.getBranchDefault())){
            criteria.and(Dict.BRANCH).is(interfaceParamShow.getBranchDefault());
        }else if(!FileUtil.isNullOrEmpty(interfaceParamShow.getBranch())){
        	criteria.and(Dict.BRANCH).is(interfaceParamShow.getBranch());
        }
        if(!FileUtil.isNullOrEmpty(interfaceParamShow.getServiceId())){
            criteria.and(Dict.SERVICE_ID).is(interfaceParamShow.getServiceId());
        }
        if(!FileUtil.isNullOrEmpty(interfaceParamShow.getServiceCalling())){
            criteria.and(Dict.SERVICE_CALLING).is(interfaceParamShow.getServiceCalling());
        }
        if (!FileUtil.isNullOrEmpty(interfaceParamShow.getTransOrServiceOrOperation())){
        	List<String> searchList = interfaceParamShow.getTransOrServiceOrOperation();
        	Criteria criteria1 = new Criteria().and(Dict.ESB_TRANS_ID).in(searchList);
        	Criteria criteria2 = new Criteria().and(Dict.ESB_SERVICE_ID).in(searchList);
        	Criteria criteria3 = new Criteria().and(Dict.ESB_OPERATION_ID).in(searchList);
        	Criteria criteria4 = new Criteria().and(Dict.INTERFACE_ALIAS).in(searchList);
        	criteria.orOperator(criteria1,criteria2,criteria3,criteria4);
		}
        Query query=new Query(criteria);
        List<SopRelation> mappedResults = mongoTemplate.find(query, SopRelation.class);
        return mappedResults;
    }

    @Override
    public SopRelation getSopRelationById(String id) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.L_ID).in(id);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, SopRelation.class);
    }

    @Override
    public List<SopRelation> getSopRelationList(String appServiceId, String branchName) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_CALLING).is(appServiceId);
        criteria.and(Dict.BRANCH).is(branchName);
        criteria.and(Dict.IS_NEW).is(1);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, SopRelation.class);
    }

    @Override
    public void updateSopRelationIsNewByIds(List<String> idList) {
        Query query = new Query(Criteria.where(Dict.L_ID).in(idList));
        Update update = Update.update(Dict.IS_NEW, 0).set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        mongoTemplate.updateMulti(query, update, SopRelation.class);
    }

    @Override
    public void saveSopRelationList(List<SopRelation> sopRelationList) {
        mongoTemplate.insert(sopRelationList, SopRelation.class);
    }

    @Override
    public List<SopRelation> getSopRelationVer(String serviceId, String interfaceAlias, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_CALLING).is(serviceId);
        criteria.and(Dict.INTERFACE_ALIAS).is(interfaceAlias);
        criteria.and(Dict.BRANCH).is(branch);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        query.skip(0).limit(Constants.FIVE);
        return mongoTemplate.find(query, SopRelation.class);
    }

    @Override
    public void updateSopRelationInterfaceName(List<InterfaceRelationShow> interfaceRelationShowList) {
        List<BathUpdateOption> updateOptionList = new ArrayList<>();
        for (InterfaceRelationShow soapRelation : interfaceRelationShowList) {
            Query query = new Query(Criteria.where(Dict.TRANS_ID).is(soapRelation.getTransId())
                    .and(Dict.SERVICE_CALLING).is(soapRelation.getServiceCalling())
                    .and(Dict.BRANCH).is(soapRelation.getBranch()));
            Update update = Update.update(Dict.INTERFACE_NAME, soapRelation.getInterfaceName())
                    .set(Dict.SERVICE_ID, soapRelation.getServiceId())
                    .set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            updateOptionList.add(new BathUpdateOption(query, update, false, true));
        }
        if (CollectionUtils.isNotEmpty(updateOptionList)) {
            BathUpdateUtil.bathUpdate(mongoTemplate, Dict.INTERFACE_SOP_RELATION, updateOptionList);
        }
    }

    @Override
    public SoapRelation getSoapId(String esbTransId) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.ESB_TRANS_ID).is(esbTransId);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, SoapRelation.class).get(0);
    }

    @Override
    public SopRelation getSopId(String esbTransId) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.ESB_TRANS_ID).is(esbTransId);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, SopRelation.class).get(0);
    }

    @Override
    public List<SoapRelation> addSoapField() {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria);
        return mongoTemplate.find(query, SoapRelation.class);
    }

    @Override
    public List<SopRelation> addSopField() {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria);
        return mongoTemplate.find(query, SopRelation.class);
    }

    @Override
    public void deleteSoap(SoapRelation soapRelation) {
        mongoTemplate.remove(soapRelation);
    }

    @Override
    public void deleteSop(SopRelation sopRelation) {
        mongoTemplate.remove(sopRelation);
    }

    @Override
    public void deleteSopRelation(Map params) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_ID).is(params.get("serviceId"));
        if(!FileUtil.isNullOrEmpty(params.get("branch"))){
            criteria.and(Dict.BRANCH).is(params.get("branch"));
        }
        Query query = new Query(criteria);
        mongoTemplate.remove(query, SopRelation.class);
        Criteria criteria1 = new Criteria();
        criteria1.and(Dict.SERVICE_CALLING).is(params.get("serviceId"));
        if(!FileUtil.isNullOrEmpty(params.get("branch"))){
            criteria1.and(Dict.BRANCH).is(params.get("branch"));
        }
        Query query1 = new Query(criteria1);
        mongoTemplate.remove(query1, SopRelation.class);
    }

	@Override
	public List<Map> queryAllRelation(InterfaceParamShow param, List<String> list) {
		Criteria criteria = new Criteria();
		criteria.and(Dict.SERVICE_CALLING).is(param.getServiceCalling());
		Pattern pattern = Pattern.compile(param.getBranchDefault(), Pattern.CASE_INSENSITIVE);
        criteria.and(Dict.BRANCH).regex(pattern);
		criteria.and(Dict.SERVICE_ID).nin(list);
		Query query = new Query(criteria);
		query.fields().include(Dict.L_ID).include(Dict.SERVICE_ID).include(Dict.TRANS_ID).include(Dict.L_INTERFACE_TYPE);;
		List<Map> restList= mongoTemplate.find(query,Map.class,Dict.INTERFACE_REST_RELATION);
		List<Map> soapList = mongoTemplate.find(query,Map.class,Dict.INTERFACE_SOAP_RELATION);
		List<Map> sopList = mongoTemplate.find(query,Map.class,Dict.INTERFACE_SOP_RELATION);
		restList.addAll(soapList);
		restList.addAll(sopList);
		return restList;
	}

}
