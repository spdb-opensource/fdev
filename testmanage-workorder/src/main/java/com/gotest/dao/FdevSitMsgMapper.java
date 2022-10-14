package com.gotest.dao;

import com.gotest.domain.FdevSitMsg;
import com.gotest.domain.SecurityTestTrans;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FdevSitMsgMapper {

    Integer addFdevSitMsg(FdevSitMsg fdevSitMsg) throws Exception;

    FdevSitMsg queryFdevSitMsg(String taskNo) throws Exception;

    FdevSitMsg querySitMsgDetail(String id) throws  Exception;

    List<FdevSitMsg> querySitMsgList(String workNo, Integer pageSize, Integer startPage, List<String> sitGroupIds, String tester, String startDate, String endDate, String stage, String orderType, List<String> orderGroupIds) throws Exception;

    Integer countSitMsgList(String workNo, List<String> sitGroupIds, String tester, String startDate, String endDate, String stage, String orderType, List<String> orderGroupIds) throws Exception;

    FdevSitMsg queryLastFdevSitMsg(String workNo, String taskNo) throws Exception;

    List<FdevSitMsg> queryTaskSitMsg(String taskNo, String orderType) throws Exception;

    List<String> queryTaskNoByOrder(String workNo) throws Exception;

    Integer updateWorkNoById(@Param("workNo")String workNo,@Param("id")Integer id);

    List<String> queryTaskNoByWorkNo(@Param("workNo")String workNo);

    Integer querySitMsgCount(@Param("taskNo")String taskNo);

    Integer updateWorkNoByTaskNos(@Param("taskIds")List<String> taskIds,@Param("newWorkNo")String newWorkNo);

    List<String> queryTaskNoAll() throws Exception;

    void updateGroupIdByTaskId(@Param("taskNo") String taskNo, @Param("groupId") String groupId);

    List<FdevSitMsg> queryAllCopyToByTaskIds(List<String> taskIds);

    void addSecurityTestTrans(SecurityTestTrans testTrans);

    void updateByWorkNo(String workOrderNo, String rqrNo);

    List<Map> querySubmitTime(String startDate, String endDate, List<String> groupIds);
}
