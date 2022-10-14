package com.spdb.fdev.fdemand.spdb.dao;


import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface IImplementUnitDao {

    /**
     * 根据需求id分页查询实施单元
     *
     * @param start
     * @param pageSize
     * @param demand_id
     * @return
     */
    List queryPaginationByDemandId(Integer start, Integer pageSize, String demand_id);

    /**
     * 根据需求id查询研发单元总数
     *
     * @param demand_id
     * @return
     */
    Long queryCountByDemandId(String demand_id);

    /**
     * 根据实施单元编号分页查询研发单元
     *
     * @param start
     * @param pageSize
     * @param ipmp_unit_no
     * @return
     */
    List queryPaginationByIpmpUnitNo(String demandId, Integer start, Integer pageSize, String ipmp_unit_no, String group);

    /**
     * 根据实施单元编号查询研发单元总数
     *
     * @param ipmp_unit_no
     * @return
     */
    Long queryPaginationByIpmpUnitNo(String demandId, String ipmp_unit_no, String group);

    /**
     * 新增研发单元
     *
     * @param fdevImplementUnit
     */
    FdevImplementUnit save(FdevImplementUnit fdevImplementUnit);

    /**
     * 查询研发单元的总条数
     *
     * @return
     */
    long queryCountAll();

    /**
     * 根据创建时间查询最新研发单元
     *
     * @param todayDate
     * @return
     */
    FdevImplementUnit queryByCreatetime(String todayDate);



    /** 更新研发单元
     *
     * @param fdevImplementUnit
     */
    void update(FdevImplementUnit fdevImplementUnit) throws Exception;

    /**
     * 根据id查询研发单元内容
     *
     * @param id
     * @return
     */
    FdevImplementUnit queryById(String id);

    /**
     * 根据id删除
     *
     * @param id
     */
    void deleteById(String id);

    /**
     * 根据板块和需求id查询本板块的一条数据
     *
     * @param demandId,group
     * @return
     */
    FdevImplementUnit queryByDemandIdAndGroupOne(String demandId,String group);


    /**
     * 根据需求id查询所有实施单元列表
     * @param demandId
     * @return
     */
    List<FdevImplementUnit> queryByDemandId(String demandId);

    /**
     * 根据研发单元编号查询研发单元
     */
    FdevImplementUnit queryByFdevNo(String fdevNo);

    /**
     * 根据Fdev实施单元编号查询实施单元
     * @param unitNo
     * @return
     */
    FdevImplementUnit queryByUnitNo(String unitNo);

    /**
     * 根据板块和需求id查询本板块
     *
     * @param demandId,group
     * @return
     */
    List<FdevImplementUnit> queryByDemandIdAndGroup(String demandId,String group);

/**
     * 根据组id查询实施单元
     * @param groupIds
     * @return
     */
    List<FdevImplementUnit>  queryImplUnitByGroupId(Set<String> groupIds);

    /**
     * 通过ipmpImplementUnitNo查询实施单元
     * @param ipmpImplementUnitNo
     * @return
     */
    List<FdevImplementUnit> queryImplUnitByipmpImplementUnitNo(String ipmpImplementUnitNo);

    String queryRealStart(String demandId, Integer status);

    /**
     * 根据需求id查询待实施及以后的实施单元
     * @param demandId
     * @return
     */
    List<FdevImplementUnit> queryImplPreImplmentByDemandId(String demandId);

    //根据需求id和小组id查询实施单元补充的最大值
    FdevImplementUnit queryByDemandIdAndGroupMax(String demandId, String partId);

    /**
     * 根据需求id查询所有实施单元列表,去掉暂缓和已撤销
     * @param demandId
     * @return List<FdevImplementUnit>
     */
    List<FdevImplementUnit> queryFdevImplementUnitByDemandId(String demandId);

    /**
     * 根据IPMP实施单元编号查询研发单元
     * @param implUnitNum
     * @return List<FdevImplementUnit>
     */
    List<FdevImplementUnit> queryByImplUnitNum(String implUnitNum,String taskNum);

    /**
     * 查询没有挂载实施单元的研发单元
     * @return List<FdevImplementUnit>
     */
    List<FdevImplementUnit> queryFdevImplementUnit();

    /**
     * 查询暂缓的研发单元
     *
     * @param demandId
     * @return
     * @throws Exception
     */
    List<FdevImplementUnit> queryRecoverFdevUnitByDemandId(String demandId);
    /**
     * 查询恢复中的研发单元
     *
     * @param demandId
     * @return
     * @throws Exception
     */
    List<FdevImplementUnit> queryDeferFdevUnitByDemandId(String demandId);
    /**
     * 查询撤销的研发单元
     *
     * @param demandId
     * @return
     * @throws Exception
     */
    List<FdevImplementUnit> queryCanceledFdevUnitByDemandId(String demandId);
    /**
     * 根据其他需求任务编号查询研发单元
     *
     * @return
     * @throws Exception
     */
    List<FdevImplementUnit> queryByOtherDemandTaskNum(String taskNum) ;
    /**
     * 日常需求查询研发单元
     *
     * @return
     * @throws Exception
     */
    List<FdevImplementUnit> queryDailyFdevUnitList(String demandId, String group,String other_demand_task_num) ;

    /**
     * 根据研发单元编号批量查询研发单元信息
     * @param unitNos
     * @return
     */
    List<Map> queryFdevUnitByUnitNos(List<String> unitNos);

    List<FdevImplementUnit> queryAllFdevUnitByDemandId(String demandId);

    /**
     * 查询研发单元列表
     * @return
     */
    Map<String,Object> queryFdevUnitList(String dateTypePlan, String dateTypeReal, List<String> groupIds, String keyword, String demandType, String demandKey, List<String> demandIds, String ipmpUnitNo, String otherDemandTaskNum, List<String> userIds, List<String> states, Integer size, Integer index);
    /**
     * 查询研发单元列表（编号/内容 模糊搜索）
     * @return
     */
    List<FdevImplementUnit> queryFdevUnitList(String fdevUnitKey,List<String> fdevUnitLeaderIds,List<String> fdevUnitNos,List<String> groupIds);
    /**
     * 获取提交业测或开始，即将延期或延期的研发单元
     * @return
     */
    List<FdevImplementUnit> queryFdevUnitOverdueList(String date,String type,String gt);

    /**
     * 获取提测当天但是还未提测的研发单元
     * @return
     */
    List<FdevImplementUnit> queryFdevUnitWarnOverdueList();


}
