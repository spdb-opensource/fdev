package com.plan.service;

import java.util.List;
import java.util.Map;

import com.plan.datamodel.PlanList;

public interface PlanListService {

    /**
     * 根据主键删除
     * @param map
     * @return 受影响行数
     */
    int deleteByPlanId(Map map) throws Exception;

    /**
     * 新增一个计划
     * @param record
     * @return 受影响行数
     */
    int insert(PlanList record)  throws Exception;

    /**
     * 根据主键查询
     * @param planId
     * @return 返回一个条数据
     */
    PlanList selectByPlanId(Integer planId) throws Exception;

    /**
     * 查询全部计划
     * @return 返回所有计划
     * @throws Exception 
     */
    List<PlanList> selectAll() throws Exception;

    /**
     * 根据主键修改一条复合的数据
     * @param record
     * @return 受影响行数
     */
    int updateByPrimaryKey(PlanList planList) throws Exception;

    /**
     * 根据条件查询总数
     * @param work_no 工单编号
     * @param PlanName 计划名称
     * @return 总数
     */
    int selectByWorkNoByPlanNameCount(String work_no,String PlanName)throws Exception;

    /**
     * 根据工单编号查询当前工单下的计划
     * @param workNo
     * @return
     */
    List<PlanList> queryByworkNo(String workNo) throws Exception;

    /**
     * 根据执行计划id修改工单编号
     * @param workNo
     * @param planId
     * @return
     */
    Integer updateWorkNoByPlanId(String workNo, Integer planId) throws Exception;
}
