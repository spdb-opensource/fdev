package com.plan.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.plan.dao.PlanListMapper;
import com.plan.dao.WorkOrderMapper;
import com.plan.dict.Constants;
import com.plan.dict.Dict;
import com.plan.dict.ErrorConstants;
import com.plan.service.IUserService;
import com.plan.service.MantisFlawService;
import com.plan.service.TaskService;
import com.plan.util.DES3;
import com.plan.util.MantisRestTemplate;
import com.plan.util.MyUtil;
import com.plan.util.Utils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class MantisFlawServiceImpl implements MantisFlawService {
    @Autowired
    private MantisRestTemplate restTemplate;
    @Autowired
    private RestTransport restTransport;
    @Value("${manits.admin.token}")
    private String adminToken;
    @Value("${fnotify.host.fdev}")
    private String fdevEmailHost;
    @Autowired
    private PlanListMapper planListMapper;
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private TaskService taskService;

    private static final Logger logger = LoggerFactory.getLogger(MantisFlawServiceImpl.class);
    @Autowired
    private IUserService userService;

    @Value("${spring.profiles.active}")
    private String env;
    @Autowired
    private DES3 des3;

    @Override
    public void addMantisFlaw(Map<String, Object> map) throws Exception {
        String workNo = (String) map.get(Dict.WORKNO);
        String manager = workOrderMapper.queryWorkManagerByWorkNo(workNo);
        if (Util.isNullOrEmpty(manager)){
            logger.error("workorder do not have workManager");
            throw new FtmsException(ErrorConstants.NOT_HAVE_WORKMANAGER);
        }
        Map<String, Object> user = userService.getCurrentUser();
        String reporter = (String) user.get(Dict.USER_NAME_EN);
        String mantis_token = (String) map.get(Dict.MANTIS_TOKEN);
        if (Util.isNullOrEmpty(mantis_token)) {
            logger.error("user do not have mantis token");
            throw new FtmsException(ErrorConstants.DO_NOT_HAVE_MANITS_TOKEN);
        }
        //新的玉衡对mantis-token解密
        if (env.equals("sit-new") || env.equals("rel-new") || env.equals("pro-new")){
            mantis_token = des3.decrypt(mantis_token);
        }
        String planlist_testcase_id = String.valueOf(map.get(Dict.PLANLIST_TESTCASE_ID));
        String serverity = (String) map.get(Dict.SEVERITY);
        String priority = (String) map.get(Dict.PRIORITY);
        String summary = (String) map.get(Dict.SUMMARY);
        String description = (String) map.get(Dict.DESCRIPTION);
        Integer project = (Integer) map.get(Dict.PROJECT);
        String handler = (String) map.get(Dict.HANDLER);//xxx
        String handlerCh = (String) map.getOrDefault(Dict.HANDLERCH, "");
        String stage = (String) map.get(Dict.STAGE);//归属阶段
        String reason = (String) map.get(Dict.REASON); //开发原因分析
        String flaw_source = (String) map.get(Dict.FLAW_SOURCE); //缺陷来源
        String system_version = (String) map.get(Dict.SYSTEM_VERSION); //系统版本
        system_version = system_version == null ? "" : system_version;
        String system_name = (String) map.get(Dict.SYSTEM_NAME); //系统名称
        system_name = system_name == null ? "" : system_name;
        String developer = (String) map.get(Dict.DEVELOPERCH);
        String developerEn = (String) map.get(Dict.DEVELOPER);
        String taskNo = (String) map.get(Dict.TASKNO);
        //获取主任务编号
        Map<String, Object> taskDetailMap = new HashMap<>();
        String fdev_groupName = "";
        String fdev_groupId = "";
        String appName_en = "";
        List<String> taskNames = new ArrayList<>();
        if (map.get(Dict.APPNAME_EN) instanceof List) {
            List<String> appNameReceive = (List<String>) map.get(Dict.APPNAME_EN);
            appNameReceive = appNameReceive.stream().filter(str -> !Util.isNullOrEmpty(str)).collect(Collectors.toList());
            if (!Util.isNullOrEmpty(appNameReceive)) {
                appName_en = String.join(",", appNameReceive);
            }
        } else {
            appName_en = String.valueOf(map.get(Dict.APPNAME_EN));
        }
        if (!Util.isNullOrEmpty(taskNo)) {
            //根据任务查询工单，判断该工单是新的fdev还是老的fdev
            Map<String, String> workOrderMap = workOrderMapper.queryWorkOrderByTaskNo(taskNo);
            String fdev_new = workOrderMap.get("fdev_new");
            if ("1".equals(fdev_new)) {
                //新fdev的任务查询任务详情
                taskDetailMap = taskService.getNewTaskById(taskNo);
                if (Util.isNullOrEmpty(taskDetailMap)) {
                    throw new FtmsException(ErrorConstants.DATA_NOT_EXIST);
                }
                fdev_groupId = workOrderMap.get("fdev_group_id");
                Map map1 = userService.queryGroupDetail(fdev_groupId);
                fdev_groupName = (String) map1.get(Dict.NAME);
                taskNames.add((String) taskDetailMap.get(Dict.NAME));
            } else {
                try {
                    taskDetailMap = taskService.queryTaskDetail(taskNo);
                    if (Util.isNullOrEmpty(taskDetailMap)) {
                        throw new FtmsException(ErrorConstants.DATA_NOT_EXIST);
                    }
                    Map<String, String> group = (Map<String, String>) taskDetailMap.get(Dict.GROUP);
                    fdev_groupId = group.get(Dict.ID);
                    fdev_groupName = group.get(Dict.NAME);
                    taskNames.add((String) taskDetailMap.get(Dict.NAME));
                } catch (Exception e) {
                    logger.info("任务模块获取组信息失败,任务id:{}", taskNo);
                }
            }
        } else {
            taskNames.addAll(workOrderMapper.queryTaskNamesByWorkNo(workNo));
        }

        List<String> cope_to_list = (List<String>) map.get(Dict.COPY_TO_LIST);//抄送人员英文名
        map.put(Dict.HANDLERCH, handlerCh);
        String plan_fix_date = (String) map.get(Dict.PLAN_FIX_DATE);//遗留缺陷预计修复时间
        String redmine_id = (String) map.get(Dict.REDMINE_ID);//对应单元实施编号
        redmine_id = redmine_id == null ? "" : redmine_id;
        String flaw_type = (String) map.get(Dict.FLAW_TYPE);//缺陷类型
        String function_module = (String) map.get(Dict.FUNCTION_MODULE); //功能模块
        function_module = function_module == null ? "" : function_module;
        List<Map<String, String>> files = (List<Map<String, String>>) map.get(Dict.FILES);
        for (Map<String, String> file : files) {
            String content = file.get(Dict.CONTENT);
            String[] split = content.split("base64,");
            file.put(Dict.CONTENT, split[1]);
        }
        //判断报告人和开发人,分派人员是否相同
        if (!Utils.isEmpty(reporter)) {
            if (reporter.equals(developerEn) || reporter.equals(handler)) {
                throw new FtmsException(ErrorConstants.UPDATE_MANTIS_PERSONNEL_ERROR);
            }
        }
        Map<String, Object> sendMap = new HashMap<String, Object>();
        sendMap.put(Dict.SUMMARY, summary);
        sendMap.put(Dict.ENV, env);
        sendMap.put(Dict.DESCRIPTION, description);
        sendMap.put(Dict.ADDITIONAL_INFORMATION, "");
        sendMap.put(Dict.PROJECT, assemblyParamMap(Dict.ID, project));
        sendMap.put(Dict.HANDLER, assemblyParamMap(Dict.NAME, handler));
        sendMap.put(Dict.PRIORITY, assemblyParamMap(Dict.ID, priority));
        sendMap.put(Dict.SEVERITY, assemblyParamMap(Dict.ID, serverity));
        sendMap.put(Dict.CATEGORY, assemblyParamMap(Dict.ID, 1));
        sendMap.put(Dict.STICKY, false);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        list.add(assemblyCustomMap(3, stage));//'归属阶段'
        list.add(assemblyCustomMap(4, reason));//'开发原因分析'
        list.add(assemblyCustomMap(5, flaw_source));//'缺陷来源'
        list.add(assemblyCustomMap(6, system_version));//'系统版本'
        list.add(assemblyCustomMap(7, system_name));//'系统名称'
        list.add(assemblyCustomMap(8, developer));//'开发人员'中文名
        list.add(assemblyCustomMap(9, Utils.dateStrToLong(plan_fix_date)));//'遗留缺陷预计修复时间'
        list.add(assemblyCustomMap(10, redmine_id));//'对应单元实施编号'
        list.add(assemblyCustomMap(11, flaw_type));//'缺陷类型'
        list.add(assemblyCustomMap(12, function_module));//'功能模块'
        list.add(assemblyCustomMap(13, workNo));
        list.add(assemblyCustomMap(14, planlist_testcase_id));
        list.add(assemblyCustomMap(65, developerEn));//开发人员英文名
        list.add(assemblyCustomMap(72, appName_en));
        if (!Util.isNullOrEmpty(taskNo)) {
            list.add(assemblyCustomMap(19, taskNo));//fdev任务编号
        }
        if (!Util.isNullOrEmpty(fdev_groupName) && !Util.isNullOrEmpty(fdev_groupId)) {
            list.add(assemblyCustomMap(21, fdev_groupName));   //所属fdev小组名称
            list.add(assemblyCustomMap(73, fdev_groupId));   //所属fdev小组id
        }
        sendMap.put(Dict.CUSTOM_FIELDS, list);
        sendMap.put(Dict.FILES, files);
        Map mantisRes = null;
        try {
            String ob = restTemplate.sendPost(restTransport.getUrl("manits.add.issue.url"), mantis_token, sendMap);
            mantisRes = (Map) JSON.parseObject(ob).get(Dict.ISSUE);
            Integer id = (Integer) mantisRes.get(Dict.ID);
            try {
                //设置缺陷的环境字段
                Map<String, Object> param = new HashMap<String, Object>(){{
                    put(Dict.ID, id);
                    put(Dict.REST_CODE, "setMantisEnv");
                }};
                restTransport.submitSourceBack(param);
            } catch (Exception e) {
                logger.info(">>>>>>设置缺陷的环境字段失败{}", id);
            }
            map.put(Dict.ID, id.toString());
            map.put(Dict.HOSTS, fdevEmailHost);
        } catch (Exception e) {
            logger.error("add issues error, summary =" + summary);
            logger.error("=====" + e.getMessage());
            throw new FtmsException(ErrorConstants.ADD_MANTIS_ISSUES_ERROR, new String[]{"请尝试更新mantisToken"});
        }
        // 新增mantis后，邮件通知
        Set<String> peoples = new HashSet<>();
        peoples.add(handler);
        peoples.add(developerEn);
        peoples.add(reporter);
        peoples.addAll(cope_to_list);
        peoples.remove(null);
        HashMap sendDate = (HashMap) map;
        sendDate.forEach((k, v) -> {
            if (v == null || "".equals(v))
                sendDate.put(k, "无");
        });
        sendDate.put(Dict.PROJECT, myUtil.getSysName((Integer) map.get(Dict.PROJECT)));
        sendDate.put(Dict.PRIORITY, myUtil.getChPriority((String) map.get(Dict.PRIORITY)));
        sendDate.put(Dict.SEVERITY, myUtil.getChServerity((String) map.get(Dict.SEVERITY)));

        sendDate.put(Constants.UPDATETASKUSER, userService.getCurrentUser().get(Dict.USER_NAME_EN));

        workOrderMapper.updateIssueTime(new Date().getTime() / 1000, planlist_testcase_id);
        String rqrmntName = workOrderMapper.queryDemandNameByWorkNo(workNo);
        String unitNo = workOrderMapper.queryUnitNoByWorkNo(workNo);
        sendDate.put(Dict.RQRMNTNAME, Util.isNullOrEmpty(rqrmntName) ? "无" : rqrmntName);
        sendDate.put(Dict.TASKLIST, Util.isNullOrEmpty(taskNames) ? "无" : String.join("、", taskNames));
        sendDate.put(Dict.MAINTASKNO, Util.isNullOrEmpty(taskNo) ? "" : taskNo);
        sendDate.put(Dict.REDMINE_ID, Util.isNullOrEmpty(unitNo) ? "无" : unitNo);
        //分派人查看缺陷方式
        String url = "1".equals(myUtil.isNewFtms()) ? "http://" + fdevEmailHost + "/fdevcli/#/demandManage/demandList"
                : "http://" + fdevEmailHost + "/fdev/#/job/list/" + taskNo;
        String access = Util.isNullOrEmpty(taskNo) ? "请进入fdev：我的缺陷查看" : "请点击进入fdev查看：" + url;
        sendDate.put(Dict.ACCESS, access);
        myUtil.sendFastMail(sendDate, peoples, Constants.ADDMANTIS, workNo);

        //如任务来自fdev，则发送即时通知
        try {
            Set<String> target = new HashSet<>();
            target.add(developerEn);
            target.add(reporter);
            target.add(handler);
            target.addAll(cope_to_list);
            target.add(workOrderMapper.queryWorkManagerByWorkNo(workNo));

            String mainTaskName = workOrderMapper.queryMainTaskNameByWorkNo(workNo);
            Map mm = new HashMap<String, String>();
            if (!Util.isNullOrEmpty(taskNo)) {
                //根据任务查询工单，判断该工单是新的fdev还是老的fdev
                Map<String, String> workOrderMap = workOrderMapper.queryWorkOrderByTaskNo(taskNo);
                String fdev_new = workOrderMap.get("fdev_new");
                if ("1".equals(fdev_new)) {
                    mm.put(Dict.ID, taskNo);
                    mm.put(Dict.REST_CODE, "getTaskById");
                } else {
                    mm.put(Dict.ID, taskNo);
                    mm.put(Dict.REST_CODE, "fdev.ftask.queryTaskDetail");
                }
                try {
                    //发fdev请求任务详情获取任务干系人
                    mm = (LinkedHashMap) restTransport.submitSourceBack(mm);
                    addTargetByRoleName(target, mm, Dict.SPDB_MASTER);
                    addTargetByRoleName(target, mm, Dict.MASTER);
                    addTargetByRoleName(target, mm, Dict.DEVELOPER);
                    addTargetByRoleName(target, mm, Dict.TESTER);
                } catch (Exception e) {
                    logger.error("查询fdev任务失败");
                }
            }
            mm.clear();
            target.remove(null);
            //发送fdev消息接口
            mm.put(Dict.CONTENT, "您有一个新的缺陷问题: " + summary + "，请及时处理");
            mm.put(Dict.TARGET, new ArrayList(target));
            mm.put(Dict.DESC, Constants.MANTISNOTICE);//缺陷通知
            mm.put(Dict.TYPE, "1".equals(myUtil.isNewFtms()) ? "2" : Constants.DEFAULT_0);
            if(!"1".equals(myUtil.isNewFtms())) {
                mm.put(Dict.HYPERLINK, "http://" + sendDate.get(Dict.HOSTS) + "/fdev/#/job/list/" + taskNo);
            }
            mm.put(Dict.REST_CODE, "fdev.fnotify.sendUserNotify");
            try {
                restTransport.submitSourceBack(mm);
            } catch (Exception e) {
                logger.error("发送fdev消息接口失败");
            }
        } catch (Exception e) {
            logger.error("发送fdev消息失败");
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

    public Map<String, Object> assemblyCustomMap(Integer id, Object value) {
        Map<String, Object> customMap = new HashMap<String, Object>();
        Map<String, Integer> custom_item = new HashMap<String, Integer>();
        custom_item.put(Dict.ID, id);
        customMap.put(Dict.FIELD, custom_item);
        customMap.put(Dict.VALUE, value);
        return customMap;
    }

    public Map<String, Object> assemblyParamMap(String type, Object value) {
        Map<String, Object> projectMap = new HashMap<String, Object>();
        projectMap.put(type, value);
        return projectMap;
    }

    @Override
    public List<Map<String, Object>> queryMantisProjects() throws Exception {
        String result = restTemplate.sendGet(restTransport.getUrl("manits.projects.url"), adminToken);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> projects = (Map<String, Object>) JSONObject.parse(result);
        List<Map<String, Object>> project_list = (List<Map<String, Object>>) projects.get(Dict.PROJECTS);
        if (env.equals("sit")) {
            //old
            Map project = new HashMap<>();
            project.put(Dict.NAME, "SIT测试缺陷");
            project.put(Dict.ID, 22);
            list.add(project);
            return list;
        }
        if (env.equals("sit-new")) {
            // new
            Map project = new HashMap<>();
            project.put(Dict.NAME, "新SIT测试缺陷");
            project.put(Dict.ID, 28);
            list.add(project);
            return list;
        }
        if (env.equals("rel")) {
            //old
            Map project = new HashMap<>();
            project.put(Dict.NAME, "REL测试缺陷");
            project.put(Dict.ID, 23);
            list.add(project);
            return list;
        }
        if (env.equals("rel-new")) {
            //new
            Map project = new HashMap<>();
            project.put(Dict.NAME, "新REL测试缺陷");
            project.put(Dict.ID, 30);
            list.add(project);
            return list;
        }
        if (env.equals("pro")) {
            for (Map<String, Object> map : project_list) {
                Map project = new HashMap<>();
                String name = (String) map.get(Dict.NAME);
                Integer id = (Integer) map.get(Dict.ID);
                //只获取生产环境的project
                if (id == 7 || id == 8 || id == 9 || id == 10 || id == 11 || id == 12 || id == 13 || id == 14 || id == 15 || id == 16 || id == 17 || id == 18 || id == 19 || id == 25 || id == 26 || id == 27) {
                    project.put(Dict.NAME, name);
                    project.put(Dict.ID, id);
                    //裝
                    list.add(project);
                }
            }
            return list;
        }

        for (Map map : project_list) {
            Map project = new HashMap<>();
            String name = (String) map.get(Dict.NAME);
            Integer id = (Integer) map.get(Dict.ID);
            if (id == 1 || id == 5 || id == 20 || id == 21 || id == 22 || id == 23 || id == 24 || id == 7 || id == 8 || id == 9 || id == 10 || id == 11 || id == 12 || id == 13 || id == 14 || id == 15 || id == 16 || id == 17 || id == 18 || id == 19 || id == 25 || id == 26 || id == 27 || id == 28 || id == 29 || id == 30 || id == 31 || id == 32) {
                continue;
            }
            project.put(Dict.NAME, name);
            project.put(Dict.ID, id);
            list.add(project);
        }
        return list;
    }

    @Override
    public boolean isTestcaseAddIssue(String planlist_testcase_id) throws Exception {
        String status = planListMapper.isTestcaseAddIssue(planlist_testcase_id);
        if (!Utils.isEmpty(status) && status.equals("3")) {
            return true;
        }
        return false;
    }

    /**
     * mantis 修改缺陷后，发送邮件通知
     *
     * @param map
     * @return
     */
    @Override
    public Object updateMantisEmail(Map map) throws Exception {
        map.put(Dict.HOSTS, fdevEmailHost);    //添加邮件中的 url 链接
        String handler_en_name = (String) map.get(Dict.HANDLER_EN_NAME);//xxx
        String reporter_en_name = (String) map.get(Dict.REPORTER_EN_NAME);//报告人英文名
        String developerEn = (String) map.get(Dict.DEVELOPER);
        String workNo = (String) map.get(Dict.WORKNO);
        String mainTaskNo = workOrderMapper.queryMainTaskNoByWorkNo(workNo);
        // 修改mantis后，邮件通知
        Set<String> peoples = new HashSet<>();
        peoples.add(handler_en_name);
        peoples.add(reporter_en_name);
        peoples.add(developerEn);
        peoples.remove(null);
        HashMap sendDate = (HashMap) map;
        sendDate.forEach((k, v) -> {
            if (k.equals(Dict.ID)) {
                sendDate.put(k, v.toString());
            }
            if (v == null || "".equals(v))
                sendDate.put(k, "无");
        });
        Map oldDate = (Map) map.get(Dict.OLDMANTISDATA);
        sendDate.put(Dict.PROJECT, myUtil.getSysName((Integer) map.get(Dict.PROJECT_ID)));
        sendDate.put(Dict.PRIORITY, myUtil.getChPriority((String) map.get(Dict.PRIORITY)));
        sendDate.put(Dict.SEVERITY, myUtil.getChServerity((String) map.get(Dict.SEVERITY)));
        sendDate.put(Dict.STATUS, myUtil.getChStatus((String) map.get(Dict.STATUS)));
        oldDate.put(Dict.PROJECT, myUtil.getSysName((Integer) oldDate.get(Dict.PROJECT_ID)));
        oldDate.put(Dict.PRIORITY, myUtil.getChPriority((String) oldDate.get(Dict.PRIORITY)));
        oldDate.put(Dict.SEVERITY, myUtil.getChServerity((String) oldDate.get(Dict.SEVERITY)));
        oldDate.put(Dict.STATUS, myUtil.getChStatus((String) oldDate.get(Dict.STATUS)));
        sendDate.put(Dict.MAINTASKNO, Util.isNullOrEmpty(mainTaskNo) ? "" : mainTaskNo);
        String rqrmntName = workOrderMapper.queryDemandNameByWorkNo(workNo);
        sendDate.put(Dict.RQRMNTNAME, Util.isNullOrEmpty(rqrmntName) ? "无" : rqrmntName);
        String unitNo = workOrderMapper.queryUnitNoByWorkNo(workNo);
        sendDate.put(Dict.REDMINE_ID, Util.isNullOrEmpty(unitNo) ? "无" : unitNo);
        String taskNo = String.valueOf(map.get(Dict.TASK_NO));
        List<String> taskNames = new ArrayList<>();
        if (!Util.isNullOrEmpty(taskNo)) {
            taskNames.add(workOrderMapper.queryTaskNameByTaskNo(taskNo));
        } else {
            taskNames.addAll(workOrderMapper.queryTaskNamesByWorkNo(workNo));
        }
        sendDate.put(Dict.TASKLIST, Util.isNullOrEmpty(taskNames) ? "无" : String.join("、", taskNames));
        //分派人查看缺陷方式
        String url = "1".equals(myUtil.isNewFtms()) ? "http://" + fdevEmailHost + "/fdevcli/#/demandManage/demandList" :
                "http://" + fdevEmailHost + "/fdev/#/job/list/" + mainTaskNo;
        String access = Util.isNullOrEmpty(mainTaskNo) ? "请进入fdev：我的缺陷查看" : "请点击进入fdev查看：" + url;
        sendDate.put(Dict.ACCESS, access);
        Set<Map.Entry> set = oldDate.entrySet();
        for (Map.Entry entry : set) {
            if (entry.getKey().equals(Dict.WORKNO))
                continue;
            if (sendDate.get(entry.getKey()) != null &&
                    !sendDate.get(entry.getKey()).toString().equals(entry.getValue().toString())) {
                sendDate.put(entry.getKey(), entry.getValue() + "----->" + sendDate.get(entry.getKey()));
            }
        }

        logger.info("------> " + sendDate.toString());
        logger.info("------> " + oldDate.toString());

        myUtil.sendFastMail(sendDate, peoples, Constants.UPDATEMANTIS, workNo);
        return null;
    }

    @Override
    public void addProIssue(Map<String, Object> map) throws Exception {
        String task_no = String.valueOf(map.get(Dict.TASK_NO));//fdev任务编号
        String requirement_name = (String) map.get(Dict.REQUIREMENT_NAME);//需求名称
        String module = (String) map.get(Dict.MODULE);//所属模块
        String occurred_time = (String) map.get(Dict.OCCURRED_TIME);//发生日期
        String is_trigger_issue = (String) map.get(Dict.IS_TRIGGER_ISSUE);//是否造成生产问题
        String problem_phenomenon = (String) map.get(Dict.PROBLEM_PHENOMENON);//问题现象
        String influence_area = (String) map.get(Dict.INFLUENCE_AREA);//影响范围
        String issue_reason = (String) map.get(Dict.ISSUE_REASON);//问题原因
        List<String> dev_responsible = (List<String>) map.get(Dict.DEV_RESPONSIBLE);//开发责任人
        List<String> audit_responsible = (List<String>) map.get(Dict.AUDIT_RESPONSIBLE); //代码审核责任人
        List<String> test_responsible = (List<String>) map.get(Dict.TEST_RESPONSIBLE); //内测责任人
        List<String> task_responsible = (List<String>) map.get(Dict.TASK_RESPONSIBLE); //牵头任务责任人
        String release_node = (String) map.get(Dict.RELEASE_NODE);//投产窗口
        List<String> issue_types = (List<String>) map.get(Dict.ISSUE_TYPE); //问题类型
        String issue_type = StringUtils.join(issue_types, "|");
        String discover_stage = (String) map.get(Dict.DISCOVER_STAGE);//发现阶段
        String is_uat_replication = (String) map.get(Dict.IS_UAT_REPLICATION);//能否uat复现
        String is_rel_replication = (String) map.get(Dict.IS_REL_REPLICATION);//能否rel复现
        String is_gray_replication = (String) map.get(Dict.IS_GRAY_REPLICATION);//能否灰度复现
        String is_involve_urgency = (String) map.get(Dict.IS_INVOLVE_URGENCY);//是否涉及紧急需求
        String improvement_measures = (String) map.get(Dict.IMPROVEMENT_MEASURES);//改进措施
        String reviewer_comment = (String) map.get(Dict.REVIEWER_COMMENT);//评审意见
        List<Map<String, String>> responsibility_list = (List<Map<String, String>>) map.get(Dict.RESPONSIBILITY_LIST);//问责列表
        List<Map<String, String>> backlog_schedule_list = (List<Map<String, String>>) map.get(Dict.BACKLOG_SCHEDULE_LIST);//待办事项列表
        String issue_level = (String) map.get(Dict.ISSUE_LEVEL);//生产问题级别
        String reviewer_status = (String) map.get(Dict.REVIEWER_STATUS);//评审状态
        String deal_status = (String) map.get(Dict.DEAL_STATUS);//处理状态
        String remark = (String) map.get(Dict.REMARK);//备注
        String orfanizer = (String) map.get(Dict.ORFANIZER);//编写人
        String first_occurred_time = (String) map.get(Dict.FIRST_OCCURRED_TIME);//首次发生时间
        String location_time = (String) map.get(Dict.LOCATION_TIME);//定位时间
        String repair_time = (String) map.get(Dict.REPAIR_TIME);//修复时间
        String reviewer_time = (String) map.get(Dict.REVIEWER_TIME);//评审时间
        List<String> reviewer = (List<String>) map.get(Dict.REVIEWER);//评审人
        String emergency_process = (String) map.get(Dict.EMERGENCY_PROCESS);//应急过程
        List<String> emergency_responsible = (List<String>) map.get(Dict.EMERGENCY_RESPONSIBLE);//应急负责人
        List<Map<String, String>> files = (List<Map<String, String>>) map.get(Dict.FILES);//附件
        for (Map<String, String> file : files) {
            String content = file.get(Dict.CONTENT);
            String[] split = content.split("base64,");
            file.put(Dict.CONTENT, split[1]);
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        list.add(assemblyCustomMap(19, task_no));//fdev任务编号
        list.add(assemblyCustomMap(20, requirement_name));//需求名称
        list.add(assemblyCustomMap(21, module));//所属模块
        list.add(assemblyCustomMap(22, Utils.dateStrToLongModel2(occurred_time)));//发生日期
        list.add(assemblyCustomMap(23, is_trigger_issue));//是否造成生产问题
        list.add(assemblyCustomMap(24, problem_phenomenon));//问题现象
        list.add(assemblyCustomMap(25, influence_area));//影响范围
        list.add(assemblyCustomMap(26, issue_reason));//问题原因
        if (!Util.isNullOrEmpty(dev_responsible)) {
            list.add(assemblyCustomMap(27, StringUtils.join(dev_responsible, ";")));//开发责任人
        }
        if (!Util.isNullOrEmpty(audit_responsible)) {
            list.add(assemblyCustomMap(29, StringUtils.join(audit_responsible, ";")));//代码审核责任人
        }
        if (!Util.isNullOrEmpty(test_responsible)) {
            list.add(assemblyCustomMap(30, StringUtils.join(test_responsible, ";")));//内测责任人
        }
        if (!Util.isNullOrEmpty(task_responsible)) {
            list.add(assemblyCustomMap(31, StringUtils.join(task_responsible, ";")));//牵头任务责任人
        }
        list.add(assemblyCustomMap(32, issue_type));//问题类型
        list.add(assemblyCustomMap(33, discover_stage));//发现阶段
        list.add(assemblyCustomMap(34, improvement_measures));//改进措施
        list.add(assemblyCustomMap(35, reviewer_comment));//评审意见
        list.add(assemblyCustomMap(66, is_uat_replication));//能否uat复现
        list.add(assemblyCustomMap(67, is_rel_replication));//能否rel复现
        list.add(assemblyCustomMap(68, is_gray_replication));//能否灰度复现
        list.add(assemblyCustomMap(69, is_involve_urgency));//是否涉及紧急需求
        list.add(assemblyCustomMap(36, issue_level));//生产问题级别
        list.add(assemblyCustomMap(37, reviewer_status));//评审状态
        list.add(assemblyCustomMap(70, orfanizer));//编写人
        list.add(assemblyCustomMap(71, release_node));//投产窗口
        list.add(assemblyCustomMap(74, first_occurred_time));//首次发生时间
        list.add(assemblyCustomMap(75, location_time));//定位时间
        list.add(assemblyCustomMap(76, repair_time));//修复时间
        list.add(assemblyCustomMap(77, reviewer_time));//评审时间
        if (!Util.isNullOrEmpty(reviewer)) {
            list.add(assemblyCustomMap(78, StringUtils.join(reviewer, ";")));//牵头任务责任人
        }
        list.add(assemblyCustomMap(79, emergency_process));//应急过程
        if (!Util.isNullOrEmpty(emergency_responsible)) {
            list.add(assemblyCustomMap(80, StringUtils.join(emergency_responsible, ";")));//牵头任务责任人
        }
        if (!Util.isNullOrEmpty(remark)) {
            list.add(assemblyCustomMap(44, remark));//备注
        }
        list.add(assemblyCustomMap(38, deal_status));//处理状态
        addResponsibility(list, responsibility_list);
        addBacklogSchedule(list, backlog_schedule_list);
        Map<String, Object> sendMap = new HashMap<String, Object>();
        sendMap.put(Dict.CUSTOM_FIELDS, list);
        sendMap.put(Dict.FILES, files);
        if (env.equals("sit")) {
            sendMap.put(Dict.PROJECT, assemblyParamMap(Dict.ID, 21));//生产问题总结-测试
        } else if (env.equals("rel")) {
            sendMap.put(Dict.PROJECT, assemblyParamMap(Dict.ID, 24));//生产问题总结 REL测试
        } else {
            sendMap.put(Dict.PROJECT, assemblyParamMap(Dict.ID, 20));//生产问题总结项目
        }
        sendMap.put(Dict.CATEGORY, assemblyParamMap(Dict.ID, 1));
        sendMap.put(Dict.STICKY, false);
        sendMap.put(Dict.SUMMARY, module + "生产问题总结");
        sendMap.put(Dict.DESCRIPTION, module + "生产问题总结");
        sendMap.put(Dict.ADDITIONAL_INFORMATION, "");
        try {
            JSONObject json = new JSONObject(sendMap);
            restTemplate.sendPost(restTransport.getUrl("manits.add.issue.url"), adminToken, sendMap);
        } catch (Exception e) {
            logger.error("add issues error, task_no =" + task_no);
            logger.error("=====" + e.getMessage());
            throw new FtmsException(ErrorConstants.ADD_PRO_ISSUES_ERROR);
        }

    }

    @Override
    public List<String> queryTaskNoByWorkNos(List<String> workNos) {
        List<String> TaskNos = workOrderMapper.queryTaskNoByWorkNos(workNos);
        return TaskNos;
    }

    @Override
    public List<Map> queryAppByWorkNo(String workNo) throws Exception {
        //获取所有分支任务id
        List<String> TaskNos = workOrderMapper.queryTaskNosByWorkNo(workNo);
        //获取主任务编号
        String mainTaskNo = workOrderMapper.queryMainTaskNoByWorkNo(workNo);
        if (!Util.isNullOrEmpty(mainTaskNo)) {
            TaskNos.add(mainTaskNo);
        }
        List<Map> result = new ArrayList<>();
        if (!Util.isNullOrEmpty(TaskNos)) {
            try {
                List<Map> taskInfos = taskService.queryInfoByTaskNos(TaskNos);
                for (Map map : taskInfos) {
                    Map taskMap = new HashMap();
                    taskMap.put(Dict.ID, map.get(Dict.PROJECT_ID));
                    taskMap.put(Dict.APPNAME_EN, map.get(Dict.PROJECT_NAME));
                    result.add(taskMap);
                }
            } catch (Exception e) {
                logger.error("任务模块获取组信息失败,主任务id" + mainTaskNo);
            }
        }
        return result;
    }

    @Override
    public String querymainTaskNoByWorkNo(String workNo) throws Exception {
        //获取主任务id
        String mainTaskNo = workOrderMapper.queryMainTaskNoByWorkNo(workNo);
        return mainTaskNo;
    }

    private void addResponsibility(List<Map<String, Object>> list, List<Map<String, String>> responsibility_list) {
        if (!Util.isNullOrEmpty(responsibility_list)) {
            if (!Util.isNullOrEmpty(responsibility_list.get(0))) {
                Map<String, String> map = responsibility_list.get(0);
                String responsible = map.get(Dict.RESPONSIBLE);//问责人
                String responsibility_type = map.get(Dict.RESPONSIBILITY_TYPE);//问责类型
                String responsibility_content = map.get(Dict.RESPONSIBILITY_CONTENT);//问责内容
                if (!Util.isNullOrEmpty(responsible)) {
                    list.add(assemblyCustomMap(56, responsible));
                }
                if (!Util.isNullOrEmpty(responsibility_type)) {
                    list.add(assemblyCustomMap(57, responsibility_type));
                }
                if (!Util.isNullOrEmpty(responsibility_content)) {
                    list.add(assemblyCustomMap(58, responsibility_content));
                }
            }
            if (responsibility_list.size() > 1 && !Util.isNullOrEmpty(responsibility_list.get(1))) {
                Map<String, String> map = responsibility_list.get(1);
                String responsible = map.get(Dict.RESPONSIBLE);//问责人
                String responsibility_type = map.get(Dict.RESPONSIBILITY_TYPE);//问责类型
                String responsibility_content = map.get(Dict.RESPONSIBILITY_CONTENT);//问责内容
                if (!Util.isNullOrEmpty(responsible)) {
                    list.add(assemblyCustomMap(59, responsible));
                }
                if (!Util.isNullOrEmpty(responsibility_type)) {
                    list.add(assemblyCustomMap(60, responsibility_type));
                }
                if (!Util.isNullOrEmpty(responsibility_content)) {
                    list.add(assemblyCustomMap(61, responsibility_content));
                }
            }
            if (responsibility_list.size() > 2 && !Util.isNullOrEmpty(responsibility_list.get(2))) {
                Map<String, String> map = responsibility_list.get(2);
                String responsible = map.get(Dict.RESPONSIBLE);//问责人
                String responsibility_type = map.get(Dict.RESPONSIBILITY_TYPE);//问责类型
                String responsibility_content = map.get(Dict.RESPONSIBILITY_CONTENT);//问责内容
                if (!Util.isNullOrEmpty(responsible)) {
                    list.add(assemblyCustomMap(62, responsible));
                }
                if (!Util.isNullOrEmpty(responsibility_type)) {
                    list.add(assemblyCustomMap(63, responsibility_type));
                }
                if (!Util.isNullOrEmpty(responsibility_content)) {
                    list.add(assemblyCustomMap(64, responsibility_content));
                }
            }
        }
    }

    private void addBacklogSchedule(List<Map<String, Object>> list, List<Map<String, String>> backlog_schedule_list) {
        if (!Util.isNullOrEmpty(backlog_schedule_list)) {
            if (!Util.isNullOrEmpty(backlog_schedule_list.get(0))) {
                Map<String, String> map = backlog_schedule_list.get(0);
                String backlog_schedule = map.get(Dict.BACKLOG_SCHEDULE);//待办事项
                String backlog_schedule_reviewer = map.get(Dict.BACKLOG_SCHEDULE_REVIEWER);//待办事项负责人
                String backlog_schedule_complete_time = map.get(Dict.BACKLOG_SCHEDULE_COMPLETE_TIME);//待办事项完成时间
                String backlog_schedule_current_completion = map.get(Dict.BACKLOG_SCHEDULE_CURRENT_COMPLETION);//待办事项当前完成情况
                String backlog_schedule_complete_percentage = map.get(Dict.BACKLOG_SCHEDULE_COMPLETE_PERCENTAGE);//待办事项完成百分比
                if (!Util.isNullOrEmpty(backlog_schedule)) {
                    list.add(assemblyCustomMap(39, backlog_schedule));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_reviewer)) {
                    list.add(assemblyCustomMap(40, backlog_schedule_reviewer));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_complete_time)) {
                    list.add(assemblyCustomMap(41, Utils.dateStrToLongModel2(backlog_schedule_complete_time)));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_current_completion)) {
                    list.add(assemblyCustomMap(42, backlog_schedule_current_completion));
                }

                if (!Util.isNullOrEmpty(backlog_schedule_complete_percentage)) {
                    list.add(assemblyCustomMap(43, backlog_schedule_complete_percentage));
                }
            }
            if (backlog_schedule_list.size() > 1 && !Util.isNullOrEmpty(backlog_schedule_list.get(1))) {
                Map<String, String> map = backlog_schedule_list.get(1);
                String backlog_schedule = map.get(Dict.BACKLOG_SCHEDULE);//待办事项
                String backlog_schedule_reviewer = map.get(Dict.BACKLOG_SCHEDULE_REVIEWER);//待办事项负责人
                String backlog_schedule_complete_time = map.get(Dict.BACKLOG_SCHEDULE_COMPLETE_TIME);//待办事项完成时间
                String backlog_schedule_current_completion = map.get(Dict.BACKLOG_SCHEDULE_CURRENT_COMPLETION);//待办事项当前完成情况
                String backlog_schedule_complete_percentage = map.get(Dict.BACKLOG_SCHEDULE_COMPLETE_PERCENTAGE);//待办事项完成百分比
                if (!Util.isNullOrEmpty(backlog_schedule)) {
                    list.add(assemblyCustomMap(46, backlog_schedule));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_reviewer)) {
                    list.add(assemblyCustomMap(47, backlog_schedule_reviewer));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_complete_time)) {
                    list.add(assemblyCustomMap(48, Utils.dateStrToLongModel2(backlog_schedule_complete_time)));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_current_completion)) {
                    list.add(assemblyCustomMap(49, backlog_schedule_current_completion));
                }

                if (!Util.isNullOrEmpty(backlog_schedule_complete_percentage)) {
                    list.add(assemblyCustomMap(50, backlog_schedule_complete_percentage));
                }
            }
            if (backlog_schedule_list.size() > 2 && !Util.isNullOrEmpty(backlog_schedule_list.get(2))) {
                Map<String, String> map = backlog_schedule_list.get(2);
                String backlog_schedule = map.get(Dict.BACKLOG_SCHEDULE);//待办事项
                String backlog_schedule_reviewer = map.get(Dict.BACKLOG_SCHEDULE_REVIEWER);//待办事项负责人
                String backlog_schedule_complete_time = map.get(Dict.BACKLOG_SCHEDULE_COMPLETE_TIME);//待办事项完成时间
                String backlog_schedule_current_completion = map.get(Dict.BACKLOG_SCHEDULE_CURRENT_COMPLETION);//待办事项当前完成情况
                String backlog_schedule_complete_percentage = map.get(Dict.BACKLOG_SCHEDULE_COMPLETE_PERCENTAGE);//待办事项完成百分比
                if (!Util.isNullOrEmpty(backlog_schedule)) {
                    list.add(assemblyCustomMap(51, backlog_schedule));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_reviewer)) {
                    list.add(assemblyCustomMap(52, backlog_schedule_reviewer));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_complete_time)) {
                    list.add(assemblyCustomMap(53, Utils.dateStrToLongModel2(backlog_schedule_complete_time)));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_current_completion)) {
                    list.add(assemblyCustomMap(54, backlog_schedule_current_completion));
                }

                if (!Util.isNullOrEmpty(backlog_schedule_complete_percentage)) {
                    list.add(assemblyCustomMap(55, backlog_schedule_complete_percentage));
                }
            }
        }
    }
}
