package com.plan.dao;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.plan.datamodel.PlanList;

@Repository
public interface PlanListMapper {

    /**
     * 根据主键删除
     * @param planId
     * @return 受影响行数
     */
    int deleteByPlanId(@Param("planId") Integer planId) throws Exception;

    /**
     * 新增一个计划
     * @param record
     * @return 受影响行数
     */
    int insert(PlanList record) throws Exception;

    /**
     * 根据主键查询
     * @param planId
     * @return 返回一个条数据
     */
    PlanList selectByPlanId(Integer planId) throws Exception;

    /**
     * 查询全部计划
     * @return 返回所有计划
     */
    List<PlanList> selectAll() throws Exception;

    /**
     * 根据主键修改一条复合的数据
     * @param record
     * @return 受影响行数
     */
    int updateByPrimaryKey(PlanList record) throws Exception;

    /**
     * 根据条件查询总数
     * @param workNo 工单编号
     * @param planName 计划名称
     * @return 总数
     */
    int selectByWorkNoByPlanNameCount(@Param(value = "workNo")String workNo,@Param(value = "planName")String planName) throws Exception;

    /**
     * 根据工单编号查询当前工单下的计划
     * @param workNo
     * @return
     */
    List<PlanList> queryByworkNo(String workNo) throws Exception;
    /**
     * 判断该测试案例是否已经执行失败
     * @param planlist_testcase_id
     * @return
     * @throws Exception
     */
	String isTestcaseAddIssue(@Param(value = "planlist_testcase_id")String planlist_testcase_id) throws Exception;
}