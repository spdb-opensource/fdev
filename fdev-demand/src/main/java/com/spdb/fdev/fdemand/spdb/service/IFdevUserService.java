package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IFdevUserService {

    Map queryUserInfo(String id);

    Map<String,String> queryGroup();

    Map<String,Map> queryByUserCoreData(Set<String> ids , Set<String> userEns) throws Exception;

    Map addCommissionEvent(DemandBaseInfo demand, List<String> userId, String type, String link, String description );
    /**
     * 查询所有条线
     */
    List<Map<String, String>> queryAllSection();
    /**
     * 查询所有二级小组
     */
    List<Map<String, Object>> queryGroupTwoAll() ;
    /**
     * 根据id查询第三级小组
     */
    Map<String, Object> getThreeLevelGroup(String id );
}
