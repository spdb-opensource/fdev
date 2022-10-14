package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.Blueking;

import java.util.List;
import java.util.Map;


public interface IBluekingDao {

    //添加蓝鲸相关数据
    void addAll(List<Blueking> data);

    //删除蓝鲸blueking表数据
    void dropCollection(String collectionName);

    //查询deployment列表
    List<Blueking> queryDeploymentList(String name);

    //查询应用内存限制
    List<Blueking> queryLimits(List<String> deploymentSet);

    //查询详情
    List<Map<String, Object>> queryBluekingDetail(String deployment, String area, String cluster, String type);

}
