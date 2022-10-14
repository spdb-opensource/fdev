package com.gotest.dao;

import com.gotest.domain.PlanList;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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
    PlanList selectByPlanId(@Param("planId") Integer planId) throws Exception;

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
    int selectByWorkNoByPlanNameCount(@Param(value = "workNo") String workNo, @Param(value = "planName") String planName) throws Exception;

    /**
     * 根据工单编号查询当前工单下的计划
     * @param workNo
     * @return
     */
    List<PlanList> queryByworkNo(@Param("workNo") String workNo) throws Exception;
    /**
     * 判断该测试案例是否已经执行失败
     * @param planlist_testcase_id
     * @return
     * @throws Exception
     */
	String isTestcaseAddIssue(@Param(value = "planlist_testcase_id") String planlist_testcase_id) throws Exception;

	int updateWorkNoByPlanId(@Param(value = "planId")Integer planId,@Param("workNo")String workNo);

	int updatePlanNameByPlanId(@Param(value = "planId")Integer planId,@Param("planName")String planName);

	int updateWorkNoByWorkNos(@Param(value = "workNos")List<String> workNos,@Param(value = "workNo")String workNo);

    /**
     * 根据测试计划id查询测试案例
     * @param planId
     * @return
     */
    List<Map> queryTestcaseByPlanId(@Param("planId") Integer planId , @Param("start_page") Integer start_page, @Param("page_size") Integer page_size);

    // //根据实施单元号查询计划
    List<PlanList> queryPlanByUnitNo(@Param("unitNo") String unitNo, @Param("start_page") Integer start_page, @Param("page_size") Integer page_size);

    //工单查询计划
    List<PlanList> queryPlanByworkNo(@Param("workOrderNo") String workOrderNo, @Param("start_page") Integer start_page, @Param("page_size") Integer page_size);

    //查询计划总数
    Integer queryPlanCountByUnitNo(@Param("unitNo") String unitNo);
}