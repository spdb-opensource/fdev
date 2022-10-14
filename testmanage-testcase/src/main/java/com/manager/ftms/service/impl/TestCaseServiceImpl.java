package com.manager.ftms.service.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.ftms.dao.PlanlistTestcaseRelationMapper;
import com.manager.ftms.dao.TestcaseMapper;
import com.manager.ftms.dao.WorkOrderMapper;
import com.manager.ftms.entity.PlanlistTestcaseRelation;
import com.manager.ftms.entity.Testcase;
import com.manager.ftms.entity.WorkOrder;
import com.manager.ftms.service.IUserService;
import com.manager.ftms.service.TestCaseService;
import com.manager.ftms.util.*;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TestCaseServiceImpl implements TestCaseService {

    private static final Logger logger = LoggerFactory.getLogger(TestCaseServiceImpl.class);

    @Autowired
    private PlanlistTestcaseRelationMapper planlistTestcaseRelationMapper;
    @Autowired
    private TestcaseMapper testcaseMapper;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private Utils utils;
    @Autowired
    private TestCaseService testCaseService;
    @Autowired
    private IUserService userService;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private TestcaseUtils testcaseUtils;
    @Autowired
    private SendEmailServiceImpl sendEmailService;

    @Override
    public List<Testcase> selectAll() {
        return testcaseMapper.selectAll();
    }

    // ------------testcase------------------------------------------

    /**
     * 根据执行计划id查询案例列表 总数
     *
     * @param planId
     * @return 案例集合
     * @throws Exception
     */
    @Override
    public Integer queryTestCount(Integer planId, String testcaseStatus, String testcaseNature, String testcaseType,
                                  String testcaseExecuteResult, String testcaseName, String planlistTestcaseId) throws Exception {
        return testcaseMapper.queryTestCount(planId, testcaseStatus, testcaseNature, testcaseType,
                testcaseExecuteResult, testcaseName, planlistTestcaseId);
    }

    /**
     * 根据执行计划id查询案例列表 分页
     *
     * @param
     * @return 案例集合
     * @throws Exception
     */
    @Override
    public List<Testcase> queryTestByPlanId(Map map) throws Exception {
        Integer planId = (Integer) (map.get(Dict.PLANID));
        Integer pageSize = (Integer) (map.getOrDefault(Dict.PAGESIZE, 8));
        List<Testcase> testcase_list;
        if (pageSize != 0) {
            Integer currentPage = (Integer) (map.getOrDefault(Dict.CURRENTPAGE, 1));
            Integer start = pageSize * (currentPage - 1);// 起始
            String testcaseStatus = String.valueOf(map.getOrDefault(Dict.TESTCASESTATUS, ""));//案例审核状态
            String testcaseNature = String.valueOf(map.getOrDefault(Dict.TESTCASENATURE, ""));//案例性质（正反）
            String testcaseType = String.valueOf(map.getOrDefault(Dict.TESTCASETYPE, ""));//案例类型
            String testcaseExecuteResult = String.valueOf(map.getOrDefault(Dict.TESTCASEEXECUTERESULT, ""));//案例执行结果
            String planlistTestcaseId = String.valueOf(map.getOrDefault(Dict.PLANLISTTESTCASEID, "")).trim();//关系id
            String testcaseName = String.valueOf(map.getOrDefault(Dict.TESTCASENAME, "")).trim();//测试案例名称
            testcase_list = testcaseMapper.queryTestByPlanId(planId, start, pageSize, testcaseStatus,
                    testcaseExecuteResult, testcaseType, testcaseNature, testcaseName, planlistTestcaseId);
        } else {
            testcase_list = testcaseMapper.queryAllTestByPlanId(planId);
        }
        return testcase_list;
    }

    /**
     * 根据执行计划id查询案例列表
     *
     * @param map
     * @return 案例集合
     * @throws Exception
     */
    @Override
    public List<Testcase> queryTestcaseByPlanId(Map map) throws Exception {
        List<Testcase> testcases = testcaseMapper.queryTestcaseByPlanId(map);
        for (Testcase testcase : testcases) {
            String testcasePeople = testcase.getTestcasePeople();
            Map user = userService.queryUserCoreData(testcasePeople);
            if (!Util.isNullOrEmpty(user)) {
                testcase.setUserName((String) user.get(Dict.USER_NAME_CN));
            }
        }
        return testcases;
    }

    /**
     * 添加案例
     *
     * @param map
     * @return 单个案例
     * @throws Exception
     */
    @Override
    public Testcase addTestcase(Map map) throws Exception {
        Map tMap = (Map) map.get("testcase");
        String testcaseNo = this.generateTestcaseNo(map);
        // 根据testcaseNo查询是否生成库里面存在的案例编号
        Testcase tc = testcaseMapper.queryDetailByTestcaseNo(testcaseNo);
        while (null != tc) {
            testcaseNo = this.generateTestcaseNo(map);
            tc = testcaseMapper.queryDetailByTestcaseNo(testcaseNo);
        }
        tMap.put("testcaseNo", testcaseNo);
        tMap.put("testcaseStatus", "0");// 新增时状态
        tMap.put("testcaseDate", Utils.dateUtil(new Date()));// 时间
        tMap.put("testcaseVersion", "1");// 案例版本号
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        try {
            tMap.put("testcasePeople", redisUtils.getCurrentUserInfoMap().get(Dict.USER_NAME_EN));
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        try {
            Testcase testcase = Utils.parseMap2Object(tMap, Testcase.class);
            int num = testcaseMapper.addTestcase(testcase);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
        }
        return testcaseMapper.queryDetailByTestcaseNo(testcaseNo);
    }

    /**
     * 根据编号查询案例
     *
     * @param testcaseNo
     * @return
     * @throws Exception
     */
    @Override
    public Testcase queryDetailByTestcaseNo(String testcaseNo) throws Exception {
        return testcaseMapper.queryDetailByTestcaseNo(testcaseNo);
    }

    /**
     * 根据实体查询案例
     *
     * @param testcaseNo
     * @return 单个案例
     * @throws Exception
     */
    @Override
    public Testcase queryCaseByTestcaseNo(String testcaseNo) throws Exception {
        return testcaseMapper.queryCaseByTestcaseNo(testcaseNo);
    }

    /**
     * 生成案例编号
     *
     * @param map
     * @return String
     */
    public String generateTestcaseNo(Map map) throws Exception {
        // 系统id
        String sysId = map.get("sysId").toString();
        if (sysId.length() == 1) {
            sysId = "00" + sysId;
        } else if (sysId.length() == 2) {
            sysId = "0" + sysId;
        }
        if (Util.isNullOrEmpty(sysId)) {
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
        }
        // 类型testcaseType
        Map m = (Map) map.get("testcase");
        if (Util.isNullOrEmpty(m)) {
            throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.TESTCASE});
        }
        // 随机数
        String random = (int) ((Math.random() * 9 + 1) * 10000) + "";
        // 案例编号
        String testcaseNo = null;
        // 生成 流水工单号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String testcaseDate = sdf.format(new Date());
        testcaseNo = testcaseDate + sysId + random;
        return testcaseNo;
    }

    /**
     * 根据实体修改案例
     *
     * @param map
     * @return 单个案例
     */
    @Override
    public int updateTestcaseByTestcaseNo(Map map, String planlistTestcaseId) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(Utils.FULL_TIME);
        String finalUpdateTime = sdf.format(new Date());
        String finalUpdatePrs = utils.getCurrentUserEnName();
        try {
            planlistTestcaseRelationMapper.updateModify(finalUpdateTime, finalUpdatePrs, planlistTestcaseId);
        } catch (Exception e) {
            logger.info("更新最终修改时间和人员失败");
        }
        return testcaseMapper.updateTestcaseByTestcaseNo(map);
    }

    /**
     * 根据编号删除案例
     *
     * @param testcaseNo
     * @return int
     * @throws Exception
     */
    @Override
    public int deleteTestcaseByTestcaseNo(String testcaseNo) throws Exception {
        return testcaseMapper.deleteTestcaseByTestcaseNo(testcaseNo);
    }

    /**
     * 根据编号修改案例状态为待审核状态
     *
     * @param testcaseNo
     * @return 受影响行数
     */
    @Override
    public int updateTestcaseByStatusWaitPass(String testcaseNo) throws Exception {
        return testcaseMapper.updateTestcaseByStatusWaitPass(testcaseNo);
    }

    /**
     * 根据编号修改案例状态为待生效状态
     *
     * @param testcaseNo
     * @return 受影响行数
     * @throws Exception
     */
    @Override
    public int updateTestcaseByStatusWaitEffect(String testcaseNo) throws Exception {
        return testcaseMapper.updateTestcaseByStatusWaitEffect(testcaseNo);
    }

    /******************************** 案例执行 ******************************/

    /**
     * 模糊匹配查询案例
     *
     * @param parameters
     * @return
     * @throws Exception
     */
    @Override
    public List<Testcase> selectTestCaseFuzzy(String parameters) throws Exception {
        return this.testcaseMapper.selectTestCaseFuzzy(parameters);
    }

    // *************************** 案例审批 ********************************

    @Override
    public List<Testcase> queryTestcaseByUserEnName(String userEnName, String status) throws Exception {
        return testcaseMapper.selectTestcaseByUserEnName(userEnName, status);
    }

    @Override
    public int auditTestcase(String testcaseNo, String status) throws Exception {
        try {
            return testcaseMapper.updateTestcaseByStatus(testcaseNo, status);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.UPDATE_ERROR, new String[]{"案例审核失败!"});
        }
    }

    /**
     * 批量删除案例
     *
     * @param testcaseNoList
     * @return
     * @throws Exception
     */
    @Override
    public int delBatchTestCaseNos(List<String> testcaseNoList) throws Exception {
        return this.testcaseMapper.delBatchTestCaseNos(testcaseNoList);
    }

    @Override
    public List<Testcase> selTestCaseCondition(Map map) throws Exception {
        String testcaseStatus = null;
        String testcaseExecuteResult = null;
        String testcaseType = null;
        String testcaseNature = null;
        Integer planId = (Integer) map.get(Dict.PLANID);
        if (!Util.isNullOrEmpty(map.get(Dict.TESTCASESTATUS))) {
            testcaseStatus = (String) map.get(Dict.TESTCASESTATUS);
        }
        if (!Util.isNullOrEmpty(map.get(Dict.TESTCASEEXECUTERESULT))) {
            testcaseExecuteResult = (String) map.get(Dict.TESTCASEEXECUTERESULT);
        }
        if (!Util.isNullOrEmpty(map.get(Dict.TESTCASETYPE))) {
            testcaseType = (String) map.get(Dict.TESTCASETYPE);
        }
        if (!Util.isNullOrEmpty(map.get(Dict.TESTCASENATURE))) {
            testcaseNature = (String) map.get(Dict.TESTCASENATURE);
        }
        return this.testcaseMapper.selTestCaseCondition(testcaseStatus, testcaseExecuteResult, testcaseType,
                testcaseNature, planId);
    }

    @Override
    public int batchAudit(List<String> testcaseNos, String testcaseStatus) throws Exception {
        try {
            return testcaseMapper.batchAudit(testcaseNos, testcaseStatus);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.UPDATE_ERROR, new String[]{"案例审批失败!"});
        }
    }

    @Override
    public List<Testcase> queryTestcaseByFuncId(Map testcase) throws Exception {
        Integer currentPage = (Integer) testcase.get(Dict.CURRENTPAGE);
        Integer pageSize = (Integer) testcase.get(Dict.PAGESIZE);
        List<Testcase> result = new ArrayList<>();
        if (!Utils.isEmpty(currentPage) && !Utils.isEmpty(pageSize)) {
            //分页查询
            Integer start = (currentPage - 1) * pageSize;
            result = testcaseMapper.selectTestcaseByFuncId(testcase, start);
        } else {
            //导出查询
            result = testcaseMapper.selectTestcaseByFuncId(testcase, null);
        }
        return changeNameToCn(result);
    }

    private List<Testcase> changeNameToCn(List<Testcase> result) {
        Map send;
        for (Testcase testcase : result) {
            String creator = testcase.getTestcasePeople();
            send = new HashMap();
            send.put(Dict.REST_CODE, "fdev.user.query");
            send.put(Dict.USER_NAME_EN, creator);
            try {
                send = (Map) ((List) restTransport.submitSourceBack(send)).get(0);
                testcase.setTestcasePeople((String) send.get(Dict.USER_NAME_CN));
            } catch (Exception e) {
                logger.error("fail to query user info" + creator + e);
            }
        }
        return result;
    }

    /**
     * 查询单个案例信息
     *
     * @param testcaseNo
     * @param planId
     * @return
     * @throws Exception
     */
    @Override
    public Testcase queryDetailByTestcaseNoAndPlanId(String testcaseNo, Integer planId) throws Exception {
        Testcase testcase = this.testcaseMapper.queryDetailByTestcaseNoAndPlanId(testcaseNo, planId);
        if (testcase != null) {
            testcase.setTestcaseNature(
                    testcase.getTestcaseNature() == "1" ? Constants.IS_CaseS : Constants.THE_CaseS);
            return testcase;
        }
        return null;
    }

    @Override
    public int updateByPrimaryKey(String testCaseNo, String remark) throws Exception {
        return this.testcaseMapper.updateByPrimaryKey(testCaseNo, remark);
    }

    /***
     * 查询当前计划下面的
     *
     * @param map
     * @return
     */
    @Override
    public Map queryPlanAllStatus(Map map) throws Exception {
        Map<String, Integer> returnMap = new HashMap<>();
        returnMap.put("exeNum", 0);
        returnMap.put("successNum", 0);
        returnMap.put("zuaiNum", 0);
        returnMap.put("failedNum", 0);
        // 当前计划下面的案例
        List<PlanlistTestcaseRelation> testcaseRelations = this.planlistTestcaseRelationMapper.selectByPlanId(map);
        for (int i = 0; i < testcaseRelations.size(); i++) {
            if (Util.isNullOrEmpty(testcaseRelations.get(i).getTestcaseExecuteResult())) {
                continue;
            }
            switch (testcaseRelations.get(i).getTestcaseExecuteResult()) {
                case "1":
                    // 成功
                    returnMap.put("successNum", returnMap.get("successNum") + 1);
                    break;
                case "2":
                    // 阻塞
                    returnMap.put("zuaiNum", returnMap.get("zuaiNum") + 1);
                    break;
                // 失败
                case "3":
                    returnMap.put("failedNum", returnMap.get("failedNum") + 1);
                    break;
                default:
                    break;
            }
            returnMap.put("exeNum", testcaseRelations.size());
        }
        return returnMap;
    }

    /**
     * 查看当前计划下面的案例
     *
     * @param map
     * @return
     * @throws Exception
     */

    public List<Testcase> selectByPlanId(Map map) throws Exception {
        List<Testcase> list = this.queryTestcaseByPlanId(map);
        return list;
    }

    @Override
    public List<Testcase> queryTestcaseByOption(TestcaseAuditQueryObject requestParam, String userEnName) throws Exception {
        List<Testcase> list = testcaseMapper.queryTestcaseByOption(requestParam.getStart(), requestParam.getPagesize(),
                requestParam, userEnName);
        for (Testcase testcase1 : list) {
            Map user = userService.queryUserCoreData(testcase1.getTestcasePeople());
            if (!Util.isNullOrEmpty(user)) {
                testcase1.setUserName((String) user.get(Dict.USER_NAME_CN));
            }
        }
        return list;
    }

    @Override
    public List<Testcase> queryTestcaseByPlanIdOrStatus(Integer planId) throws Exception {
        return this.testcaseMapper.queryTestcaseByPlanIdOrStatus(planId);
    }

    @Override
    public List<String> queryMaxCaseNo(String testcaseDate) throws Exception {
        return testcaseMapper.queryMaxCaseNo(testcaseDate);
    }

    @Override
    public Map<String, Integer> queryAllStatus(Integer planId) throws Exception {
        return testcaseMapper.queryAllStatus(planId);
    }

    @Override
    public int countTestcase(TestcaseAuditQueryObject requestParam, String userEnName) throws Exception {
        return testcaseMapper.countTestcase(requestParam, userEnName);
    }

    @Override
    public int countTestcaseByFuncId(Map map) throws Exception {
        return testcaseMapper.countTestcaseByFuncId(map);
    }

    @Override
    public void changeNecessary(String testcaseNo, String necessaryFlag) throws Exception {
        testcaseMapper.changeNecessary(testcaseNo, necessaryFlag);
    }

    @Override
    public Map<String, Integer> queryUserApprovalList() throws Exception {
        String userName = utils.getCurrentUserEnName();
        Map<String, Integer> map = testcaseMapper.queryUserApprovalList(userName);
        if (Util.isNullOrEmpty(map)) {
            map = new HashMap<String, Integer>();
            map.put(Dict.WAITAPPROVAL, 0);
            map.put(Dict.WAITEFFECT, 0);
        }
        return map;
    }

    @Override
    public int updateTestcaseOrder(Map map) throws Exception {
        //存放案例序号发生变化的案例
        List<Map> change = new ArrayList<Map>();
        int i = 0;
        Integer planId = (Integer) map.get("planId");
        List<Map> allCase = (List<Map>) map.get("allCase");
        List<Testcase> beforeList = testcaseMapper.queryAllTestByPlanId(planId);
        for (Testcase testcase : beforeList) {
            String testcasePeople = testcase.getTestcasePeople();
            Map user = userService.queryUserCoreData(testcasePeople);
            if (!Util.isNullOrEmpty(user)) {
                testcase.setUserName((String) user.get(Dict.USER_NAME_CN));
            }
        }
        //排序之前的案例
        Map<String, String> beforeMap = new HashMap<String, String>();
        for (Testcase testcase : beforeList) {
            beforeMap.put(testcase.getTestcaseNo(), testcase.getOrderNum());
        }
        //排序之后的案例
        Map<String, String> nowMap = new HashMap<String, String>();
        for (Map item : allCase) {
            Set<String> set = item.keySet();
            String key = set.iterator().next();
            nowMap.put(key, item.get(key) + "");
        }
        Iterator<String> iterator = beforeMap.keySet().iterator();
        while (iterator.hasNext()) {
            Map<String, Object> changeMap = new HashMap<String, Object>();
            String key = iterator.next();
            if (nowMap.get(key) != beforeMap.get(key)) {
                changeMap.put("testcaseNo", key);
                changeMap.put("orderNum", nowMap.get(key));
                change.add(changeMap);
            }
        }
        for (Map item : change) {
            i = 0;
            String testcaseNo = (String) item.get("testcaseNo");
            String orderNum = item.get("orderNum") + "";
            i = testcaseMapper.updateTestcaseOrder(orderNum, planId, testcaseNo);
        }
        return i;
    }

    /**
     * 根据工单号查询其下任务联系人
     *
     * @param map
     * @return
     * @throws Exception
     */
    @Override
    public Map queryRelativePeople(Map map) throws Exception {
        String workNo = String.valueOf(map.get(Dict.WORKNO));
        Set<String> userNameEnsFtms = new HashSet<>();
        Set<String> userNameEnsFdev = new HashSet<>();
        //玉衡三角色
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workNo);
        userNameEnsFtms.add(workOrder.getWorkManager());
        if (!Util.isNullOrEmpty(workOrder.getGroupLeader())) {
            userNameEnsFtms.addAll(Arrays.asList(workOrder.getGroupLeader().split(",")));
        }
        if (!Util.isNullOrEmpty(workOrder.getTesters())) {
            userNameEnsFtms.addAll(Arrays.asList(workOrder.getTesters().split(",")));
        }
        if (!Util.isNullOrEmpty(workOrder.getMainTaskNo())) {
            //老工单主任务人员数据
            Map taskInfo = testcaseUtils.queryTaskDetail(workOrder.getMainTaskNo());
            if (!Util.isNullOrEmpty(taskInfo)) {
                testcaseUtils.addUserNameEnByRoleName(userNameEnsFdev, taskInfo, Dict.SPDBMASTER);
                testcaseUtils.addUserNameEnByRoleName(userNameEnsFdev, taskInfo, Dict.MASTER);
                testcaseUtils.addUserNameEnByRoleName(userNameEnsFdev, taskInfo, Dict.DEVELOPER);
            }
        }
        List<String> taskNos = workOrderMapper.queryTaskByNo(workNo);
        if (!Util.isNullOrEmpty(taskNos) && "0".equals(utils.isNewFtms())) {
            //任务集里的人员数据
            for (String taskNo : taskNos) {
                Map taskInfo = testcaseUtils.queryTaskDetail(taskNo);
                if (!Util.isNullOrEmpty(taskInfo)) {
                    testcaseUtils.addUserNameEnByRoleName(userNameEnsFdev, taskInfo, Dict.SPDBMASTER);
                    testcaseUtils.addUserNameEnByRoleName(userNameEnsFdev, taskInfo, Dict.MASTER);
                    testcaseUtils.addUserNameEnByRoleName(userNameEnsFdev, taskInfo, Dict.DEVELOPER);
                }
            }
        }
        if (!Util.isNullOrEmpty(taskNos) && "1".equals(utils.isNewFtms())) {
            for (String taskNo : taskNos) {
                Map taskInfo = sendEmailService.getNewTaskById(taskNo);
                List<String> assigneeList = (List<String>) taskInfo.get("assigneeList");
                String nameEn = (String) userService.queryUserById(assigneeList.get(0)).get(Dict.USER_NAME_EN);
                userNameEnsFdev.add(nameEn);
            }
        }
        //组装玉衡数据
        List<Map> userListFtms = new ArrayList<>();
        for (String userNameEn : userNameEnsFtms) {
            Map userInfo = testcaseUtils.queryUserCoreDataByNameEn(userNameEn);
            Map<String, String> userUnit;
            if (!Util.isNullOrEmpty(userInfo)) {
                userUnit = new HashMap<>();
                userUnit.put(Dict.USER_NAME_EN, String.valueOf(userInfo.get(Dict.USER_NAME_EN)));
                userUnit.put(Dict.USER_NAME_CN, String.valueOf(userInfo.get(Dict.USER_NAME_CN)));
                userUnit.put(Dict.EMAIL, String.valueOf(userInfo.get(Dict.EMAIL)));
                userListFtms.add(userUnit);
            }
        }
        //组装fdev数据
        List<Map> userListFdev = new ArrayList<>();
        for (String userNameEn : userNameEnsFdev) {
            Map userInfo = testcaseUtils.queryUserCoreDataByNameEn(userNameEn);
            Map<String, String> userUnit;
            if (!Util.isNullOrEmpty(userInfo)) {
                userUnit = new HashMap<>();
                userUnit.put(Dict.USER_NAME_EN, String.valueOf(userInfo.get(Dict.USER_NAME_EN)));
                userUnit.put(Dict.USER_NAME_CN, String.valueOf(userInfo.get(Dict.USER_NAME_CN)));
                userUnit.put(Dict.EMAIL, String.valueOf(userInfo.get(Dict.EMAIL)));
                userListFdev.add(userUnit);
            }
        }
        //根据工单提供备选计划信息
        List<Map<String, String>> planInfo = workOrderMapper.queryPlanByOrderNo(workNo);
        Map result = new HashMap();
        result.put(Dict.FTMS, userListFtms);
        result.put(Dict.FDEV, userListFdev);
        result.put(Dict.PLAN, planInfo);
        return result;
    }

    /**
     * 统计通用的案例
     *
     * @param requestParam
     * @param userEnName
     * @return
     */
    @Override
    public Integer queryTestcaseByOptionCount(TestcaseAuditQueryObject requestParam, String userEnName) {
        Integer count = testcaseMapper.queryTestcaseByOptionCount(requestParam, userEnName);
        return count;
    }
}
