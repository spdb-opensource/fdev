package com.spdb.fdev.fdemand.spdb.dao;


import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.DesignDoc;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.entity.TechType;

import java.util.*;


public interface IDemandBaseInfoDao {

    List query(DemandBaseInfo demandBaseinfo) throws Exception;

    List query2();

    /**
     * 需求录入
     * @param demandBaseinfo
     * @return
     */
    DemandBaseInfo save(DemandBaseInfo demandBaseinfo);

    /**
     * 根据需求类型查询最新科技需求编号
     * @param demandType
     * @return
     */
    DemandBaseInfo queryBydemandType(String demandType);
    
    DemandBaseInfo update(DemandBaseInfo demandBaseinfo) throws Exception;

    DemandBaseInfo updateDesign_status(String demandId,String design_status) throws Exception;

    DemandBaseInfo updateImpl(DemandBaseInfo demandBaseinfo) throws Exception;
    
    DemandBaseInfo updateUploader(String firstUploader,String demandId,String uploader) throws Exception;

    DemandBaseInfo queryById(String id);

    List queryPagination(Integer start, Integer pageSize, String keyword);
    
    Long countOa_contact_no(String oa_contact_no) throws Exception;
    
    Long queryCount(String keyword);

    //查询需求列表；
    //List<DemandBaseInfo> queryDemandList() throws Exception;   
    //按条件查询需求列表
    List<DemandBaseInfo> queryDemandList(String isCodeCheck, String userid, String keyword,
            Map delayDate,
            List<String> groupid,
            List<String> state, List<HashMap> stateListMap,
            Map featureDate,
            String type, String sortBy, Boolean descending,
            String priority,
            Map relDateMap,
            String groupState,
            String designState,List<String> demand_label) throws Exception;

    List<DemandBaseInfo> queryDemandBaseInfos(String isCodeCheck, Integer start, Integer pageSize,
            String userid, String keyword,
            Map delayDate,
            List<String> groupid,
            List<String> state, List<HashMap> stateListMap,
            Map featureDate,
            String type, String sortBy, Boolean descending,
            String priority,
            Map relDateMap,
            String groupState,
            String designState,List<String> demand_label)throws Exception;
    
    List<FdevImplementUnit> queryAvailableIpmpUnit();

    List<FdevImplementUnit> queryAvailableIpmpUnit(HashSet<String> set);

    DemandBaseInfo queryDemandByImplUnitsId(String implUnitsId);

    /**
     * 根据组id和优先级查询需求
     *
     * @param groupids 组ids
     * @param priority 优先级
     * @param isParent 是否包含子组
     * @return
     * @throws Exception
     */
    List<DemandBaseInfo> queryDemandByGroupIdAndPriority(Set<String> groupids, String priority, Boolean isParent) throws Exception;

    /**
     * 根据组ids来查询需求
     *
     * @param groupId
     * @return
     */
    List<DemandBaseInfo> queryDemandByGroupId(Set<String> groupId);

    /**
     * 通过ids来查询需求
     *
     * @param ids
     * @return
     */
    List<DemandBaseInfo> queryDemandByIds(Set<String> ids);

    DemandBaseInfo queryDemandByImplUnitsIdAndPriority(String implUnitsId, String priority);

    List<DemandBaseInfo> queryDemandByGroupId(List<String> groupIds);
    
    
    Long queryDemandsCount(String isCodeCheck,String userid, String keyword,
            Map delayDate,
            List<String> groupid,
            List<String> state, List<HashMap> stateListMap,
            Map featureDate,
            String type, String sortBy, Boolean descending,
            String priority,
            Map relDateMap,
            String groupState,
            String designState,List<String> demand_label) throws Exception;

    DemandBaseInfo updateRqrmntUiReporter(String demandId, String uiDesignReporter);

    Long updateDesignAndMap(String rqrId, String newStatus, Map<String, List<Map<String, String>>> designMap, String id);

    /**
     * 更新审核意见
     *
     * @param rqrmntId
     * @param desingRemark
     * @return
     */
    Long updateDesignRermark(String rqrmntId, String desingRemark);

    List<DemandBaseInfo> queryTaskForDesign();

    long updateDesignDoc(String id, DesignDoc designDoc);
    
    List<DemandBaseInfo> queryReviewDetailList(String ui_verify_user, List<String> demand_leader_group);
    
    List<DemandBaseInfo> queryReviewDetailLists(String ui_verify_user, List<String> groupIds, String startDate, String endDate);
    
    public Map queryGroup(Map param) throws Exception ;

    //查询这些小组下所有的已投产和已归档的需求
    long countDemandByGroup(Set<String> groupIdsOne, String startDate, String endDate);
    
    List<DemandBaseInfo> queryImpingDemand(List<String> groupIdList);

    /**
     * 根据组ids来查询评估超过14天的需求
     *
     * @param groupId
     * @return
     */
    List<DemandBaseInfo> queryDemandByGroupIdAssessOver(Set<String> groupId,int todayLast);
    /**
     * 根据oaContactNo来查询的需求
     *
     * @param oaContactNo
     * @return DemandBaseInfo
     */
    DemandBaseInfo queryByOaContactNo(String oaContactNo);

    DemandBaseInfo updateDemandBaseInfo(DemandBaseInfo demandBaseInfo) throws Exception;

    List<TechType> queryTechType(Map<String, Object> param) throws Exception;

    DemandBaseInfo queryByOaContactNoAndStatus(String oaContactNo,int status);

    List<DemandBaseInfo> queryDemandByCodeOrderNo(String no);

    /**
     * 按需求编号/名称 模糊查询需求列表
     *
     * @param demandKey
     * @return DemandBaseInfo
     */
    List<DemandBaseInfo> queryDemandBaseList(String demandKey) ;
    /**
     * 按需求编号/名称 模糊查询需求列表
     *
     * @param ids
     * @return DemandBaseInfo
     */
    List<DemandBaseInfo> queryByIds(List<String> ids);


    List<DemandBaseInfo> queryByKeyWord(String keyWord);
}
