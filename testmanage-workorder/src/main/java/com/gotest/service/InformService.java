package com.gotest.service;

import com.gotest.domain.FdevSitMsg;
import com.gotest.domain.MessageFdev;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 消息通知数据接口
 */
public interface InformService {


    List<MessageFdev> queryMessage(Map map) throws Exception;

    Map queryMsgCountByUserEnName(Map map) throws Exception;

    Map updateOneMsgByMsgId(Map map) throws Exception;

    Map updateAllMsgByUserEnName(Map map) throws Exception;

    Collection<String> addMsgFromFdev(Map map) throws Exception;

    List<MessageFdev> queryMessageRecord(Map map) throws Exception;

    Map queryMessageRecordCount(Map map) throws Exception;
    
    Collection<String> queryAllUser(Map map) throws Exception;

    String addFdevSitMsg(Map<String, Object> map) throws Exception;

    String addFdevSitMsg(FdevSitMsg fdevSitMsg) throws Exception;

    Map queryFdevSitMsg(Map map) throws  Exception;

    Map<String, Object> querySitMsgDetail(String id) throws Exception;

    List<FdevSitMsg> querySitMsgList(String workNo, Integer pageSize, Integer startPage, List<String> groupId, List<String> orderGroupId, String tester, String startDate, String endDate, String stage, String orderType, Boolean isIncludeChildren) throws Exception;

    Integer countSitMsgList(String workNo, List<String> groupId, List<String> orderGroupId, String tester, String startDate, String endDate, String stage, String orderType, Boolean isIncludeChildren) throws Exception;

    void sendStartUatMail(Map map) throws Exception;

    void sendSitDoneMail(Map map) throws Exception;

    List<FdevSitMsg> queryTaskSitMsg(String taskNo) throws  Exception;

    void fdevSubmitSitTag(Map map) throws Exception;

    Map<String, String> queryUatMailInfo(Map map) throws Exception;

    void getSitSubmitGroup() throws Exception;

    /**
     * 查询工单对应的最后一次提测信息中的交易清单文件路径
     * @param workNo
     * @return
     */
    String queryLastTransFilePath(String workNo) throws Exception;

    /**
     * 添加安全测试涉及交易列表数据
     * @param fdevSitMsgId
     * @param transList
     */
    void addSecurityTestTrans(String fdevSitMsgId, List<Map> transList);

    /**
     * 查询提测准时率
     * @param startDate
     * @param endDate
     * @param groupIds
     * @param isIncludeChildren
     * @return
     */
    List<Map> querySubmitTimelyAndMantis(String startDate, String endDate, List<String> groupIds, Boolean isIncludeChildren) throws Exception;

    /**
     * 查询需求相关测试数据
     * @param demandNo
     * @return
     */
    List<Map> queryInnerTestData(String demandNo) throws Exception;
}
