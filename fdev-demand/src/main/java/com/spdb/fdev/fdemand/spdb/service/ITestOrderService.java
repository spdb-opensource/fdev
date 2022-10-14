package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.dto.testorder.TestOrderDto;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import com.spdb.fdev.fdemand.spdb.entity.TestOrder;
import com.spdb.fdev.fdemand.spdb.entity.TestOrderFile;
import com.spdb.fdev.fdemand.spdb.vo.PageVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ITestOrderService {
    /**
     * 创建提测单
     *
     * @throws Exception
     */
    void addTestOrder(TestOrder testOrder, MultipartFile[] requirementSpecification,
                      MultipartFile[] demandInstructionBook, MultipartFile[] otherFiles) throws Exception ;
    /**
     * 提交提测单
     *
     * @throws Exception
     */
    void submitTestOrder(Map<String, Object> params) throws Exception ;
    /**
     * 撤销提测单
     *
     * @throws Exception
     */
    void deleteTestOrder(Map<String, Object> params) throws Exception ;
    /**
     * 修改提测单
     *
     * @throws Exception
     */
    void updateTestOrder(TestOrder testOrder) throws Exception ;
    /**
     * 复制按钮控制
     *
     * @throws Exception
     */
    Map<String,Object> queryCopyFlag(Map<String, Object> params) throws Exception ;
    /**
     * 查询云测试平台测试人员
     *
     * @throws Exception
     */
    List getTestManagerInfo(Map<String, Object> params) throws Exception ;

    /**
     * 查询提测单列表
     * @param dto
     * @return
     * @throws Exception
     */
    PageVo<TestOrder> queryTestOrderList(TestOrderDto dto) throws Exception ;

    TestOrder queryTestOrder(String id) throws Exception ;

    void uploadTestOrderFile(MultipartFile[] files, String testOrderId, String fileType) throws Exception ;

    List<TestOrderFile> queryTestOrderFile(String testOrderId) throws Exception ;

    void deleteTestOrderFile(List<String> ids) throws Exception ;

    /**
     * 根据需求id查询研发单元
     * @param dto
     * @return
     * @throws Exception
     */
    List<FdevImplementUnit> queryFdevUnitListByDemandId(String demandId) throws Exception ;

    List<Map<String, Object>> queryInnerTestTab(List<String> fdevUnitNos) throws Exception ;
    /**
     * 业务需求归档提测单
     * @throws Exception
     */
    void businessFileTestOrder(IpmpUnit ipmpUnit) throws Exception ;
    /**
     * 研发单元提测提醒邮件
     * @return
     * @throws Exception
     */
    void fdevUnitWarnDelay() throws Exception ;
    /**
     * 科技需求归档提测单
     * @throws Exception
     */
    void getTechReqTestInfo() throws Exception;
}
