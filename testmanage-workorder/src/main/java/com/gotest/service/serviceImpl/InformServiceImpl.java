package com.gotest.service.serviceImpl;

import com.gotest.controller.InformController;
import com.gotest.dao.*;
import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.domain.*;
import com.gotest.service.*;
import com.gotest.utils.CommonUtils;
import com.gotest.utils.MailUtil;
import com.gotest.utils.MyUtil;
import com.gotest.utils.OrderUtil;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RefreshScope
public class InformServiceImpl implements InformService {

    private Logger logger = LoggerFactory.getLogger(InformController.class);
    @Autowired
    private MessageFdevMapper msgFdevMapper;
    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private FdevSitMsgMapper fdevSitMsgMapper;
    @Autowired
    private ITaskApi taskApi;
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private TaskListMapper taskListMapper;
    @Autowired
    private OrderUtil orderUtil;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private INotifyApi iNotifyApi;
    @Value("${fdev.task.domain}")
    private String fdevTask;
    @Value("${security.test.addressee}")
    private List<String> addressee;
    @Autowired
    private IUserService userService;
    @Autowired
    private OrderDimensionService orderDimensionService;

    @Value("${user.testManager.role.id}")
    private String testManagerRoleId;

    @Value("${user.testLeader.role.id}")
    private String testLeaderRoleId;

    @Value("${user.tester.role.id}")
    private String testerRoleId;

    @Value("${user.testAdmin.role.id}")
    private String testAdminRoleId;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private IFdevGroupApi fdevGroupApi;
    @Autowired
    private IDemandService demandService;

    @Autowired
    private NewFdevMapper newFdevMapper;

    @Autowired
    private ITaskApi iTaskApiImpl;

    @Autowired
    private MantisService mantisService;
    /**
     * 查询 所有未读的消息，即messageFlag=1
     * 根据用户的userEnName，currentPage，pageSize
     */
    @Override
    public List<MessageFdev> queryMessage(Map map) throws Exception {
        String userEnName = (String) map.getOrDefault(Dict.USER_EN_NAME, ErrorConstants.DATA_NOT_EXIST);
        Integer pageSize = (Integer) map.getOrDefault(Dict.PAGESIZE, Constants.PAGESIZE_DEF);
        Integer currentPage = (Integer) map.getOrDefault(Dict.CURRENTPAGE, Constants.CURRENTPAGE_DEF);
        Integer start = pageSize * (currentPage - 1);//起始
        List<MessageFdev> resultlist = msgFdevMapper.queryMessage(userEnName, start, pageSize);
        return resultlist;
    }

    /**
     * 根据用户的 userEnName 查询未读消息的总条数
     */
    @Override
    public Map queryMsgCountByUserEnName(Map map) throws Exception {
        String userEnName = (String) map.getOrDefault(Dict.USER_EN_NAME, ErrorConstants.DATA_NOT_EXIST);
        if ("".equals(userEnName) || ErrorConstants.DATA_NOT_EXIST.equals(userEnName)) {
            map.put(Dict.TOTAL, 0);
        } else {
            Integer count = msgFdevMapper.queryMsgCountByUserEnName(userEnName);
            map.put(Dict.TOTAL, count);
        }
        return map;
    }

    /**
     * 消息记录
     * 查询 所有的消息，即messageFlag=1
     * *  根据用户的userEnName，currentPage，pageSize
     */
    @Override
    public List<MessageFdev> queryMessageRecord(Map map) throws Exception {
        String userEnName = (String) map.getOrDefault(Dict.USER_EN_NAME, Constants.FAIL_GET);
        Integer pageSize = (Integer) map.getOrDefault(Dict.PAGESIZE, Constants.PAGESIZE_DEF);
        Integer currentPage = (Integer) map.getOrDefault(Dict.CURRENTPAGE, Constants.CURRENTPAGE_DEF);
        Integer start = pageSize * (currentPage - 1);//起始
        String messageFlag = (String) map.getOrDefault(Dict.MESSAGEFLAG, Constants.FAIL_GET);
        //超级管理员权限判定
        Map<String, Object> currentUser = redisUtils.getCurrentUserInfoMap();
        if (Util.isNullOrEmpty(currentUser)) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        List<String> role_id = (List<String>) currentUser.get(Dict.ROLE_ID);
        List<MessageFdev> resultlist;
        if (role_id.contains(testAdminRoleId)) {
            resultlist = msgFdevMapper.queryMasterMessageRecord(start, pageSize, messageFlag);
        } else {
            resultlist = msgFdevMapper.queryMessageRecord(userEnName, start, pageSize, messageFlag);
        }
        return resultlist;
    }

    /**
     * 消息记录 根据用户的 userEnName 查询消息的总条数
     */
    @Override
    public Map queryMessageRecordCount(Map map) throws Exception {
        String userEnName = (String) map.getOrDefault(Dict.USER_EN_NAME, Constants.FAIL_GET);
        String messageFlag = map.getOrDefault(Dict.MESSAGEFLAG, Constants.FAIL_GET).toString();
        //超级管理员权限判定
        Map<String, Object> currentUser = redisUtils.getCurrentUserInfoMap();
        if (Util.isNullOrEmpty(currentUser)) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        List<String> role_id = (List<String>) currentUser.get(Dict.ROLE_ID);
        Integer count;
        if (role_id.contains(testAdminRoleId)) {
            count = msgFdevMapper.queryMasterMessageRecordCount(messageFlag);
        } else {
            count = msgFdevMapper.queryMessageRecordCount(userEnName, messageFlag);
        }
        map.put(Dict.TOTAL, count);
        return map;
    }


    /**
     * 根据messageId 更新该条数据表示已读
     */
    @Override
    public Map updateOneMsgByMsgId(Map map) throws Exception {
        Integer messageId = (Integer) map.getOrDefault(Dict.MESSAGE_ID, Constants.MESSAGE_ID_DEF);
        Integer count = msgFdevMapper.updateOneMsgByMsgId(messageId);
        map.put("count", count);
        return map;
    }

    /**
     * 根据 userEnName 一键所有未读信息 变 已读
     */
    @Override
    public Map updateAllMsgByUserEnName(Map map) throws Exception {
        String userEnName = (String) map.getOrDefault(Dict.USER_EN_NAME, ErrorConstants.DATA_NOT_EXIST);
        Integer count = msgFdevMapper.updateAllMsgByUserEnName(userEnName);
        map.put("count", count);
        return map;
    }

    /**
     * Fdev推送消息 过来
     * taskNo：任务编号
     * jobId：工单编号
     * taskName：任务名
     * taskStage：工单状态
     * testers：通知人员英文名 Map{(1,"aa"),(2, "22")}
     * taskReason：测试原因
     * JIRANo：JIRA编号
     * taskDesc：描述
     * 如果有人员，就通知人员，如果没有，就通知对应工单下的所有人
     */
    @Override
    public Collection<String> addMsgFromFdev(Map map) throws Exception {
        Collection<String> sendUser = new HashSet<>();
        List<String> testers = (List) map.getOrDefault(Dict.TESTERS, new ArrayList<>());
        MessageFdev messageFdev = null;
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo((String) map.getOrDefault(Dict.JOBID, Constants.FAIL_GET));
        if (!Util.isNullOrEmpty(testers)) {
            sendUser.addAll(testers);
        } else {

            Set<String> allUser = new HashSet<>();
            // 发送给 管理员
            if (!Util.isNullOrEmpty(workOrder.getWorkManager()))
                allUser.add(workOrder.getWorkManager());

            //发送 给测试组长
            if (!Util.isNullOrEmpty(workOrder.getGroupLeader()))
                allUser.addAll(Arrays.asList(workOrder.getGroupLeader().split(",")));

            // 发送给 测试人员
            if (!Util.isNullOrEmpty(workOrder.getTesters()))
                allUser.addAll(Arrays.asList(workOrder.getTesters().split(",")));
            sendUser.addAll(allUser);
        }
        Iterator<String> iterator = sendUser.iterator();
        while (iterator.hasNext()) {
            messageFdev = new MessageFdev();
            String userEnName = iterator.next().toString();
            messageFdev.setRqrNo(workOrder.getDemandNo());
            messageFdev.setUserEnName(userEnName);
            messageFdev.setTaskNo((String) map.getOrDefault(Dict.JOBNO, Constants.FAIL_GET));
            messageFdev.setTaskName((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET));
            messageFdev.setWorkNo((String) map.getOrDefault(Dict.JOBID, Constants.FAIL_GET));
            messageFdev.setWorkStage((String) map.getOrDefault(Dict.TASKSTAGE, Constants.FAIL_GET));
            messageFdev.setTaskReason((String) map.getOrDefault(Dict.TASKREASON, Constants.FAIL_GET));
            messageFdev.setJiraNo((String) map.getOrDefault(Dict.JIRANO, Constants.FAIL_GET));
            messageFdev.setTaskDesc((String) map.getOrDefault(Dict.TASKDESC, Constants.FAIL_GET));
            messageFdev.setCreateTime((int) (new Date().getTime() / 1000));
            messageFdev.setMessageFlag(Constants.NUMBER_1); //未读
            msgFdevMapper.addMsgFromFdev(messageFdev);
        }

        return sendUser;
    }

    @Override
    public Collection<String> queryAllUser(Map map) throws Exception {
        Collection<String> sendUser = new HashSet<>();
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo((String) map.getOrDefault(Dict.JOBID, Constants.FAIL_GET));
        Set<String> allUser = new HashSet<>();
        // 发送给 管理员
        if (!Util.isNullOrEmpty(workOrder.getWorkManager()))
            allUser.add(workOrder.getWorkManager());

        //发送 给测试组长
        if (!Util.isNullOrEmpty(workOrder.getGroupLeader()))
            allUser.addAll(Arrays.asList(workOrder.getGroupLeader().split(",")));

        // 发送给 测试人员
        if (!Util.isNullOrEmpty(workOrder.getTesters()))
            allUser.addAll(Arrays.asList(workOrder.getTesters().split(",")));
        sendUser.addAll(allUser);
        return sendUser;
    }

    /**
     * 新增fdev提测记录
     *
     * @param map
     * @return
     * @throws Exception
     */
    public String addFdevSitMsg(Map<String, Object> map) throws Exception {
        //回归测试范围
        String regressionTestScope = (String) map.get(Dict.REGRESSIONTESTSCOPE);
        //接口变动
        String interfaceChange = (String) map.get(Dict.INTERFACECHANGE);
        //其他系统变动
        String otherSystemChange = (String) map.get(Dict.OTHERSYSTEMCHANGE);
        //数据库变动
        String databaseChange = (String) map.get(Dict.DATABASECHANGE);
        //测试环境
        String env = (String) map.get(Dict.TESTENV);
        //开发人员
        List<String> developers = (List<String>) map.get(Dict.DEVELOPER);
        //应用名
        String appName = (String) map.get(Dict.APPNAME);
        //工单号
        String workNo = (String) map.get(Dict.JOBID);
        //客户端版本
        String clientVersion = (String) map.get(Dict.CLIENTVERSION);
        //需求编号
        String rqrNo = (String) map.get(Dict.RQRNO);
        //任务组id
        String groupId = (String) map.get(Dict.GROUPID);
        //抄送人员
        String copyTo = "";
        if(!CommonUtils.isNullOrEmpty(map.get(Dict.COPYTO))) {
            copyTo = String.join(",", (List<String>) map.get(Dict.COPYTO));
        }
        FdevSitMsg fdevSitMsg = new FdevSitMsg();
        fdevSitMsg.setTaskNo(String.valueOf(map.get(Dict.JOBNO)));
        fdevSitMsg.setTestReason(String.valueOf(map.get(Dict.TASKREASON)));
        fdevSitMsg.setRepairDesc(String.valueOf(map.get(Dict.TASKDESC)));
        fdevSitMsg.setJiraNo("");
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        String date = sdf.format(new Date());
        fdevSitMsg.setCreateTime(date);
        fdevSitMsg.setAppName(appName);
        fdevSitMsg.setClientVersion(clientVersion);
        fdevSitMsg.setRegressionTestScope(regressionTestScope);
        fdevSitMsg.setTestEnv(env);
        fdevSitMsg.setDeveloper(String.join(",", developers));
        fdevSitMsg.setOtherSystemChange(otherSystemChange);
        fdevSitMsg.setDatabaseChange(databaseChange);
        fdevSitMsg.setInterfaceChange(interfaceChange);
        fdevSitMsg.setWorkNo(workNo);
        fdevSitMsg.setRqrNo(rqrNo);
        fdevSitMsg.setGroupId(groupId);
        fdevSitMsg.setCopyTo(copyTo);
        fdevSitMsgMapper.addFdevSitMsg(fdevSitMsg);
        return fdevSitMsg.getId().toString();
    }

    @Override
    public String addFdevSitMsg(FdevSitMsg fdevSitMsg) throws Exception {
        fdevSitMsgMapper.addFdevSitMsg(fdevSitMsg);
        return fdevSitMsg.getId().toString();
    }

    /**
     * 根据任务id查询fdev提测信息
     *
     * @param map
     * @return
     * @throws Exception
     */
    public Map<String, String> queryFdevSitMsg(Map map) throws Exception {
        Map<String, String> result = new HashMap<>();
        String taskNo = String.valueOf(map.get(Dict.ID));
        //先从"FTMS_SUBMIT_SIT_RECORD"获取，若无数据（即任务为改造前的存量数据），则从"msg_fdev"取值
        FdevSitMsg fdevSitMsg = fdevSitMsgMapper.queryFdevSitMsg(taskNo);
        if (!Util.isNullOrEmpty(fdevSitMsg)) {
            result.put(Dict.TASKDESC, fdevSitMsg.getRepairDesc());
        } else {
            String workNo = workOrderMapper.queryWorkNoByTaskId(taskNo, (String) map.get(Dict.ORDERTYPE));
            String desc = msgFdevMapper.queryFirstTestDesc(workNo);
            result.put(Dict.TASKDESC, desc);
        }
        if (Util.isNullOrEmpty(result.get(Dict.TASKDESC))) {
            logger.error("query fdevSitMsg fail");
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"此任务id无提测记录"});
        }
        return result;
    }

    /**
     * 根据消息id查询提测信息详情
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> querySitMsgDetail(String id) throws Exception {
        Map result = new HashMap();
        //处理提测信息
        FdevSitMsg fdevSitMsg = fdevSitMsgMapper.querySitMsgDetail(id);
        String developers = taskApi.queryTaskDevelopNameCns(fdevSitMsg.getTaskNo(), Dict.USERNAMECN);
        fdevSitMsg.setDeveloper(developers);
        if (!Util.isNullOrEmpty(fdevSitMsg.getTesters())) {
            String testsers = myUtil.changeEnNameToCn(fdevSitMsg.getTesters());
            fdevSitMsg.setTesters(testsers);
        }
        //映射提测原因
        getTestReason(fdevSitMsg);
        result.put(Dict.SUBMITINFO, fdevSitMsg);
        String taskNo = fdevSitMsg.getTaskNo();
        String fdevNew = workOrderMapper.queryNewFdevBytaskId(taskNo, Constants.ORDERTYPE_FUNCTION);
        //判断新老fdev
        if ("1".equals(fdevNew)) {
            try {
                Map<String, Object> task = taskApi.getNewTaskById(taskNo);
                String createUserId = (String) task.get(Dict.CREATEUSERID);
                Map<String, Object> map1 = userService.queryUserCoreDataById(createUserId);
                //任务创建人
                Map creator = new HashMap();
                creator.put(Dict.ID, task.get(Dict.CREATEUSERID));
                creator.put(Dict.USER_NAME_CN, map1.get(Dict.USER_NAME_CN));
                creator.put(Dict.USER_NAME_EN, map1.get(Dict.USER_NAME_EN));
                task.put(Dict.CREATOR, creator);
                //任务处理人
                List<String> assigneeList = (List<String>) task.get(Dict.ASSIGNEELIST);
                Map<String, Object> map3 = userService.queryUserCoreDataById(assigneeList.get(0));
                Map developer = new HashMap();
                developer.put(Dict.ID, assigneeList.get(0));
                developer.put(Dict.USER_NAME_CN, map3.get(Dict.USER_NAME_CN));
                developer.put(Dict.USER_NAME_EN, map3.get(Dict.USER_NAME_EN));
                task.put(Dict.DEVELOPER, developer);
                //任务所属组
                Map<String, Object> groupMap = userService.queryGroupDetailById((String) task.get(Dict.ASSIGNEEGROUPID));
                Map group = new HashMap();
                group.put(Dict.NAME, groupMap.get(Dict.NAME));
                group.put(Dict.ID, task.get(Dict.ASSIGNEEGROUPID));
                task.put(Dict.GROUP, group);
                //任务分支信息
                task.put(Dict.FEATURE_BRANCH, task.get(Dict.BRANCHNAME));
                //任务所属实施单元和需求信息
                Map unit = demandService.queryNewUnitInfoById((String) task.get(Dict.IMPLUNITID));
                if (!Util.isNullOrEmpty(unit)) {
                    task.put(Dict.IMPLUNITNUM, unit.get(Dict.IMPLUNITNUM));
                }
                result.put(Dict.TASKINFO, task);
            } catch (Exception e) {
                logger.error("fail to get task info");
                throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询任务详情失败"});
            }
        } else {
            try {
                Map taskInfo = (Map) taskApi.queryTaskDetailByIds(Arrays.asList(fdevSitMsg.getTaskNo())).get(fdevSitMsg.getTaskNo());
                result.put(Dict.TASKINFO, taskInfo);
            } catch (Exception e) {
                logger.error("fail to get task info");
                throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询任务详情失败"});
            }
        }
        return result;
    }

    @Override
    public List<FdevSitMsg> querySitMsgList(String workNo, Integer pageSize, Integer startPage,
                                            List<String> sitGroupIds, List<String> orderGroupIds, String tester, String startDate,
                                            String endDate, String stage, String orderType, Boolean isIncludeChildren) throws Exception {
        //如果要包含子组，查子组
        if (!Util.isNullOrEmpty(sitGroupIds) && isIncludeChildren) {
            Map<String, List<Map>> groupMap = userService.queryChildGroupByIds(sitGroupIds);
            sitGroupIds = new ArrayList<>();
            for (List<Map> groupList : groupMap.values()) {
                sitGroupIds.addAll(groupList.stream().map(group -> (String)group.get(Dict.ID)).distinct().collect(Collectors.toList()));
            }
            sitGroupIds.remove(null);
        }
        if (!Util.isNullOrEmpty(orderGroupIds) && isIncludeChildren) {
            Map<String, List<Map>> groupMap = userService.queryChildGroupByIds(orderGroupIds);
            orderGroupIds = new ArrayList<>();
            for (List<Map> groupList : groupMap.values()) {
                orderGroupIds.addAll(groupList.stream().map(group -> (String)group.get(Dict.ID)).collect(Collectors.toList()));
            }
        }
        List<FdevSitMsg> list = fdevSitMsgMapper.querySitMsgList(workNo, pageSize, startPage, sitGroupIds, tester, startDate, endDate, stage, orderType, orderGroupIds);
        if (!Util.isNullOrEmpty(list)) {
            //过滤新fdev的提测信息
            List<FdevSitMsg> oldFdevSitMsgs = list.stream().filter(e -> !"1".equals(e.getFdev_new())).collect(Collectors.toList());
            List<String> oldTaskIds = oldFdevSitMsgs.stream().map(fdevSitMsg -> fdevSitMsg.getTaskNo()).collect(Collectors.toList());
            oldTaskIds.remove(null);
            //获取老fdev任务信息
            List<Map> tasks = new ArrayList<>();
            if (!Util.isNullOrEmpty(oldTaskIds)) {
                tasks = taskApi.queryTaskBaseInfoByIds(oldTaskIds, null, Arrays.asList(Dict.ID, Dict.NAME, Dict.DEVELOPER));
            }
            //取出任务的开发人员id
            Set<String> taskUserIds = new HashSet<>();
            taskUserIds.remove(null);
            for (Map task : tasks) {
                if(!CommonUtils.isNullOrEmpty(task.get(Dict.DEVELOPER))) {
                    taskUserIds.addAll((List<String>)task.get(Dict.DEVELOPER));
                }
            }
            //批量查询用户信息
            Map userInfoMap = userService.queryUserByIds(taskUserIds);
            //将任务id和任务信息对应起来
            Map<String, Map> taskInfoMap = new HashMap();
            for (Map task : tasks) {
                taskInfoMap.put((String) task.get(Dict.ID), task);
            }
            //批量查询小组信息
            Map<String, Map> groupInfoMap = new HashMap<>();
            Set<String> groupIds = oldFdevSitMsgs.stream().map(fdevSitMsg -> fdevSitMsg.getGroupId()).collect(Collectors.toSet());
            groupIds.addAll(oldFdevSitMsgs.stream().map(fdevSitMsg -> fdevSitMsg.getOrderGroupId()).collect(Collectors.toSet()));
            groupIds.remove(null);
            List<Map> groupList = userService.queryGroupByIds(new ArrayList<>(groupIds));
            if (!CommonUtils.isNullOrEmpty(groupList)) {
                for (Map group : groupList) {
                    groupInfoMap.put((String) group.get(Dict.ID), group);
                }
            }
            for (FdevSitMsg fdevSitMsg : list) {
                //判断是否是新fdev_new
                String fdev_new = fdevSitMsg.getFdev_new();
                if ("1".equals(fdev_new)) {
                    //注入开发人员
                    Map<String, Object> task = getNewTaskById(fdevSitMsg.getTaskNo());
                    if (Util.isNullOrEmpty(task)) {
                        continue;
                    }
                    List<String> assigneeList = (List<String>) task.get("assigneeList");
                    String userId = assigneeList.get(0);
                    //注入开发人员
                    Map<String, Object> map = userService.queryUserCoreDataById(userId);
                    String usernameCn = (String) map.get(Dict.USERNAMECN);
                    fdevSitMsg.setDeveloper(usernameCn);
                    fdevSitMsg.setFdevTaskName((String) task.get(Dict.NAME));
                } else {
                    //拼接开发人员中文名
                    Map taskInfo = taskInfoMap.get(fdevSitMsg.getTaskNo());
                    if (!CommonUtils.isNullOrEmpty(taskInfo)) {
                        List<String> developers = (List<String>) taskInfo.get(Dict.DEVELOPER);
                        StringBuffer developerNameStr = new StringBuffer();
                        String developerName = "";
                        if (!CommonUtils.isNullOrEmpty(developers)) {
                            for (String developer : developers) {
                                if(!CommonUtils.isNullOrEmpty(userInfoMap.get(developer))) {
                                    Map userInfo = (Map) userInfoMap.get(developer);
                                    developerNameStr.append(userInfo.get(Dict.USER_NAME_CN));
                                    developerNameStr.append(",");
                                }
                            }
                            if (developerNameStr.toString().endsWith(",")) {
                                developerName = developerNameStr.substring(0, developerNameStr.length()-1);
                            }
                        }
                        fdevSitMsg.setDeveloper(developerName);
                    }

                    try {
                        fdevSitMsg.setFdevTaskName((String) taskInfo.get(Dict.NAME));
                    } catch (Exception e) {
                        logger.error("fail to get fdevTaskName");
                        fdevSitMsg.setFdevTaskName("/");
                    }
                }
                //设置提测信息所属组(就是任务所属组)名称
                String group_id = fdevSitMsg.getGroupId();
                if (!Util.isNullOrEmpty(group_id)) {
                    Map<String, Object> group = groupInfoMap.get(group_id);
                    if (!Util.isNullOrEmpty(group)) {
                        fdevSitMsg.setGroupName((String) group.get(Dict.NAME));
                    } else {
                        fdevSitMsg.setGroupName("/");
                    }
                } else {
                    fdevSitMsg.setGroupName("/");
                }
                //设置工单所属组名称
                String orderGroupId = fdevSitMsg.getOrderGroupId();
                if (!Util.isNullOrEmpty(orderGroupId)) {
                    Map<String, Object> group = groupInfoMap.get(orderGroupId);
                    if (!Util.isNullOrEmpty(group)) {
                        fdevSitMsg.setOrderGroupName((String) group.get(Dict.NAME));
                    } else {
                        fdevSitMsg.setOrderGroupName("/");
                    }
                } else {
                    fdevSitMsg.setOrderGroupName("/");
                }
                //注入测试人员
                if (!Util.isNullOrEmpty(fdevSitMsg.getTesters())) {
                    String testsers = myUtil.changeEnNameToCn(fdevSitMsg.getTesters());
                    fdevSitMsg.setTesters(testsers);
                }
                //提测原因转中文
                getTestReason(fdevSitMsg);
            }
        }
        return list;
    }

    @Override
    public Integer countSitMsgList(String workNo, List<String> sitGroupIds, List<String> orderGroupIds, String tester, String startDate, String endDate, String stage, String orderType, Boolean isIncludeChildren) throws Exception {
        //如果要包含子组，查子组
        if (!Util.isNullOrEmpty(sitGroupIds) && isIncludeChildren) {
            Map<String, List<Map>> groupMap = userService.queryChildGroupByIds(sitGroupIds);
            sitGroupIds = new ArrayList<>();
            for (List<Map> groupList : groupMap.values()) {
                sitGroupIds.addAll(groupList.stream().map(group -> (String)group.get(Dict.ID)).collect(Collectors.toList()));
            }
        }
        if (!Util.isNullOrEmpty(orderGroupIds) && isIncludeChildren) {
            Map<String, List<Map>> groupMap = userService.queryChildGroupByIds(orderGroupIds);
            orderGroupIds = new ArrayList<>();
            for (List<Map> groupList : groupMap.values()) {
                orderGroupIds.addAll(groupList.stream().map(group -> (String)group.get(Dict.ID)).collect(Collectors.toList()));
            }
        }
        return fdevSitMsgMapper.countSitMsgList(workNo, sitGroupIds, tester, startDate, endDate, stage, orderType, orderGroupIds);
    }

    private void getTestReason(FdevSitMsg fdevSitMsg) {
        String testReason = "";
        switch (fdevSitMsg.getTestReason()) {
            case "1":
                testReason = Constants.TEST_REASON_1;
                break;
            case "2":
                testReason = Constants.TEST_REASON_2;
                break;
            case "3":
                testReason = Constants.TEST_REASON_3;
                break;

        }
        fdevSitMsg.setTestReason(testReason);
    }

    /**
     * 发送提交业测邮件
     *
     * @param map
     * @throws Exception
     */
    @Override
    public void sendStartUatMail(Map map) throws Exception {
        String workNo = String.valueOf(map.get(Dict.WORKNO));
        WorkOrder order = workOrderMapper.queryWorkOrderByNo(workNo);
        String fdevNew = order.getFdevNew();
        if ("1".equals(fdevNew)) {
            Set<String> emails = new HashSet();
            //判断是否是fdev创建工单
            String flag = order.getWorkOrderFlag();
            if ("0".equals(flag)) {
                logger.error("order does not come from fdev");
                throw new FtmsException(ErrorConstants.MAINTASKNO_NOT_EXIST_ERROR);
            }
            Set<String> to = new HashSet();
            if (map.keySet().contains("additionalCCPerson")) {
                List<String> additionalCCPerson = (List<String>) map.get("additionalCCPerson");
                emails.addAll(additionalCCPerson);
            }
            List<String> taskNames = new ArrayList<>();
            //发fdev获取任务人员信息，根据工单获取任务
            List<TaskList> taskLists = taskListMapper.queryTaskByNo(workNo);
            List<String> taskIds = taskLists.stream().map(e -> e.getTaskno()).collect(Collectors.toList());
            Set<String> copyToIds = new HashSet<>();
            if (!Util.isNullOrEmpty(taskIds)) {
                for (String taskId : taskIds) {
                    Map<String, Object> task = iTaskApiImpl.getNewTaskById(taskId);
                    if (Util.isNullOrEmpty(task)) {
                        logger.error("order does not come from fdev");
                        throw new FtmsException(ErrorConstants.MAINTASKNO_NOT_EXIST_ERROR);
                    }
                    List<String> assigneeList = (List<String>) task.get("assigneeList");
                    to.add(assigneeList.get(0));
                    to.add((String) task.get("createUserId"));
                    taskNames.add((String) task.get(Dict.NAME));
                }
                List<FdevSitMsg> copyTos = fdevSitMsgMapper.queryAllCopyToByTaskIds(taskIds);
                copyTos.forEach(copyTo -> copyToIds.addAll(Arrays.asList((copyTo.getCopyTo()).split(","))));
            }
            //玉衡三角色添加收件人
            String workManager = order.getWorkManager();//工单负责人
            String workLeader = order.getGroupLeader();//小组长
            String testers = order.getTesters();//测试人员
            Set<String> ftms = new HashSet<>();
            ftms.add(workManager);
            if (!Util.isNullOrEmpty(workLeader)) {
                ftms.addAll(Arrays.asList(workLeader.split(",")));
            }
            if (!Util.isNullOrEmpty(testers)) {
                ftms.addAll(Arrays.asList(testers.split(",")));
            }
            ftms.remove("");
            to.addAll(getFdevUserIdsByEn(ftms));
            to.addAll(copyToIds);
            for (String users : to) {
                try {
                    Map user = userService.queryUserCoreDataById(users);
                    if (!Util.isNullOrEmpty(user)) {
                        emails.add((String) user.getOrDefault("email", null));
                    }
                } catch (Exception e) {
                    logger.error("id:" + users + "用户查询异常");
                }
            }
            if (Util.isNullOrEmpty(emails)) {
                logger.error("no target to send mail");
                throw new FtmsException(ErrorConstants.NO_MAIL_TARGET_ERROR);
            }
            //邮件数组
            if (emails.size() > 0) {
                emails.remove("");
            }
            String[] target = emails.toArray(new String[emails.size()]);
            //查询fdev组
            String groupName = "";
            //获取uat联系人
            String uatContact = "";
            String uatContactMail = "";
            String uatContactName = "";
            String jira = "";
            if (!Util.isNullOrEmpty(order.getFdevGroupId())) {
                groupName = "【" + String.valueOf(fdevGroupApi.queryGroupDetail(order.getFdevGroupId()).get(Dict.NAME)) + "】";
                uatContact = groupMapper.queryUatContact(order.getFdevGroupId());
            }
            if (!Util.isNullOrEmpty(uatContact)) {
                Map<String, Object> user = userService.queryUserCoreDataByNameEn(uatContact);
                if (!Util.isNullOrEmpty(user)) {
                    uatContactMail = (String) user.getOrDefault(Dict.EMAIL, null);
                    uatContactName = (String) user.getOrDefault("user_name_cn", null);
                    jira += "如有问题请在jira上登记，并指派给" + uatContactName + "（" + uatContactMail + "）";
                }
            }
            //组装model数据
            HashMap<String, String> model = new HashMap<>();
            String rqrmntNo = order.getDemandNo();
            String rqrmntName = order.getDemandName();
            model.put(Dict.RQRMNTNO, rqrmntNo);
            model.put(Dict.RQRMNTNAME, rqrmntName);
            model.put(Dict.MAINTASKNAME, order.getMainTaskName());
            model.put(Dict.PLANPRODATE, ifNull(String.valueOf(map.get(Dict.PLANPRODATE)), "无"));
            if (Util.isNullOrEmpty(map.get(Dict.REPAIR_DESC))) {
                model.put(Dict.REPAIR_DESC, "无");
            } else {
                String repairDesc = String.valueOf(map.get(Dict.REPAIR_DESC)).replace("\n", "<br/>");
                model.put(Dict.REPAIR_DESC, MyUtil.replace160Char(repairDesc));
            }
            model.put(Dict.REGRESSIONTESTSCOPE, ifNull(String.valueOf(map.get(Dict.REGRESSIONTESTSCOPE)), "无"));
            model.put(Dict.INTERFACECHANGE, ifNull(String.valueOf(map.get(Dict.INTERFACECHANGE)), "否"));
            model.put(Dict.DATABASECHANGE, ifNull(String.valueOf(map.get(Dict.DATABASECHANGE)), "否"));
            model.put(Dict.CLIENTVERSION, ifNull(String.valueOf(map.get(Dict.CLIENTVERSION)), "无"));
            if (Util.isNullOrEmpty(map.get(Dict.TESTENV))) {
                model.put(Dict.TESTENV, "无");
            } else {
                String repairDesc = String.valueOf(map.get(Dict.TESTENV)).replace("\n", "<br/>");
                model.put(Dict.TESTENV, MyUtil.replace160Char(repairDesc));
            }
            model.put(Dict.APPNAME, ifNull(String.valueOf(map.get(Dict.APPNAME)), "无"));
            model.put(Dict.OTHERSYSTEMCHANGE, ifNull(String.valueOf(map.get(Dict.OTHERSYSTEMCHANGE)), "否"));
            model.put(Dict.DEVELOPER, ifNull(String.valueOf(map.get(Dict.DEVELOPER)), "无"));
            model.put(Dict.TASKLIST, String.join("、", taskNames));
            model.put(Dict.UNIT, order.getUnit());
            if (Util.isNullOrEmpty(map.get(Dict.UATREMARK))) {
                model.put(Dict.UATREMARK, "无");
            } else {
                String repairDesc = String.valueOf(map.get(Dict.UATREMARK)).replace("\n", "<br/>");
                model.put(Dict.UATREMARK, MyUtil.replace160Char(repairDesc));
            }
            model.put(Dict.JIRA, jira);
            //需求
            String file = "";
            model.put(Dict.FILE, file);
            String subject = groupName + "【提交业务测试】"
                    + rqrmntNo + "  " + String.valueOf(order.getMainTaskName());
            mailUtil.sendSitReportMail(subject, "ftms_uatInform", model, target);
            //邮件发送后，修改工单的sit_flag为2，代表已提业测
            workOrderMapper.updateUatFlag(workNo);
        } else {
            //如果主任务编号不存在，则不是fdev的任务，无法发送邮件;如果没有提测记录，则报错
            String flag = order.getWorkOrderFlag();
            if ("0".equals(flag)) {
                logger.error("order does not come from fdev");
                throw new FtmsException(ErrorConstants.MAINTASKNO_NOT_EXIST_ERROR);
            }
            List<String> taskNames = new ArrayList<>();
            Set<String> to = new HashSet();
            //发fdev获取任务人员信息
            String rqrmntNo = "";
            String rqrSerialNo = "";
            String rqrmntName = "";
            if (!Util.isNullOrEmpty(order.getMainTaskNo())) {
                Map send = new HashMap();
                send.put(Dict.ID, order.getMainTaskNo());
                send.put(Dict.REST_CODE, "queryTaskDetail");
                send = (Map) restTransport.submitSourceBack(send);
                rqrmntNo = String.valueOf(((Map) send.get(Dict.DEMAND)).get(Dict.OA_CONTACT_NO));//需求编号
                rqrSerialNo = String.valueOf(send.get(Dict.RQRMNT_NO));//需求编码
                rqrmntName = String.valueOf(((Map) send.get(Dict.DEMAND)).get(Dict.OA_CONTACT_NAME));
                taskNames.add((String) send.get(Dict.NAME));
            } else {
                Map<String, Object> unitResult = demandService.queryByFdevNoAndDemandId(order.getUnit());
                rqrmntNo = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NO));
                rqrSerialNo = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.ID));
                rqrmntName = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NAME));
            }
            List<TaskList> taskList = taskListMapper.queryTaskByNo(workNo);
            if (!Util.isNullOrEmpty(order.getMainTaskNo())) {
                Map sendSub = new HashMap();
                sendSub.put(Dict.ID, order.getMainTaskNo());
                sendSub.put(Dict.REST_CODE, "queryTaskDetail");
                sendSub = (Map) restTransport.submitSourceBack(sendSub);
                addUserIdByRoleName(to, sendSub, Dict.SPDBMASTER);//行内负责人
                addUserIdByRoleName(to, sendSub, Dict.MASTER);//任务负责人
                addUserIdByRoleName(to, sendSub, Dict.DEVELOPER);//开发人员
            }
            if (!Util.isNullOrEmpty(taskList)) {
                for (TaskList t : taskList) {
                    Map sendSub = new HashMap();
                    sendSub.put(Dict.ID, t.getTaskno());
                    sendSub.put(Dict.REST_CODE, "queryTaskDetail");
                    sendSub = (Map) restTransport.submitSourceBack(sendSub);
                    addUserIdByRoleName(to, sendSub, Dict.SPDBMASTER);//行内负责人
                    addUserIdByRoleName(to, sendSub, Dict.MASTER);//任务负责人
                    addUserIdByRoleName(to, sendSub, Dict.DEVELOPER);//开发人员
                    taskNames.add((String) sendSub.get(Dict.NAME));
                }
            }
            Set<String> emails = new HashSet();
            //玉衡三角色添加收件人
            String workManager = order.getWorkManager();//工单负责人
            String workLeader = order.getGroupLeader();//小组长
            String testers = order.getTesters();//测试人员
            Set<String> ftms = new HashSet<>();
            ftms.add(workManager);
            if (!Util.isNullOrEmpty(workLeader)) {
                ftms.addAll(Arrays.asList(workLeader.split(",")));
            }
            if (!Util.isNullOrEmpty(testers)) {
                ftms.addAll(Arrays.asList(testers.split(",")));
            }
            ftms.remove("");
            to.addAll(getFdevUserIdsByEn(ftms));
            for (String users : to) {
                try {
                    Map user = userService.queryUserCoreDataById(users);
                    if (!Util.isNullOrEmpty(user)) {
                        emails.add((String) user.getOrDefault("email", null));
                    }
                } catch (Exception e) {
                    logger.error("id:" + users + "用户查询异常");
                }
            }
            if (Util.isNullOrEmpty(emails)) {
                logger.error("no target to send mail");
                throw new FtmsException(ErrorConstants.NO_MAIL_TARGET_ERROR);
            }
            //邮件数组
            emails.remove("");
            String[] target = emails.toArray(new String[emails.size()]);
            //查询fdev组
            String groupName = "";
            //获取uat联系人
            String uatContact = "";
            String uatContactMail = "";
            String uatContactName = "";
            String jira = "";
            if (!Util.isNullOrEmpty(order.getFdevGroupId())) {
                groupName = "【" + String.valueOf(fdevGroupApi.queryGroupDetail(order.getFdevGroupId()).get(Dict.NAME)) + "】";
                uatContact = groupMapper.queryUatContact(order.getFdevGroupId());
            }
            if (!Util.isNullOrEmpty(uatContact)) {
                Map<String, Object> user = userService.queryUserCoreDataByNameEn(uatContact);
                if (!Util.isNullOrEmpty(user)) {
                    uatContactMail = (String) user.getOrDefault(Dict.EMAIL, null);
                    uatContactName = (String) user.getOrDefault("user_name_cn", null);
                    jira += "如有问题请在jira上登记，并指派给" + uatContactName + "（" + uatContactMail + "）";
                }
            }
            //组装model数据
            HashMap<String, String> model = new HashMap<>();
            model.put(Dict.RQRMNTNO, rqrmntNo);
            model.put(Dict.RQRMNTNAME, rqrmntName);
            model.put(Dict.MAINTASKNAME, order.getMainTaskName());
            model.put(Dict.PLANPRODATE, ifNull(String.valueOf(map.get(Dict.PLANPRODATE)), "无"));
            if (Util.isNullOrEmpty(map.get(Dict.REPAIR_DESC))) {
                model.put(Dict.REPAIR_DESC, "无");
            } else {
                String repairDesc = String.valueOf(map.get(Dict.REPAIR_DESC)).replace("\n", "<br/>");
                model.put(Dict.REPAIR_DESC, MyUtil.replace160Char(repairDesc));
            }
            model.put(Dict.REGRESSIONTESTSCOPE, ifNull(String.valueOf(map.get(Dict.REGRESSIONTESTSCOPE)), "无"));
            model.put(Dict.INTERFACECHANGE, ifNull(String.valueOf(map.get(Dict.INTERFACECHANGE)), "否"));
            model.put(Dict.DATABASECHANGE, ifNull(String.valueOf(map.get(Dict.DATABASECHANGE)), "否"));
            model.put(Dict.CLIENTVERSION, ifNull(String.valueOf(map.get(Dict.CLIENTVERSION)), "无"));
            if (Util.isNullOrEmpty(map.get(Dict.TESTENV))) {
                model.put(Dict.TESTENV, "无");
            } else {
                String repairDesc = String.valueOf(map.get(Dict.TESTENV)).replace("\n", "<br/>");
                model.put(Dict.TESTENV, MyUtil.replace160Char(repairDesc));
            }
            model.put(Dict.APPNAME, ifNull(String.valueOf(map.get(Dict.APPNAME)), "无"));
            model.put(Dict.OTHERSYSTEMCHANGE, ifNull(String.valueOf(map.get(Dict.OTHERSYSTEMCHANGE)), "否"));
            model.put(Dict.DEVELOPER, ifNull(String.valueOf(map.get(Dict.DEVELOPER)), "无"));
            model.put(Dict.TASKLIST, String.join("、", taskNames));
            model.put(Dict.UNIT, order.getUnit());
            if (Util.isNullOrEmpty(map.get(Dict.UATREMARK))) {
                model.put(Dict.UATREMARK, "无");
            } else {
                String repairDesc = String.valueOf(map.get(Dict.UATREMARK)).replace("\n", "<br/>");
                model.put(Dict.UATREMARK, MyUtil.replace160Char(repairDesc));
            }
            model.put(Dict.JIRA, jira);
            //需求
            String file = "";
            if (!Util.isNullOrEmpty(rqrSerialNo)) {
                file = "需求相关文档地址：  " + fdevTask + "/fdev/#/rqrmn/rqrProfile/" + rqrSerialNo;
            }
            model.put(Dict.FILE, file);
            String subject = groupName + "【提交业务测试】"
                    + rqrmntNo + "  " + String.valueOf(order.getMainTaskName());
            mailUtil.sendSitReportMail(subject, "ftms_uatInform", model, target);
            //邮件发送后，修改工单的sit_flag为2，代表已提业测
            workOrderMapper.updateUatFlag(workNo);
        }
    }

    private void addUserIdByRoleName(Set<String> target, Map source, String role) {
        if (!Util.isNullOrEmpty(source.get(role))) {
            List<Map<String, String>> list = (List<Map<String, String>>) source.get(role);
            for (Map m : list) {
                target.add(String.valueOf(m.get(Dict.ID)));
            }
        }
    }

    /**
     * 将fdev任务详情中的干系人纳入消息接收人
     *
     * @param target
     * @param source
     * @param role
     */
    private void addTargetByRoleName(Set<String> target, Map source, String role) {
        if (!Util.isNullOrEmpty(source.get(role))) {
            List<Map<String, String>> list = (List<Map<String, String>>) source.get(role);
            for (Map m : list) {
                target.add(m.get(Dict.USER_NAME_EN).toString());
            }
        }
    }

    private String ifNull(String str, String replace) {
        if (Util.isNullOrEmpty(str) || "null".equals(str)) {
            return replace;
        }
        return str;
    }

    /**
     * 发送sit测试报告邮件和玉衡通知
     *
     * @param map
     * @throws Exception
     */
    @Override
    public void sendSitDoneMail(Map map) throws Exception {
        String workNo = String.valueOf(map.get(Dict.WORKNO));
        String mainTaskName = String.valueOf(map.get(Dict.MAINTASKNAME));
        Set<String> emails = new HashSet();
        if (map.keySet().contains("additionalCCPerson")) {
            List<String> additionalCCPerson = (List<String>) map.get("additionalCCPerson");
            emails.addAll(additionalCCPerson);
        }
        //玉衡三角色添加收件人
        WorkOrder order = workOrderMapper.queryWorkOrderByNo(workNo);
        String workManager = order.getWorkManager();//工单负责人
        String workLeader = order.getGroupLeader();//小组长
        String testers = order.getTesters();//测试人员
        Set<String> ftms = new HashSet<>();
        ftms.add(workManager);
        if (!Util.isNullOrEmpty(workLeader)) {
            ftms.addAll(Arrays.asList(workLeader.split(",")));
        }
        if (!Util.isNullOrEmpty(testers)) {
            ftms.addAll(Arrays.asList(testers.split(",")));
        }
        ftms.remove("");
        String imageLink = String.valueOf(map.getOrDefault(Dict.IMAGELINK, Constants.FAIL_GET));
        if (Util.isNullOrEmpty(imageLink)) {
            imageLink = order.getImageLink();
        }
        //发fdev获取任务人员信息
        Set<String> to = new HashSet();
        String mainTaskNo = order.getMainTaskNo();
        Map send = new HashMap();
        String rqrmntNo = "";
        String groupName = "";
        String unit = "";
        String rqrmntName = "";
        List<String> taskNames = new ArrayList<>();
        // 存放提测时抄送的人，后面加入到收件人中
        Set<String> copyToIds = new HashSet<>();
        if (!Util.isNullOrEmpty(mainTaskNo)) {
            //存在主任务编号，说明是fdev任务且工单是根据主任务生成
            try {
                send.put(Dict.ID, mainTaskNo);
                send.put(Dict.REST_CODE, "queryTaskDetail");
                send = (Map) restTransport.submitSourceBack(send);
                addUserIdByRoleName(to, send, Dict.SPDBMASTER);//行内负责人
                addUserIdByRoleName(to, send, Dict.MASTER);//任务负责人
                addUserIdByRoleName(to, send, Dict.DEVELOPER);//开发人员
                rqrmntNo = String.valueOf(((Map) send.get(Dict.DEMAND)).get(Dict.OA_CONTACT_NO));
                rqrmntName = String.valueOf(((Map) send.get(Dict.DEMAND)).get(Dict.OA_CONTACT_NAME));
                unit = rqrmntNo;
                taskNames.add((String) send.get(Dict.NAME));
            } catch (Exception e) {
                logger.error("fail to query fdev task info" + mainTaskNo + ":" + e);
            }
            if (!Util.isNullOrEmpty(order.getFdevGroupId())) {
                groupName = "【" + String.valueOf(fdevGroupApi.queryGroupDetail(order.getFdevGroupId()).get(Dict.NAME)) + "】";
            }
        } else {
            if ("1".equals(order.getWorkOrderFlag())) {
                if ("1".equals(order.getFdevNew())) {
                    rqrmntNo = order.getDemandNo();
                    rqrmntName = order.getDemandName();
                    String fdevGroupId = order.getFdevGroupId();
                    if (!Util.isNullOrEmpty(fdevGroupId)) {
                        groupName = "【" + fdevGroupApi.queryGroupDetail(fdevGroupId).get(Dict.NAME) + "】";
                    }
                    unit = rqrmntNo;
                    //根据任务taskNo查相关人员
                    List<TaskList> taskList = taskListMapper.queryTaskByNo(workNo);
                    List<String> taskIds = taskList.stream().map(e -> e.getTaskno()).collect(Collectors.toList());

                    if (!Util.isNullOrEmpty(taskIds)) {
                        for (String taskId : taskIds) {
                            Map<String, Object> task = iTaskApiImpl.getNewTaskById(taskId);
                            if (Util.isNullOrEmpty(task)) {
                                logger.error("order does not come from fdev");
                                throw new FtmsException(ErrorConstants.MAINTASKNO_NOT_EXIST_ERROR);
                            }
                            List<String> assigneeList = (List<String>) task.get("assigneeList");
                            to.add(assigneeList.get(0));
                            to.add((String) task.get("createUserId"));
                            taskNames.add((String) task.get(Dict.NAME));
                        }
                        List<FdevSitMsg> copyTos = fdevSitMsgMapper.queryAllCopyToByTaskIds(taskIds);
                        copyTos.forEach(copyTo -> copyToIds.addAll(Arrays.asList((copyTo.getCopyTo()).split(","))));
                    }
                } else {
                    //无主任务编号，但是是fdev任务，说明工单根据实施单元生成
                    unit = order.getUnit();
                    Map<String, Object> unitResult = new HashMap<>();
                    try {
                        unitResult = demandService.queryByFdevNoAndDemandId(unit);
                        rqrmntNo = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NO));
                        rqrmntName = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NAME));
                        String fdevGroupId = String.valueOf(((Map) unitResult.get(Dict.IMPLEMENT_UNIT_INFO)).get(Dict.GROUP));
                        if (!Util.isNullOrEmpty(fdevGroupId)) {
                            groupName = "【" + String.valueOf(fdevGroupApi.queryGroupDetail(fdevGroupId).get(Dict.NAME)) + "】";
                        }
                    } catch (Exception e) {
                        logger.error("fail to query fdev unit info" + order.getUnit() + ":" + e);
                    }
                    //查询任务集，根据子任务taskNo查相关人员
                    try {
                        List<TaskList> taskList = taskListMapper.queryTaskByNo(workNo);
                        if (!Util.isNullOrEmpty(taskList)) {
                            for (TaskList t : taskList) {
                                Map sendSub = new HashMap();
                                sendSub.put(Dict.ID, t.getTaskno());
                                sendSub.put(Dict.REST_CODE, "queryTaskDetail");
                                sendSub = (Map) restTransport.submitSourceBack(sendSub);
                                addUserIdByRoleName(to, sendSub, Dict.SPDBMASTER);//行内负责人
                                addUserIdByRoleName(to, sendSub, Dict.MASTER);//任务负责人
                                addUserIdByRoleName(to, sendSub, Dict.DEVELOPER);//开发人员
                                taskNames.add((String) sendSub.get(Dict.NAME));
                            }
                        }
                    } catch (Exception e) {
                        logger.error("fail to query fdev subtask info");
                    }
                }
            }
        }
        map.put(Dict.RQRMNTNO, ifNull(rqrmntNo, ""));
        map.put(Dict.RQRMNTNAME, ifNull(rqrmntName, ""));
        map.put(Dict.UNIT, ifNull(unit, "无"));

        Set ftmsIds = getFdevUserIdsByEn(ftms);
        to.addAll(copyToIds);
        to.addAll(ftmsIds);
        for (String users : to) {
            try {
                Map<String, Object> user = userService.queryUserCoreDataById(users);
                if (!Util.isNullOrEmpty(user)) {
                    emails.add((String) user.getOrDefault(Dict.EMAIL, null));
                }
            } catch (Exception e) {
                logger.error("id:" + users + "用户查询异常");
            }
        }
        String subject = "";//收件人
        String templateName = "";//报告模版文件名
        String messageType = "";//报告发送完成后的消息通知类型
        //如果是安全测试报告，添加固定收件人
        if (Constants.ORDERTYPE_SECURITY.equals(order.getOrderType())) {
            emails.addAll(addressee);
            //查询功能测试组长的邮箱
//            Map<String, String> resultMap = groupMapper.queryAutoWorkOrder(order.getFdevGroupId());
            String groupLeaderStr = order.getGroupLeader();//功能测试组长
            if (!CommonUtils.isNullOrEmpty(groupLeaderStr)) {
                List<String> groupLeaderList = Arrays.asList(groupLeaderStr.split(","));
                for (String leaderName : groupLeaderList) {
                    Map<String, Object> userInfo = userService.queryUserCoreDataByNameEn(leaderName);
                    if (!CommonUtils.isNullOrEmpty(userInfo)) {
                        emails.add((String) userInfo.get(Dict.EMAIL));
                    }
                }
            }
            subject = groupName + "【安全测试报告】" + order.getMainTaskName();
            templateName = "ftms_securityTestReport";
            messageType = "安全测试完成";
        }else {
            subject = groupName + "【内测报告】" + rqrmntNo + " " + mainTaskName;
            templateName = "ftms_sitReport";
            messageType = "SIT测试完成";
        }
        if (Util.isNullOrEmpty(emails)) {
            logger.error("no target to send mail");
            throw new FtmsException(ErrorConstants.NO_MAIL_TARGET_ERROR);
        }
        map.put(Dict.IMAGELINK, Util.isNullOrEmpty(imageLink) ? " " : imageLink);
        map.put(Dict.TASKLIST, String.join("、", taskNames));
        //邮件数组
        String[] targetEmail = emails.toArray(new String[emails.size()]);
        mailUtil.sendSitReportMail(subject, templateName, (HashMap) map, targetEmail);
        //发送玉衡通知
        Set<String> target = new HashSet<String>();
        //不对当前用户发送消息
        String userName = myUtil.getCurrentUserEnName();
        target.addAll(ftms);
        target.remove(userName);
        Map messageMap = new HashMap();
        messageMap.put("content", mainTaskName);
        messageMap.put("target", new ArrayList<>(target));
        messageMap.put("type", messageType);
        messageMap.put("hyperlink", "");
        if (!(target == null || target.size() <= 0)) {
            iNotifyApi.sendUserNotify(messageMap);
        }
        workOrderMapper.updateImageLink(workNo, imageLink);
    }

    private Set getFdevUserIdsByEn(Set<String> ftms) throws Exception {
        Set<String> result = new HashSet<>();
        for (String en : ftms) {
            Map<String, Object> user = userService.queryUserCoreDataByNameEn(en);
            if (!Util.isNullOrEmpty(user)) {
                result.add((String) user.get(Dict.ID));
            }
        }
        return result;
    }

    /**
     * fdev提测后更新任务tag
     *
     * @param map
     * @throws Exception
     */
    public void fdevSubmitSitTag(Map map) throws Exception {
        String taskNo = String.valueOf(map.get(Dict.JOBNO));
        Map send = new HashMap();
        send.put(Dict.ID, taskNo);
        send.put(Dict.REST_CODE, "queryTaskDetail");
        send = (Map) restTransport.submitSourceBack(send);
        List<String> tagList = (List<String>) send.get(Dict.TAG);
        String tag = "已提内测";
        if (!tagList.contains(tag)) {
            tagList.add(tag);
        }
        tagList.remove("提测打回");
        tagList.remove("内测完成");
        Map sendFdev = new HashMap();
        sendFdev.put(Dict.ID, taskNo);
        sendFdev.put(Dict.TAG, tagList);
        sendFdev.put(Dict.REST_CODE, "updatetaskinner");
        restTransport.submitSourceBack(sendFdev);
    }

    @Override
    public List<FdevSitMsg> queryTaskSitMsg(String taskNo) throws Exception {
        return fdevSitMsgMapper.queryTaskSitMsg(taskNo, Constants.ORDERTYPE_ALL);
    }

    /**
     * 查询业测邮件正文信息
     *
     * @param map
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, String> queryUatMailInfo(Map map) throws Exception {
        Map<String, String> result = new HashMap<>();
        String workNo = String.valueOf(map.get(Dict.WORKNO));
        WorkOrder order = workOrderMapper.queryWorkOrderByNo(workNo);
        //如果主任务编号不存在，则不是fdev的任务，无法发送邮件;如果没有提测记录，则报错
        List<FdevSitMsg> fdevSitMsg = orderUtil.checkFdevTaskAndSitMsg(order);
        //获取工单信息
        result.put(Dict.MAINTASKNAME, ifNull(order.getMainTaskName(), "无"));//主任务名
        result.put(Dict.UNIT, ifNull(order.getUnit(), "无"));//需求编号
        result.put(Dict.PLANPRODATE, ifNull(order.getPlanProDate(), "无"));//计划投产日期
        Set<String> repair_desc = new HashSet<>();
        Set<String> regressionTestScope = new HashSet<>();
        Set<String> interfaceChange = new HashSet<>();
        Set<String> databaseChange = new HashSet<>();
        Set<String> clientVersion = new HashSet<>();
        Set<String> testEnv = new HashSet<>();
        Set<String> appName = new HashSet<>();
        Set<String> otherSystemChange = new HashSet<>();
        Set<String> developer = new HashSet<>();
        for (FdevSitMsg fsg : fdevSitMsg) {
            if (!Util.isNullOrEmpty(fsg.getRepairDesc())) {
                repair_desc.add(fsg.getRepairDesc());
            }
            if (!"不涉及".equals((fsg.getRegressionTestScope()))) {
                regressionTestScope.add(fsg.getRegressionTestScope());
            }
            if (!"否".equals((fsg.getInterfaceChange()))) {
                interfaceChange.add(fsg.getInterfaceChange());
            }
            if (!"否".equals((fsg.getDatabaseChange()))) {
                databaseChange.add(fsg.getDatabaseChange());
            }
            if (!"不涉及".equals((fsg.getClientVersion()))) {
                clientVersion.add(fsg.getClientVersion());
            }
            if (!"不涉及".equals((fsg.getTestEnv()))) {
                testEnv.add(fsg.getTestEnv());
            }
            if (!Util.isNullOrEmpty(fsg.getAppName())) {
                appName.add(fsg.getAppName());
            }
            if (!"否".equals(fsg.getOtherSystemChange())) {
                otherSystemChange.add(fsg.getOtherSystemChange());
            }
            if (!Util.isNullOrEmpty(fsg.getDeveloper())) {
                String[] devs = fsg.getDeveloper().split(",");
                for (String dev : devs) {
                    Map<String, Object> user = userService.queryUserCoreDataByNameEn(dev);
                    if (!Util.isNullOrEmpty(user)) {
                        developer.add((String) user.getOrDefault("user_name_cn", ""));
                    }
                }
            }
        }
        result.put(Dict.REPAIR_DESC, ifNull(String.join(";", repair_desc), "无"));//测试内容
        result.put(Dict.REGRESSIONTESTSCOPE, ifNull(String.join(";", regressionTestScope), "不涉及"));//回归测试范围
        result.put(Dict.INTERFACECHANGE, ifNull(String.join(";", interfaceChange), "否"));//接口变动
        result.put(Dict.DATABASECHANGE, ifNull(String.join(";", databaseChange), "否"));//数据库改动
        result.put(Dict.CLIENTVERSION, ifNull(String.join(";", clientVersion), "不涉及"));//客户端更新
        result.put(Dict.TESTENV, ifNull(String.join(";", testEnv), "不涉及"));//测试环境
        result.put(Dict.APPNAME, ifNull(String.join(";", appName), "无"));//需求涉及的应用名称
        result.put(Dict.OTHERSYSTEMCHANGE, ifNull(String.join(";", otherSystemChange), "否"));//关联系统同步改造
        result.put(Dict.DEVELOPER, ifNull(String.join(";", developer), "无"));//开发人员
        return result;
    }

    @Override
    public void getSitSubmitGroup() throws Exception {

    }

    //查询新fdev的任务详情
    private Map<String, Object> getNewTaskById(String id) {
        Map send = new HashMap();
        send.put(Dict.ID, id);
        send.put(Dict.REST_CODE, "getTaskById");
        try {
            return (Map<String, Object>) restTransport.submitSourceBack(send);
        } catch (Exception e) {
            logger.error("fail to get new Task info");
        }
        return null;
    }

    @Override
    public String queryLastTransFilePath(String workNo) throws Exception {
        FdevSitMsg fdevSitMsg = fdevSitMsgMapper.queryLastFdevSitMsg(workNo, "");
        if(!CommonUtils.isNullOrEmpty(fdevSitMsg)) {
            return fdevSitMsg.getTransFilePath();
        }
        return null;
    }

    @Override
    public void addSecurityTestTrans(String fdevSitMsgId, List<Map> transList) {
        for (Map<String,String> transInfo : transList) {
            SecurityTestTrans testTrans = new SecurityTestTrans();
            testTrans.setSubmitSitId(Integer.parseInt(fdevSitMsgId));
            testTrans.setTransIndex(transInfo.get(Dict.INDEX));
            testTrans.setTransName(transInfo.get(Dict.TRANSNAME));
            testTrans.setTransDesc(transInfo.get(Dict.TRANSDESC));
            testTrans.setFunctionMenu(transInfo.get(Dict.FUNCTIONMENU));
            fdevSitMsgMapper.addSecurityTestTrans(testTrans);
        }
    }

    @Override
    public List<Map> querySubmitTimelyAndMantis(String startDate, String endDate, List<String> groupIds, Boolean isIncludeChildren) throws Exception {
        //如果打开了包含子组开关，查询子组信息
        Map<String, List<Map>> groupMap = null;
        Map<String,String> groupNameMap = null;
        Map<String, List<String>> childGroupIdMap = new HashMap<>();
        Set<String> inChildGroupIds = new HashSet<>();
        if (isIncludeChildren) {
            groupMap = userService.queryChildGroupByIds(groupIds);
            groupNameMap = new HashMap<>();
            for (String key : groupMap.keySet()) {
                List<Map> groupList = groupMap.get(key);
                List<String> childGroupIds = new ArrayList<>();
                for (Map group : groupList) {
                    childGroupIds.add((String)group.get(Dict.ID));
                    groupNameMap.put((String)group.get(Dict.ID), (String) group.get(Dict.NAME));
                }
                childGroupIdMap.put(key, childGroupIds);
                inChildGroupIds.addAll(childGroupIds);
            }
            inChildGroupIds.remove(null);
        } else {
            inChildGroupIds.addAll(groupIds);
            groupNameMap = userService.queryGroupNameByIds(groupIds);
            for (String groupId : groupIds) {
                childGroupIdMap.put(groupId, Arrays.asList(groupId));
            }
        }
        //查询时间段内每个任务及其提交测试时间
        List<Map> fdevSitMsgList = fdevSitMsgMapper.querySubmitTime(startDate, endDate, new ArrayList<>(inChildGroupIds));
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.YYYYMMDD_2);
        Date pointStartDate = null;//每个统计节点的开始日期
        Date pointEndDate = null;//每个统计节点的结束日期
        Map<String, Object> pointData;//每个统计节点的数据
        StringBuffer str = null;
        boolean endFlag = true;
        List<Map> result = new LinkedList<>();
        do {
            pointData = new HashMap<>();
            try {
                //获取每个节点的开始时间和结束时间，按每周一个节点统计
                str = new StringBuffer();
                if (pointEndDate == null) {
                    pointStartDate = sdf.parse(startDate);
                } else {
                    pointStartDate = MyUtil.getDateByDayNum(pointEndDate, 1);
                }
                str.append(sdf.format(pointStartDate).substring(0,10).replaceAll("-",""));
                str.append("-");
                pointEndDate = MyUtil.getDateByDayNum(pointStartDate, 6);
                //如果统计节点的结束日期晚于查询的结束日期，取查询结束日期
                if (pointEndDate.after(sdf.parse(endDate)) || pointEndDate.equals(sdf.parse(endDate))) {
                    pointEndDate = sdf.parse(endDate);
                    endFlag = false;
                }
                str.append(sdf.format(pointEndDate).substring(0,10).replaceAll("-",""));
            } catch (Exception e) {
                throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"日期数据格式错误"});
            }
            //查询各组缺陷数
            Map<String, Integer> mantisCountMap = mantisService.countMantisByGroup(sdf.format(pointStartDate), sdf.format(pointEndDate), new ArrayList<>(inChildGroupIds));
            //查询时间段内该组提测准时率
            Map map = new HashMap();
            map.put(Dict.STARTDATE, sdf.format(pointStartDate));
            map.put(Dict.ENDDATE, sdf.format(pointEndDate));
            map.put(Dict.GROUPID, groupIds);
            map.put(Dict.ISPARENT, isIncludeChildren ? "1" : "0");
            List<Map<String, Object>> qualityReportMap = orderDimensionService.qualityReportNew(map, orderDimensionService.cacheQualityReport());
            //将小组id和提测准时率对应起来
            Map<String, String> timelyMap = new HashMap<>();
            for (Map<String, Object> report : qualityReportMap) {
                timelyMap.put(String.valueOf(report.get(Dict.FDEVGROUPID)), String.valueOf(report.get(Dict.TIMELYRATE)));
            }
            List<Map> groupDataList = new ArrayList<>();//各个小组的数据集合
            Map<String, String> groupData = null;//每个小组数据
            for (String groupId : groupIds) {
                groupData = new HashMap<>();
                int sitTaskCount = 0;//提测任务数量
                List<String> childGroupIds = childGroupIdMap.get(groupId);//获取当前组的子组id集合
                for (Map fdevSitMsg : fdevSitMsgList) {
                    if (!childGroupIds.contains(fdevSitMsg.get(Dict.GROUPID))) {
                        continue;
                    }
                    Date realSitDate = sdf.parse(String.valueOf(fdevSitMsg.get(Dict.REALSITTIME)));
                    //提测任务数，实际sit时间在指定时间段内的任务数量
                    if ((realSitDate.after(pointStartDate) || realSitDate.equals(pointStartDate))
                            && (realSitDate.before(pointEndDate) || realSitDate.equals(pointEndDate))) {
                        sitTaskCount++;
                    }
                }
                groupData.put(Dict.TASKCOUNT, String.valueOf(sitTaskCount));
                groupData.put(Dict.TIMELYRATE, timelyMap.get(groupId));
                int mantisCount = 0;
                for (String childGroupId : childGroupIds) {
                    mantisCount = mantisCount + (mantisCountMap.get(childGroupId) == null ? 0 : mantisCountMap.get(childGroupId));
                }
                groupData.put(Dict.COUNT_MANTIS, String.valueOf(mantisCount));
                groupData.put(Dict.GROUPNAME, groupNameMap.get(groupId));
                groupDataList.add(groupData);
            }
            pointData.put(str.toString(), groupDataList);
            result.add(pointData);
        } while (endFlag);
        return result;
    }

    @Override
    public List<Map> queryInnerTestData(String demandNo) throws Exception {
        List<Map> result = new ArrayList<>();
        //查询需求下功能工单
        List<WorkOrder> workOrderList = workOrderMapper.queryWorkOrderByDemandNo(demandNo);
        if (!CommonUtils.isNullOrEmpty(workOrderList)) {
            for (WorkOrder workOrder : workOrderList) {
                workOrder.setGroupLeader(myUtil.changeEnNameToCn(workOrder.getGroupLeader()));
                workOrder.setTesters(myUtil.changeEnNameToCn(workOrder.getTesters()));
                Map dataMap = MyUtil.beanToMap(workOrder);
                dataMap = orderDimensionService.exportSitReportData(workOrder.getWorkOrderNo(), dataMap);
                result.add(dataMap);
            }
        }
        return result;
    }

}
