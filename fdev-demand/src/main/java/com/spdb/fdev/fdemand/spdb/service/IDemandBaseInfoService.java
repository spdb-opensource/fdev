package com.spdb.fdev.fdemand.spdb.service;


import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.TechType;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface IDemandBaseInfoService {
    List query(DemandBaseInfo demandBaseInfo) throws Exception;

    List<Map<String, Object>> queryForSelect() throws Exception;

    /**
     * 查询科技业务需求
     */
    List<Map<String, Object>> queryTechBusinessForSelect() throws Exception;

    /**
     * 需求录入
     * @param demandBaseInfo
     */
    DemandBaseInfo save(DemandBaseInfo demandBaseInfo) throws Exception;

    DemandBaseInfo update(DemandBaseInfo demandBaseInfo) throws Exception;

    DemandBaseInfo updateImpl(DemandBaseInfo demandBaseInfo) throws Exception;

    DemandBaseInfo queryById(String id) throws Exception;

    Map queryPagination(Map<String, Object> param);

    Long queryCount(String keyword);

    /**
     * 查询需求id对应的需求数量
     * @param oa_contact_no
     * @return
     * @throws Exception
     */
    Long demandCount(String oa_contact_no) throws Exception;

    void defer(Map<String, Object> param) throws Exception;

    void recover(Map<String, Object> param) throws Exception;
    /**
     * 需求归档
     * @param param
     */
    void placeOnFile(Map<String, Object> param) throws Exception;

    void repeal(Map<String, Object> param) throws Exception;

	List<Map<String,Object>> queryTaskByDemandId(Map<String, Object> param);

	//按条件查询需求列表
    Map queryDemandList(Map map) throws Exception;

    /**
     * 通过ids 来查询需求
     *
     * @param ids
     * @return
     * @throws Exception
     */
    List<DemandBaseInfo> queryDemandByIds(Set<String> ids) throws Exception;

    List<DemandBaseInfo> queryDemandByGroupId(List<String> groupIds);

    List<DemandBaseInfo> queryDemandByGroupIdAndPriority(Set<String> groupids, String priority, Boolean isParent) throws Exception;

    List<DemandBaseInfo> queryDemandByGroupId(Set<String> groupId);

    DemandBaseInfo queryDemandByImplUnitsIdAndPriority(String implUnitsId, String priority);

    DemandBaseInfo queryDemandByImplUnitsId(String implUnitsId);

    DemandBaseInfo updateRqrmntUiReporter(String rqrId, String uiDesignReporter);

    void sendUsersToDo(DemandBaseInfo demand, List<String> userIds, String type);

    void updateDemandForTask(Map<String, Object> param) throws Exception;

    DemandBaseInfo calcWorkload(DemandBaseInfo demandBaseInfo);

    Map<String, Object> queryPartInfo(Map<String, Object> param) throws Exception;

    List<DemandBaseInfo> queryDemandByGroupIdAssessOver(Set<String> groupIds,int todayLast);

    List<TechType> queryTechType(Map<String, Object> param) throws Exception;

    void updateDemandCodeOrderNo(Map<String, Object> param) throws Exception;

    List<DemandBaseInfo> getDemandsInfoByIds(Map<String, Object> param);

    List<String> queryDemandFile(String demandId) throws Exception;
}
