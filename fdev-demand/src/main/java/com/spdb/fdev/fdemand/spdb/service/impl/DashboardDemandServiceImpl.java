package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.DashboradUtils;
import com.spdb.fdev.fdemand.base.utils.GroupTreeUtil;
import com.spdb.fdev.fdemand.base.utils.GroupUtil;
import com.spdb.fdev.fdemand.spdb.dao.IDemandBaseInfoDao;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.entity.GroupTree;
import com.spdb.fdev.fdemand.spdb.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RefreshScope
@Service
public class DashboardDemandServiceImpl implements DashboardDemandService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private IpmpTaskService ipmpTaskService;
    @Autowired
    private IRoleService iRoleService;
    @Autowired
    private IImplementUnitService implementUnitService;
    @Autowired
    private IDemandBaseInfoService demandBaseInfoService;
    @Autowired
    private GroupTreeUtil groupTreeUtil;
    @Value("${isSendEmail}")
    private boolean isSendEmail;
    @Value("${demand.email.group}")
    private List<String> rqrmntEmailGroup;
    @Value("${demand.email.groupReceiver}")
    private String rqrmntGroupReceiver;
    @Value("${demand.email.fixedReceiver}")
    private List<String> rqrmntFixedReceiver;
    @Value("${demand.email.filePath}")
    private String filePath;
    @Autowired
    private IMailService mailService;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private ReportExportService reportExportService;
    @Autowired
    private ITaskService taskService;
    @Autowired
    private GroupUtil groupUtil;

    @Value("${demand.group.id}")
    private String groupId;

    @Value("${demand.part.id}")
    private String partId;
    @Value("${demand.parent.id}")
    private String parentId;

    @Autowired
    private IDemandBaseInfoDao demandBaseInfoDao;

    @Override
    public List getDemandStatis(Map map) throws Exception {
        List<String> filterGroup = (List<String>) map.get("groupIds");
        String priority = (String) map.get("priority");
        Boolean isParent = (Boolean) map.get("isParent");
        if (CommonUtils.isNullOrEmpty(isParent))
            isParent = false;
        //获取所有组
        List<LinkedHashMap<String, Object>> fdevGroup = this.findRqrmntGroup();
        List<String> groupexitArray = new ArrayList();
        List<LinkedHashMap<String, Object>> fdevGroupFilter = new ArrayList();
        fdevGroup.forEach(item -> {
            if (!groupexitArray.contains(item.get("id")) && filterGroup.contains(item.get("id"))) {
                fdevGroupFilter.add(item);
                groupexitArray.add((String) item.get("id"));
            }
        });
        //2.循环每个组进行获取任务
        List<Map> dashboradArray = new ArrayList();
        //项目组资源统计表查询
        Map userStatis = this.queryUserStatis(groupexitArray, isParent);
        List<LinkedHashMap<String, Object>> groups = (List<LinkedHashMap<String, Object>>) userStatis.get("groups");
        //保留两位小数
        DecimalFormat df = new DecimalFormat("0.00");
        for (LinkedHashMap<String, Object> groupItem : fdevGroupFilter) {
            //获取groupId
            String groupId = ((String) groupItem.get(Dict.ID));
            if (CommonUtils.isNullOrEmpty(groupId))
                continue;
            int count = 0;      //资源组规模
            for (LinkedHashMap<String, Object> group : groups) {
                if (groupId.equals(group.get("groupid"))) {
                    count = (Integer) group.get("userSum");
                    break;
                }
            }
            List<DemandBaseInfo> demandStatis;
            Set<String> groupIds = new HashSet<>();
            if (isParent) {
                List groupArr = new ArrayList<>();
                groupArr.add(groupId);
                //获取得到所有子组f
                groupIds = iRoleService.queryChildGroupByIds(groupArr);
            } else {
                groupIds.add(groupId);
            }
            //根据优先级、groups查询需求
            if (!CommonUtils.isNullOrEmpty(priority)) {
                demandStatis = this.demandBaseInfoService.queryDemandByGroupIdAndPriority(groupIds, priority, isParent);
            } else
                demandStatis = this.demandBaseInfoService.queryDemandByGroupId(groupIds);

            Map resultDashborad = DashboradUtils.getStatisgroup();
            resultDashborad.put("groupId", groupId);
            resultDashborad.put(Dict.GROUPNAME, groupItem.get(Dict.NAME));
            //项目组规模
            resultDashborad.put(Dict.COUNT, count);
            for (DemandBaseInfo demand : demandStatis) {
                //统计各阶段需求
                DashboradUtils.statisDemandArray(demand, resultDashborad);
                //统计非本组牵头的开发阶段
            }

            List<LinkedHashMap> groupTasks;
            groupTasks = this.taskService.findTaskByTerms(groupIds, false);
            Set<String> demandIds = getRqrmntsNo(groupTasks);
            List<DemandBaseInfo> demandInfos = this.demandBaseInfoService.queryDemandByIds(demandIds);
            //统计参与的数据
            DashboradUtils.statisOthersRqrmnt(groupIds, demandInfos, resultDashborad);

            //每一个组的 总计
            int totalkj = (int) resultDashborad.get("ypgkj") + (int) resultDashborad.get("pgkj")
                    + (int) resultDashborad.get("dsskj") + (int) resultDashborad.get("kaifakj")
                    + (int) resultDashborad.get("cssitkj") + (int) resultDashborad.get("csuatkj")
                    + (int) resultDashborad.get("csrelkj")
                    + (int) resultDashborad.get("nogdevelopkj");
            resultDashborad.put("totalkj", totalkj);
            int totalyw = (int) resultDashborad.get("ypgyw") + (int) resultDashborad.get("pgyw")
                    + (int) resultDashborad.get("dssyw") + (int) resultDashborad.get("kaifayw")
                    + (int) resultDashborad.get("cssityw") + (int) resultDashborad.get("csuatyw")
                    + (int) resultDashborad.get("csrelyw")
                    + (int) resultDashborad.get("nogdevelopyw");
            resultDashborad.put("totalyw", totalyw);
            //项目组规模
            resultDashborad.put("count", count);
            //开发总人均负荷=（开发阶段（牵头）+开发阶段（参与））/项目组规模
            double kaifarjfh = 0;
            if (count != 0) {
                kaifarjfh = ((int) resultDashborad.get("kaifakj") + (int) resultDashborad.get("kaifayw") +
                        (int) resultDashborad.get("nogdevelopkj") +
                        (int) resultDashborad.get("nogdevelopyw")) / (double) count;
                resultDashborad.put("kaifarjfh", df.format(kaifarjfh));
            } else {
                resultDashborad.put("kaifarjfh", "∞");
            }
            //业务需求人均负荷= 业务需求/项目组规模
            double ywxqrjfh = 0;
            if (count != 0) {
                ywxqrjfh = ((int) totalyw) / (double) count;
                resultDashborad.put("ywxqrjfh", df.format(ywxqrjfh));
            } else {
                resultDashborad.put("ywxqrjfh", "∞");
            }
            dashboradArray.add(resultDashborad);
        }
        //排序
        dashboradArray.sort((s, t) -> {
            if (s.get("groupName").toString().compareTo(t.get("groupName").toString()) < 0)
                return -1;
            else
                return 1;
        });
        return dashboradArray;
    }


    /**
     * 获取所有任务的需求ids
     *
     * @param groupTasks
     * @return
     */
    private Set<String> getRqrmntsNo(List<LinkedHashMap> groupTasks) {
        Set<String> demandIds = new HashSet();
        if (CommonUtils.isNullOrEmpty(groupTasks)) {
            return demandIds;
        }
        for (LinkedHashMap groupTask : groupTasks) {
            if (!CommonUtils.isNullOrEmpty(groupTask.get("rqrmnt_no")))
                demandIds.add((String) groupTask.get("rqrmnt_no"));
        }
        return demandIds;
    }


    /**
     * @param groupIds
     * @param isParent
     * @return
     */
    private List<LinkedHashMap> findTaskByTerms(Set<String> groupIds, Boolean isParent) {
        if (CommonUtils.isNullOrEmpty(groupIds)) {
            return null;
        }
        List<String> stage = new ArrayList<>();
        stage.add("develop");
        stage.add("sit");
        stage.add("uat");
        stage.add("rel");
        stage.add("create-info");
        stage.add("create-app");
        stage.add("create-feature");
        List<String> groups = new ArrayList<>();
        groups.addAll(groupIds);
        return queryByTerms(groups, stage, isParent);
    }

    /**
     * 发送任务模块查询任务信息
     *
     * @param groups
     * @param stage
     * @param isParent
     * @return
     */
    private List<LinkedHashMap> queryByTerms(List<String> groups, List<String> stage, Boolean isParent) {
        Map send = new HashMap();
        if (CommonUtils.isNullOrEmpty(groups) || CommonUtils.isNullOrEmpty(stage)) {
            return null;
        }
        send.put(Dict.REST_CODE, "queryByTerms");
        send.put("group", groups);
        send.put("stage", stage);
        send.put("isIncludeChildren", isParent);
        Object result = null;
        try {
            result = restTransport.submit(send);
        } catch (Exception e) {
            logger.error("发送任务模块接口/queryByTerms失败");
        }
        if (null == result)
            throw new FdevException(ErrorConstants.FTASK_QUERY_ERROR, new String[]{"发送任务模块接口/queryByTerms失败"});

        return (List) result;
    }

    /**
     * 查询出所有 fdev的组 getGroups
     * 涉及计算量较大，所以添加redis缓存
     *
     * @return
     */
    private List findRqrmntGroup() {
//        if (!CommonUtils.isNullOrEmpty(redisTemplate.opsForList().range(Constants.REDISGETGROUPS, 0, -1))) {
//            return (List<LinkedHashMap<String, String>>) redisTemplate.opsForList().range(Constants.REDISGETGROUPS, 0, -1);
//        }
        List resDate;
        Map sendDate = new HashMap();
        sendDate.put(Dict.REST_CODE, Dict.GETGROUPS);
        try {
            resDate = (List<LinkedHashMap>) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FdevException(ErrorConstants.FTASK_QUERY_ERROR);
        }
        List resDate2 = DashboradUtils.getGroupFullName(resDate);
        if (CommonUtils.isNullOrEmpty(redisTemplate.opsForList().range(Constants.REDISGETGROUPS, 0, -1))) {
            redisTemplate.opsForList().rightPushAll(Constants.REDISGETGROUPS, resDate2);
            redisTemplate.expire(Constants.REDISGETGROUPS, 6, TimeUnit.HOURS);
        }
        return resDate2;
    }


    /**
     * 获取项目组资源
     */
    private Map queryUserStatis(List<String> groupIds, Boolean isIncludeChildren) {
        Map resDate;
        Map sendDate = new HashMap();
        sendDate.put(Dict.REST_CODE, "queryUserStatis");
        sendDate.put("ids", groupIds);
        sendDate.put("isIncludeChildren", isIncludeChildren);
        try {
            resDate = (Map) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FdevException(ErrorConstants.FUSER_QUERY_ERROR);
        }
        return resDate;
    }

    @Override
    public List queryImplUnit(Map map) throws Exception {
        List<String> filterGroup = (List<String>) map.get(Dict.GROUPIDS);
        String priority = (String) map.get(Dict.PRIORITY);
        Boolean isParent = (Boolean) map.get(Dict.ISPARENT);
        //获取所有组
        List<LinkedHashMap<String, String>> fdevGroup = ipmpTaskService.getAllGroup();
        //存放组id
        List<String> groupexitArray = new ArrayList();
        //存放组的详细信息
        List<LinkedHashMap<String, String>> fdevGroupFilter = new ArrayList();
        fdevGroup.forEach(item -> {
            if (!groupexitArray.contains(item.get(Dict.ID)) && filterGroup.contains(item.get(Dict.ID))) {
                fdevGroupFilter.add(item);
                groupexitArray.add(item.get(Dict.ID));
            }
        });
        //循环每个组进行获取任务
        List<Map> statisArray = new ArrayList();
        //保留两位小数
        DecimalFormat df = new DecimalFormat("0.00");
        //获得组内开发资源
        List list = new ArrayList();
        if (!CommonUtils.isNullOrEmpty(groupexitArray))
            //用于统计开发人均负荷
            list = iRoleService.queryDevResource(groupexitArray);
        //依次查询每个组下的实施单元
        for (int i = 0; i < fdevGroupFilter.size(); i++) {
            LinkedHashMap<String, String> implUnitGroup = fdevGroupFilter.get(i);
            if (!CommonUtils.isNullOrEmpty(implUnitGroup)) {
                //获取每个组下的所有实施单元
                List<FdevImplementUnit> demandImplUnitStatis = implementUnitService.queryImplUnitByGroupId(implUnitGroup.get(Dict.ID), isParent);
                demandImplUnitStatis.removeIf(x -> !"tech".equals(x.getDemand_type()) && !"business".equals(x.getDemand_type()));
                //统计实施单元任务数据
                Map statisgroup = DashboradUtils.getStatisImplUnits();
                statisgroup.put("groupId", implUnitGroup.get(Dict.ID));
                statisgroup.put(Dict.GROUPNAME, implUnitGroup.get(Dict.NAME));
                for (int y = 0; y < demandImplUnitStatis.size(); y++) {
                    FdevImplementUnit implunit = demandImplUnitStatis.get(y);
                    //查询实施单元所属需求
                    DemandBaseInfo demandStatis = new DemandBaseInfo();
                    if (!CommonUtils.isNullOrEmpty(implunit.getGroup())) {
                        //如果 优先级 不为空，则查询优先级高的需求（需求的优先级即为实施单元的优先级）0：高，1：中，2：一般，3：低
                        if (!CommonUtils.isNullOrEmpty(priority)) {
                            demandStatis = demandBaseInfoService.queryDemandByImplUnitsIdAndPriority(implunit.getDemand_id(), priority);
                        } else {
                            demandStatis = demandBaseInfoService.queryDemandByImplUnitsId(implunit.getDemand_id());
                        }
                    }

                    if (!CommonUtils.isNullOrEmpty(demandStatis)) {
                        //获得实施单元类型（科技类和业务类的分类按照当前实施计划所属需求的分类来算）
                        String type = "yw";
                        if ("tech".equals(implunit.getDemand_type())) {
                            type = "kj";
                        }

                        //各阶段统计
                        DashboradUtils.statisImplUnitsArray(implunit, statisgroup, type);
                        //超期统计
                        DashboradUtils.statisImplUnitsBeOverdue(implunit, statisgroup, type);
                    }
                }
                //待实施+开发+测试sit+测试uat+测试rel
                int totalkj = (int) statisgroup.get("dsskj") + (int) statisgroup.get("kaifakj")
                        + (int) statisgroup.get("cssitkj") + (int) statisgroup.get("csuatkj")
                        + (int) statisgroup.get("csrelkj");
                statisgroup.put("totalkj", totalkj);
                int totalyw = (int) statisgroup.get("dssyw") + (int) statisgroup.get("kaifayw")
                        + (int) statisgroup.get("cssityw") + (int) statisgroup.get("csuatyw")
                        + (int) statisgroup.get("csrelyw");
                statisgroup.put("totalyw", totalyw);

                //每个组开发人员数量
                int count = 0;
                for (int j = 0; j < list.size(); j++) {
                    Map groupInfo = (Map) list.get(j);
                    if (groupInfo.get(Dict.ID).equals(implUnitGroup.get(Dict.ID))) {
                        count = (int) groupInfo.get(Dict.COUNT);
                    }

                }
                //开发人均负荷=开发阶段任务/组内开发资源
                double kaifarjfh = 0;
                if (count != 0) {
                    kaifarjfh = ((int) statisgroup.get("kaifakj") + (int) statisgroup.get("kaifayw")) / (double) count;
                    statisgroup.put("kaifarjfh", df.format(kaifarjfh));
                } else {
                    statisgroup.put("kaifarjfh", "∞");
                }
                statisArray.add(statisgroup);
            }
        }
        //排序
        statisArray.sort((s, t) -> {
            if (s.get(Dict.GROUPNAME).toString().compareTo(t.get(Dict.GROUPNAME).toString()) < 0)
                return -1;
            else
                return 1;
        });
        return statisArray;
    }

    /**
     * 分组查询需求统计
     *
     * @param groups
     * @param priority
     * @param isParent
     * @return
     */
    @Override
    public List queryGroupDemand(List groups, String priority, boolean isParent) throws Exception {
        //获取小组名称及小组开发资源
        List<Map> groupResult = this.queryDevResource(groups);
        Map groupName = groupResult.stream().collect(Collectors.toMap(k -> k.get(Dict.ID), v -> v.get(Dict.NAME)));
        Map groupDevNum = groupResult.stream().collect(Collectors.toMap(k -> k.get(Dict.ID), v -> v.get(Dict.COUNT)));

        //根据优先级获取全量需求条目
        Map demandType;
        Map leaderGroup;
        Map relatePartGroup;
        Map demandStatusSpecial;
        if ("0".equals(priority)) {
            Map rqrResult = rqrTypeMapping(true);
            demandType = (Map) rqrResult.get("demandType");
            relatePartGroup = (Map) rqrResult.get("relatePartGroup");
            leaderGroup = (Map) rqrResult.get("leaderGroup");
            demandStatusSpecial = (Map) rqrResult.get("demandStatusSpecial");
        } else {
            Map rqrResult = rqrTypeMapping(false);
            demandType = (Map) rqrResult.get("demandType");
            relatePartGroup = (Map) rqrResult.get("relatePartGroup");
            leaderGroup = (Map) rqrResult.get("leaderGroup");
            demandStatusSpecial = (Map) rqrResult.get("demandStatusSpecial");
        }
        List returnList = new ArrayList();
        DecimalFormat df = new DecimalFormat("0.00");
        for (Object group : groups) {
            Map result = new HashMap();
            int count = (int) groupDevNum.get(group);
            result.put(Dict.COUNT, count);
            result.put(Dict.GROUP_ID, group);
            result.put(Dict.GROUPNAME, groupName.get(group));
            int dsskj = 0, dssyw = 0, cssityw = 0, cssitkj = 0, nogdevelopkj = 0, nogdevelopyw = 0, kaifayw = 0, kaifakj = 0, csuatyw = 0, csuatkj = 0, csrelkj = 0, csprokj = 0, csrelyw = 0, csproyw = 0;
            int waitkj = 0, waityw = 0;
            Map<String, String> record = new HashMap<>();
            List<Map<String, Object>> taskInfos = getTaskInfo((String) group, isParent);
            for (Map<String, Object> fdevTask : taskInfos) {
                if (!CommonUtils.isNullOrEmpty(fdevTask.get("rqrmnt_no")))
                    record = accumulate(fdevTask, record);
            }
            for (String rqrmntNo : record.keySet()) {
                if (isInnerDemand(rqrmntNo, demandType)) {
                    //判断是否是暂缓任务
                    if (isWaitDemand(rqrmntNo, demandStatusSpecial)) {
                        waitkj++;
                        continue;
                    }
                    switch (record.get(rqrmntNo)) {
                        case "create-info":
                        case "create-app":
                        case "create-feature":
                            dsskj++;
                            break;
                        case "develop":
                            String leaderGroupStr = (String) leaderGroup.get(rqrmntNo);
                            Set relateGroup = (Set) relatePartGroup.get(rqrmntNo);
                            if (!CommonUtils.isNullOrEmpty(leaderGroupStr) && leaderGroupStr.equals(group)){
                                nogdevelopkj++;
                                break;
                            } else if (!CommonUtils.isNullOrEmpty(relateGroup) && relateGroup.contains(group)) {
                                kaifakj++;
                                break;
                            }
                            break;
                        case "sit":
                            cssitkj++;
                            break;
                        case "uat":
                            csuatkj++;
                            break;
                        case "rel":
                            csrelkj++;
                            break;
                        case "production":
                        case "file":
                            csprokj++;
                            break;
                        default:
                            break;
                    }
                } else {
                    //判断是否是暂缓任务
                    if (isWaitDemand(rqrmntNo, demandStatusSpecial)) {
                        waityw++;
                        continue;
                    }
                    switch (record.get(rqrmntNo)) {
                        case "create-info":
                        case "create-app":
                        case "create-feature":
                            dssyw++;
                            break;
                        case "develop":
                            String leaderGroupStr = (String) leaderGroup.get(rqrmntNo);
                            Set relateGroup = (Set) relatePartGroup.get(rqrmntNo);
                            if (!CommonUtils.isNullOrEmpty(leaderGroupStr) && leaderGroupStr.equals(group)){
                                nogdevelopkj++;
                                break;
                            } else if (!CommonUtils.isNullOrEmpty(relateGroup) && relateGroup.contains(group)) {
                                kaifakj++;
                                break;
                            }
                            break;
                        case "sit":
                            cssityw++;
                            break;
                        case "uat":
                            csuatyw++;
                            break;
                        case "rel":
                            csrelyw++;
                            break;
                        case "production":
                        case "file":
                            csproyw++;
                            break;
                        default:
                            break;
                    }
                }
            }
            result.put("dsskj", dsskj);
            result.put("kaifakj", kaifakj);
            result.put("nogdevelopkj", nogdevelopkj);
            result.put("cssitkj", cssitkj);
            result.put("csuatkj", csuatkj);
            result.put("csrelkj", csrelkj);
            result.put("csprokj", csprokj);
            result.put("dssyw", dssyw);
            result.put("kaifayw", kaifayw);
            result.put("nogdevelopyw", nogdevelopyw);
            result.put("cssityw", cssityw);
            result.put("csuatyw", csuatyw);
            result.put("csrelyw", csrelyw);
            result.put("csproyw", csproyw);
            result.put("waitkj", waitkj);
            result.put("waityw", waityw);
            int ywxq = dssyw + kaifayw + nogdevelopyw + cssityw + csuatyw + csrelyw + waityw;
            int kjxq = dsskj + kaifakj + nogdevelopkj + cssitkj + csuatkj + csrelkj + waitkj;
            int kaifaxq = kaifakj + kaifayw + nogdevelopkj + nogdevelopyw;
            double ywxqrjfh = 0;
            double kaifarjfh = 0;
            if (count != 0) {
                kaifarjfh = kaifaxq / (double) count;
                ywxqrjfh = ywxq / (double) count;
                result.put("ywxqrjfh", df.format(ywxqrjfh));
                result.put("kaifarjfh", df.format(kaifarjfh));
            } else {
                result.put("ywxqrjfh", "∞");
                result.put("kaifarjfh", "∞");
            }
            result.put("totalkj", kjxq);
            result.put("totalyw", ywxq);
            returnList.add(result);
        }

        return returnList;
    }


    /**
     * 需求延期邮件提醒
     *
     * @throws Exception
     */
    @Override
    public void sendDemandEmail() throws Exception {

        if (!CommonUtils.isNullOrEmpty(rqrmntEmailGroup)) {
            //获取全量用户信息
            List<Map> queryUser = roleService.queryUser();
            String groupName = null;
            for (String groupId : rqrmntEmailGroup) {
                List<DemandBaseInfo> demands = new ArrayList<>();
                //查询当前组和子组数据
                List<LinkedHashMap> resDate = roleService.queryChildGroupById(groupId);
                if (!CommonUtils.isNullOrEmpty(resDate)) {
                    for (LinkedHashMap linkedHashMap : resDate) {
                        String id = (String) linkedHashMap.get(Dict.ID);
                        List<String> listId = new ArrayList<>();
                        listId.add(id);
                        //获取该组下所有的需求信息
                        List<DemandBaseInfo> demandBaseInfos = demandBaseInfoService.queryDemandByGroupId(listId);
                        if (!CommonUtils.isNullOrEmpty(demandBaseInfos)) {
                            demands.addAll(demandBaseInfos);
                        }
                        if (id.equals(groupId)) {
                            groupName = (String) linkedHashMap.get(Dict.NAME);
                        }
                    }
                }

                if (!CommonUtils.isNullOrEmpty(demands)) {
                    //获取当前日期
                    String formatDate = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
                    //当前周一 二日期 yy-mm-dd
                    String monday = CommonUtils.getCalendar(Calendar.MONDAY);//本周一日期
                    String sunday = CommonUtils.getCalendar(Calendar.SUNDAY);//星期天
                    List<Map> initString = new ArrayList<>();//用于邮件展示
                    List<Map> sitString = new ArrayList<>();
                    List<String> appendUserEmail = new ArrayList<>();//到期需求科技负责人邮箱集合
                    //即将到期需求
                    List<DemandBaseInfo> devDemandList = new ArrayList<>();//开发提醒需求集合,用于excel表格
                    List<DemandBaseInfo> sitDemandList = new ArrayList<>();//提测提醒需求集合
                    //延期需求
                    List<Map> initOverdueString = new ArrayList<>();//用于邮件展示
                    List<Map> sitOverdueString = new ArrayList<>();
                    List<String> OverdueUserEmail = new ArrayList<>();//延期需求科技负责人邮箱集合
                    List<DemandBaseInfo> devDemandOverdue = new ArrayList<>();//开发提醒需求集合,用于excel表格
                    List<DemandBaseInfo> sitDemandOverdue = new ArrayList<>();//提测提醒需求集合
                    for (DemandBaseInfo demand : demands) {
                        //科技负责人
                        String appendUserCn = CommonUtils.appendUserCn(demand, queryUser);
                        if (CommonUtils.isNullOrEmpty(demand.getPlan_start_date())) {
                            continue;
                        }
                        //未延期：计划启动日期，当计划日期进入当前周的周一到周日才进行提醒
                        String estInitDate = demand.getPlan_start_date();
                        if ((estInitDate.compareTo(monday) > 0 || estInitDate.compareTo(monday) == 0)
                                && (estInitDate.compareTo(sunday) < 0 || estInitDate.compareTo(sunday) == 0)) {
                            //开发启动提醒: 当前日期<计划启动日期
                            if ((estInitDate.compareTo(formatDate) > 0 || estInitDate.compareTo(formatDate) == 0) && CommonUtils.isNullOrEmpty(demand.getReal_start_date())) {
                                //距离延期剩余天数
                                long countDay = CommonUtils.countDay(estInitDate, formatDate);
                                //拼装提醒内容
                                Map appendMap = CommonUtils.appendMap(demand.getOa_contact_no(), demand.getOa_contact_name(), estInitDate, appendUserCn, countDay);
                                initString.add(appendMap);
                                //开发需求科技负责人邮箱
                                List<String> initEmail = CommonUtils.appendUserEmail(demand, queryUser);
                                for (String str : initEmail) {
                                    if (!appendUserEmail.contains(str)) {
                                        appendUserEmail.add(str);
                                    }
                                }
                                if (!devDemandList.contains(demand)) {
                                    devDemandList.add(demand);
                                }
                            }
                        }
                        if (CommonUtils.isNullOrEmpty(demand.getPlan_inner_test_date())) {
                            continue;
                        }
                        //未延期：计划提交内测时间
                        String estSitDate = CommonUtils.getSubstring(demand.getPlan_inner_test_date());
                        if ((estSitDate.compareTo(monday) > 0 || estSitDate.compareTo(monday) == 0)
                                && (estSitDate.compareTo(sunday) < 0 || estSitDate.compareTo(sunday) == 0)) {
                            //提测启动提醒: 当前日期<计划提交内测日期, 实际启动日期存在
                            if ((estSitDate.compareTo(formatDate) > 0 || estSitDate.compareTo(formatDate) == 0) && (!CommonUtils.isNullOrEmpty(demand.getReal_start_date()))) {
                                long countDay = CommonUtils.countDay(estSitDate, formatDate);
                                Map appendStr = CommonUtils.appendMap(demand.getOa_contact_no(), demand.getOa_contact_name(), estSitDate, appendUserCn, countDay);
                                sitString.add(appendStr);
                                //提测需求科技负责人邮箱
                                List<String> sitEmail = CommonUtils.appendUserEmail(demand, queryUser);
                                for (String str : sitEmail) {
                                    if (!appendUserEmail.contains(str)) {
                                        appendUserEmail.add(str);
                                    }
                                }
                                if (!sitDemandList.contains(demand)) {
                                    sitDemandList.add(demand);
                                }
                            }
                        }

                        //已延期启动告警: 当前日期>计划启动日期, 实际启动日期为空证此需求未启动
                        if (estInitDate.compareTo(formatDate) < 0 && CommonUtils.isNullOrEmpty(demand.getReal_start_date())) {
                            long countDay = CommonUtils.countDay(formatDate, estInitDate);
                            //拼接需求
                            Map appendStr = CommonUtils.appendMap(demand.getOa_contact_no(), demand.getOa_contact_name(), estInitDate, appendUserCn, countDay);
                            initOverdueString.add(appendStr);
                            //需求科技负责人邮箱
                            List<String> initEmail = CommonUtils.appendUserEmail(demand, queryUser);
                            for (String str : initEmail) {
                                //保证不存入重复用户
                                if (!OverdueUserEmail.contains(str)) {
                                    OverdueUserEmail.add(str);
                                }
                            }
                            if (!devDemandOverdue.contains(demand)) {
                                devDemandOverdue.add(demand);
                            }
                        }
                        //已延期提测告警: 当前日期>计划提交业测日期, 实际提交业测日期为空, 实际提交内测日期存在代表此任务已经内测完成
                        if (!CommonUtils.isNullOrEmpty(demand.getPlan_test_date())) {
                        	String estUatDate = CommonUtils.getSubstring(demand.getPlan_test_date());
                        	if (estUatDate.compareTo(formatDate) < 0 && CommonUtils.isNullOrEmpty(demand.getReal_test_date()) && !CommonUtils.isNullOrEmpty(demand.getReal_inner_test_date())) {
                        		long countDay = CommonUtils.countDay(formatDate, estUatDate);
                        		Map appendStr = CommonUtils.appendMap(demand.getOa_contact_no(), demand.getOa_contact_name(), estUatDate, appendUserCn, countDay);
                        		sitOverdueString.add(appendStr);
                        		//提测需求科技负责人邮箱
                        		List<String> sitEmail = CommonUtils.appendUserEmail(demand, queryUser);
                        		for (String str : sitEmail) {
                        			if (!OverdueUserEmail.contains(str)) {
                        				OverdueUserEmail.add(str);
                        			}
                        		}
                        		if (!sitDemandOverdue.contains(demand)) {
                        			sitDemandOverdue.add(demand);
                        		}
                        	}
						}
                    }
                    if (!(CommonUtils.isNullOrEmpty(initString) && CommonUtils.isNullOrEmpty(sitString))) {
                        //即将到期需求提醒通知
                        setEmailContent(Dict.FDEMAND_EXPIREDMSG, groupName, groupId, appendUserEmail, monday, sunday, devDemandList, sitDemandList,
                                queryUser, Dict.EXPIRED_DEV, Dict.EXPIRED_SIT, initString, sitString);
                    }
                    if (!(CommonUtils.isNullOrEmpty(initOverdueString) && CommonUtils.isNullOrEmpty(sitOverdueString))) {
                        //仍然延期的需求告警通知
                        setEmailContent(Dict.FDEMAND_OVERDUEMSG, groupName, groupId, OverdueUserEmail, monday, sunday, devDemandOverdue, sitDemandOverdue,
                                queryUser, Dict.OVERDUE_DEV, Dict.OVERDUE_SIT, initOverdueString, sitOverdueString);
                    }
                }
            }
        }

    }


    //邮件相关
    private void setEmailContent(String template, String groupName, String groupId, List<String> appendUserEmail, String monday, String sunday, List<DemandBaseInfo> devDemandList,
                                 List<DemandBaseInfo> sitDemandList, List<Map> queryUser, String flag1, String flag2, List<Map> initString, List<Map> sitString) throws Exception {
        //拼接邮件主题
        String subject = CommonUtils.appendSubject(template, groupName);
        if (CommonUtils.isNullOrEmpty(subject)) {
            throw new FdevException(ErrorConstants.RQRMNT_EMAIL_SUBJECT_BE_EMPTY);
        }
        //拼接收件人: 科技负责人+组收件人+固定收件人
        if (!CommonUtils.isNullOrEmpty(rqrmntGroupReceiver)) {
            List<String> appendGroupReceiver = CommonUtils.appendGroupReceiver(rqrmntGroupReceiver, groupId);
            for (String str : appendGroupReceiver) {
                if (!appendUserEmail.contains(str)) {
                    appendUserEmail.add(str);
                }
            }
        }
        for (String str : rqrmntFixedReceiver) {
            if (!appendUserEmail.contains(str)) {
                appendUserEmail.add(str);
            }
        }
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        //生成excel文件,导出到服务器指定目录下
        if (!CommonUtils.isNullOrEmpty(devDemandList)) {
            String excelPath_dev = reportExportService.emailDemandExport(devDemandList, queryUser, flag1, groupName, filePath);
            filePaths.add(excelPath_dev);
        }
        if (!CommonUtils.isNullOrEmpty(sitDemandList)) {
            String excelPath_sit = reportExportService.emailDemandExport(sitDemandList, queryUser, flag2, groupName, filePath);
            filePaths.add(excelPath_sit);
        }
        if (!CommonUtils.isNullOrEmpty(filePaths)) {
            //发送邮件
            if (isSendEmail) {
                HashMap<String, Object> model = new HashMap<>();
                model.put(Dict.START_DATE, monday);
                model.put(Dict.END_DATE, sunday);
                model.put(Dict.GROUPNAME, groupName);
                model.put(Dict.INITSTRING, initString);
                model.put(Dict.SITSTRING, sitString);
                String mailContent = mailService.getOverDueMsg(template,model);
                HashMap<String, Object> sendMap = new HashMap<>();
                sendMap.put(Dict.EMAIL_CONTENT, mailContent);
                try {
                    mailService.sendEmail(subject, template, sendMap, appendUserEmail, filePaths);
                } catch (Exception e) {
                    logger.error("#########发送需求超期通知邮件失败########" + e.getMessage());
                    throw new FdevException("#########发送需求超期通知邮件失败########");
                }
            }
        }
    }

    private Map<String, String> accumulate(Map fdevTask, Map<String, String> record) {
        String rqrmnt_no = (String) fdevTask.get("rqrmnt_no");
        String taskStage = (String) fdevTask.get("stage");
        String uat_test_time = (String) fdevTask.get("uat_test_time");
        if ("sit".equals(taskStage) && !CommonUtils.isNullOrEmpty(uat_test_time)) {
            taskStage = "uat";
        }
        if (record.keySet().contains(fdevTask.get("rqrmnt_no"))) {
            // 已记录
            String recordStage = DashboradUtils.minStage(record.get(fdevTask.get("rqrmnt_no")), taskStage);
            //过滤掉同一需求相同阶段的不同任务，将这种情况认为只有一种
            if (recordStage.equals(record.get(fdevTask.get("rqrmnt_no")))) {
                return record;
            } else {
                record.put((String) fdevTask.get("rqrmnt_no"), taskStage);
            }
        }
        record.put((String) fdevTask.get("rqrmnt_no"), taskStage);
        return record;
    }


    /**
     * 判断是否是科技内部需求
     *
     * @param rqrmntNo
     * @param demandType
     * @return
     */
    private boolean isInnerDemand(String rqrmntNo, Map demandType) {
        if (!CommonUtils.isNullOrEmpty(rqrmntNo)) {
            String type = (String) demandType.get(rqrmntNo);
            if (!CommonUtils.isNullOrEmpty(type)) {
                if (type.equals("tech")) {
                    return true;
                } else
                    return false;
            }
        }
        return false;
    }

    /**
     * 判断是否是暂缓需求
     *
     * @param demandNo
     * @param demandStatusSpecial
     * @return
     */
    private boolean isWaitDemand(String demandNo, Map demandStatusSpecial) {
        if (!CommonUtils.isNullOrEmpty(demandNo)) {
            Integer specialStatus = (Integer) demandStatusSpecial.get(demandNo);
            if (specialStatus != null && (specialStatus == 1 || specialStatus == 2))
                return true;
            else
                return false;
        }
        return false;
    }

    /**
     * @param groups 小组id集合
     * @return
     */
    public List queryDevResource(List groups) {
        List result;
        Map param = new HashMap();
        param.put("groupIds", groups);
        param.put(Dict.REST_CODE, "queryDevResource");
        try {
            result = (List) restTransport.submit(param);
        } catch (ClassCastException e) {
            throw new FdevException(ErrorConstants.FUSER_QUERY_ERROR, new String[]{"查询开发人员数数目返回值格式异常"});
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"组id "});
        }
        return result;
    }

    private Map rqrTypeMapping(boolean priority) throws Exception {
        DemandBaseInfo queryParam = new DemandBaseInfo();
        //优先级:   0 高 , 1 中 , 2 一般， 3 低
        queryParam.setPriority(priority ? "0" : null);
        List<DemandBaseInfo> rqrResult = this.demandBaseInfoService.query(queryParam);
        Map<String, Map> result = new HashedMap();
        Map demandType = new HashMap();
        Map relatePartGroup = new HashMap();
        Map leaderGroup = new HashMap();
        Map demandStatusSpecial = new HashMap();        //暂缓状态
        if (CommonUtils.isNullOrEmpty(rqrResult)) {
            throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR, new String[]{"查询需求数据为空"});
        }
        rqrResult.forEach(o -> {
            demandType.put(o.getId(), o.getDemand_type());//需求类型，tech科技，business业务
            String demand_leader_group = (String) o.getDemand_leader_group();
            Set<String> relate_part = (Set<String>) o.getRelate_part();
            if (null == relate_part) relate_part = new HashSet<>();
            leaderGroup.put(o.getId(), demand_leader_group);
            //去除涉及板块中的牵头组
            if (relate_part.contains(demand_leader_group)) {
                relate_part.remove(demand_leader_group);
            }
            relatePartGroup.put(o.getId(), relate_part);
            //特殊状态
            demandStatusSpecial.put(o.getId(), o.getDemand_status_special());
        });
        result.put("demandType", demandType);
        result.put("relatePartGroup", relatePartGroup);
        result.put("leaderGroup", leaderGroup);
        result.put("demandStatusSpecial", demandStatusSpecial);
        return result;
    }

    /**
     * 发任务模块获取得到任务信息 通过groups
     *
     * @param group
     * @param isParent
     * @return
     */
    private List<Map<String, Object>> getTaskInfo(String group, Boolean isParent) {

        //可用的状态
        List stagelist = new ArrayList();
        stagelist.add("create-info");
        stagelist.add("create-app");
        stagelist.add("create-feature");
        stagelist.add("develop");
        stagelist.add("sit");
        stagelist.add("uat");
        stagelist.add("rel");
        stagelist.add("production");
        stagelist.add("file");

        Map param = new HashMap();
        param.put(Dict.REST_CODE, "queryTaskNumByGroups");
        param.put("group", group);
//        param.put("groupTree", groupTree);
        param.put("stages", stagelist);
        param.put("isParent", isParent);
        Object result;
        try {
            result = restTransport.submit(param);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FdevException(ErrorConstants.FTASK_QUERY_ERROR, new String[]{"查询任务通过groups，发送任务模块失败"});
        }
//        List<Map<String, Object>> taskInfo = (List<Map<String, Object>>) result.get("data");
        return (List<Map<String, Object>>) result;
    }

    @Override
    public Map<String, Object> queryEndDemandDashboard(Map map) {
        //根据开始日期和结束日期查询已投产和已归档的需求
        String startDate = map.get(Dict.START_DATE).toString().replaceAll("\\/", "-");
        String endDate = map.get(Dict.END_DATE).toString().replaceAll("\\/", "-");
        if(startDate.compareToIgnoreCase(endDate) > 0){
            //开始时间比结束时间晚，说明用户选择范围不对
            throw new FdevException(ErrorConstants.START_END_DATE_ERROR);
        }
        //查4个大组的已投产和已归档的需求
        List<Map<String,Object>> queryGroupDemand = queryGroupDemandCommon(groupId, startDate, endDate);
        //查几个板块的已投产和已归档的需求
        List<Map<String,Object>> queryPartDemand = queryGroupDemandCommon(partId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put(Dict.QUERY_GROUP_DEMAND, queryGroupDemand);
        result.put(Dict.QUERY_PART_DEMAND, queryPartDemand);
        return result;
    }

    public List<Map<String,Object>> queryGroupDemandCommon (String groupId, String startDate, String endDate){
        String[] groupIdAllArr = groupId.split(",");
        List<Map<String,Object>> list = new ArrayList<>();
        long count = 0;
        if(!CommonUtils.isNullOrEmpty(groupIdAllArr)){
            for (String groupOne : groupIdAllArr){
                List listAll = roleService.queryChildGroupById(groupOne);
                if(!CommonUtils.isNullOrEmpty(listAll)){
                    Set<String> groupIdsOne = new HashSet<>();
                    Map<String,Object> resultGroupOne = new HashMap<>();
                    listAll.forEach(map -> {
                        Map mapOne = (Map) map;
                        groupIdsOne.add((String) mapOne.get(Dict.ID));
                        if(groupOne.equals((String)mapOne.get(Dict.ID))){
                            resultGroupOne.put(Dict.GROUPNAME,mapOne.get(Dict.NAME));
                        }
                    });
                    long demandAmt = demandBaseInfoDao.countDemandByGroup(groupIdsOne, startDate, endDate);
                    resultGroupOne.put(Dict.DEMANDAMT,demandAmt);
                    resultGroupOne.put(Dict.GROUP_ID_TF, groupOne);
                    list.add(resultGroupOne);
                    count += demandAmt;
                }
            }
        }
        DecimalFormat df = new DecimalFormat("#.0000");
        double lastResult = 1.0000;
        if(!CommonUtils.isNullOrEmpty(list)){
            for(int i = 0; i < list.size() - 1; i++){
                Map<String,Object> resultGroup = list.get(i);
                double demandProp = 0.0000;
                if(count != 0){
                    demandProp = ((Long)resultGroup.get(Dict.DEMANDAMT)).doubleValue()/count;
                    BigDecimal b = new BigDecimal(demandProp);
                    double demandPropTemp = b.setScale(4,BigDecimal.ROUND_HALF_UP).doubleValue();
                    lastResult = lastResult - demandPropTemp;
                    resultGroup.put(Dict.DEMANDPROP, demandPropTemp);
                }
            }
            Map<String,Object> resultGroupLast = list.get(list.size() - 1);
            resultGroupLast.put(Dict.DEMANDPROP,lastResult);
        }
        return list;
    }
    /**
     * 查询实施中需求全景图
     *
     * @param
     * @return
     * @throws Exception
     */
	@Override
	public Map queryImpingDemandDashboard() throws Exception {
		String[] groupIds = parentId.split(",");

		List<String> groupIdsList = Arrays.asList(groupIds);
		List<String> groupIdList =new ArrayList<>();
		//小組
        groupIdList.addAll(groupUtil.getGroupByParent(groupIdsList));

		//获取实施中需求信息
		List<DemandBaseInfo> impingDemand = demandBaseInfoDao.queryImpingDemand(groupIdList);

		//互联网条线实施中需求
		List<Map<String, Object>> intImpingDemandList = queryIntImpingDemandList(impingDemand);
		//各组实施中需求
		List<Map<String, Object>> preGroupImpDemandList = queryImpDemandMap(groupId,impingDemand);
		//零售组实施中需求总量
		List<Map<String, Object>> retailImpDemandList = queryImpDemandMap(partId,impingDemand);

		Map map = new HashMap();
		map.put(Dict.INTIMPINGDEMAND, intImpingDemandList);//互联网条线实施中需求
		map.put(Dict.PREGROUPIMPDEMAND, preGroupImpDemandList);//各组实施中需求
		map.put(Dict.RETAILIMPDEMAND, retailImpDemandList);//零售组实施中需求总量
		return map;
	}

	/**
     * 查询需求全景图所需GroupId
     *
     * @param
     * @return
     * @throws Exception
     */
	@Override
	public Map queryIntGroupId() {
		String[] groupIds = groupId.split(",");
		String[] partIds = partId.split(",");
		List<String> groupIdsList = Arrays.asList(groupIds);
		List<String> partIdsList = Arrays.asList(partIds);
		Map map = new HashMap();
		map.put(Dict.GROUPIDS, groupIdsList);//各组ID
		map.put(Dict.PARTIDS, partIdsList);//零售组各组ID
		return map;
	}

	private List<Map<String, Object>> queryImpDemandMap(String groupIdArray , List<DemandBaseInfo> impingDemand) {
		List<Map<String, Object>> impDemandList = new ArrayList<Map<String, Object>>();

		//小组ID
		String[] groupIds =groupIdArray.split(",");

		long demandCount = 0;//小组总需求数
		long moduleDemandCount = 0 ;//各板块总需求数
		for(String moduleId : groupIds ) {
			//初始化小组数据
			String groupName = "";//组名
			String group = "";//小组ID
			long demandAmt = 0 ;//需求数
			long urgAmt = 0 ;//紧急需求数
			long busiPreEvaluateDemand = 0 ;//业务预评估需求
			long busiToImpDemand = 0 ;//业务待实施需求 long
			long busiDevelopingDemand = 0 ;;//业务开发中需求
			long busiBusiDemand = 0 ;//业务业测需求
			long busiStagingDemand = 0 ;//业务准生产需求
			long kjPreEvaluateDemand = 0 ;//科技预评估需求
			long kjDToImpDemand  = 0 ;//科技待实施需求
			long kjDevelopingDemand = 0 ;//科技开发中需求
			long kjBusiTestDemand = 0 ;//科技业测需求
			long kjStagingDemand = 0 ;//科技准生产需求
			long coderAmt = 0 ;//开发人员数量
			long intDev = 0 ;//行内开发
			long intTest = 0 ;//行内测试
			long extDev = 0 ;//行外开发
			long extTest = 0 ;//行外测试

			//获取小组ID和其子组
			List<LinkedHashMap> groupList = iRoleService.queryChildGroupById(moduleId);
			if(!CommonUtils.isNullOrEmpty(groupList)) {
				for(LinkedHashMap groupMap : groupList ) {
					String groupId = (String) groupMap.get(Dict.ID);//小组ID
					if(moduleId.equals(groupId)) {
						groupName = (String) groupMap.get(Dict.NAME);
						group = groupId ;
					}

					if(!CommonUtils.isNullOrEmpty(groupId)) {
						for(DemandBaseInfo demand : impingDemand) {
							if(groupId.equals(demand.getDemand_leader_group())) {
								demandAmt++;
								if("0".equals(demand.getPriority())) {
									urgAmt++;
								}
								if(Dict.BUSINESS.equals(demand.getDemand_type())) {//业务需求
									Integer demandStatusNorma = demand.getDemand_status_normal();
									if(demandStatusNorma == 0 || demandStatusNorma == 1 ){//预评估
										busiPreEvaluateDemand++;
									}else if (demandStatusNorma == 2) {//待实施
										busiToImpDemand++;
									}else if (demandStatusNorma == 3 || demandStatusNorma == 4 ) {//开发中
										busiDevelopingDemand++;
									}else if (demandStatusNorma == 5 ) {//业测
										busiBusiDemand++;
									}else if (demandStatusNorma == 6 ) {//准生产
										busiStagingDemand++;
									}
								}else if (Dict.TECH.equals(demand.getDemand_type())) {//科技需求
									Integer demandStatusNorma = demand.getDemand_status_normal();
									if(demandStatusNorma == 0 || demandStatusNorma == 1 ){//预评估
										kjPreEvaluateDemand++;
									}else if (demandStatusNorma == 2) {//待实施
										kjDToImpDemand++;
									}else if (demandStatusNorma == 3 || demandStatusNorma == 4 ) {//开发中
										kjDevelopingDemand++;
									}else if (demandStatusNorma == 5 ) {//业测
										kjBusiTestDemand++;
									}else if (demandStatusNorma == 6 ) {//准生产
										kjStagingDemand++;
									}
								}
							}
						}
						//获取当前小组用户信息
						List<Map> groupUser = iRoleService.queryGroupUser(groupId);
						for(Map groupUserMap : groupUser) {
							String email = (String) groupUserMap.get(Dict.EMAIL);
							String status = (String) groupUserMap.get(Dict.STATUS);//0为在职
							if("0".equals(status)) {
								boolean isInt= false;//默认行外
								if(!CommonUtils.isNullOrEmpty(email) && email.endsWith("com.cn")) {
									isInt = true;//行内
								}

								List<Map> roleList = (List<Map>) groupUserMap.get(Dict.ROLE);
								if(!CommonUtils.isNullOrEmpty(roleList)) {
									for(Map role : roleList) {
										String name = (String) role.get(Dict.NAME);
										if(isInt) {
											if(Constants.DEVELOP.equals(name)) {//行内开发
												coderAmt++;
												intDev++;
											}else if (Constants.TEST.equals(name)) {//行内测试
												intTest++;
											}
										}else {
											if(Constants.DEVELOP.equals(name)) {//行外开发
												coderAmt++;
												extDev++;
											}else if (Constants.TEST.equals(name)) {//行外测试
												extTest++;
											}
										}
									}
								}
							}

						}
					}
				}
			}
			//组装map
			Map<String, Object> retailImpDemandMap = groupImpDemandMap( groupName, group ,demandAmt , urgAmt , coderAmt ,
					busiPreEvaluateDemand , busiToImpDemand , busiDevelopingDemand , busiBusiDemand, busiStagingDemand ,
					kjPreEvaluateDemand , kjDToImpDemand , kjDevelopingDemand , kjBusiTestDemand , kjStagingDemand ,
					intDev , intTest , extDev , extTest);
			//统计总需求数
			demandCount = demandCount + demandAmt ;
			impDemandList.add(retailImpDemandMap)	;
		}
		for(Map<String, Object> retailImpDemandMap : impDemandList) {
			retailImpDemandMap.put(Dict.DEMANDPROP,(float)(long)retailImpDemandMap.get(Dict.DEMANDAMT)/demandCount );//需求占比
		}
		return impDemandList;
	}
	//各组实施中需求Map初始化
	public Map<String, Object> groupImpDemandMap(String groupName,String groupId,long demandAmt,long urgAmt,long coderAmt,long busiPreEvaluateDemand
			,long busiToImpDemand,long busiDevelopingDemand,long busiBusiDemand,long busiStagingDemand,long kjPreEvaluateDemand
			,long kjDToImpDemand ,long kjDevelopingDemand,long kjBusiTestDemand,long kjStagingDemand,long intDev,long intTest
			,long extDev,long extTest) {
		DecimalFormat df1 = new DecimalFormat("0.00");//保留两位小数
		Map<String, Object> groupImpDemandMap = new HashMap<String, Object>();
		groupImpDemandMap.put(Dict.GROUPNAME, groupName);//组名
		groupImpDemandMap.put(Dict.GROUP_ID_TF, groupId);//组名
		groupImpDemandMap.put(Dict.DEMANDAMT, demandAmt);//需求数
		groupImpDemandMap.put(Dict.URGAMT, urgAmt);//紧急需求数
		groupImpDemandMap.put(Dict.BUSIPREEVALUATEDEMAND, busiPreEvaluateDemand);//业务预评估需求
		groupImpDemandMap.put(Dict.BUSITOIMPDEMAND, busiToImpDemand);//业务待实施需求
		groupImpDemandMap.put(Dict.BUSIDEVELOPINGDEMAND, busiDevelopingDemand);//业务开发中需求
		groupImpDemandMap.put(Dict.BUSIBUSIDEMAND, busiBusiDemand);//业务业测需求
		groupImpDemandMap.put(Dict.BUSISTAGINGDEMAND, busiStagingDemand);//业务准生产需求
		groupImpDemandMap.put(Dict.KJPREEVALUATEDEMAND, kjPreEvaluateDemand);//科技预评估需求
		groupImpDemandMap.put(Dict.KJDTOIMPDEMAND, kjDToImpDemand);//科技待实施需求
		groupImpDemandMap.put(Dict.KJDEVELOPINGDEMAND, kjDevelopingDemand);//科技开发中需求
		groupImpDemandMap.put(Dict.KJBUSITESTDEMAND, kjBusiTestDemand);//科技业测需求
		groupImpDemandMap.put(Dict.KJSTAGINGDEMAND, kjStagingDemand);//科技准生产需求
		groupImpDemandMap.put(Dict.CODERAMT, coderAmt);//开发人员数量
		groupImpDemandMap.put(Dict.INTDEV, intDev);//行内开发
		groupImpDemandMap.put(Dict.INTTEST, intTest);//行内测试
		groupImpDemandMap.put(Dict.EXTDEV, extDev);//行外开发
		groupImpDemandMap.put(Dict.EXTTEST, extTest);//行外测试
		long groupCount = intDev + intTest + extDev + extTest;//小组总人数
		groupImpDemandMap.put(Dict.STAFFLOAD,   coderAmt == 0 ? "0" : df1.format(((float)demandAmt/coderAmt)) );//人员负荷 开发为0 则负荷也为0
		groupImpDemandMap.put(Dict.INTDEVPROP,  intDev == 0 ? "0" : (float)intDev/groupCount );//行内开发占比 人员为0 则赋值为0%
		groupImpDemandMap.put(Dict.INTTESTPROP, intTest == 0 ? "0" : (float)intTest/groupCount);//行内测试占比
		groupImpDemandMap.put(Dict.EXTDEVPROP,  extDev == 0 ? "0" : (float)extDev/groupCount);//行外测试占比
		groupImpDemandMap.put(Dict.EXTTESTPROP, extTest == 0 ? "0" : (float)extTest/groupCount);//行外测试占比
		return groupImpDemandMap;
	}

	//互联网条线实施中需求
	public List<Map<String, Object>>  queryIntImpingDemandList(List<DemandBaseInfo> impingDemand) {
		List<Map<String, Object>> intImpingDemandList = new ArrayList<Map<String, Object>>();
		//初始化数据
		Map<String, Object> businessDemandMap = new HashMap<String, Object>();
		businessDemandMap.put(Dict.DEMANDTYPE, Dict.BUSINESS);//需求类型
		businessDemandMap.put(Dict.PREEVALUAT, 0);//预评估
		businessDemandMap.put(Dict.TOIMP, 0);//待实施
		businessDemandMap.put(Dict.DEVELOPING, 0);//开发中
		businessDemandMap.put(Dict.BUSITEST, 0);//业测
		businessDemandMap.put(Dict.STAGING, 0);//准生产
		Map<String, Object> techDemandMap = new HashMap<String, Object>();
		techDemandMap.put(Dict.DEMANDTYPE, Dict.TECH);//需求类型
		techDemandMap.put(Dict.PREEVALUAT, 0);//预评估
		techDemandMap.put(Dict.TOIMP, 0);//待实施
		techDemandMap.put(Dict.DEVELOPING, 0);//开发中
		techDemandMap.put(Dict.BUSITEST, 0);//业测
		techDemandMap.put(Dict.STAGING, 0);//准生产
		long businessTotal = 0;
		long techTotal = 0;
		for (DemandBaseInfo demand : impingDemand) {
			if(Dict.BUSINESS.equals(demand.getDemand_type())) {//业务需求
				businessTotal++;
				Integer demandStatusNorma = demand.getDemand_status_normal();
				if(demandStatusNorma == 0 || demandStatusNorma == 1 ){//预评估
					businessDemandMap.put(Dict.PREEVALUAT, (int)businessDemandMap.get(Dict.PREEVALUAT) + 1);//预评估
				}else if (demandStatusNorma == 2) {//待实施
					businessDemandMap.put(Dict.TOIMP, (int)businessDemandMap.get(Dict.TOIMP) + 1);//待实施
				}else if (demandStatusNorma == 3 || demandStatusNorma == 4 ) {//开发中
					businessDemandMap.put(Dict.DEVELOPING, (int)businessDemandMap.get(Dict.DEVELOPING) + 1);//开发中
				}else if (demandStatusNorma == 5 ) {//业测
					businessDemandMap.put(Dict.BUSITEST, (int)businessDemandMap.get(Dict.BUSITEST) + 1);//业测
				}else if (demandStatusNorma == 6 ) {//准生产
					businessDemandMap.put(Dict.STAGING, (int)businessDemandMap.get(Dict.STAGING) + 1);//准生产
				}
			}else if (Dict.TECH.equals(demand.getDemand_type())) {//科技需求
				techTotal++;
				Integer demandStatusNorma = demand.getDemand_status_normal();
				if(demandStatusNorma == 0 || demandStatusNorma == 1 ){//预评估
					techDemandMap.put(Dict.PREEVALUAT, (int)techDemandMap.get(Dict.PREEVALUAT) + 1);//预评估
				}else if (demandStatusNorma == 2) {//待实施
					techDemandMap.put(Dict.TOIMP, (int)techDemandMap.get(Dict.TOIMP) + 1);//待实施
				}else if (demandStatusNorma == 3 || demandStatusNorma == 4 ) {//开发中
					techDemandMap.put(Dict.DEVELOPING, (int)techDemandMap.get(Dict.DEVELOPING) + 1);//开发中
				}else if (demandStatusNorma == 5 ) {//业测
					techDemandMap.put(Dict.BUSITEST, (int)techDemandMap.get(Dict.BUSITEST) + 1);//业测
				}else if (demandStatusNorma == 6 ) {//准生产
					techDemandMap.put(Dict.STAGING, (int)techDemandMap.get(Dict.STAGING) + 1);//准生产
				}
			}
        }
		long total = businessTotal + techTotal;
		businessDemandMap.put(Dict.TOTAL, businessTotal);//业务需求总数
		businessDemandMap.put(Dict.TOTALPROP, total != 0 &&  businessTotal != 0 ? (float)businessTotal/total : "0" );//业务需求总数占比
		techDemandMap.put(Dict.TOTAL, techTotal);//科技需求总数
		techDemandMap.put(Dict.TOTALPROP, total != 0 &&  techTotal != 0 ? (float)techTotal/total : "0" );//科技需求总数占比
		intImpingDemandList.add(businessDemandMap);//业务需求
		intImpingDemandList.add(techDemandMap);//科技需求
		return intImpingDemandList;
	}

}
