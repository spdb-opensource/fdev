package com.spdb.fdev.fdevinterface.spdb.dao;

import com.spdb.fdev.fdevinterface.spdb.entity.EsbRelation;

import java.util.List;

public interface EsbRelationDao {

    void saveEsbRelation(List<EsbRelation> esbRelationList);

    void deleteEsbRelation();

    List<EsbRelation> getEsbRelationByProviderMsgType(List<String> serviceAndOperationIds);

    List<EsbRelation> getEsbRelationByConsumerMsgType(List<String> serviceAndOperationIds);

    List<EsbRelation> getEsbRelationByTranId(List<String> transIds);

    EsbRelation getEsbRelation(String transId, String consumerMsgType);

    List<EsbRelation> getEsbRelation(String transId);



}
