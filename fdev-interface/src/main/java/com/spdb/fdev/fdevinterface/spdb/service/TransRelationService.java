package com.spdb.fdev.fdevinterface.spdb.service;

import com.spdb.fdev.fdevinterface.spdb.entity.TransRelation;
import com.spdb.fdev.fdevinterface.spdb.vo.TransParamShow;

import java.util.List;
import java.util.Map;

public interface TransRelationService {

    void save(List<TransRelation> transRelationList, String appServiceId, String branchName, String channel);

    Map showTransRelation(TransParamShow paramShow);

    void deleteTransRelationData(Map params);

}
