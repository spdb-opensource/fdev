package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.dto.testorder.TestOrderQueryDto;
import com.spdb.fdev.fdemand.spdb.entity.TestOrder;
import com.spdb.fdev.fdemand.spdb.vo.PageVo;

import java.util.List;

public interface ITestOrderDao {
    /**
     * 根据id查询提测单
     * @throws Exception
     */
    TestOrder queryTestOrderById(String id) throws Exception ;
    /**
     * 根据研发单元编号查询提测单
     * @throws Exception
     */
    List<TestOrder> queryTestOrderByFdevUnitNo(String fdev_implement_unit_no) throws Exception;
    /**
     * 根据实施单元编号查询提测单
     * @throws Exception
     */
    List<TestOrder> queryTestOrderByImplUnitNo(String implUnitNum) throws Exception;
    /**
     * 新增提测单
     * @throws Exception
     */
    void addTestOrder(TestOrder testOrder) throws Exception ;
    /**
     * 撤销提测单
     * @throws Exception
     */
    TestOrder deleteTestOrder(String id,String userId,String date) throws Exception ;
    /**
     * 归档提测单
     * @throws Exception
     */
    TestOrder fileTestOrder(String id,String date) throws Exception;
    /**
     * 修改提测单
     * @throws Exception
     */
    void updateTestOrder(TestOrder testOrder) throws Exception ;

    /**
     * 查询当天的提测单列表
     * @throws Exception
     */
    TestOrder queryByCreateTime(String todayDate) throws Exception ;

    /**
     * 修改提测单
     * @throws Exception
     */
    PageVo<TestOrder> queryTestOrder(TestOrderQueryDto dto) throws Exception ;

    /**
     * 获取所有提测状态为 已提测的科技需求提测单
     * @throws Exception
     */
    List<TestOrder> queryTechTestOrder() throws Exception ;
    /**
     * 根据需求ID查询提测单
     * @throws Exception
     */
    List<TestOrder> queryTestOrderByDemandId(String demandId) throws Exception ;

}
