package com.spdb.fdev.fdemand.spdb.service;


import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


public interface IImplementUnitService {

    Map queryPaginationByDemandId(Map<String, Object> param) throws Exception;

    FdevImplementUnit add(FdevImplementUnit fdevImplementUnit) throws Exception;

    void update(FdevImplementUnit fdevImplementUnit) throws Exception;

    FdevImplementUnit supply(FdevImplementUnit fdevImplementUnit) throws Exception;

    void deleteById(Map<String, Object> param) throws Exception;

    List<FdevImplementUnit> queryByDemandId(String demandId);

	/**
     * 研发单元的评估
     * 同步更新需求的状态，若该需求下的研发单元仍有待评估的，则不动。若研发单元均已进入开发中及之后的状态，则以最前的为准。
     * @param param
     * @throws Exception
     */
    void assess(Map<String, Object> param) throws Exception;

	
    Map queryByFdevNo(Map<String, Object> param);

    void updateIpmpUnitForTask(Map<String, Object> param) throws Exception;

    List<FdevImplementUnit> queryAvailableIpmpUnit(Map<String, Object> param) throws Exception;

    List<FdevImplementUnit> queryAvailableIpmpUnitNew(Map<String, Object> param) throws Exception;

    /**
     * 点击恢复后，则可以修改
     * @param param
     * @throws Exception
     */
	void updateByRecover(Map<String, Object> param) throws Exception;

	Map<String, Object> queryByFdevNoAndDemandId(Map<String, Object> param);

    List<FdevImplementUnit>  queryImplByGroupAndDemandId(Map<String, Object> param) throws Exception;

	List<FdevImplementUnit> queryImplUnitByGroupId(String id, Boolean isParent) throws Exception;

    List<FdevImplementUnit> queryImplUnitByipmpImplementUnitNo(String ipmpImplementUnitNo);

    //根据需求id查询待实施及之后的实施单元
    List<FdevImplementUnit> queryImplPreImplmentByDemandId(String demandId);

    FdevImplementUnit queryFdevImplUnitDetail(Map<String, Object> param);

    //挂载
    void mount(Map<String, Object> param) throws Exception;

    Map queryPaginationByIpmpUnitNo(Map<String, Object> param) throws Exception;

    //判断评估完成按钮亮不亮，给出不亮的原因
    Map assessButton(Map<String, Object> param) throws Exception;

    //计算剩余可填工时
    Map checkWork(Map<String, Object> param) throws Exception;

    String isShowAdd(Map<String, Object> param) throws Exception;

    List<FdevImplementUnit> queryDailyFdevUnitList(Map<String, Object> param) throws Exception ;

    List<Map> queryFdevUnitByUnitNos(List<String> unitNos);

    /**
     * 查看研发单元列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    Map<String, Object> queryFdevUnitList(Map<String, Object> params) throws Exception ;
    /**
     * 导出研发单元列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    void exportFdevUnitList(Map<String, Object> params, HttpServletResponse resp) throws Exception ;

    /**
     * 研发单元即将超期 超期提醒邮件 每天早上9点
     * 邮件提醒研发单元是否提交业测延期、邮件提醒研发单元是否提交业测即将延期、邮件提醒研发单元是否开发延期、邮件提醒研发单元是否开发即将延期
     * @param params
     * @return
     * @throws Exception
     */
    void fdevUnitOverdueEmail(Map<String, Object> params) throws Exception;
    /**
     * 更新需求的涉及小组评估状态
     */
    void updateDemandAssessStatus(DemandBaseInfo demandBaseInfo) ;

}
