package com.spdb.fdev.fdevinterface.spdb.dao.impl;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.spdb.dao.EsbRelationDao;
import com.spdb.fdev.fdevinterface.spdb.entity.EsbRelation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EsbRelationDaoImpl implements EsbRelationDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveEsbRelation(List<EsbRelation> esbRelationList) {
        mongoTemplate.insert(esbRelationList, Dict.INTERFACE_ESB_RELATION);
    }

    @Override
    public void deleteEsbRelation() {
        mongoTemplate.dropCollection(EsbRelation.class);
    }

    @Override
    public List<EsbRelation> getEsbRelationByProviderMsgType(List<String> serviceAndOperationIds) {
        Criteria criteria = new Criteria();
        if (CollectionUtils.isNotEmpty(serviceAndOperationIds)) {
            criteria.and(Dict.SERVICE_AND_OPERATION_ID).in(serviceAndOperationIds);
            criteria.and(Dict.PROVIDER_MSG_TYPE).is(Dict.SOAP);
        }
        return mongoTemplate.find(new Query(criteria), EsbRelation.class);
    }

    @Override
    public List<EsbRelation> getEsbRelationByConsumerMsgType(List<String> serviceAndOperationIds) {
        Criteria criteria = new Criteria();
        if (CollectionUtils.isNotEmpty(serviceAndOperationIds)) {
            criteria.and(Dict.SERVICE_AND_OPERATION_ID).in(serviceAndOperationIds);
            criteria.and(Dict.CONSUMER_MSG_TYPE).is(Dict.SOAP);
        }
        return mongoTemplate.find(new Query(criteria), EsbRelation.class);
    }

    @Override
    public List<EsbRelation> getEsbRelationByTranId(List<String> transIds) {
        Criteria criteria = new Criteria();
        if (CollectionUtils.isNotEmpty(transIds)) {
            criteria.and(Dict.TRAN_ID).in(transIds);
            criteria.and(Dict.CONSUMER_MSG_TYPE).is(Dict.SOP);
        }
        return mongoTemplate.find(new Query(criteria), EsbRelation.class);
    }

    @Override
    public EsbRelation getEsbRelation(String transId, String consumerMsgType) {
        Criteria criteria = new Criteria();
        if (Dict.SOAP.equals(consumerMsgType)) {
            criteria.and(Dict.SERVICE_AND_OPERATION_ID).is(transId);
        } else {
            criteria.and(Dict.TRAN_ID).is(transId);
        }
        criteria.and(Dict.CONSUMER_MSG_TYPE).is(consumerMsgType);
        return mongoTemplate.findOne(new Query(criteria), EsbRelation.class);
    }

    @Override
    public List<EsbRelation> getEsbRelation(String transId) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SERVICE_AND_OPERATION_ID).is(transId);
        return mongoTemplate.find(new Query(criteria), EsbRelation.class);
    }

}
