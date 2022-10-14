package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ICommonBusinessService {
    /**
     * 根据需求id查询所有的实施单元，并修改实施单元的状态
     * 正常：已归档
     * 特殊：暂缓、恢复中、恢复完成
     *
     * @param demandId
     * @param status
     * @param field
     */
    void updateUnitStatus(String demandId, int status, String field) throws Exception;

    void updateUnitDate(String demandId, String date, String field) throws Exception;

    /**
     * 实施单元日期修改，则同步修改需求对应的日期
     * 若是字段为计划启动日期，则需要判断需求中该字段是否有值，若有值，则判断参数是否比需求中的早，若是，
     * 则更新该值，反之，则无需修改，若无值，则更新
     * 若是字段为计划提交内测、提交业测、用户测试完成、用户测试完成日期，则判断参数是否比需求中的晚，若是，
     * 则更新，反之，不更新，若无值，则更新
     *
     * @param demandId
     * @param planStartDate
     * @param planInnerTestDate
     * @param planTestDate
     * @param planTestFinishDate
     * @param planProductDate
     */
    DemandBaseInfo updateDemandDateByImp(String demandId, Integer demandStatusNormal, String planStartDate, String planInnerTestDate, String planTestDate,
                               String planTestFinishDate, String planProductDate, Integer demandStatusSpecial) throws Exception;

    /**
     * 生成玉衡工单
     * @param unitNo fdev实施单元编号
     * @param internalTestStart 计划开始Sit时间
     * @param internalTestEnd 计划开始Uat时间
     * @param expectedProductDate 计划pro投产时间
     * @param requireRemark 工单备注
     * @param group_id 组id
     * @param group_name 组中文名
     */
    void createYuhengOrder(String unitNo, String internalTestStart, String internalTestEnd,
                           String expectedProductDate, String requireRemark, String group_id,String group_name);


    //根据需求id查询所有实施单元的日期，并返回最早的计划启动日期，最晚的内测/业测/rel/投产日期
    HashMap<String,String> dateByDemandIdMap(String demandId);

	void deleteOrder(String demandId);

    /**
     * 根据实施单元编号，修改玉衡的计划日期
     * @param unitNo
     * @param internalTestStart ：计划提交sit的时间
     * @param internalTestEnd ：计划提交uat的时间
     * @param expectedProductDate : 计划投产的时间
     */
    void updateYuhengPlanDate(String unitNo, String internalTestStart, String internalTestEnd, String expectedProductDate);
    //根据实施单元编号删除玉衡工单
    void deleteImpl(String fdevid);

    //根据需求id查询评估完成的研发单元的日期，并返回最早的计划启动日期，最晚的内测/业测/rel/投产日期
    HashMap<String,String> dateByDemandIdMapAssessOver(String demandId);

    Map<String,Object> testPlanQuery(String taskId );

    String queryWorkNoByTaskId(String taskId );

    Map<String,Object> exportSitReportData( String workNo ) ;

    Map<String,Object> queryOrderByOrderNo( String workNo ) ;

    List<Map> queryInnerTestData(String demandNo);
}
