package com.spdb.fdev.fdevinterface.spdb.dao;

import com.spdb.fdev.fdevinterface.spdb.entity.TransRelation;
import com.spdb.fdev.fdevinterface.spdb.vo.TransParamShow;

import java.util.List;
import java.util.Map;

public interface TransRelationDao {

    void save(List<TransRelation> transRelationList);

    void delete(String serviceCalling, String branch, String channel);

    Map showTransRelation(TransParamShow paramShow);

    void deleteTransRelation(Map params);

}
