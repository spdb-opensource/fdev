package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import com.spdb.fdev.fdemand.spdb.entity.OtherDemandTask;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRoleService {

    boolean isDemandManager() throws Exception;

    boolean isDemandManager(User user) throws Exception;

    Map<String, Object> queryUserbyid(String id) throws Exception;

    Map<String, Object> queryUserbyUserNameEn(String userNameEn) throws Exception;

    Map<String, Object> queryUserbyGitId(String gitlab_user_id) throws Exception;

    Map<String, Object> queryByGroupId(String id);

//    @LazyInitProperty(redisKeyExpression = "queryGroup.{id}")
    Map<String, Object> queryGroup(String id);

    void addUserName(Map map, String user_id);

    //是否为需求牵头人
    boolean isDemandLeader(String demand_id, String user_id) throws Exception;

    //是否为需求牵头人
    boolean isDemandLeader(DemandBaseInfo demandBaseInfo, String user_id) throws Exception;

    //是否为板块牵头人
    boolean isDemandGroupLeader(String demand_id, String user_id) throws Exception;

    Set<String> queryChildGroupByIds(List<String> ids) throws Exception;

    List queryChildGroupById(String id);

    List queryDevResource(List<String> groupIds);

    //查询全量用户
    List<Map> queryUser();

    Map queryByUserCoreData(List userIds);

    Map queryFdevRoleByName(String uiDesinger);

    Map addCommissionEvent(DemandBaseInfo rqrmnt, List<String> userId, String type);

    List<LinkedHashMap> queryUserByRole(String mainId);

    void sendNotify(List<String> userIds, String content, String rqrmntId);
    boolean isPartAsesser(String demandId,String implpart,User user);

	List<Map> queryGroupUser(String groupId);

    /**
     * 查询小组下的某种角色
     * @param groupId
     * @return
     */
	List<Map> queryGroupManage(String groupId, String roleId);

    boolean isIpmpUnitLeader(String demandId, String user_en) throws Exception;

    boolean isFdevUnitLeader(String impl_unit_id, String user_id);

    /**
     * 研发单元牵头人
     */
    boolean isFdevUnitListLeader(List<FdevImplementUnit> fdevImplementUnitList, String user_id) ;

    boolean isIpmpUnitLeader(IpmpUnit ipmpUnit, String user_en) throws Exception;

    /**
     * 是否是其他需求任务负责人
     */
    boolean isOtherDemandTaskLeader(OtherDemandTask otherDemandTask, String userId) throws Exception;

    /**
     * 是否需求下某个其它需求任务负责人
     */
    boolean isOtherDemandTaskLeader(String demandId, String userId) throws Exception;

    /**
     * 判断该用户是否为评估需求的牵头人
     * @param demandAssessId
     * @param userId
     * @return
     * @throws Exception
     */
    boolean isManagerAndDemandAssessLeader(String demandAssessId, String userId) throws Exception;

    List<Map<String, String>> queryGroupByIds(Set<String> groupIdList) throws Exception;
}
