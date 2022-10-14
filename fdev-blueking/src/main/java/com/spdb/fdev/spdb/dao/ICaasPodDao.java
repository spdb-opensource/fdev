package com.spdb.fdev.spdb.dao;

import com.spdb.fdev.spdb.entity.CaasPod;

import java.util.List;

/**
 * @Author:guanz2
 * @Date:2021/9/29-16:26
 * @Description:
 */
public interface ICaasPodDao {

    void addAll(List<CaasPod> listPod);

    void dropCollection(String collectionName);

    List<CaasPod> queryCaasPodCondition(String deployment);

    List<CaasPod> queryCaasPodCondition(String deployment, String cluster, String namespace);

    long queryCountByCondition(String cluster);
}
