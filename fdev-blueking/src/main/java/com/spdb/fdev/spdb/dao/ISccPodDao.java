package com.spdb.fdev.spdb.dao;

import com.spdb.fdev.spdb.entity.SccPod;

import java.util.List;

/**
 * @Author:guanz2
 * @Date:2021/10/3-17:42
 * @Description: scc_pod
 */
public interface ISccPodDao {

    //插入数据
    void addAll(List<SccPod> list);

    //删除集合
    void dropCollection(String collectionName);

    long queryCountByCondition(String cluster);

    long queryCountByCondition(List clusters);

    long queryClusterCount(String cluster);

    long queryClusterCount(List clusters);

    /*
    * @author:guanz2
    * @Description:查询一个确定应用的所有pod
    */
    List queryPodsByCondition(String namespace_code , String owner_code);

    List<String> getDistinctField(String field);
}
