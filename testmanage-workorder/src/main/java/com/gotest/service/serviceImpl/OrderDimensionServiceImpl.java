package com.gotest.service.serviceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.gotest.dao.*;
import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.domain.*;
import com.gotest.service.IUserService;
import com.gotest.service.OrderDimensionService;
import com.gotest.utils.CommonUtils;
import com.gotest.utils.MailUtil;
import com.gotest.utils.MyUtil;
import com.gotest.utils.OrderUtil;
import com.test.testmanagecommon.cache.LazyInitPropertyLong;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

//@SuppressWarnings("all")
@Service
@RefreshScope
public class OrderDimensionServiceImpl implements OrderDimensionService {

    private Logger logger = LoggerFactory.getLogger(OrderDimensionServiceImpl.class);
    @Autowired
    private OrderDimensionMapper odsMapper;
    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private MessageFdevMapper msgMapper;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private FdevGroupApiImpl fdevGroupApi;
    @Value("${spring.profiles.active}")
    private String port;
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private IUserService userService;
    @Autowired
    private MantisServiceImpl mantisService;
    @Autowired
    private QualityReportMapper qualityReportMapper;
    @Autowired
    private ITaskApiImpl iTaskApi;
    @Autowired
    private TaskListMapper taskListMapper;

    /**
     * 表格
     * 展示内容字段：
     * 工单编号、主任务名称、工单状态、案例总执行数、案例执行通过数1、案例执行失败数2、案例执行阻塞数3、案例未执行数
     */
    @Override
    public List<OrderDimension> queryOrderDimension(Map<String, Object> map) throws Exception {
        String startTime = (String)map.getOrDefault(Dict.STARTTIME, Constants.FAIL_GET);
        String endTime = (String)map.getOrDefault(Dict.ENDTIME, Constants.FAIL_GET);
        String groupId = (String)map.getOrDefault(Dict.GROUPID, Constants.FAIL_GET);
        String orderNameOrNo = String.valueOf(map.getOrDefault(Dict.ORDERNAMEORNO, Constants.FAIL_GET));
        String tester = (String)map.getOrDefault(Dict.TESTER, Constants.FAIL_GET);
        String stage = (String)map.getOrDefault(Dict.STAGE, Constants.FAIL_GET);
        boolean flag = (boolean)map.get(Dict.ISPARENT);
        List<String> groupIds = new ArrayList<>();
        if(!Util.isNullOrEmpty(groupId)){
            if(flag){
                List<Map<String, String>> maps = userService.queryChildGroupById(groupId);
                Set<String> collect = maps.stream().map(e -> e.get(Dict.ID)).collect(Collectors.toSet());
                groupIds.addAll(collect);
            }else{
                groupIds.add(groupId);
            }
        }
        //工单类型，默认查询功能工单
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        List<OrderDimension> resultList = odsMapper.queryOrderDimension(startTime, endTime, groupIds, orderNameOrNo, tester, stage, orderType);
        Map<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.STARTDATE, startTime);
        sendMap.put(Dict.ENDDATE, endTime);
        sendMap.put(Dict.REST_CODE, "mantis.countIssueDetailByOrderNos");
        Map<String, Object> mantisResult = (Map<String, Object>) restTransport.submit(sendMap);
        for(OrderDimension wr : resultList) {
            String workNo = wr.getWorkNo();
            // 测试人员
            String testers = myUtil.changeEnNameToCn(wr.getTesters());
            if(!CommonUtils.isNullOrEmpty(testers)) {
                wr.setTesters(testers);
            } else {
                wr.setTesters("");
            }
            String group_id = wr.getGroupId();
            if(!Util.isNullOrEmpty(group_id)){
                Map<String, Object> group = userService.queryGroupDetailById(group_id);
                if(!Util.isNullOrEmpty(group)){
                    wr.setGroupName((String)group.get(Dict.FULLNAME));
                }
            }else{
                wr.setGroupName("/");
            }
            Map<String, Object> mantisWorkOrder = (Map<String, Object>) mantisResult.get("workOrders");
            if(!CommonUtils.isNullOrEmpty(mantisWorkOrder)) {
                Map<String, Object> mantisDetail = (Map<String, Object>) mantisWorkOrder.get(workNo);
                if(!CommonUtils.isNullOrEmpty(mantisDetail)) {
                    // 有效缺陷总数
                    wr.setCaseMantis((Integer) mantisDetail.get("effectiveIssue"));
                    // 开发责任人
                    wr.setDeveloper((String) mantisDetail.get("developer_cn"));
                    wr.setRqrNum((Integer) mantisDetail.get("rqrNum"));
                    wr.setRqrRuleNum((Integer) mantisDetail.get("rqrRuleNum"));
                    wr.setFuncLackNum((Integer) mantisDetail.get("funcLackNum"));
                    wr.setFuncErrNum((Integer) mantisDetail.get("funcErrNum"));
                    wr.setHistoryNum((Integer) mantisDetail.get("historyNum"));
                    wr.setOptimizeNum((Integer) mantisDetail.get("optimizeNum"));
                    wr.setBackNum((Integer) mantisDetail.get("backNum"));
                    wr.setPackageNum((Integer) mantisDetail.get("packageNum"));
                }
            }
        }
        return resultList;
    }

    /**
     * 饼状图：
     * 需要字段：工单状态、工单数
     */
    @Override
    public List<WorkOrderStatus> queryWorkOrderStatus(Map map) throws Exception {
        String startTime = (String) map.getOrDefault(Dict.STARTDATE, Constants.FAIL_GET);
        String endTime = (String) map.getOrDefault(Dict.ENDDATE, Constants.FAIL_GET);
        String groupId = (String) map.getOrDefault(Dict.GROUPID, Constants.FAIL_GET);
        boolean flag = (boolean) map.getOrDefault(Dict.ISPARENT, Constants.FAIL_GET);
        List groupIds = new ArrayList();
        if(!Util.isNullOrEmpty(groupId)){
            if(flag){
                List<Map<String, String>> maps = userService.queryChildGroupById(groupId);
                Set<String> collect = maps.stream().map(e -> e.get(Dict.ID)).collect(Collectors.toSet());
                groupIds.addAll(collect);
            }else{
                groupIds.add(groupId);
            }
        }
        List<WorkOrderStatus>  result = odsMapper.queryWorkOrderStatus(startTime, endTime, groupIds);
        List<WorkOrderStatus> workOrderStatusList = new ArrayList<>();
        for(int i = 10; i >=0; i--){
            if(i == 5 || i ==7|| i==8 ){
                continue;
            }
            WorkOrderStatus workOrderStatus = new WorkOrderStatus(String.valueOf(i), 0);
            workOrderStatusList.add(workOrderStatus);
        }
        for(WorkOrderStatus w : workOrderStatusList){
            for(WorkOrderStatus r : result){
                if(w.getWorkStage().equals(r.getWorkStage())){
                    w.setStageCount(r.getStageCount());
                }
            }
        }
        return workOrderStatusList;
    }

    /**
     * sit报告处 数据详情查询
     */
    @Override
    public List<WorkOrderUser> queryUpSitOrder(Integer currentPage, Integer pageSize, String taskName, String workOrderNo,
                                               String userEnName, String done, String sortManager, String orderType) throws Exception {
        Integer start = pageSize * (currentPage - 1);//起始
        List<WorkOrder> userOrderList = null;
        try {
            userOrderList = workOrderMapper.queryUpSitReport(start, pageSize, taskName, workOrderNo, userEnName, done, sortManager, orderType);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{taskName});
        }
        List<WorkOrderUser> orderUserList = myUtil.makeEnNameToChName(userOrderList);
        return orderUserList;
    }

    /**
     * sit报告处 数据详情总数---分页用
     */
    @Override
    public Integer queryUpSitOrderCount(String taskName, String workOrderNo, String userEnName, String done, String orderType) {
        Integer count = null;
        try {
            count = workOrderMapper.queryUpSitOrderCount(taskName, workOrderNo, userEnName, done, orderType);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{taskName});
        }
        return count;
    }

    /**
     * 导出sit报告 数据
     * 测试任务名称（mainTaskName）实施单元(unitNo) 测试任务编号(mainTaskNo)
     * 工单编号(workNo) 测试小组长（groupLeader）测试人员(testers)
     * 测试结果(testResult)：  测试结果=通过/（全部-无效）  取百分号，小数点后两位
     * 测试范围(testRange)：   msg_fdev表的task_desc字段的数据拼接
     * 数组：planData
     * 测试计划名称(planName) 测试案例总数（allCase）
     * 执行通过数(sumSucc)：执行计划失败数(sumBlock) 执行案例阻碍数(sumFail)：
     * 有效缺陷数(validMantis)：根据缺陷信息走
     * 无效缺陷数(braceMantis)：根据缺陷信息走
     */
    @Override
    public Map exportSitReportData(String workNo, Map map) {
        Map<String, BigDecimal> testResultMap; //测试结果
        List<String> testDescs; //测试范围
        List<Map<String, String>> planDate; // 根据工单编号获取，对应的执行计划，案例的执行情况
        try {
            testResultMap = odsMapper.queryTestResult(workNo);
            testDescs = msgMapper.queryTestDesc(workNo);
            planDate = odsMapper.queryPlanStatusCount(workNo);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{e.getMessage()});
        }

        //组装测试结果字段
        String testResult = "0";
        if (!Util.isNullOrEmpty(testResultMap)) {
            testResultMap.remove(null);
            BigDecimal allCase = testResultMap.get(Dict.ALLCASE);
            BigDecimal sumPass = testResultMap.get(Dict.SUMPASS);
            BigDecimal sumUseless = testResultMap.get(Dict.SUMUSELESS);
            if (!allCase.toString().equals("0") && !allCase.equals(sumUseless)) {
                testResult = String.format("%.2f", sumPass.divide(allCase.subtract(sumUseless), 6, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100))) + "%";
            }
        }
        map.put(Dict.TESTRESULT, testResult);

        //组装测试范围字段
        testDescs.remove(null);
        StringBuffer testRange = new StringBuffer();
        if (!Util.isNullOrEmpty(testDescs)) {
            for (int i = 1; i <= testDescs.size(); i++) {
                testRange.append(i + ": ").append(testDescs.get(i-1));
                if (i < testDescs.size()) {
                    testRange.append("<br>");
                }
            }
        }
        map.put(Dict.TESTRANGE, testRange.toString());

        //组装测试计划数据
        //根据 工单编号，检索获取到对应的缺陷数据
        ArrayList<LinkedHashMap> mantisData = getMantisData(workNo, "", "");
        for (Map<String, String> plan : planDate) {
            plan.put(Dict.VALIDMANTIS, 0 + "");
            plan.put(Dict.BRACEMANTIS, 0 + "");
            if (!Util.isNullOrEmpty(plan.get(Dict.IDS))) { //首先判断案例是否存在
                List<String> ids = Arrays.asList(plan.get(Dict.IDS).split(","));
                //便利执行计划的同时，顺手给 有/无效缺陷 数值给组装一下
                for (LinkedHashMap mantis : mantisData) {
                    if (ids.contains((String) mantis.get(Dict.PLANTCASEID))) { //当该缺陷在 该执行计划下
                        //判断缺陷类型 并赋值
                        if (matinsSourceToNum(mantis) == 1) {
                            plan.put(Dict.VALIDMANTIS, (Integer.parseInt(plan.get(Dict.VALIDMANTIS)) + 1) + "");
                        } else if (matinsSourceToNum(mantis) == 0) {
                            plan.put(Dict.BRACEMANTIS, (Integer.parseInt(plan.get(Dict.BRACEMANTIS)) + 1) + "");
                        }
                    }
                }
            }
        }
        map.put(Dict.PLANDATA, planDate);
        //组装饼图数据
        putPitData(map, mantisData);
        return map;
    }

    private void validateResult(Map result){
        if(Util.isNullOrEmpty(result.get(Dict.MAINTASKNAME))){
            result.put(Dict.MAINTASKNAME,"");
        }
        if(Util.isNullOrEmpty(result.get(Dict.UNIT))){
            result.put(Dict.UNIT,"");
        }
        if(Util.isNullOrEmpty(result.get(Dict.MAINTASKNO))){
            result.put(Dict.MAINTASKNO,"");
        }
        if(Util.isNullOrEmpty(result.get(Dict.WORKORDERNO))){
            result.put(Dict.WORKORDERNO,"");
        }
        if(Util.isNullOrEmpty(result.get(Dict.GROUPLEADER))){
            result.put(Dict.GROUPLEADER,"");
        }
        if(Util.isNullOrEmpty(result.get(Dict.TESTERS))){
            result.put(Dict.TESTERS,"");
        }
        if(Util.isNullOrEmpty(result.get(Dict.TESTRESULT))){
            result.put(Dict.TESTRESULT,"");
        }
        if(Util.isNullOrEmpty(result.get(Dict.TESTRANGE))){
            result.put(Dict.TESTRANGE,"");
        }
    }

    private void addToNameEn(List<String> toNameEn, String names){
        String[] split = names.split(",");
        for (String nameEn : split) {
            toNameEn.add(nameEn);
        }
    }

    private void fdevAddToNameEn(List<String> toNameEn, List<Map> peoples){
        for (Map people :peoples) {
            toNameEn.add((String)people.get(Dict.USER_NAME_EN));
        }
    }


    /**
     * 获取缺陷数据
     */

    public ArrayList<LinkedHashMap> getMantisData(String workNo, String startTime, String endTime) {
        Map sendDate = new HashMap();
        if (workNo != null) {
            sendDate.put(Dict.WORKNO, workNo);
        }
        sendDate.put(Dict.PAGESIZE, String.valueOf(Integer.MAX_VALUE));
        ArrayList<LinkedHashMap> mantisList = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> oncemantisList = new ArrayList<LinkedHashMap>();
        int startPage = 1;
        do {
            sendDate.put(Dict.CURRENTPAGE, String.valueOf(startPage++));
            sendDate.put(Dict.STARTDATE, startTime);
            sendDate.put(Dict.ENDDATE, endTime);
            sendDate.put(Dict.REST_CODE, "mantis.query");
            try {
                Map<String, Object> result = (Map<String, Object>) restTransport.submit(sendDate);
                oncemantisList = (ArrayList<LinkedHashMap>)result.get("issues");
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new FtmsException(ErrorConstants.MANTIS_ERROR);
            }
            mantisList.addAll(oncemantisList);
        } while (oncemantisList.size() == Integer.MAX_VALUE);

        return mantisList;
    }


    /**
     * 匹配缺陷原因
     */
    public int matinsSourceToNum(LinkedHashMap mantis) {
        String flawSource = (String) mantis.get(Dict.FLAWSOURCE);
        String status = (String) mantis.get(Dict.STATUS);
        int i = -1;
        if (status.equals("30")) { //若缺陷状态为30（确认拒绝）  则缺陷为
            return 0;
        }
        switch (flawSource) {
            case "业务需求问题":
                i = 1;
                break;
            case "需规问题":
                i = 1;
                break;
            case "功能实现不完整":
                i = 1;
                break;
            case "功能实现错误":
                i = 1;
                break;
            case "历史遗留问题":
                i = 1;
                break;
            case "优化建议":
                i = 1;
                break;
            case "后台问题":
                i = 1;
                break;
            case "打包问题":
                i = 1;
                break;
            case "兼容性异常":
                i = 1;
                break;
            case "数据问题":
                i = 0;
                break;
            case "环境问题":
                i = 0;
                break;
            case "其他原因":
                i = 0;
                break;
            case "水平越权漏洞":
                i = 1;
                break;
            case "重放攻击漏洞":
                i = 1;
                break;
            case "安全功能设计机制不健全":
                i = 1;
                break;
            default:
                break;
        }
        return i;
    }

    /**
     * 将 六个饼图数据存入map中
     */
    public void putPitData(Map map, ArrayList<LinkedHashMap> mantisData) {
        disposeMantisData(map, mantisData, Dict.FLAWTYPE, Dict.NAME, Dict.VALUE);
        disposeMantisData(map, mantisData, Dict.SEVERITY, Dict.NAME, Dict.VALUE);
        disposeMantisData(map, mantisData, Dict.REPORTER, Dict.NAME, Dict.VALUE);
        disposeMantisData(map, mantisData, Dict.HANDLER, Dict.NAME, Dict.VALUE);
        disposeMantisData(map, mantisData, Dict.REDMINEID, Dict.NAME, Dict.VALUE);
        disposeMantisData(map, mantisData, Dict.STATUS, Dict.NAME, Dict.VALUE);
    }

    /**
     * 缺陷数据 统计处理
     */
    public void disposeMantisData(Map map, ArrayList<LinkedHashMap> mantisList, String mantisKey, String key, String value) {
        if (Util.isNullOrEmpty(mantisList)) {
            return;
        }
        Map<Object, Integer> putData = new HashMap();
        for (LinkedHashMap eleMantis : mantisList) {
            Object obj = eleMantis.get(mantisKey);
            if (Util.isNullOrEmpty(putData.get(obj))) {
                putData.put(obj, 1);
            } else {
                putData.put(obj, putData.get(obj) + 1);
            }
        }
        List<Map<String, Object>> putList = new LinkedList<>();
        Map<String, Object> listMap;
        for (Map.Entry<Object, Integer> entry : putData.entrySet()) {
            listMap = new HashMap<>();
            listMap.put(key, entry.getKey());
            listMap.put(value, entry.getValue());
            putList.add(listMap);
        }
        map.put(mantisKey, putList);
    }


    @Override
    public List<Map<String, Object>> queryDayGroupReport(String startDate, String endDate, String groupId) throws Exception {
        //组名称	主任务名称	工单状态	测试人员	开发人员	案例总数	当日编写案例数	执行案例总数	当日执行案例数	缺陷总数	已修复缺陷	未修复缺陷
        List<Map<String, Object>> orderData = odsMapper.queryDayGroupReport(startDate, endDate, groupId);
        for (Map<String, Object> map : orderData) {
            String mainTaskNo = (String) map.get(Dict.MAINTASKNO);
            String developerList = null;
            if(!Util.isNullOrEmpty(mainTaskNo)){
                developerList = getDeveloperList(mainTaskNo);
            }
            Map<String, Object> group = userService.queryGroupDetailById(groupId);
            if(!Util.isNullOrEmpty(group)){
                map.put(Dict.GROUPNAME, group.get(Dict.NAME));
            }
            map.put(Dict.DEVELOPER, Util.isNullOrEmpty(developerList)?"":developerList);
            String workNo = (String) map.get(Dict.WORKNO);
            Map<String, Object> allCase = odsMapper.queryDayGroupAllCase(workNo);
			map.put(Dict.SUMCASE, allCase.get(Dict.SUMCASE));
			map.put(Dict.SUMEXE, allCase.get(Dict.SUMEXE));
            Map sendMap = new HashMap();
            sendMap.put(Dict.WORKNO, workNo);
            sendMap.put(Dict.REST_CODE, "mantis.queryWorkOrderIssues");
            Map<String, Integer> issues = new HashMap<>();
            map.put(Dict.TESTERS, changeToNameCn((String) map.get(Dict.TESTERS)));
            if (!CommonUtils.isNullOrEmpty(workNo)) {
                HttpHeaders header = new HttpHeaders();
                header.add("Content-Type", "application/json");
                issues = (Map<String, Integer>) restTransport.submitWithHeaders(sendMap, header);
            }
            if (!CommonUtils.isNullOrEmpty(issues)) {
                map.putAll(issues);
            }
            map.put(Dict.WORKSTAGE, formatWorkStage(String.valueOf(map.get(Dict.WORKSTAGE))));
        }
        return orderData;
    }

    public String formatWorkStage(String workStage) {
        switch (workStage) {
            case "0":
                workStage = "未分配";
                break;
            case "1":
                workStage = "开发中";
                break;
            case "2":
                workStage = "sit";
                break;
            case "3":
                workStage = "uat";
                break;
            case "4":
                workStage = "已投产";
                break;
            case "6":
                workStage = "uat(含风险)";
                break;
            case "8":
                workStage = "撤销";
                break;
            case "9":
                workStage = "分包测试（内测完成）";
                break;
            case "10":
                workStage = "分包测试（含风险）";
                break;
            case "11":
                workStage = "废弃";
                break;
            case "12":
                workStage = "安全测试（内测完成）";
                break;
            case "13":
                workStage = "安全测试（含风险）";
                break;
            case "14":
                workStage = "安全测试（不涉及）";
                break;
            default:
                workStage = "";
        }
        return workStage;
    }


    ////查询工单对应的开发人员 且获取对应的中文名字符串
    private String getDeveloperList(String taskNo) throws Exception {
        String developNameCns = "";
        List<Map> develop = new ArrayList<>();
        Map sendMap = new HashMap<>();
        sendMap.put(Dict.ID, taskNo);
        sendMap.put(Dict.REST_CODE, "fdev.task.query");
        List<Map<String, Object>> submit = (List<Map<String, Object>>) restTransport.submitSourceBack(sendMap);
        if (!CommonUtils.isNullOrEmpty(submit)) {
            develop = (List) submit.get(0).get(Dict.DEVELOPER);
        }
        for (Map names : develop) {
            String nameCn = (String) names.get(Dict.USERNAMECN);
            if (developNameCns.equals("")) {
                developNameCns = nameCn;
            } else {
                developNameCns += "," + nameCn;
            }
        }
        return developNameCns;

    }

    /**
     * 将组和工单号按照组维度来组装数据
     * @param list
     * @return
     */
    private Map<String, List<String>> dataChange(List<Map<String, Object>> list) {
        Map<String, List<String>> dataMap = new HashMap();
        for (Map<String, Object> map : list) {
            String groupId = String.valueOf(map.get(Dict.GROUPID));//组id
            String workNo = String.valueOf(map.get(Dict.WORKNO));//工单号
            if (CommonUtils.isNullOrEmpty(dataMap.get(groupId))) {
                List<String> workNos = new ArrayList<>();
                workNos.add(workNo);
                dataMap.put(groupId, workNos);
            } else {
                List<String> workNos = dataMap.get(groupId);
                workNos.add(workNo);
            }
        }
        return dataMap;
    }

    /**
     * 组装已完成工单数
     * @param list
     * @return
     */
    private Map<String, Object> AlreadyDataChange(List<Map<String, Object>> list) {
        Map<String, Object> doneDevOrders = new HashMap();
        for (Map<String, Object> map : list) {
            String groupId = String.valueOf(map.get(Dict.GROUPID));//组id
            Map<String, Integer> doneDevOrder = new HashMap<>();
            doneDevOrder.put(Dict.ALREADYORDER, Integer.parseInt(map.get(Dict.ALREADYORDER).toString()));
            doneDevOrder.put(Dict.DEVORDER, Integer.valueOf(String.valueOf(map.get(Dict.DEVORDER))));
            doneDevOrder.put(Dict.CREATEORDER, Integer.valueOf(String.valueOf(map.get(Dict.CREATEORDER))));
            doneDevOrder.put(Dict.UNDERWAYORDER, Integer.valueOf(String.valueOf(map.get(Dict.UNDERWAYORDER))));
            doneDevOrders.put(groupId, doneDevOrder);
        }
        return doneDevOrders;
    }

    private Map<String, Integer> countOrderIssue(String workNo, String startDate, String endDate) throws Exception {
        Integer sumIssue = 0;
        Integer sumSolveIssue = 0;
        Integer sumUnSolveIssue = 0;
        Integer developMantis = 0;
        Integer currentMantisSum = 0;
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        if (!CommonUtils.isNullOrEmpty(workNo)) {
            Map sendMap = new HashMap();
            sendMap.put(Dict.WORKNO, workNo);
            sendMap.put(Dict.STARTDATE, startDate);
            sendMap.put(Dict.ENDDATE, endDate);
            sendMap.put(Dict.REST_CODE, "mantis.queryWorkOrderIssues");
            Map<String, Integer> issues = (Map<String, Integer>) restTransport.submitWithHeaders(sendMap, header);
            if (!CommonUtils.isNullOrEmpty(issues)) {
                sumIssue += issues.get(Dict.ISSUENUM);
                sumSolveIssue += issues.get(Dict.SOLVEISSUE);
                sumUnSolveIssue += issues.get(Dict.UNSOLVEISSUE);
                developMantis += issues.get(Dict.DEVELOPMANTIS);
                currentMantisSum += issues.get(Dict.CURRENTMANTISSUM);
            }
        }
        Map<String, Integer> result = new HashMap<>();
        result.put(Dict.SOLVEISSUE, sumSolveIssue);
        result.put(Dict.UNSOLVEISSUE, sumUnSolveIssue);
        result.put(Dict.ISSUENUM, sumIssue);
        result.put(Dict.DEVELOPMANTIS, developMantis);
        result.put(Dict.CURRENTMANTISSUM, currentMantisSum);
        return result;
    }
    private Map<String, Object> countIssueDetailByOrderNos(List<String> workNos, String startDate, String endDate) throws Exception {
        Map<String, Object> issues = new HashMap<>();
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        if(!Util.isNullOrEmpty(workNos)){
            Map sendMap = new HashMap();
            sendMap.put(Dict.WORKNO, workNos);
            sendMap.put(Dict.STARTDATE, startDate);
            sendMap.put(Dict.ENDDATE, endDate);
            sendMap.put(Dict.REST_CODE, "mantis.countIssueDetailByOrderNos");
            issues = (Map<String, Object>) restTransport.submitWithHeaders(sendMap, header);
        }
        return  issues;
    }

    private Map<String, Integer> countGroupIssue(List<String> workNos) throws Exception {
        Integer sumIssue = 0;
        Integer sumSolveIssue = 0;
        Integer sumUnSolveIssue = 0;
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        if (!CommonUtils.isNullOrEmpty(workNos)) {
            for (String workNo : workNos) {
                Map sendMap = new HashMap();
                sendMap.put(Dict.WORKNO, workNo);
                sendMap.put(Dict.REST_CODE, "mantis.queryWorkOrderIssues");
                Map<String, Integer> issues = (Map<String, Integer>) restTransport.submitWithHeaders(sendMap, header);
                if (!CommonUtils.isNullOrEmpty(issues)) {
                    Integer solveIssue = issues.get(Dict.SOLVEISSUE);
                    Integer unSolveIssue = issues.get(Dict.UNSOLVEISSUE);
                    Integer issueNum = issues.get(Dict.ISSUENUM);
                    sumIssue += issueNum;
                    sumSolveIssue += solveIssue;
                    sumUnSolveIssue += unSolveIssue;
                }
            }
        }
        Map<String, Integer> result = new HashMap<>();
        result.put(Dict.SOLVEISSUE, sumSolveIssue);
        result.put(Dict.UNSOLVEISSUE, sumUnSolveIssue);
        result.put(Dict.ISSUENUM, sumIssue);
        return result;
    }

    /**
     * 组装日报大表数据
     * @param list
     * @return
     */
    private List<Map<String, Object>> convertDate(List<Map<String, Object>> list) throws Exception{
        List<Map<String, Object>> groupDates = new ArrayList<>();
        //组id数组，用来检测是否新开map
        List<String> groupIds = new ArrayList<>();
        for (Map<String, Object> map : list) {
            //获取字段
            String groupId = String.valueOf(map.get(Dict.GROUPID));//组id
            //如果组id没有的脏数据，剔除
            if(Util.isNullOrEmpty(groupId)){
                continue;
            }
            Map<String, Object> group = userService.queryGroupDetailById(groupId);
            String groupName = "";
            if(!Util.isNullOrEmpty(group)){
                groupName = (String)group.get(Dict.NAME);
            }
            String workNo = String.valueOf(map.get(Dict.WORKNO));//工单号
            //如果组id未出现过
            if(!groupIds.contains(groupId)){
                groupIds.add(groupId);
                Map<String, Object> groupDate = new HashMap<>();
                groupDate.put(Dict.GROUPID, groupId);
                groupDate.put(Dict.GROUPNAME, groupName);
                List<String> workNos = new ArrayList<>();
                workNos.add(workNo);
                groupDate.put(Dict.WORKNOS, workNos);
                groupDates.add(groupDate);
            }else{
                //组id出现过
                for(Map<String, Object> groupDate : groupDates){
                    if(groupId.equals(groupDate.get(Dict.GROUPID))){
                        ((List<String>)groupDate.get(Dict.WORKNOS)).add(workNo);
                    }
                }
            }
        }
        return groupDates;
    }

    /**
     * 组维度报表
     * @param startDate
     * @param endDate
     * @param group
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> queryDayTotalReport(String startDate, String endDate, List<String> group, boolean isParent) throws Exception {
        List<String> groupList = new ArrayList<>();
        if(!Util.isNullOrEmpty(group)){
            if(isParent){
                for (String groupId : group) {
                    List<Map<String, String>> maps = userService.queryChildGroupById(groupId);
                    Set<String> collect = maps.stream().map(e -> e.get(Dict.ID)).collect(Collectors.toSet());
                    groupList.addAll(collect);
                }
            }else{
                groupList.addAll(group);
            }
        }
        String groupFilter = "'" + String.join("','", groupList) + "'";
        //1.选定时间内新建案例关系 2.选定时间内执行案例 3.选定时间内提uat 4.处于sit (工单号，组id，组名)
        List<Map<String, Object>> orderData = odsMapper.queryDayTotalWorks(startDate, endDate, groupFilter);
        //将组和工单号按照组维度来组装数据
        List<Map<String, Object>> orderMap = convertDate(orderData);
        //选定时间内已提uat的工单数(已完成工单);开发中工单数;进行中工单数;新建工单数，组id
        Map<String, Object> doneDevOrders = AlreadyDataChange(odsMapper.queryAlreadyOrder(startDate, endDate));
        //遍历每个组
        orderMap.parallelStream().forEach(map ->{
            try{
                String groupId = String.valueOf(map.get(Dict.GROUPID));
                List<String> workNos = (List<String>)map.get(Dict.WORKNOS);
                Integer sumOrder = Integer.valueOf(workNos.size());
                map.put(Dict.SUMORDER, sumOrder);
                Integer unExeCaseAll = 0;//待测案例总数
                Integer sumExeAll = 0;//总执行数
                Integer sumCaseAll = 0;//案例总数
                Integer sumDayExe = 0;//当日执行总数
                Integer sumDayCreate = 0;//当日编写总数
                Integer issueNum = 0;//缺陷总数
                Integer solveIssue = 0;//已修复缺陷
                Integer unSolveIssue = 0;//未修复缺陷
                Integer effectiveIssue = 0;//有效缺陷
                Integer ineffectiveIssue = 0;//无效缺陷
                Integer developMantis = 0;//涉及开发质量缺陷数
                Integer rqrRuleNum = 0;//需规问题缺陷
                Integer funcLackNum = 0;//功能实现不完整缺陷
                Integer funcErrNum = 0;//功能实现错误缺陷
                Integer rqrNum = 0;//业务需求问题缺陷
                Integer historyNum = 0;//历史遗留问题缺陷
                Integer optimizeNum = 0;//优化建议缺陷
                Integer backNum = 0;//后台问题缺陷
                Integer packageNum = 0;//打包问题缺陷
                Integer dataNum = 0;//数据问题缺陷
                Integer envNum = 0;//环境问题缺陷
                Integer otherNum = 0;//其他原因缺陷

                Integer alreadyOrder = 0;//已完成工单数
                Integer devOrder = 0;//开发中工单数
                Integer createOrder = 0;//新建工单数
                Integer underwayOrder = 0;//进行中工单数
                float underwayOrderAvg = 0;//进行中人均工单数
                float sumOrderAvg = 0;//人均工单数
                float sumDayExeAvg = 0;//当日人均执行案例数
                float sumDayCreateAvg = 0;//当日人均编写案例数
                float issueRate = 0;//缺陷率
                Integer develpoProIssue = 0;//开发相关生产问题
                Integer sitProIssue = 0;//内测相关生产问题
                Integer sumProIssue = 0;//生产问题总数

                Integer dayOrderDone = 0;//当前内测完成工单
                Integer dayOrderSubmit = 0;//当前提交内测工单
                Integer noPlanOrder = 0;//待启动工单
                Integer noTesterOrder = 0;//待分配工单

                if (!CommonUtils.isNullOrEmpty(workNos)) {
                    String workStr = "'"+String.join("','", workNos)+"'";
                    //组装案例数据
                    Map orderInfo = odsMapper.queryOrderCaseInfo(workStr, startDate, endDate);
                    unExeCaseAll += Integer.valueOf(orderInfo.get(Dict.UNEXECASE).toString());
                    sumExeAll += Integer.valueOf(orderInfo.get(Dict.SUMEXE).toString());
                    sumCaseAll += Integer.valueOf(orderInfo.get(Dict.SUMCASE).toString());
                    sumDayExe += Integer.valueOf(orderInfo.get(Dict.DAYEXE).toString());
                    sumDayCreate += Integer.valueOf(orderInfo.get(Dict.DAYCREATE).toString());
                    //组装缺陷数据
                    Map<String, Object> issues = countIssueDetailByOrderNos(workNos, startDate, endDate);
                    issueNum += (Integer)issues.get(Dict.SUMISSUE);
                    solveIssue += (Integer)issues.get(Dict.SOLVEISSUE);
                    unSolveIssue += (Integer)issues.get(Dict.UNSOLVEISSUE);
                    effectiveIssue += (Integer)issues.get(Dict.EFFECTIVEISSUE);
                    ineffectiveIssue += (Integer)issues.get(Dict.INEFFECTIVEISSUE);
                    developMantis += (Integer)issues.get(Dict.DEVISSUE);
                    rqrRuleNum += (Integer)issues.get(Dict.RQRRULENUM);
                    funcLackNum += (Integer)issues.get(Dict.FUNCLACKNUM);
                    funcErrNum += (Integer)issues.get(Dict.FUNCERRNUM);
                    rqrNum += (Integer)issues.get(Dict.RQRNUM);
                    historyNum += (Integer)issues.get(Dict.HISTORYNUM);
                    optimizeNum += (Integer)issues.get(Dict.OPTIMIZENUM);
                    backNum += (Integer)issues.get(Dict.BACKNUM);
                    packageNum += (Integer)issues.get(Dict.PACKAGENUM);
                    dataNum += (Integer)issues.get(Dict.DATANUM);
                    envNum += (Integer)issues.get(Dict.ENVNUM);
                    otherNum += (Integer)issues.get(Dict.OTHERNUM);
                }
                map.put(Dict.SUMCASE, sumCaseAll);
                map.put(Dict.SUMEXE, sumExeAll);
                map.put(Dict.UNEXECASE, unExeCaseAll);
                map.put(Dict.DAYEXE, sumDayExe);
                map.put(Dict.DAYCREATE, sumDayCreate);
                map.put(Dict.ISSUENUM, issueNum);
                map.put(Dict.SOLVEISSUE, solveIssue);
                map.put(Dict.UNSOLVEISSUE, unSolveIssue);
                map.put(Dict.EFFECTIVEISSUE, effectiveIssue);
                map.put(Dict.INEFFECTIVEISSUE, ineffectiveIssue);
                map.put(Dict.DEVELOPMANTIS, developMantis);
                map.put(Dict.DEVELOPMANTISAVG, floatToString(((float)developMantis/sumOrder)));
                map.put(Dict.RQRRULENUM, rqrRuleNum);
                map.put(Dict.FUNCLACKNUM, funcLackNum);
                map.put(Dict.FUNCERRNUM, funcErrNum);
                map.put(Dict.RQRNUM, rqrNum);
                map.put(Dict.HISTORYNUM, historyNum);
                map.put(Dict.OPTIMIZENUM, optimizeNum);
                map.put(Dict.BACKNUM, backNum);
                map.put(Dict.PACKAGENUM, packageNum);
                map.put(Dict.DATANUM, dataNum);
                map.put(Dict.ENVNUM, envNum);
                map.put(Dict.OTHERNUM, otherNum);
                //查询组内人数
                Integer countTester = userService.queryGroupTester(groupId);
                Map<String, Object> groupMap = userService.queryGroupDetailById(groupId);
                if(!Util.isNullOrEmpty(groupMap)){
                    map.put(Dict.GROUPNAME, groupMap.get(Dict.NAME));
                }
                map.put(Dict.COUNTUSER,  userService.queryGroupTester(groupId));
                if (!CommonUtils.isNullOrEmpty(doneDevOrders) && !CommonUtils.isNullOrEmpty(doneDevOrders.get(groupId))) {
                    alreadyOrder = ((Map<String, Integer>)doneDevOrders.get(groupId)).get(Dict.ALREADYORDER);
                    devOrder = ((Map<String, Integer>)doneDevOrders.get(groupId)).get(Dict.DEVORDER);
                    createOrder =  ((Map<String, Integer>)doneDevOrders.get(groupId)).get(Dict.CREATEORDER);
                    underwayOrder = ((Map<String, Integer>)doneDevOrders.get(groupId)).get(Dict.UNDERWAYORDER);
                }
                if (countTester != 0) {
                    underwayOrderAvg = (float) underwayOrder / countTester; //进行中人均任务数
                    sumOrderAvg = (float) sumOrder / countTester; //人均任务数
                    sumDayExeAvg = (float) sumDayExe / countTester;
                    sumDayCreateAvg = (float) sumDayCreate / countTester;
                }
                if(sumDayExe !=0){
                    issueRate = (float) effectiveIssue / sumDayExe;
                }
                map.put(Dict.SUMORDERAVG, floatToString(sumOrderAvg));
                map.put(Dict.UNDERWAYORDERAVG, floatToString(underwayOrderAvg));
                map.put(Dict.ALREADYORDER, alreadyOrder);
                map.put(Dict.DEVORDER, devOrder);
                map.put(Dict.CREATEORDER, createOrder);
                map.put(Dict.UNDERWAYORDER, underwayOrder);
                map.put(Dict.DAYEXEAVG, floatToString(sumDayExeAvg));
                map.put(Dict.DAYCREATEAVG, floatToString(sumDayCreateAvg));
                map.put(Dict.ISSUERATE, floatToString(issueRate));
                map.remove(Dict.WORKNOS);
                //生产缺陷数量处理
                Map sendProIssue = new HashMap();
                sendProIssue.put(Dict.MODULE, map.get(Dict.FDEVGROUPID));
                sendProIssue.put(Dict.START_TIME, startDate);
                sendProIssue.put(Dict.END_TIME, endDate);
                sendProIssue.put(Dict.ISINCLUDECHILDREN, true);
                sendProIssue.put(Dict.CURRENT_PAGE, Constants.DEFAULT_1);
                sendProIssue.put(Dict.PAGE_SIZE, 500);
                sendProIssue.put(Dict.REST_CODE, "mantis.queryProIssues");
                List<Map<String, Object>> proIssueList = new ArrayList<>();
                try {
              //      proIssueList = (List<Map<String, Object>>)restTransport.submit(sendProIssue);
                } catch (Exception e) {
                    logger.error("fail to query proIssue" + e);
                }
                for(Map<String, Object> pro : proIssueList){
                    if(String.valueOf(pro.get(Dict.ISSUE_TYPE)).contains(Constants.DEVPROISSUE)){
                        develpoProIssue++;
                    }
                    if(String.valueOf(pro.get(Dict.ISSUE_TYPE)).contains(Constants.SITPROISSUE)){
                        sitProIssue++;
                    }
                    sumProIssue++;
                }
                map.put(Dict.DEVELPOPROISSUE, develpoProIssue);
                map.put(Dict.SITPROISSUE, sitProIssue);
                map.put(Dict.SUMPROISSUE, sumProIssue);
                //查询原人员报表中组工单信息
                Map sendOrder = new HashMap();
                sendOrder.put(Dict.STARTDATE, startDate);
                sendOrder.put(Dict.ENDDATE, endDate);
                sendOrder.put(Dict.GROUPID, groupId);
                Map<String, Integer> orderResult = queryGroupInfo(sendOrder);
                if(!Util.isNullOrEmpty(orderResult)){
                    dayOrderDone = orderResult.get(Dict.DAYORDERDONE);
                    dayOrderSubmit = orderResult.get(Dict.DAYORDERSUBMIT);
                    noPlanOrder = orderResult.get(Dict.NOPLANORDER);
                    noTesterOrder = orderResult.get(Dict.NOTESTERORDER);
                }
                map.put(Dict.DAYORDERDONE, dayOrderDone);
                map.put(Dict.DAYORDERSUBMIT, dayOrderSubmit);
                map.put(Dict.NOPLANORDER, noPlanOrder);
                map.put(Dict.NOTESTERORDER, noTesterOrder);
            }catch(Exception e){
                lambdaThrowException(e);
            }
        });
        return orderMap;
    }

    /**
     * 组维度报表导出
     * @param map
     * @param resp
     * @throws Exception
     */
    @Override
    public void exportGroupStatement(Map map, HttpServletResponse resp) throws Exception {
        List<String> group = (List<String>)map.get(Dict.GROUP);
        String startDate = String.valueOf(map.get(Dict.STARTDATE));
        String endDate = String.valueOf(map.get(Dict.ENDDATE));
        boolean isParent = (boolean)(map.get(Dict.ISPARENT));
        List<Map<String, Object>> groupInfos = queryDayTotalReport(startDate, endDate, group, isParent);
        XSSFWorkbook workbook;
        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet();
            setCellValue(workbook, 0, 0, 0, "组名");
            setCellValue(workbook, 0, 0, 1, "总人数");
            setCellValue(workbook, 0, 0, 2, "工单总数");
            setCellValue(workbook, 0, 0, 3, "人均工单数");
            setCellValue(workbook, 0, 0, 4, "开发中工单数");
            setCellValue(workbook, 0, 0, 5, "当前完成工单数");
            setCellValue(workbook, 0, 0, 6, "新建工单");
            setCellValue(workbook, 0, 0, 7, "进行中工单数");
            setCellValue(workbook, 0, 0, 8, "人均进行中工单数");
            setCellValue(workbook, 0, 0, 9, "当前日期内测完成工单");
            setCellValue(workbook, 0, 0, 10, "当前日期提交内测工单");
            setCellValue(workbook, 0, 0, 11, "组内待启动工单");
            setCellValue(workbook, 0, 0, 12, "组内待分配工单");
            setCellValue(workbook, 0, 0, 13, "案例总数");
            setCellValue(workbook, 0, 0, 14, "总执行数");
            setCellValue(workbook, 0, 0, 15, "编写案例数");
            setCellValue(workbook, 0, 0, 16, "案例执行数");
            setCellValue(workbook, 0, 0, 17, "人均编写案例数");
            setCellValue(workbook, 0, 0, 18, "人均案例执行数");
            setCellValue(workbook, 0, 0, 19, "待测案例总数");
            setCellValue(workbook, 0, 0, 20, "缺陷总数");
            setCellValue(workbook, 0, 0, 21, "缺陷率");
            setCellValue(workbook, 0, 0, 22, "有效新增缺陷");
            setCellValue(workbook, 0, 0, 23, "无效新增缺陷");
            setCellValue(workbook, 0, 0, 24, "开发相关缺陷");
            setCellValue(workbook, 0, 0, 25, "平均开发缺陷");
            setCellValue(workbook, 0, 0, 26, "已修复缺陷");
            setCellValue(workbook, 0, 0, 27, "未修复缺陷");
            setCellValue(workbook, 0, 0, 28, "业务需求问题");
            setCellValue(workbook, 0, 0, 29, "需规问题");
            setCellValue(workbook, 0, 0, 30, "功能实现不完整");
            setCellValue(workbook, 0, 0, 31, "功能实现错误");
            setCellValue(workbook, 0, 0, 32, "历史遗留问题");
            setCellValue(workbook, 0, 0, 33, "优化建议");
            setCellValue(workbook, 0, 0, 34, "后台问题");
            setCellValue(workbook, 0, 0, 35, "打包问题");
            setCellValue(workbook, 0, 0, 36, "数据问题");
            setCellValue(workbook, 0, 0, 37, "环境问题");
            setCellValue(workbook, 0, 0, 38, "其他原因");
            setCellValue(workbook, 0, 0, 39, "生产问题总数");
            setCellValue(workbook, 0, 0, 40, "开发相关生产问题");
            setCellValue(workbook, 0, 0, 41, "内测相关生产问题");
            int i = 1;
            for (Map<String, Object> groupInfo : groupInfos) {
                String groupName = "";
                String group_id = String.valueOf(groupInfo.get(Dict.GROUPID));
                Map<String, Object> groupMap = userService.queryGroupDetailById(group_id);
                if(!Util.isNullOrEmpty(groupMap)){
                    groupName = (String)groupMap.get(Dict.NAME);
                }
                setCellValue(workbook, 0, i, 0, groupName);
                setCellValue(workbook, 0, i, 1, String.valueOf(groupInfo.get(Dict.COUNTUSER)));
                setCellValue(workbook, 0, i, 2, String.valueOf(groupInfo.get(Dict.SUMORDER)));
                setCellValue(workbook, 0, i, 3, String.valueOf(groupInfo.get(Dict.SUMORDERAVG)));
                setCellValue(workbook, 0, i, 4, String.valueOf(groupInfo.get(Dict.DEVORDER)));
                setCellValue(workbook, 0, i, 5, String.valueOf(groupInfo.get(Dict.ALREADYORDER)));
                setCellValue(workbook, 0, i, 6, String.valueOf(groupInfo.get(Dict.CREATEORDER)));
                setCellValue(workbook, 0, i, 7, String.valueOf(groupInfo.get(Dict.UNDERWAYORDER)));
                setCellValue(workbook, 0, i, 8, String.valueOf(groupInfo.get(Dict.UNDERWAYORDERAVG)));
                setCellValue(workbook, 0, i, 9, String.valueOf(groupInfo.get(Dict.DAYORDERDONE)));
                setCellValue(workbook, 0, i, 10, String.valueOf(groupInfo.get(Dict.DAYORDERSUBMIT)));
                setCellValue(workbook, 0, i, 11, String.valueOf(groupInfo.get(Dict.NOPLANORDER)));
                setCellValue(workbook, 0, i, 12, String.valueOf(groupInfo.get(Dict.NOTESTERORDER)));
                setCellValue(workbook, 0, i, 13, String.valueOf(groupInfo.get(Dict.SUMCASE)));
                setCellValue(workbook, 0, i, 14, String.valueOf(groupInfo.get(Dict.SUMEXE)));
                setCellValue(workbook, 0, i, 15, String.valueOf(groupInfo.get(Dict.DAYCREATE)));
                setCellValue(workbook, 0, i, 16, String.valueOf(groupInfo.get(Dict.DAYEXE)));
                setCellValue(workbook, 0, i, 17, String.valueOf(groupInfo.get(Dict.DAYCREATEAVG)));
                setCellValue(workbook, 0, i, 18, String.valueOf(groupInfo.get(Dict.DAYEXEAVG)));
                setCellValue(workbook, 0, i, 19, String.valueOf(groupInfo.get(Dict.UNEXECASE)));
                setCellValue(workbook, 0, i, 20, String.valueOf(groupInfo.get(Dict.ISSUENUM)));
                setCellValue(workbook, 0, i, 21, String.valueOf(groupInfo.get(Dict.ISSUERATE)));
                setCellValue(workbook, 0, i, 22, String.valueOf(groupInfo.get(Dict.EFFECTIVEISSUE)));
                setCellValue(workbook, 0, i, 23, String.valueOf(groupInfo.get(Dict.INEFFECTIVEISSUE)));
                setCellValue(workbook, 0, i, 24, String.valueOf(groupInfo.get(Dict.DEVELOPMANTIS)));
                setCellValue(workbook, 0, i, 25, String.valueOf(groupInfo.get(Dict.DEVELOPMANTISAVG)));
                setCellValue(workbook, 0, i, 26, String.valueOf(groupInfo.get(Dict.SOLVEISSUE)));
                setCellValue(workbook, 0, i, 27, String.valueOf(groupInfo.get(Dict.UNSOLVEISSUE)));
                setCellValue(workbook, 0, i, 28, String.valueOf(groupInfo.get(Dict.RQRNUM)));
                setCellValue(workbook, 0, i, 29, String.valueOf(groupInfo.get(Dict.RQRRULENUM)));
                setCellValue(workbook, 0, i, 30, String.valueOf(groupInfo.get(Dict.FUNCLACKNUM)));
                setCellValue(workbook, 0, i, 31, String.valueOf(groupInfo.get(Dict.FUNCERRNUM)));
                setCellValue(workbook, 0, i, 32, String.valueOf(groupInfo.get(Dict.HISTORYNUM)));
                setCellValue(workbook, 0, i, 33, String.valueOf(groupInfo.get(Dict.OPTIMIZENUM)));
                setCellValue(workbook, 0, i, 34, String.valueOf(groupInfo.get(Dict.BACKNUM)));
                setCellValue(workbook, 0, i, 35, String.valueOf(groupInfo.get(Dict.PACKAGENUM)));
                setCellValue(workbook, 0, i, 36, String.valueOf(groupInfo.get(Dict.DATANUM)));
                setCellValue(workbook, 0, i, 37, String.valueOf(groupInfo.get(Dict.ENVNUM)));
                setCellValue(workbook, 0, i, 38, String.valueOf(groupInfo.get(Dict.OTHERNUM)));
                setCellValue(workbook, 0, i, 39, String.valueOf(groupInfo.get(Dict.SUMPROISSUE)));
                setCellValue(workbook, 0, i, 40, String.valueOf(groupInfo.get(Dict.DEVELPOPROISSUE)));
                setCellValue(workbook, 0, i, 41, String.valueOf(groupInfo.get(Dict.SITPROISSUE)));
                i++;
            }
        } catch (Exception e) {
            logger.error("e" + e);
            throw new FtmsException(ErrorConstants.EXPORT_EXCEL_ERROR);
        }
        resp.reset();
        resp.setContentType("application/octet-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Content-Disposition", "attachment;filename=" + "groupReport.xlsx");
        workbook.write(resp.getOutputStream());
    }

    static <E extends Exception> void lambdaThrowException(Exception e)throws E { throw (E)e; }

    private String floatToString(float num) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(num);
    }

    private String floatToString1(float num) {
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        return decimalFormat.format(num);
    }

    @Override
    public List<Map<String, String>> exportReport(String startDate, String endDate) throws Exception {
        List<Map<String, String>> orderData = odsMapper.exportReport(startDate, endDate);
        for (Map<String, String> map : orderData) {
            String workNo = map.get(Dict.WORKNO);
            HttpHeaders header = new HttpHeaders();
            header.add("Content-Type", "application/json");
            Map sendMap = new HashMap<>();
            sendMap.put(Dict.WORKNO, workNo);
            sendMap.put(Dict.REST_CODE, "mantis.queryOrderUnderwayIssues");
            Map<String, Integer> issues = (Map<String, Integer>) restTransport.submitWithHeaders(sendMap, header);
            Integer issueNum = 0;
            if (!CommonUtils.isNullOrEmpty(issues) && !CommonUtils.isNullOrEmpty(issues.get(Dict.ISSUENUM))) {
                issueNum = issues.get(Dict.ISSUENUM);
            }
            String workLeader = map.get(Dict.WORKLEADER);
            map.put(Dict.WORKLEADER, changeToNameCn(workLeader));
            map.put(Dict.ISSUENUM, String.valueOf(issueNum));
        }
        return orderData;
    }

    private String changeToNameCn(String nameEns) throws Exception {
        if (!CommonUtils.isNullOrEmpty(nameEns)) {
            String[] split = nameEns.split(",");
            String nameCns = "";
            for (String nameEn : split) {
                Map<String, Object> user = userService.queryUserCoreDataByNameEn(nameEn);
                if(!Util.isNullOrEmpty(user)){
                    String nameCn = (String)user.get(Dict.USER_NAME_CN);
                    if (nameCns != "") {
                        nameCns += "," + nameCn;
                    } else {
                        nameCns += nameCn;
                    }
                }
            }
            return nameCns;
        }
        return "";
    }

    /**
     * 装填单条工单数据
     *
     * @param newOrderDimension
     * @param groupName
     * @return
     * @throws Exception
     */
    private void setNewOrder(Map newOrderDimension, String groupName, List<Map<String, String>> resultList) throws Exception {
        Map<String, String> newListElem = new HashMap<>();
        //如果页面有组名筛选条件,忽略组名不一致的工单数据
        if (!MyUtil.isNullOrEmpty(groupName)) {
            if (newOrderDimension.get(Dict.GROUPNAME) != null) {
                String groupNamequery = newOrderDimension.get(Dict.GROUPNAME).toString();
                if (!groupName.equals(groupNamequery)) {
                    return;
                } else {
                    newListElem.put(Dict.GROUPNAME, groupNamequery);
                }
            } else {
                return;
            }
        } else {
            if (!MyUtil.isNullOrEmpty(newOrderDimension.get(Dict.GROUPNAME))) {
                newListElem.put(Dict.GROUPNAME, newOrderDimension.get(Dict.GROUPNAME).toString());
            } else {
                newListElem.put(Dict.GROUPNAME, "未分组");
            }
        }
        //工单编号
        if (!MyUtil.isNullOrEmpty(newOrderDimension.get(Dict.WORKNO))) {
            newListElem.put(Dict.WORKNO, (String) newOrderDimension.get(Dict.WORKNO));
        } else {
            newListElem.put(Dict.WORKNO, null);
        }
        //工单名称
        if (!MyUtil.isNullOrEmpty(newOrderDimension.get(Dict.MAINTASKNAME))) {
            newListElem.put(Dict.MAINTASKNAME, (String) newOrderDimension.get(Dict.MAINTASKNAME));
        } else {
            newListElem.put(Dict.MAINTASKNAME, null);
        }
        //案例总数
        if (!MyUtil.isNullOrEmpty(newOrderDimension.get(Dict.SUMCASE))) {
            newListElem.put(Dict.SUMCASE, newOrderDimension.get(Dict.SUMCASE).toString());
        } else {
            newListElem.put(Dict.SUMCASE, null);
        }
        //测试人员
        if (!MyUtil.isNullOrEmpty(newOrderDimension.get(Dict.TESTERS))) {
            newListElem.put(Dict.TESTERS, getTesters(((String) newOrderDimension.get(Dict.TESTERS)).split(",")));
        } else {
            newListElem.put(Dict.TESTERS, null);
        }
        //进UAT时间
        if (!MyUtil.isNullOrEmpty(newOrderDimension.get(Dict.UAT_SUBMIT_DATE))) {
            newListElem.put(Dict.UAT_SUBMIT_DATE, newOrderDimension.get(Dict.UAT_SUBMIT_DATE).toString());
        } else {
            newListElem.put(Dict.UAT_SUBMIT_DATE, null);
        }
        //fdev任务创建人
        if (!MyUtil.isNullOrEmpty(newOrderDimension.get(Dict.MAINTASKNO))) {
            newListElem.put(Dict.CREATOR, getTaskCreator((String) newOrderDimension.get(Dict.MAINTASKNO)));
        } else {
            newListElem.put(Dict.CREATOR, null);
        }
        //案例执行数
        if (!MyUtil.isNullOrEmpty(newOrderDimension.get(Dict.SUMEXE))) {
            newListElem.put(Dict.SUMEXE, newOrderDimension.get(Dict.SUMEXE).toString());
        } else {
            newListElem.put(Dict.SUMEXE, "0");
        }
        //缺陷
        if (!MyUtil.isNullOrEmpty(newOrderDimension.get(Dict.WORKNO))) {
            //缺陷总数
            List<Map<String, String>> mantisCountlist = getmantisCountByNo((String) newOrderDimension.get(Dict.WORKNO));
            Integer sum = 0;
            Integer sumUnhandled = 0;
            Integer sumClosed = 0;
            Integer sumRejected = 0;
            for (Map m : mantisCountlist) {
                sum += (Integer) m.get(Dict.COUNT);
                //【有效缺陷总数】：缺陷状态为【关闭】的缺陷总数
                if ("90".equals(m.get(Dict.STATUS).toString())) {
                    sumClosed += (Integer) m.get(Dict.COUNT);
                }
                //【无效缺陷总数】：缺陷状态为【确认拒绝】的缺陷总数
                if ("30".equals(m.get(Dict.STATUS).toString())) {
                    sumRejected += (Integer) m.get(Dict.COUNT);
                }
                //【未处理缺陷总数】：缺陷状态为【新建】【打开】【已修复】【拒绝】的缺陷总数
                if ("10".equals(m.get(Dict.STATUS).toString()) || "20".equals(m.get(Dict.STATUS).toString())
                        || "50".equals(m.get(Dict.STATUS).toString()) || "80".equals(m.get(Dict.STATUS).toString())) {
                    sumUnhandled += (Integer) m.get(Dict.COUNT);
                }
            }
            newListElem.put(Dict.SUMMANTIS, sum == 0 ? "0" : sum.toString());
            newListElem.put(Dict.UNHANDLEDMANTIS, sumUnhandled == 0 ? "0" : sumUnhandled.toString());
            newListElem.put(Dict.CLOSEDMANTIS, sumClosed == 0 ? "0" : sumClosed.toString());
            newListElem.put(Dict.REJECTEDMANTIS, sumRejected == 0 ? "0" : sumRejected.toString());
        } else {
            newListElem.put(Dict.SUMMANTIS, null);
            newListElem.put(Dict.CLOSEDMANTIS, null);
            newListElem.put(Dict.REJECTEDMANTIS, null);
            newListElem.put(Dict.UNHANDLEDMANTIS, null);
        }
        resultList.add(newListElem);
    }

    /**
     * 根据工单测试人英文获取测试人员中文
     *
     * @param names
     * @return
     * @throws Exception
     */
    private String getTesters(String[] names) throws Exception {
        StringBuilder testers = new StringBuilder();
        for (String s : names) {
            try {
                Map user = userService.queryUserCoreDataByNameEn(s);
                if (!MyUtil.isNullOrEmpty(user)) {
                    String name = (String)user.get(Dict.USER_NAME_CN);
                    testers.append(name + " ");
                }
            } catch (Exception e) {
                throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"根据用户邮箱英文查中文失败"});
            }
        }
        return testers.toString();
    }

    /**
     * 根据主任务名获取fdev任务创建人
     *
     * @param mainTaskNo
     * @return
     * @throws Exception
     */
    private String getTaskCreator(String mainTaskNo) {
        Map mm = new HashMap<String, String>();
        mm.put(Dict.ID, mainTaskNo);
        mm.put(Dict.REST_CODE, "queryTaskDetail");
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap = (Map) restTransport.submitSourceBack(mm);
        } catch (Exception e) {
            logger.error("查询fdev任务失败");
            return null;
        }
        return (String) ((Map) resultMap.get(Dict.CREATOR)).get("user_name_cn");
    }

    /**
     * 根据工单编号查询其下各缺陷状态的缺陷数量
     *
     * @param workNo
     * @return
     * @throws Exception
     */
    private List<Map<String, String>> getmantisCountByNo(String workNo) throws Exception {
        Map<String, String> sendMap = new HashMap<>();
        sendMap.put(Dict.WORKNO, workNo);
        sendMap.put(Dict.REST_CODE, "mantis.countMantisByWorkNo");
        List<Map<String, String>> map = new ArrayList<>();
        try {
            map = (List<Map<String, String>>) restTransport.submit(sendMap);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询缺陷异常！"});
        }
        return map;
    }

    /**
     * excel填值
     *
     * @param workbook   excel对象
     * @param sheetIndex
     * @param rowIndex
     * @param cellIndex
     * @param cellValue
     * @throws Exception
     */
    private void setCellValue(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex, String cellValue)
            throws Exception {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        if (sheet == null) {
            sheet = workbook.createSheet(String.valueOf(sheetIndex));
        }
        if (sheet.getRow(rowIndex) == null) {
            sheet.createRow(rowIndex);
        }
        if (sheet.getRow(rowIndex).getCell(cellIndex) == null) {
            sheet.getRow(rowIndex).createCell(cellIndex);
        }
        sheet.getRow(rowIndex).getCell(cellIndex).setCellValue(cellValue);
    }

    /**
     * 导出日报报表组表excel
     *
     * @param requestMap
     * @param resp
     * @throws Exception
     */
    @Override
    public void exportDayGroupReport(Map<String, String> requestMap, HttpServletResponse resp) throws Exception {
        String startDate = requestMap.get(Dict.STARTDATE);
        String endDate = requestMap.get(Dict.ENDDATE);
        String groupId = requestMap.get(Dict.GROUPID);
        List<Map<String, Object>> orderData = queryDayGroupReport(startDate, endDate, groupId);
        XSSFWorkbook workbook;
        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet();
            setCellValue(workbook, 0, 0, 0, "组名称");
            setCellValue(workbook, 0, 0, 1, "主任务名称");
            setCellValue(workbook, 0, 0, 2, "工单状态");
            setCellValue(workbook, 0, 0, 3, "测试人员");
            setCellValue(workbook, 0, 0, 4, "开发人员");
            setCellValue(workbook, 0, 0, 5, "案例总数");
            setCellValue(workbook, 0, 0, 6, "当日编写案例数");
            setCellValue(workbook, 0, 0, 7, "执行案例总数");
            setCellValue(workbook, 0, 0, 8, "当日执行案例数");
            setCellValue(workbook, 0, 0, 9, "缺陷总数");
            setCellValue(workbook, 0, 0, 10, "已修复缺陷");
            setCellValue(workbook, 0, 0, 11, "未修复缺陷");
            int i = 1;
            for (Map<String, Object> order : orderData) {
                String group_id = (String)order.get(Dict.GROUP_ID);
                Map<String, Object> group = userService.queryGroupDetailById(group_id);
                String groupName = "";
                if (!Util.isNullOrEmpty(group)){
                    groupName = (String)group.get(Dict.NAME);
                }
                setCellValue(workbook, 0, i, 0, groupName);
                setCellValue(workbook, 0, i, 1, String.valueOf(order.getOrDefault(Dict.MAINTASKNAME,"")));
                setCellValue(workbook, 0, i, 2, String.valueOf(order.getOrDefault(Dict.WORKSTAGE,"")));
                setCellValue(workbook, 0, i, 3, String.valueOf(order.getOrDefault(Dict.TESTERS,"")));
                setCellValue(workbook, 0, i, 4, String.valueOf(order.getOrDefault(Dict.DEVELOPER,"")));
                setCellValue(workbook, 0, i, 5, String.valueOf(order.getOrDefault(Dict.SUMCASE,Constants.DEFAULT_0)));
                setCellValue(workbook, 0, i, 6, String.valueOf(order.getOrDefault(Dict.DAYCASE,Constants.DEFAULT_0)));
                setCellValue(workbook, 0, i, 7, Util.isNullOrEmpty(order.get(Dict.SUMEXE))?String.valueOf(Constants.DEFAULT_0):String.valueOf(order.get(Dict.SUMEXE)));
                setCellValue(workbook, 0, i, 8, String.valueOf(order.getOrDefault(Dict.DAYEXE,Constants.DEFAULT_0)));
                setCellValue(workbook, 0, i, 9, String.valueOf(order.getOrDefault(Dict.ISSUENUM,Constants.DEFAULT_0)));
                setCellValue(workbook, 0, i, 10, String.valueOf(order.getOrDefault(Dict.SOLVEISSUE,Constants.DEFAULT_0)));
                setCellValue(workbook, 0, i, 11, String.valueOf(order.getOrDefault(Dict.UNSOLVEISSUE,Constants.DEFAULT_0)));
                i++;
            }
        } catch (Exception e) {
            logger.error("e" + e);
            throw new FtmsException(ErrorConstants.EXPORT_EXCEL_ERROR);
        }
        resp.reset();
        resp.setContentType("application/octet-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Content-Disposition", "attachment;filename=" + "GroupDayReport.xlsx");
        workbook.write(resp.getOutputStream());
    }

    /**
     * 根据用户和期限查询工单信息
     * @param map
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, String>> queryOrderInfoByUser(Map map)throws Exception {
        List<Map<String, String>> orderInfos = new ArrayList<>();
        //获取数据
        String startDate = String.valueOf(map.get(Dict.STARTDATE));
        String endDate = String.valueOf(map.get(Dict.ENDDATE));
        String userEnName = String.valueOf(map.get(Dict.USER_EN_NAME));
        //查询选定时间段内特定测试人员的工单号（执行案例或新建案例）
        List<String> workNos = odsMapper.queryOrderNoByUser(startDate, endDate, userEnName);
        //查询缺陷总数，未修复缺陷数，当前该人新增缺陷数
        Map sendMantis = new HashMap();
        sendMantis.put(Dict.USER_NAME_EN, userEnName);
        sendMantis.put(Dict.WORKNOS, workNos);
        sendMantis.put(Dict.STARTDATE, startDate);
        sendMantis.put(Dict.ENDDATE, endDate);
        sendMantis.put(Dict.REST_CODE, "mantis.queryIssueByTimeUserNew");
        List<Map<String, String>> issueInfoList = null;
        try {
            issueInfoList = (List<Map<String, String>>)restTransport.submit(sendMantis);
        } catch (Exception e) {
            logger.error("e:"+e);
            logger.error("查询mantis失败");
        }
        Map<String, Map> issueMap = new HashMap<>();
        if (!CommonUtils.isNullOrEmpty(issueInfoList)) {
            for (Map<String, String> issueInfo : issueInfoList) {
                issueMap.put(issueInfo.get(Dict.WORKNO), issueInfo);
            }
        }
        if(!Util.isNullOrEmpty(workNos)){
            for (String workNo : workNos) {
                //查询该工单信息
                Map<String, String> orderInfo = odsMapper.queryOrderInfoByNo(workNo, startDate, endDate, userEnName);
                String unSolveIssue = "0";
                String dayIssueNum = "0";
                Map<String, String> issueInfo = issueMap.get(workNo);
                if(!Util.isNullOrEmpty(issueInfo)){
                    unSolveIssue = String.valueOf(issueInfo.getOrDefault(Dict.UNSOLVEISSUE, "0"));
                    dayIssueNum = String.valueOf(issueInfo.getOrDefault(Dict.DAYISSUENUM, "0"));
                }
                orderInfo.put(Dict.UNSOLVEISSUE, unSolveIssue);
                orderInfo.put(Dict.DAYISSUENUM, dayIssueNum);
                //查询工单下非开发阶段的任务数量
                List<String> taskNos = taskListMapper.queryTaskNoByOrder(workNo);
                if (CommonUtils.isNullOrEmpty(taskNos)) {
                    orderInfo.put(Dict.TASKCOUNT, "0");
                } else {
                    List<Map> taskList = iTaskApi.queryTaskBaseInfoByIds(taskNos, null, Arrays.asList(Dict.STAGE));
                    if (CommonUtils.isNullOrEmpty(taskList)) {
                        orderInfo.put(Dict.TASKCOUNT, "0");
                    } else {
                        long taskCount = taskList.stream().filter(task -> !"develop".equals((String)task.get(Dict.STAGE))).count();
                        orderInfo.put(Dict.TASKCOUNT, String.valueOf(taskCount));
                    }
                }
                orderInfos.add(orderInfo);
            }
        }
        return orderInfos;
    }

    /**
     * 根据queryTaskDetail返回数据和角色返回人名字符串
     * @param map
     * @param role
     * @return
     * @throws Exception
     */
    private String getNameByRole(Map map, String role) throws Exception {
        StringBuilder name = new StringBuilder(" ");
        List<Map<String, String>> people = (List<Map<String, String>>)map.get(role);
        if(!Util.isNullOrEmpty(people)){
            for(Map<String, String> person : people){
                name.append(person.get(Dict.USERNAMECN)+" ");
            }
        }
        return name.toString().trim();
    }

    /**
     * 根据组id，选定日期范围查询组内工单情况
     * @param map
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Integer> queryGroupInfo(Map map) throws Exception {
        Map<String, Integer> groupInfo = new HashMap<>();
        String groupId = String.valueOf(map.get(Dict.GROUPID));
        String startDate = String.valueOf(map.get(Dict.STARTDATE));
        String endDate = String.valueOf(map.get(Dict.ENDDATE));
        Integer dayOrderDone = 0;//当前内测完成工单
        Integer dayOrderSubmit = 0;//当前提交内测工单
        Integer noPlanOrder = 0;//待启动工单
        Integer noTesterOrder = 0;//待分配工单
        dayOrderDone = odsMapper.queryDayOrderDone(groupId, startDate, endDate);
        dayOrderSubmit = odsMapper.queryDayOrderSubmit(groupId, startDate, endDate);
        Map<String, Object> undoOrder = odsMapper.queryUndoOrder(groupId);
        if(!Util.isNullOrEmpty(undoOrder)){
            noPlanOrder = Integer.valueOf(undoOrder.get(Dict.NOPLANORDER).toString());
            noTesterOrder = Integer.valueOf(undoOrder.get(Dict.NOTESTERORDER).toString());
        }
        groupInfo.put(Dict.NOPLANORDER, noPlanOrder);
        groupInfo.put(Dict.NOTESTERORDER, noTesterOrder);
        groupInfo.put(Dict.DAYORDERSUBMIT, dayOrderSubmit);
        groupInfo.put(Dict.DAYORDERDONE, dayOrderDone);
        return groupInfo;
    }

    /**
     * 导出个人维度报表
     * @param requestMap
     * @param resp
     * @throws Exception
     */
    @Override
    public void exportPersonalDimensionReport(Map<String,Object> requestMap, HttpServletResponse resp) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        //获取参数
        String startDate = String.valueOf(requestMap.get(Dict.STARTDATE));
        String endDate = String.valueOf(requestMap.get(Dict.ENDDATE));
        String groupId = String.valueOf(requestMap.getOrDefault(Dict.GROUPID, ""));
        String userNameEn = String.valueOf(requestMap.getOrDefault(Dict.USER_EN_NAME, ""));
        boolean isParent = (boolean)requestMap.get(Dict.ISPARENT);
        //根据组id和用户英文查询用户信息
        Map send = new HashMap();
        send.put(Dict.STARTDATE, startDate);
        send.put(Dict.ENDDATE, endDate);
        send.put(Dict.GROUPID, groupId);
        send.put(Dict.USERNAME, userNameEn);
        send.put(Dict.ISPARENT, isParent);
        send.put(Dict.REST_CODE, "tuser.countUserTestCaseByTime");
        List<Map<String, String>> users;
        try {
            users = (List<Map<String, String>>)restTransport.submit(send);
        } catch (Exception e) {
            logger.error("fail to fetch user information from tuser" + e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询tuser用户信息失败"});
        }
        //如果查不到用户数据，空excel
        if(!Util.isNullOrEmpty(users)){
            try {
                //根据组名创建sheet
                Sheet sheet = workbook.createSheet(users.get(0).get(Dict.GROUP_NAME));
                //根据组信息写表头
                writeGroupHead(workbook, sheet);
                //遍历用户数据写入
                int row = 1;
                for(Map<String, String> map : users){
                    String userNameCn = map.get(Dict.USER_NAME);
                    Map sendOrder = new HashMap();
                    sendOrder.put(Dict.STARTDATE, startDate);
                    sendOrder.put(Dict.ENDDATE, endDate);
                    sendOrder.put(Dict.USER_EN_NAME, map.get(Dict.USER_NAME_EN));
                    //获取单个用户的工单数据
                    List<Map<String, String>> orderInfos = queryOrderInfoByUser(sendOrder);
                    for(Map<String, String> orderInfo : orderInfos){
                        writeOrderInfo(workbook, sheet, row, orderInfo, userNameCn);
                        row++;
                    }
                }
            } catch (Exception e) {
                logger.error("fail to write excel" + e);
                throw new FtmsException(ErrorConstants.EXPORT_EXCEL_ERROR);
            }
        }else{
            //如果查询不到users信息，给空表
            Sheet sheet = workbook.createSheet();
            setCellValue(workbook, 0, 0, 0, "该查询条件下没有数据");
        }
        resp.reset();
        resp.setContentType("application/octet-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Content-Disposition", "attachment;filename=" + "PersonalDimensionReport.xlsx");
        workbook.write(resp.getOutputStream());
    }

    /**
     * 获取组信息
     * @param startDate
     * @param endDate
     * @param groupId
     * @return
     * @throws Exception
     */
    private Map<String, Integer> getGroupInfo(String startDate, String endDate, String groupId) throws Exception {
        Map sendGroupId = new HashMap();
        sendGroupId.put(Dict.STARTDATE, startDate);
        sendGroupId.put(Dict.ENDDATE, endDate);
        sendGroupId.put(Dict.GROUPID, groupId);
        Map<String, Integer> groupInfo = queryGroupInfo(sendGroupId);
        return groupInfo;
    }

    /**
     * 写表头组信息
     * @param workbook
     * @param sheet
     * @throws Exception
     */
    private void writeGroupHead(XSSFWorkbook workbook, Sheet sheet) throws Exception {
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), 0, 0, "任务名");
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), 0, 1, "测试人员");
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), 0, 2, "当期案例编写数");
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), 0, 3, "当期案例执行数");
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), 0, 4, "当期删除案例数");
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), 0, 5, "当期案例修改数");
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), 0, 6, "当期执行阻塞数");
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), 0, 7, "当期执行失败数");
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), 0, 8, "当期新增缺陷数（有效）");
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), 0, 9, "当期新增未修复缺陷数（有效）");
    }

    /**
     * 写入工单信息
     * @param workbook
     * @param sheet
     * @param map
     * @throws Exception
     */
    private void writeOrderInfo(XSSFWorkbook workbook, Sheet sheet, int row, Map<String, String> map, String userNameCn) throws Exception {
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), row, 0, String.valueOf(map.get(Dict.MAINTASKNAME)));
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), row, 1, userNameCn);
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), row, 2, String.valueOf(map.get(Dict.DAYCREATE)));
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), row, 3, String.valueOf(map.get(Dict.DAYEXE)));
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), row, 4, String.valueOf(map.get(Dict.DAYDELETE)));
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), row, 5, String.valueOf(map.get(Dict.DAYUPDATE)));
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), row, 6, String.valueOf(map.get(Dict.DAYBLOCK)));
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), row, 7, String.valueOf(map.get(Dict.DAYFAIL)));
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), row, 8, String.valueOf(map.get(Dict.DAYISSUENUM)));
        setCellValue(workbook, workbook.getSheetIndex(sheet.getSheetName()), row, 9, String.valueOf(map.get(Dict.UNSOLVEISSUE)));
    }

    /**
     * 导出工单维度报表
     * @param map
     * @param resp
     * @throws Exception
     */
    public void exportOrderDimension(Map map, HttpServletResponse resp) throws Exception {
        List<OrderDimension> orderList = queryOrderDimension(map);
        //查询所有工单的任务
        List<String> workNoList = orderList.stream().map(OrderDimension::getWorkNo).distinct().collect(Collectors.toList());
        workNoList.remove(null);
        List<Map> testTaskList = taskListMapper.queryTaskNoByOrders(workNoList);
        List<String> taskIds = testTaskList.stream().map(task -> (String)task.get(Dict.TASKNO)).distinct().collect(Collectors.toList());
        taskIds.remove(null);
        List<Map> fdevTaskList = new ArrayList<>();
        try {
            fdevTaskList = iTaskApi.queryTaskBaseInfoByIds(taskIds, Arrays.asList(Dict.MASTER,Dict.SPDBMASTER,Dict.DEVELOPER,Dict.GROUP),
                    Arrays.asList(Dict.ID,Dict.NAME,Dict.STAGE,Dict.MASTER,Dict.SPDBMASTER,Dict.DEVELOPER,Dict.GROUP));
        } catch (Exception e) {
            logger.error("fail to get task info");
        }
        //将任务编号和任务信息对应起来
        Map<String, Map> taskMap = new HashMap<>();
        for (Map task : fdevTaskList) {
            taskMap.put((String) task.get(Dict.ID), task);
        }
        //将工单号和任务集合对应起来
        Map<String, List> orderMap = new HashMap<>();
        for (String workNo : workNoList) {
            List<Map> orderTaskList = new ArrayList<>();
            for (Map taskInfo : testTaskList) {
                if (workNo.equals(taskInfo.get(Dict.WORKNO))) {
                    orderTaskList.add(taskMap.get(taskInfo.get(Dict.TASKNO)));
                }
            }
            orderMap.put(workNo, orderTaskList);
        }
        XSSFWorkbook workbook;
        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet();
            setCellValue(workbook, 0, 0, 0, "工单编号");
            setCellValue(workbook, 0, 0, 1, "主任务名称");
            setCellValue(workbook, 0, 0, 2, "小组名称");
            setCellValue(workbook, 0, 0, 3, "工单状态");
            setCellValue(workbook, 0, 0, 4, "测试人员");
            setCellValue(workbook, 0, 0, 5, "测试进度");
            setCellValue(workbook, 0, 0, 6, "案例总数");
            setCellValue(workbook, 0, 0, 7, "执行案例总数");
            setCellValue(workbook, 0, 0, 8, "未执行案例总数");
            setCellValue(workbook, 0, 0, 9, "案例通过数");
            setCellValue(workbook, 0, 0, 10, "案例失败数");
            setCellValue(workbook, 0, 0, 11, "案例阻塞数");
            setCellValue(workbook, 0, 0, 12, "有效缺陷总数");
            setCellValue(workbook, 0, 0, 13, "业务需求问题");
            setCellValue(workbook, 0, 0, 14, "需规问题");
            setCellValue(workbook, 0, 0, 15, "功能实现不完整");
            setCellValue(workbook, 0, 0, 16, "功能实现错误");
            setCellValue(workbook, 0, 0, 17, "历史遗留问题");
            setCellValue(workbook, 0, 0, 18, "优化建议");
            setCellValue(workbook, 0, 0, 19, "后台问题");
            setCellValue(workbook, 0, 0, 20, "打包问题");
            setCellValue(workbook, 0, 0, 21, "缺陷开发责任人");
            setCellValue(workbook, 0, 0, 22, "工单类型");
            //第2个sheet页展示工单下的任务列表信息
            workbook.createSheet();
            setCellValue(workbook, 1, 0, 0, "工单编号");
            setCellValue(workbook, 1, 0, 1, "任务名称");
            setCellValue(workbook, 1, 0, 2, "行内负责人");
            setCellValue(workbook, 1, 0, 3, "负责人");
            setCellValue(workbook, 1, 0, 4, "开发人员");
            setCellValue(workbook, 1, 0, 5, "所属组");
            setCellValue(workbook, 1, 0, 6, "任务阶段");
            int i = 1;
            int j = 1;
            for (OrderDimension order : orderList) {
                setCellValue(workbook, 0, i, 0, String.valueOf(order.getWorkNo()));
                setCellValue(workbook, 0, i, 1, String.valueOf(order.getMainTaskName()));
                setCellValue(workbook, 0, i, 2, String.valueOf(order.getGroupName()));
                setCellValue(workbook, 0, i, 3, getStageCn(order.getWorkStage()));
                if(!CommonUtils.isNullOrEmpty(order.getTesters())) {
                    setCellValue(workbook, 0, i, 4, order.getTesters());
                }
                setCellValue(workbook, 0, i, 5, order.getPercentage() + "%");
                setCellValue(workbook, 0, i, 6, String.valueOf(order.getCaseCount()));
                setCellValue(workbook, 0, i, 7, String.valueOf(order.getCaseExecute()));
                setCellValue(workbook, 0, i, 8, String.valueOf(order.getCaseNoExecute()));
                setCellValue(workbook, 0, i, 9, String.valueOf(order.getCasePass()));
                setCellValue(workbook, 0, i, 10, String.valueOf(order.getCaseFailure()));
                setCellValue(workbook, 0, i, 11, String.valueOf(order.getCaseBlock()));
                setCellValue(workbook, 0, i, 12, String.valueOf(order.getCaseMantis()));
                setCellValue(workbook, 0, i, 13, String.valueOf(order.getRqrNum()));
                setCellValue(workbook, 0, i, 14, String.valueOf(order.getRqrRuleNum()));
                setCellValue(workbook, 0, i, 15, String.valueOf(order.getFuncLackNum()));
                setCellValue(workbook, 0, i, 16, String.valueOf(order.getFuncErrNum()));
                setCellValue(workbook, 0, i, 17, String.valueOf(order.getHistoryNum()));
                setCellValue(workbook, 0, i, 18, String.valueOf(order.getOptimizeNum()));
                setCellValue(workbook, 0, i, 19, String.valueOf(order.getBackNum()));
                setCellValue(workbook, 0, i, 20, String.valueOf(order.getPackageNum()));
                if(!CommonUtils.isNullOrEmpty(order.getDeveloper())) {
                    setCellValue(workbook, 0, i, 21, order.getDeveloper());
                }
                setCellValue(workbook, 0, i, 22, OrderUtil.getOrderTypeCH(order.getOrderType()));
                if (!CommonUtils.isNullOrEmpty(taskIds)) {
                    List<Map> taskInfos = orderMap.get(order.getWorkNo());
                    taskInfos.remove(null);
                    for (Map taskInfo : taskInfos) {
                        setCellValue(workbook, 1, j, 0, String.valueOf(order.getWorkNo()));
                        setCellValue(workbook, 1, j, 1, String.valueOf(taskInfo.get(Dict.NAME)));
                        setCellValue(workbook, 1, j, 2, myUtil.getNameByUser(taskInfo.get(Dict.SPDBMASTER)));
                        setCellValue(workbook, 1, j, 3, myUtil.getNameByUser(taskInfo.get(Dict.MASTER)));
                        setCellValue(workbook, 1, j, 4, myUtil.getNameByUser(taskInfo.get(Dict.DEVELOPER)));
                        setCellValue(workbook, 1, j, 5, String.valueOf(taskInfo.get(Dict.GROUP)));
                        setCellValue(workbook, 1, j, 6, String.valueOf(taskInfo.get(Dict.STAGE)));
                        j++;
                    }
                }
                i++;
            }
        } catch (Exception e) {
            logger.error("e" + e);
            throw new FtmsException(ErrorConstants.EXPORT_EXCEL_ERROR);
        }
        resp.reset();
        resp.setContentType("application/octet-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Content-Disposition", "attachment;filename=" + "WorkOrderReport.xlsx");
        workbook.write(resp.getOutputStream());
    }

    private String getStageCn(String stage) throws Exception {
        switch (Integer.valueOf(stage)){
            case 0 : return "未分配";
            case 1 : return "开发中";
            case 2 : return "SIT";
            case 3 : return "UAT";
            case 4 : return "已投产";
            case 5 : return "sit/uat并行";
            case 6 : return "UAT（含风险）";
            case 7 : return "UAT（提测）";
            case 8 : return "撤销";
            case 9 : return "分包测试（内测完成）";
            case 10 : return "分包测试（含风险）";
            case 11 : return "已废弃";
            case 12 : return "安全测试（内测完成）";
            case 13 : return "安全测试（含风险）";
            case 14 : return "安全测试（不涉及）";
            default: return "";
        }
    }

    /**
     * 趋势图
     * @param dateType
     * @param startDate
     * @param times
     * @param groupIds
     * @return
     * @throws Exception
     */
    public Map<String, Object> tendencyChart(String dateType, String startDate, String times, List<String> groupIds) throws Exception {
        if(Integer.valueOf(times)>12){
            throw new FtmsException(ErrorConstants.TIMES_OUT_OF_RANGE);
        }
        Map<String, Object> result = new HashMap<>();
        List<Integer> orderNum = new ArrayList<>();//工单数
        List<Integer> sumExe = new ArrayList<>();//总执行数
        List<Integer> testMantis = new ArrayList<>();//内测相关缺陷
        List<Integer> developMantis = new ArrayList<>();//开发相关缺陷
        List<Integer> effectiveIssue = new ArrayList<>();//有效缺陷
        List<Integer> ineffectiveIssue = new ArrayList<>();//无效缺陷
        List<Integer> rqrRuleNum = new ArrayList<>();//需规问题缺陷
        List<Integer> funcLackNum = new ArrayList<>();//功能实现不完整缺陷
        List<Integer> funcErrNum = new ArrayList<>();//功能实现错误缺陷
        List<Integer> rqrNum = new ArrayList<>();//业务需求问题缺陷
        List<Integer> historyNum = new ArrayList<>();//历史遗留问题缺陷
        List<Integer> optimizeNum = new ArrayList<>();//优化建议缺陷
        List<Integer> backNum = new ArrayList<>();//后台问题缺陷
        List<Integer> packageNum = new ArrayList<>();//打包问题缺陷
        List<Integer> dataNum = new ArrayList<>();//数据问题缺陷
        List<Integer> envNum = new ArrayList<>();//环境问题缺陷
        List<Integer> otherNum = new ArrayList<>();//其他原因缺陷
        List<Integer> proIssueDev = new ArrayList<>();//开发相关生产问题
        List<Integer> proIssueSit = new ArrayList<>();//内测相关生产问题
        List<String> effectiveIssueRate = new ArrayList<>();//有效缺陷率
        List<String> xMsg = new ArrayList<>();//x轴
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //循环查询每个时间段数据
        for(int i = 0; i < Integer.valueOf(times); i++){
            //处理endDate
            String endDate = null;
            if("0".equals(dateType)){
                endDate = startDate;
                xMsg.add(endDate);
            }
            if("1".equals(dateType)){
                Date date = sdf.parse(startDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.DATE, 6);
                date = cal.getTime();
                endDate = sdf.format(date);
                xMsg.add(startDate + "至" + endDate);
            }
            if("2".equals(dateType)){
                String[] time = startDate.split("-");
                int year = Integer.valueOf(time[0]);
                int month = Integer.valueOf(time[1]);
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month-1);
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
                endDate = sdf.format(cal.getTime());
                cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DATE));
                startDate = sdf.format(cal.getTime());
                xMsg.add(month + "月");
            }
            //获取工单数和执行数
            Map<String, Integer> elm = odsMapper.querytendencyChart(startDate, endDate, groupIds);
            orderNum.add(elm.get(Dict.ORDERNUM));
            sumExe.add(elm.get(Dict.SUMEXE));
            //发mantis查缺陷
            Map sendMantis = new HashMap();
            sendMantis.put(Dict.STARTDATE, startDate);
            sendMantis.put(Dict.ENDDATE, endDate);
            sendMantis.put(Dict.GROUPIDS, groupIds);
            sendMantis.put(Dict.REST_CODE, "mantis.countIssueDetailByOrderNos");
            Map<String, Object> mantisList = new HashMap<>();
            try {
                mantisList = (Map<String, Object>)restTransport.submit(sendMantis);
            } catch (Exception e) {
                logger.error("fail to query mantis" + e);
            }
            developMantis.add((Integer)mantisList.get(Dict.DEVISSUE));
            testMantis.add((Integer)mantisList.get(Dict.SUMISSUE));
            effectiveIssue.add((Integer)mantisList.get(Dict.EFFECTIVEISSUE));
            ineffectiveIssue.add((Integer)mantisList.get(Dict.INEFFECTIVEISSUE));
            rqrRuleNum.add((Integer)mantisList.get(Dict.RQRRULENUM));
            funcLackNum.add((Integer)mantisList.get(Dict.FUNCLACKNUM));
            funcErrNum.add((Integer)mantisList.get(Dict.FUNCERRNUM));
            rqrNum.add((Integer)mantisList.get(Dict.RQRNUM));
            historyNum.add((Integer)mantisList.get(Dict.HISTORYNUM));
            optimizeNum.add((Integer)mantisList.get(Dict.OPTIMIZENUM));
            backNum.add((Integer)mantisList.get(Dict.BACKNUM));
            packageNum.add((Integer)mantisList.get(Dict.PACKAGENUM));
            dataNum.add((Integer)mantisList.get(Dict.DATANUM));
            envNum.add((Integer)mantisList.get(Dict.ENVNUM));
            otherNum.add((Integer)mantisList.get(Dict.OTHERNUM));
            if ((Integer)mantisList.get(Dict.SUMISSUE) == 0) {
                effectiveIssueRate.add("0%");
            } else {
                BigDecimal effectiveBd = new BigDecimal((Integer)mantisList.get(Dict.EFFECTIVEISSUE));
                BigDecimal sumIssueBd = BigDecimal.valueOf((Integer)mantisList.get(Dict.SUMISSUE));
                effectiveIssueRate.add(effectiveBd.divide(sumIssueBd, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).stripTrailingZeros().toPlainString() + "%");
            }
            //生产缺陷数量
            Map sendProIssue = new HashMap();
            sendProIssue.put(Dict.START_TIME, startDate);
            sendProIssue.put(Dict.END_TIME, endDate);
            sendProIssue.put(Dict.ISINCLUDECHILDREN, true);
            sendProIssue.put(Dict.CURRENT_PAGE, Constants.DEFAULT_1);
            sendProIssue.put(Dict.PAGE_SIZE, 500);
            sendProIssue.put(Dict.PROBLEMTYPE, Arrays.asList(new String[]{"开发","内测"}));
            sendProIssue.put(Dict.REST_CODE, "mantis.queryProIssues");
            List<Map<String, Object>> proIssueDevList = new ArrayList<>();
            List<Map<String, Object>> proIssueSitList = new ArrayList<>();
            try {
            //    List<Map<String, Object>> proIssueList = (List<Map<String, Object>>)restTransport.submit(sendProIssue);
             //   proIssueDevList = proIssueList.stream().filter(e -> (String.valueOf(e.get(Dict.ISSUE_TYPE))).contains("开发")).collect(Collectors.toList());
              //  proIssueSitList = proIssueList.stream().filter(e -> (String.valueOf(e.get(Dict.ISSUE_TYPE))).contains("内测")).collect(Collectors.toList());
            } catch (Exception e) {
                logger.error("fail to query proIssue" + e);
            }
            proIssueDev.add(proIssueDevList.size());
            proIssueSit.add(proIssueSitList.size());
            //处理startDate
            if("0".equals(dateType)||"1".equals(dateType)||"2".equals(dateType)){
                Date date = sdf.parse(endDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.DATE, 1);
                date = cal.getTime();
                startDate = sdf.format(date);
            }
        }
        result.put(Dict.ORDERNUM, orderNum);
        result.put(Dict.SUMEXE, sumExe);
        result.put(Dict.DEVELOPMANTIS, developMantis);
        result.put(Dict.TESTMANTIS, testMantis);
        result.put(Dict.EFFECTIVEISSUE, effectiveIssue);
        result.put(Dict.INEFFECTIVEISSUE, ineffectiveIssue);
        result.put(Dict.RQRRULENUM, rqrRuleNum);
        result.put(Dict.FUNCLACKNUM, funcLackNum);
        result.put(Dict.FUNCERRNUM, funcErrNum);
        result.put(Dict.RQRNUM, rqrNum);
        result.put(Dict.HISTORYNUM, historyNum);
        result.put(Dict.OPTIMIZENUM, optimizeNum);
        result.put(Dict.BACKNUM, backNum);
        result.put(Dict.PACKAGENUM, packageNum);
        result.put(Dict.DATANUM, dataNum);
        result.put(Dict.ENVNUM, envNum);
        result.put(Dict.OTHERNUM, otherNum);
        result.put(Dict.PROISSUEDEV, proIssueDev);
        result.put(Dict.PROISSUESIT, proIssueSit);
        result.put(Dict.XMSG, xMsg);
        result.put(Dict.EFFECTIVEISSUERATE, effectiveIssueRate);
      return result;
    }

    @Override
    public Map<String, Object> queryDiscountChart(Map map) {
        String type = (String) map.get(Dict.TYPE);
        String groupId = (String) map.getOrDefault(Dict.GROUPID, Constants.FAIL_GET);
        String startDate = (String) map.get(Dict.STARTDATE);
        Integer times = (Integer) map.get(Dict.TIMES);
        Map<String, Object> result = new HashMap<>();
        List<Integer> newWorkOrder = new ArrayList<>();
        List<Integer> processWorkOrder = new ArrayList<>();
        List<String> stage = new ArrayList<>();
        for (int i = 1; i <= times; i++) {
            String endDate;
            if ("0".equals(type)) {
                    // 日
                    endDate = OrderUtil.timeAddDay(startDate);
                    // 查询日期时间段
                    stage.add(startDate);
                } else if ("1".equals(type)) {
                    // 周
                    endDate = OrderUtil.timeAddWeek(startDate);
                    // 查询日期时间段
                    stage.add(startDate + "至" + endDate);
                } else {
                    // 月
                    endDate = OrderUtil.timeAddmonth(startDate);
                    // 查询日期时间段
                    stage.add(startDate + "至" + endDate);
                }
                // 新建工单
                Integer newNumber = odsMapper.queryNewWorkOrder(startDate, endDate, groupId);
                // 进行中的工单
                Integer processNumber = odsMapper.queryProcessingWorkOrder(startDate, endDate, groupId);
                newWorkOrder.add(newNumber);
                processWorkOrder.add(processNumber);
                startDate = endDate;
            }
            result.put("stage", stage);
            result.put("newWorkOrder", newWorkOrder);
            result.put("processWorkOrder", processWorkOrder);
            return result;
        }
    /**
     * 缺陷维度报表导出
     * @param map
     * @param resp
     * @throws Exception
     */
    public void exportMantisStatement(Map map, HttpServletResponse resp) throws Exception {
        String dateType = String.valueOf(map.get(Dict.DATETYPE));
        String startDate = String.valueOf(map.get(Dict.STARTDATE));
        String times = String.valueOf(map.get(Dict.TIMES));
        List<String> groupIds = (List<String>) map.get(Dict.GROUPIDS);
        Map<String, Object> mantisInfo = tendencyChart(dateType, startDate, times, groupIds);
        XSSFWorkbook workbook;
        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet();
            Map cellName = new HashMap();
            setCellValueAndName(workbook, 0, 0, 0, "日期", Dict.XMSG, cellName);
            setCellValueAndName(workbook, 0, 0, 1, "工单数", Dict.ORDERNUM, cellName);
            setCellValueAndName(workbook, 0, 0, 2, "案例执行数", Dict.SUMEXE, cellName);
            setCellValueAndName(workbook, 0, 0, 3, "缺陷总数", Dict.TESTMANTIS, cellName);
            setCellValueAndName(workbook, 0, 0, 4, "开发相关缺陷", Dict.DEVELOPMANTIS, cellName);
            setCellValueAndName(workbook, 0, 0, 5, "有效缺陷数", Dict.EFFECTIVEISSUE, cellName);
            setCellValueAndName(workbook, 0, 0, 6, "无效缺陷数", Dict.INEFFECTIVEISSUE, cellName);
            setCellValueAndName(workbook, 0, 0, 7, "业务需求问题", Dict.RQRNUM, cellName);
            setCellValueAndName(workbook, 0, 0, 8, "需规问题", Dict.RQRRULENUM, cellName);
            setCellValueAndName(workbook, 0, 0, 9, "功能实现不完整", Dict.FUNCLACKNUM, cellName);
            setCellValueAndName(workbook, 0, 0, 10, "功能实现错误", Dict.FUNCERRNUM, cellName);
            setCellValueAndName(workbook, 0, 0, 11, "历史遗留问题", Dict.HISTORYNUM, cellName);
            setCellValueAndName(workbook, 0, 0, 12, "优化建议", Dict.OPTIMIZENUM, cellName);
            setCellValueAndName(workbook, 0, 0, 13, "后台问题", Dict.BACKNUM, cellName);
            setCellValueAndName(workbook, 0, 0, 14, "打包问题", Dict.PACKAGENUM, cellName);
            setCellValueAndName(workbook, 0, 0, 15, "数据问题", Dict.DATANUM, cellName);
            setCellValueAndName(workbook, 0, 0, 16, "环境问题", Dict.ENVNUM, cellName);
            setCellValueAndName(workbook, 0, 0, 17, "其他原因", Dict.OTHERNUM, cellName);
            setCellValueAndName(workbook, 0, 0, 18, "开发相关生产问题", Dict.PROISSUEDEV, cellName);
            setCellValueAndName(workbook, 0, 0, 19, "内测相关生产问题", Dict.PROISSUESIT, cellName);
            setCellValueAndName(workbook, 0, 0, 20, "有效缺陷率", Dict.EFFECTIVEISSUERATE, cellName);
            for(String key : mantisInfo.keySet()){
                List<Object> elm = (List<Object>)mantisInfo.get(key);
                Integer cell = (Integer)cellName.get(key);
                int i = 1;
                for(Object e : elm){
                    setCellValue(workbook, 0, i, cell, String.valueOf(e));
                    i++;
                }
            }
        } catch (Exception e) {
            logger.error("e" + e);
            throw new FtmsException(ErrorConstants.EXPORT_EXCEL_ERROR);
        }
        resp.reset();
        resp.setContentType("application/octet-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Content-Disposition", "attachment;filename=" + "mantispReport.xlsx");
        workbook.write(resp.getOutputStream());
    }

    private void setCellValueAndName(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex, String cellValue, String key, Map map) throws Exception {
        setCellValue(workbook, sheetIndex, rowIndex, cellIndex, cellValue);
        map.put(key, cellIndex);
    }

    /**
     * 质量报表
     * @param map
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> qualityReport(Map map) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        Map<String, String> resultUnit;
        List<String> fdevGroupIds = (List<String>)map.get(Dict.GROUPID);
        String isParent = String.valueOf(map.get(Dict.ISPARENT));
        String startDate = String.valueOf(map.getOrDefault(Dict.STARTDATE, ""));
        String endDate = String.valueOf(map.getOrDefault(Dict.ENDDATE, ""));
        String fdevGroupId;
        if(Constants.NUMBER_1.equals(isParent)){
            Set<String> childGroup = getFdevChildGroup(fdevGroupIds);
            fdevGroupId = "'" + String.join("','", childGroup) + "'";
        }else{
            fdevGroupId = "'" + String.join("','", fdevGroupIds) + "'";
        }
        //提测准时率（列表）
        List<Map<String, String>> timely =  odsMapper.sitSubmitTimely(fdevGroupId, startDate, endDate);
        Map<String, Object> timelyData = makeIdKey(timely);
        //冒烟测试通过率（柱状图）
        List<Map<String, String>> smoke =  odsMapper.smokePass(fdevGroupId, startDate, endDate);
        Map<String, Object> smokeData = makeIdKey(smoke);
        //缺陷密度（柱状图）//缺陷reopen率（柱状图）//严重缺陷解决时长（柱状图）
        List<Map<String, String>> exeTimes = odsMapper.queryExeTimeByFdevGroup(fdevGroupId, startDate, endDate);
        Map sendMantis = new HashMap();
        sendMantis.put(Dict.GROUP, fdevGroupId);
        sendMantis.put(Dict.REST_CODE, "mantis.qualityReport");
        sendMantis.put(Dict.STARTDATE, startDate);
        sendMantis.put(Dict.ENDDATE, endDate);
        Map<String, Object> mantis = new HashMap<>();
        try {
            mantis = (Map<String, Object>)restTransport.submit(sendMantis);
        } catch (Exception e) {
            logger.error("fail to get mantis infomation");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询缺陷失败"});
        }
        Map<String, Object> mantisInfo = makeMantisInfo(exeTimes, mantis);
        for(String id : fdevGroupIds){
            Integer timelyNumSum = 0;
            Integer totalSum = 0;
            Integer smokeSum = 0;
            Integer smokeTotal = 0;
            Integer effectiveIssue = 0;
            Integer exeTime = 0;
            Integer reopenNum = 0;
            Integer retotal = 0;
            long sevSpendTime = 0;
            Integer sevNum = 0;
            long normalSpendTime = 0;
            Integer normalNum = 0;
            if("1".equals(isParent)){
                List<String> fdevId = new ArrayList<>();
                fdevId.add(id);
                Set<String> childGroup = getFdevChildGroup(fdevId);
                for(String id1 : childGroup){
                    if(!Util.isNullOrEmpty(timelyData.get(id1))){
                        Integer timelyNum = Integer.valueOf(((Map)timelyData.get(id1)).get(Dict.TIMELY).toString());
                        Integer total = Integer.valueOf((((Map)timelyData.get(id1)).get(Dict.TOTAL)).toString());
                        timelyNumSum += timelyNum;
                        totalSum += total;
                    }
                    if(!Util.isNullOrEmpty(smokeData.get(id1))){
                        Integer smoke1 = Integer.valueOf(((Map)smokeData.get(id1)).get(Dict.TIMELY).toString());
                        Integer snoketotal1 = Integer.valueOf((((Map)smokeData.get(id1)).get(Dict.TOTAL)).toString());
                        smokeSum += smoke1;
                        smokeTotal += snoketotal1;
                    }
                    if(!Util.isNullOrEmpty(mantisInfo.get(id1))){
                        Integer effectiveIssue1 = Integer.valueOf(((Map)mantisInfo.get(id1)).getOrDefault(Dict.EFFECTIVEISSUE, 0).toString());
                        Integer exeTime1 = Integer.valueOf(((Map)mantisInfo.get(id1)).getOrDefault(Dict.EXETIME, 0).toString());
                        Integer reopenNum1 = Integer.valueOf(((Map)mantisInfo.get(id1)).getOrDefault(Dict.REOPENNUM, 0).toString());
                        Integer retotal1 = Integer.valueOf(((Map)mantisInfo.get(id1)).getOrDefault(Dict.TOTAL, 0).toString());
                        Long sevSpendTime1 = Long.valueOf(((Map)mantisInfo.get(id1)).getOrDefault(Dict.SEVSPENDTIME, 0).toString());
                        Integer sevNum1 = Integer.valueOf(((Map)mantisInfo.get(id1)).getOrDefault(Dict.SEVNUM, 0).toString());
                        Long normalSpendTime1 = Long.valueOf(((Map)mantisInfo.get(id1)).getOrDefault(Dict.NORMALSPENDTIME, 0).toString());
                        Integer normalNum1 = Integer.valueOf(((Map)mantisInfo.get(id1)).getOrDefault(Dict.NORMALNUM, 0).toString());
                        effectiveIssue += effectiveIssue1;
                        exeTime += exeTime1;
                        reopenNum += reopenNum1;
                        retotal += retotal1;
                        sevSpendTime += sevSpendTime1;
                        sevNum += sevNum1;
                        normalSpendTime += normalSpendTime1;
                        normalNum += normalNum1;
                    }
                }
            }else{
                if(!Util.isNullOrEmpty(timelyData.get(id))){
                    Integer timelyNum = Integer.valueOf(((Map)timelyData.get(id)).get(Dict.TIMELY).toString());
                    Integer total = Integer.valueOf((((Map)timelyData.get(id)).get(Dict.TOTAL)).toString());
                    timelyNumSum += timelyNum;
                    totalSum += total;
                }
                if(!Util.isNullOrEmpty(smokeData.get(id))){
                    Integer smoke1 = Integer.valueOf(((Map)smokeData.get(id)).get(Dict.TIMELY).toString());
                    Integer snoketotal1 = Integer.valueOf((((Map)smokeData.get(id)).get(Dict.TOTAL)).toString());
                    smokeSum += smoke1;
                    smokeTotal += snoketotal1;
                }
                if(!Util.isNullOrEmpty(mantisInfo.get(id))){
                    Integer effectiveIssue1 = Integer.valueOf(((Map)mantisInfo.get(id)).getOrDefault(Dict.EFFECTIVEISSUE, 0).toString());
                    Integer exeTime1 = Integer.valueOf(((Map)mantisInfo.get(id)).getOrDefault(Dict.EXETIME, 0).toString());
                    Integer reopenNum1 = Integer.valueOf(((Map)mantisInfo.get(id)).getOrDefault(Dict.REOPENNUM, 0).toString());
                    Integer retotal1 = Integer.valueOf(((Map)mantisInfo.get(id)).getOrDefault(Dict.TOTAL, 0).toString());
                    Long sevSpendTime1 = Long.valueOf(((Map)mantisInfo.get(id)).getOrDefault(Dict.SEVSPENDTIME, 0).toString());
                    Integer sevNum1 = Integer.valueOf(((Map)mantisInfo.get(id)).getOrDefault(Dict.SEVNUM, 0).toString());
                    Long normalSpendTime1 = Long.valueOf(((Map)mantisInfo.get(id)).getOrDefault(Dict.NORMALSPENDTIME, 0).toString());
                    Integer normalNum1 = Integer.valueOf(((Map)mantisInfo.get(id)).getOrDefault(Dict.NORMALNUM, 0).toString());
                    effectiveIssue += effectiveIssue1;
                    exeTime += exeTime1;
                    reopenNum += reopenNum1;
                    retotal += retotal1;
                    sevSpendTime += sevSpendTime1;
                    sevNum += sevNum1;
                    normalSpendTime += normalSpendTime1;
                    normalNum += normalNum1;
                }
            }
            float timelyRate = totalSum==0?0:(float)timelyNumSum / totalSum;
            float smokeRate = smokeTotal==0?0:(float)smokeSum / smokeTotal;
            float mantisRate = exeTime==0?0:(float)effectiveIssue / exeTime;
            float reopenRate = retotal==0?0:(float)reopenNum / retotal;
            float sevAvgTime = sevNum==0?0:(float)sevSpendTime / sevNum /60/60/24;
            float normalAvgTime = normalNum==0?0:(float)normalSpendTime / normalNum/60/60/24;
            resultUnit = new HashMap<>();
            String name = "";
            try {
                name = String.valueOf(fdevGroupApi.queryGroupDetail(id).get(Dict.NAME));
            } catch (Exception e) {
                logger.error("fail to find group by :" + id);
            }
            resultUnit.put(Dict.FDEVGROUPID, id);
            resultUnit.put(Dict.NAME, name);
            resultUnit.put(Dict.TIMELYRATE, String.valueOf(floatToString1(timelyRate)));//提测准时率
            resultUnit.put(Dict.SMOKERATE, String.valueOf(floatToString1(smokeRate)));//冒烟测试通过率
            resultUnit.put(Dict.MANTISRATE, String.valueOf(floatToString1(mantisRate)));//缺陷密度
            resultUnit.put(Dict.REOPENRATE, String.valueOf(floatToString1(reopenRate)));//缺陷reopen率
            resultUnit.put(Dict.SEVAVGTIME, String.valueOf(floatToString1(sevAvgTime))+"天");//严重缺陷解决平均时长
            resultUnit.put(Dict.NORMALAVGTIME, String.valueOf(floatToString1(normalAvgTime))+"天");//缺陷解决平均时长
            result.add(resultUnit);
        }
        return result;
    }

    private Map<String, Object> makeMantisInfo(List<Map<String, String>> exeTimes, Map<String, Object> mantis) throws Exception{
        Map result = new HashMap();
        Map groupInfo;
        for(Map<String, String> exeTime : exeTimes){
            groupInfo = new HashMap();
            String fdevGroupId = exeTime.get(Dict.FDEVGROUPID);
            String times = String.valueOf(exeTime.get(Dict.EXETIME));
            groupInfo.put(Dict.EXETIME, times);
            result.put(fdevGroupId, groupInfo);
        }
        List<Map<String, String>> effectiveIssue = (List<Map<String, String>>)mantis.get(Dict.EFFECTIVEISSUE);
        List<Map<String, String>> reopenIssue = (List<Map<String, String>>)mantis.get(Dict.REOPENISSUE);
        List<Map<String, String>> solveTime = (List<Map<String, String>>)mantis.get(Dict.SOLVETIME);
        for(Map<String, String> eff : effectiveIssue){
            String fdevGroupId = eff.get(Dict.FDEVGROUPID);
            String effIssue = String.valueOf(eff.get(Dict.EFFECTIVEISSUE));
            if(!Util.isNullOrEmpty(result.get(fdevGroupId))){
                ((Map)result.get(fdevGroupId)).put(Dict.EFFECTIVEISSUE, effIssue);
            }else{
                groupInfo = new HashMap();
                groupInfo.put(Dict.EFFECTIVEISSUE, effIssue);
                result.put(fdevGroupId, groupInfo);
            }
        }
        for(Map<String, String> reo : reopenIssue){
            String fdevGroupId = reo.get(Dict.FDEVGROUPID);
            String reoNum = String.valueOf(reo.get(Dict.REOPENNUM));
            String total = String.valueOf(reo.get(Dict.TOTAL));
            if(!Util.isNullOrEmpty(result.get(fdevGroupId))){
                ((Map)result.get(fdevGroupId)).put(Dict.REOPENNUM, reoNum);
                ((Map)result.get(fdevGroupId)).put(Dict.TOTAL, total);
            }else{
                groupInfo = new HashMap();
                groupInfo.put(Dict.REOPENNUM, reoNum);
                groupInfo.put(Dict.TOTAL, total);
                result.put(fdevGroupId, groupInfo);
            }
        }
        for(Map<String, String> solve : solveTime){
            String fdevGroupId = solve.get(Dict.FDEVGROUPID);
            String sevSpendTime = String.valueOf(solve.get(Dict.SEVSPENDTIME));
            String sevNum = String.valueOf(solve.get(Dict.SEVNUM));
            String normalSpendTime = String.valueOf(solve.get(Dict.NORMALSPENDTIME));
            String normalNum = String.valueOf(solve.get(Dict.NORMALNUM));
            if(!Util.isNullOrEmpty(result.get(fdevGroupId))){
                ((Map)result.get(fdevGroupId)).put(Dict.SEVSPENDTIME, sevSpendTime);
                ((Map)result.get(fdevGroupId)).put(Dict.SEVNUM, sevNum);
                ((Map)result.get(fdevGroupId)).put(Dict.NORMALSPENDTIME, normalSpendTime);
                ((Map)result.get(fdevGroupId)).put(Dict.NORMALNUM, normalNum);
            }else{
                groupInfo = new HashMap();
                groupInfo.put(Dict.SEVSPENDTIME, sevSpendTime);
                groupInfo.put(Dict.SEVNUM, sevNum);
                groupInfo.put(Dict.NORMALSPENDTIME, normalSpendTime);
                groupInfo.put(Dict.NORMALNUM, normalNum);
                result.put(fdevGroupId, groupInfo);
            }
        }
        return  result;
    }

    private Set<String> getFdevChildGroup(List<String> fdevGroupIds) throws Exception {
        Set<String> childGroup = new HashSet<>();
        for(String fdevGroupId : fdevGroupIds){
            Map send = new HashMap();
            send.put(Dict.REST_CODE, "queryChildGroupById");
            send.put(Dict.ID, fdevGroupId);
            List<Map<String, Object>> result = new ArrayList<>();
            try {
                result = (List<Map<String, Object>>)restTransport.submitSourceBack(send);
                for(Map<String, Object> group : result){
                    childGroup.add(String.valueOf(group.get(Dict.ID)));
                }
            } catch (Exception e) {
                logger.error("fail to query child group for groupId :" + fdevGroupId + "," + e);
            }
        }
        return childGroup;
    }

    private Map<String, Object> makeIdKey(List<Map<String, String>>  timely) throws Exception {
        Map<String, Object>  result = new HashMap<>();
        Map<String, String> data;
        for(Map<String, String> timelyUnit : timely){
            String fdevGroupId = timelyUnit.get(Dict.FDEVGROUPID);
            data = new HashMap<>();
            data.put(Dict.TIMELY, timelyUnit.get(Dict.TIMELY));
            data.put(Dict.TOTAL, timelyUnit.get(Dict.TOTAL));
            result.put(fdevGroupId, data);
        }
        return result;
    }

    /**
     * 获取质量报表的全量底数
     * @return
     * @throws Exception
     */
    @Override
    @LazyInitPropertyLong(redisKeyExpression = "cacheQualityReport")
    public Map<String, Object> cacheQualityReport() throws Exception {
        Map result = new HashMap();
        //获取提测和打回数据
        List<Map<String, String>> submitInfo = qualityReportMapper.querySubmitInfo();
        submitInfo = addTaskInfo(submitInfo);
        //获取案例执行数据
        List<Map<String, String>> exeTimeInfo = qualityReportMapper.queryExeTime();
        //获取缺陷信息
        List<Map<String, String>> mantisInfo = mantisService.queryQualityAll();
        mantisInfo = dealMantisInfo(mantisInfo);
        //获取全量自己完成mergeRequest的数据(人,组,merge时间)
//        List<Map<String, String>> mergeRecord = getSelfMergeInfo();
//        result.put(Dict.SELF_MERGE, mergeRecord);
        result.put(Dict.SUBMITINFO, submitInfo);
        result.put(Dict.EXETIMEINFO, exeTimeInfo);
        result.put(Dict.MANTISINFO, mantisInfo);
        return result;
    }

    private List<Map<String, String>> dealMantisInfo(List<Map<String, String>> mantisInfo) throws Exception{
        //批量查询工单类型和新老fdev标志
        List<Map> workOrderList = workOrderMapper.queryOrderTypeByNos(mantisInfo.stream().map(mantis -> mantis.get(Dict.WORKNO)).collect(Collectors.toList()));
        Map<String, Map> workOrderMap = new HashMap<>();
        for (Map workOrder : workOrderList) {
            workOrderMap.put((String) workOrder.get(Dict.WORKNO), workOrder);
        }
        List<String> taskNos = mantisInfo.stream().map(submit -> submit.get(Dict.TASKNO)).distinct().collect(Collectors.toList());
        taskNos.remove(null);
        List<Map> taskList = iTaskApi.queryTaskBaseInfoByIds(taskNos, null, Arrays.asList(Dict.ID, Dict.NAME));
        //将任务编号和任务信息对应
        Map<String, Map> taskMap = new HashMap<>();
        if (!Util.isNullOrEmpty(taskList)) {
            for (Map taskInfo : taskList) {
                taskMap.put(String.valueOf(taskInfo.get(Dict.ID)), taskInfo);
            }
        }
        Iterator<Map<String, String>> iterator = mantisInfo.iterator();
        while (iterator.hasNext()){
            Map<String, String> mantisInfoUnit = iterator.next();
            mantisInfoUnit.put(Dict.STATUS, myUtil.getChStatus(String.valueOf(mantisInfoUnit.get(Dict.STATUS))));
            String taskNo = mantisInfoUnit.get(Dict.TASKNO);
            String workNo = mantisInfoUnit.get(Dict.WORKNO);
            Map workOrder = workOrderMap.get(workNo);
            //去除安全工单
            if (!CommonUtils.isNullOrEmpty(workOrder) && Constants.ORDERTYPE_SECURITY.equals(workOrder.get(Dict.ORDERTYPE))) {
                iterator.remove();
            }
            //判断该工单是否是新老fdev产生
            if (!CommonUtils.isNullOrEmpty(workOrder) && "1".equals(String.valueOf(workOrder.get(Dict.FDEVNEW)))){
                //查询新的fdev任务
                if (!Util.isNullOrEmpty(taskNo)){
                    try {
                        Map<String, Object> task = iTaskApi.getNewTaskById(taskNo);
                        mantisInfoUnit.put(Dict.TASKNAME, String.valueOf(task.get(Dict.NAME)));
                    } catch (Exception e) {
                        logger.error("fail to get taskInfo for id: " + taskNo);
                    }
                }
            }else {
                if (!Util.isNullOrEmpty(taskNo)) {
                    try {
                        Map taskInfo = taskMap.get(taskNo);
                        mantisInfoUnit.put(Dict.TASKNAME, String.valueOf(taskInfo.get(Dict.NAME)));
                    } catch (Exception e) {
                        logger.error("fail to get taskInfo for id: " + taskNo);
                    }
                }
            }
        }
        return mantisInfo;
    }

    private List<Map<String, String>> addTaskInfo(List<Map<String, String>> submitInfo) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        Iterator<Map<String, String>> iterator = submitInfo.iterator();
        List<String> taskNos = submitInfo.stream().map(submit -> submit.get(Dict.TASKNO)).distinct().collect(Collectors.toList());
        taskNos.remove(null);
        List<Map> taskList = iTaskApi.queryTaskBaseInfoByIds(taskNos, Arrays.asList(Dict.SPDBMASTER,Dict.MASTER,Dict.DEVELOPER,Dict.GROUP,Dict.STAGE),
                Arrays.asList(Dict.ID, Dict.SPDBMASTER,Dict.MASTER,Dict.DEVELOPER,Dict.GROUP,Dict.STAGE));
        //将任务编号和任务信息对应
        Map<String, Map> taskMap = new HashMap<>();
        if (!Util.isNullOrEmpty(taskList)) {
            for (Map taskInfo : taskList) {
                taskMap.put(String.valueOf(taskInfo.get(Dict.ID)), taskInfo);
            }
        }
        while (iterator.hasNext()){
            Map<String, String> resultUnit = iterator.next();
            String fdevGroupId = resultUnit.get(Dict.FDEVGROUPID);
            String taskNo = String.valueOf(resultUnit.get(Dict.TASKNO));
            Map taskInfo = null;
            //判断任务是否是新老fdev
            String newFdev = workOrderMapper.queryNewFdevBytaskId(taskNo, Constants.ORDERTYPE_FUNCTION);
            if ("1".equals(newFdev)){
               taskInfo = iTaskApi.getNewTaskById(taskNo);
               if (Util.isNullOrEmpty(taskInfo)){
                   iterator.remove();
               }else {
                //行内负责，负责人，开发人员，所属组，任务阶段
                   List<String> assignIds =(List<String>) taskInfo.get("assigneeList");
                   String userId = assignIds.get(0);
                   Map<String, Object> map = userService.queryUserCoreDataById(userId);
                   String  userName=(String) map.get(Dict.USER_NAME_CN);
                   //如果工单组和任务组不同，取任务组
                   String assigneeGroupId =(String) taskInfo.get("assigneeGroupId");
                   if (!fdevGroupId.equals(assigneeGroupId)){
                       resultUnit.put(Dict.FDEVGROUPID,assigneeGroupId);
                   }
                   Map<String, Object> map1 = userService.queryGroupDetailById(assigneeGroupId);
                   String groupNameCn="";
                   if (!Util.isNullOrEmpty(map1)){
                      groupNameCn= (String) map1.get(Dict.NAME);
                   }
                   String type = String.valueOf(taskInfo.get("type"));
                   resultUnit.put(Dict.SPDBMASTER, userName);
                   resultUnit.put(Dict.MASTER, userName);
                   resultUnit.put(Dict.DEVELOPER, userName);
                   resultUnit.put(Dict.GROUPNAME, groupNameCn);
                   resultUnit.put(Dict.STAGE, type);
                   result.add(resultUnit);
               }
            }
            else {
                try {
                    taskInfo = taskMap.get(taskNo);
                    if (Util.isNullOrEmpty(taskInfo)) {
                        iterator.remove();
                    } else {
                        //行内负责，负责人，开发人员，所属组，任务阶段
                        String spdbMasterName = myUtil.getFdevNameCn(taskInfo, Dict.SPDBMASTER);
                        String masterName = myUtil.getFdevNameCn(taskInfo, Dict.MASTER);
                        String developerName = myUtil.getFdevNameCn(taskInfo, Dict.DEVELOPER);
                        //如果工单组和任务组不同，取任务组
                        String taskGroupId = String.valueOf(taskInfo.get(Dict.GROUPID));
                        if (!fdevGroupId.equals(taskGroupId)) {
                            resultUnit.put(Dict.FDEVGROUPID, taskGroupId);
                        }
                        String groupNameCn = String.valueOf(taskInfo.get(Dict.GROUP));
                        String stage = String.valueOf(taskInfo.get(Dict.STAGE));
                        resultUnit.put(Dict.SPDBMASTER, spdbMasterName);
                        resultUnit.put(Dict.MASTER, masterName);
                        resultUnit.put(Dict.DEVELOPER, developerName);
                        resultUnit.put(Dict.GROUPNAME, groupNameCn);
                        resultUnit.put(Dict.STAGE, stage);
                        result.add(resultUnit);
                    }
                } catch (Exception e) {
                    iterator.remove();
                }
            }
        }
        return submitInfo;
    }

    private List<Map<String, String>> getSelfMergeInfo() throws Exception {
        //获取全量自己完成mergeRequest的数据(人,组,merge时间)
        List<Map<String, Object>> developers = userService.queryDeveloper();
        List<Map<String, String>> mergeRecord = new ArrayList<>();
        Map mergeUnit;
        for(Map<String, Object> developer : developers){
            String gitToken = String.valueOf(developer.get(Dict.GIT_TOKEN));
            String gitUser = String.valueOf(developer.get(Dict.GIT_USER));
            String userNameEn =  String.valueOf(developer.get(Dict.USER_NAME_EN));
            String fdevGroupId = String.valueOf(((Map)developer.get(Dict.GROUP)).get(Dict.ID));
            String fdevGroupName = String.valueOf(((Map)developer.get(Dict.GROUP)).get(Dict.NAME));
            if("效能".equals(fdevGroupName)){
                continue;
            }
            JSONArray mergeRequest = new JSONArray();
            JSONArray mergeRequestUnit = new JSONArray();
            int page = 1;
            do{
                try {
                    mergeRequestUnit = userService.queryMergeRequest(gitUser, gitToken, String.valueOf(page));
                    mergeRequest.addAll(mergeRequestUnit);
                } catch (Exception e) {
                    logger.error("fail to query userinfo" + userNameEn + e);
                    continue;
                }
                page++;
            }while(mergeRequestUnit.size()==100);
            for(Object merge : mergeRequest){
                mergeUnit = new HashMap();
                Map<String, Object> to = JSONObject.parseObject(((JSONObject)merge).toJSONString(),new TypeReference<Map<String, Object>>(){});
                String mergeBy = null;
                try {
                    mergeBy = String.valueOf(((Map)to.get(Dict.MERGED_BY)).get(Dict.USERNAME));
                    String mergeTime = String.valueOf(to.get(Dict.MERGED_AT));
                    if(mergeBy.equals(gitUser)){
                        mergeUnit.put(Dict.MERGED_BY, userNameEn);
                        mergeUnit.put(Dict.FDEVGROUPID, fdevGroupId);
                        mergeUnit.put(Dict.MERGED_AT,  mergeTime.substring(0,10).replace("-",""));
                        mergeRecord.add(mergeUnit);
                    }
                } catch (Exception e) {
                    logger.error("fail to query merge info" + e);
                    continue;
                }
            }
        }
        return mergeRecord;
    }

    @Override
    public List<Map<String, Object>> qualityReportNew(Map map, Map<String, Object> data) throws Exception {
        List<String> fdevGroupIds = (List<String>)map.get(Dict.GROUPID);
        String isParent = String.valueOf(map.get(Dict.ISPARENT));
        String startDate = String.valueOf(map.getOrDefault(Dict.STARTDATE, ""));
        String endDate = String.valueOf(map.getOrDefault(Dict.ENDDATE, ""));
        //批量查询小组信息
        fdevGroupIds.remove(null);
        List<Map> groupList = userService.queryGroupByIds(fdevGroupIds);
        //将小组id和小组信息对应起来
        Map<String, Map> groupInfoMap = new HashMap<>();
        for (Map group : groupList) {
            groupInfoMap.put((String) group.get(Dict.ID), group);
        }
        //获取全量组id为key的数据
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<String, Long>> basic = makeUnitDate(data, startDate, endDate);
        basic = addChildData(isParent, fdevGroupIds, basic);
        for(String fdevGroupId : fdevGroupIds){
            Map<String, Object> unit = new HashMap<>();
            if(Util.isNullOrEmpty(basic.get(fdevGroupId))){
               basic.put(fdevGroupId, new HashMap<>());
            }
            Long total = basic.get(fdevGroupId).getOrDefault(Dict.TOTAL,0L);
            Long timely = basic.get(fdevGroupId).getOrDefault(Dict.TIMELY, 0L);
//            Long rollBackNum = basic.get(fdevGroupId).getOrDefault(Dict.ROLLBACKNUM, 0L);
            Long smokeNoPassNum = basic.get(fdevGroupId).getOrDefault(Dict.SMOKENOPASSNUM, 0L);
            Long exeTime = basic.get(fdevGroupId).getOrDefault(Dict.EXETIME, 0L);
            Long sumMantis = basic.get(fdevGroupId).getOrDefault(Dict.SUMMANTIS, 0L);
            Long reopenNum = basic.get(fdevGroupId).getOrDefault(Dict.REOPENNUM, 0L);
            Long normalNum = basic.get(fdevGroupId).getOrDefault(Dict.NORMALNUM, 0L);
            Long solveTime = basic.get(fdevGroupId).getOrDefault(Dict.SOLVETIME, 0L);
            Long severity = basic.get(fdevGroupId).getOrDefault(Dict.SEVERITY, 0L);
            Long severeSolveTime = basic.get(fdevGroupId).getOrDefault(Dict.SEVERESOLVETIME, 0L);
//            Long selfMerge = basic.get(fdevGroupId).getOrDefault(Dict.SELF_MERGE, 0L);
            float timelyRate = total==0?0:(float)timely / total;
            float smokeRate = total==0?0:(float)(total - smokeNoPassNum) / total;
            float mantisRate = exeTime==0?0:(float)sumMantis / exeTime;
            float reopenRate = sumMantis==0?0:(float)reopenNum / sumMantis;
            float sevAvgTime = severity==0?0:(float)severeSolveTime / severity /60/60/24;
            float normalAvgTime = normalNum==0?0:(float)solveTime / normalNum/60/60/24;
            unit.put(Dict.FDEVGROUPID, fdevGroupId);
            Map<String, Object> group = groupInfoMap.get(fdevGroupId);
            unit.put(Dict.NAME, group.get(Dict.NAME) == null ? "" : group.get(Dict.NAME));
            unit.put(Dict.TIMELYRATE, String.valueOf(floatToString1(timelyRate)));//提测准时率
            unit.put(Dict.SMOKERATE, String.valueOf(floatToString1(smokeRate)));//冒烟测试通过率
            unit.put(Dict.MANTISRATE, String.valueOf(floatToString1(mantisRate)));//缺陷密度
            unit.put(Dict.REOPENRATE, String.valueOf(floatToString1(reopenRate)));//缺陷reopen率
            unit.put(Dict.SEVAVGTIME, String.valueOf(floatToString1(sevAvgTime))+"天");//严重缺陷解决平均时长
            unit.put(Dict.NORMALAVGTIME, String.valueOf(floatToString1(normalAvgTime))+"天");//缺陷解决平均时长
            unit.put(Dict.EXETIME, String.valueOf(exeTime));//案例执行次数
            unit.put(Dict.SUMMANTIS, String.valueOf(sumMantis));//有效缺陷总数
//            unit.put(Dict.SELF_MERGE, selfMerge);
            resultList.add(unit);
        }
        return resultList;
    }

    /**
     * 组装子组数据
     * @param isParent
     * @param fdevGroupIds
     * @param basic
     * @return
     * @throws Exception
     */
    private Map<String, Map<String, Long>> addChildData(String isParent, List<String> fdevGroupIds, Map<String, Map<String, Long>> basic) throws Exception{
        if(Constants.NUMBER_1.equals(isParent)){
            for(String fdevGroupId : fdevGroupIds){
                if(Util.isNullOrEmpty(basic.get(fdevGroupId))){
                    basic.put(fdevGroupId, new HashMap<>());
                }
                List<Map<String, String>> resultList = userService.queryChildGroupById(fdevGroupId);
                if(resultList.size()>1){
                    //如果有子组
                    for(Map<String, String> result : resultList){
                        String groupId = result.get(Dict.ID);
                        if(fdevGroupId.equals(groupId)){
                            continue;
                        }
                        if(!Util.isNullOrEmpty(basic.get(groupId))){
                            basic.get(fdevGroupId).put(Dict.TOTAL, (basic.get(fdevGroupId).getOrDefault(Dict.TOTAL, 0L) + basic.get(groupId).getOrDefault(Dict.TOTAL, 0L)));
                            basic.get(fdevGroupId).put(Dict.TIMELY, (basic.get(fdevGroupId).getOrDefault(Dict.TIMELY, 0L) + basic.get(groupId).getOrDefault(Dict.TIMELY, 0L)));
                            basic.get(fdevGroupId).put(Dict.ROLLBACKNUM, (basic.get(fdevGroupId).getOrDefault(Dict.ROLLBACKNUM, 0L) + basic.get(groupId).getOrDefault(Dict.ROLLBACKNUM, 0L)));
                            basic.get(fdevGroupId).put(Dict.EXETIME, (basic.get(fdevGroupId).getOrDefault(Dict.EXETIME, 0L) + basic.get(groupId).getOrDefault(Dict.EXETIME, 0L)));
                            basic.get(fdevGroupId).put(Dict.SUMMANTIS, (basic.get(fdevGroupId).getOrDefault(Dict.SUMMANTIS, 0L) + basic.get(groupId).getOrDefault(Dict.SUMMANTIS, 0L)));
                            basic.get(fdevGroupId).put(Dict.REOPENNUM, (basic.get(fdevGroupId).getOrDefault(Dict.REOPENNUM, 0L) + basic.get(groupId).getOrDefault(Dict.REOPENNUM, 0L)));
                            basic.get(fdevGroupId).put(Dict.SOLVETIME, (basic.get(fdevGroupId).getOrDefault(Dict.SOLVETIME, 0L) + basic.get(groupId).getOrDefault(Dict.SOLVETIME, 0L)));
                            basic.get(fdevGroupId).put(Dict.SEVERITY, (basic.get(fdevGroupId).getOrDefault(Dict.SEVERITY, 0L) + basic.get(groupId).getOrDefault(Dict.SEVERITY, 0L)));
                            basic.get(fdevGroupId).put(Dict.NORMALNUM, (basic.get(fdevGroupId).getOrDefault(Dict.NORMALNUM, 0L) + basic.get(groupId).getOrDefault(Dict.NORMALNUM, 0L)));
                            basic.get(fdevGroupId).put(Dict.SEVERESOLVETIME, (basic.get(fdevGroupId).getOrDefault(Dict.SEVERESOLVETIME, 0L) + basic.get(groupId).getOrDefault(Dict.SEVERESOLVETIME, 0L)));
//                            basic.get(fdevGroupId).put(Dict.SELF_MERGE, (basic.get(fdevGroupId).getOrDefault(Dict.SELF_MERGE, 0L) + basic.get(groupId).getOrDefault(Dict.SELF_MERGE, 0L)));
                        }
                    }
                }
            }
        }
        return basic;
    }


    /**
     * TOTAL,TIMELY,ROLLBACKNUM,EXETIME,SUMMANTIS,REOPENNUM,SOLVETIME,SEVERITY,SEVERESOLVETIME,SELF_MERGE
     * @param data
     * @param startDate
     * @param endDate
     * @return
     */
    private Map<String, Map<String, Long>> makeUnitDate(Map<String, Object> data, String startDate, String endDate) {
        Map<String, Map<String, Long>> result = new HashMap<>();
        List<Map<String, String>> submitInfo = (List<Map<String, String>>)data.get(Dict.SUBMITINFO);
        List<Map<String, String>> exeTimeInfo = (List<Map<String, String>>)data.get(Dict.EXETIMEINFO);
        List<Map<String, String>> mantisInfo = (List<Map<String, String>>)data.get(Dict.MANTISINFO);
//        List<Map<String, String>> selfMergeInfo = (List<Map<String, String>>)data.get(Dict.SELF_MERGE);
        //获取提测准时数据，时间卡计划sit时间
        for(Map<String, String> submitInfoUnit : submitInfo){
            //排除时间外的数据
            if(Util.isNullOrEmpty(submitInfoUnit.get(Dict.PLANSITDATE))){
                continue;
            }
            //如果用户选择了时间范围，则先根据时间范围筛选
            boolean realSubmit = false;
            Long planSitDate = Long.valueOf(submitInfoUnit.get(Dict.PLANSITDATE));
            Long realSubmitTime = Util.isNullOrEmpty(submitInfoUnit.get(Dict.REALSITTIME))?
                    null:Long.valueOf(submitInfoUnit.get(Dict.REALSITTIME));
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            Long now = Long.valueOf(simpleDateFormat.format(date));
            //如果计划提测晚于今天而且实际还没提测，则不统计
            if(planSitDate>=now&&Util.isNullOrEmpty(realSubmitTime)){
                continue;
            }
            if(!Util.isNullOrEmpty(startDate)&&!Util.isNullOrEmpty(endDate)){
                Long start = Long.valueOf(startDate.replace("-", ""));
                Long end = Long.valueOf(endDate.replace("-", ""));
                boolean planSitTime = planSitDate>=start && planSitDate<=end;//计划sit时间在选择范围内
                if(!Util.isNullOrEmpty(submitInfoUnit.get(Dict.REALSITTIME))){
                    realSubmit = realSubmitTime>=start && realSubmitTime<=end;//实际提测时间在选择范围内
                }
                boolean shouldCollect = planSitTime || realSubmit;
                if(!shouldCollect){
                    continue;
                }
            }
            //获取组id
            String fdevGroupId = submitInfoUnit.get(Dict.FDEVGROUPID);
            if(Util.isNullOrEmpty(result.get(fdevGroupId))){
                result.put(fdevGroupId, new HashMap<>());
            }
            //提测总数+1
            if(Util.isNullOrEmpty(result.get(fdevGroupId).get(Dict.TOTAL))){
                result.get(fdevGroupId).put(Dict.TOTAL, 1L);
            }else{
                Long total = result.get(fdevGroupId).get(Dict.TOTAL);
                total++;
                result.get(fdevGroupId).put(Dict.TOTAL, total);
            }
            //提测准时数
            if(!Util.isNullOrEmpty(submitInfoUnit.get(Dict.REALSITTIME))){
                Long realSitTime = Long.valueOf(submitInfoUnit.get(Dict.REALSITTIME));
                if(realSitTime<=planSitDate){
                    if(Util.isNullOrEmpty(result.get(fdevGroupId).get(Dict.TIMELY))){
                        result.get(fdevGroupId).put(Dict.TIMELY, 1L);
                    }else{
                        Long timely = result.get(fdevGroupId).get(Dict.TIMELY);
                        timely++;
                        result.get(fdevGroupId).put(Dict.TIMELY, timely);
                    }
                }
            }
            //打回数
            if(!Util.isNullOrEmpty(submitInfoUnit.get(Dict.ROLLBACKTIME))){
                if(Util.isNullOrEmpty(result.get(fdevGroupId).get(Dict.ROLLBACKNUM))){
                    result.get(fdevGroupId).put(Dict.ROLLBACKNUM, 1L);
                }else{
                    Long rollBackNum = result.get(fdevGroupId).get(Dict.ROLLBACKNUM);
                    rollBackNum++;
                    result.get(fdevGroupId).put(Dict.ROLLBACKNUM, rollBackNum);
                }
                //冒烟测试不通过数
                if ("3".equals(submitInfoUnit.get(Dict.REASON))) {
                    if(Util.isNullOrEmpty(result.get(fdevGroupId).get(Dict.SMOKENOPASSNUM))){
                        result.get(fdevGroupId).put(Dict.SMOKENOPASSNUM, 1L);
                    }else{
                        Long rollBackNum = result.get(fdevGroupId).get(Dict.SMOKENOPASSNUM);
                        rollBackNum++;
                        result.get(fdevGroupId).put(Dict.SMOKENOPASSNUM, rollBackNum);
                    }
                }
            }
        }
        //exeTimeInfo
        for(Map<String, String> exeTimeInfoUnit : exeTimeInfo){
            //排除时间外的数据
            Long createTime = Long.valueOf(exeTimeInfoUnit.get(Dict.EXETIME));
            if(!Util.isNullOrEmpty(startDate)&&!Util.isNullOrEmpty(endDate)){
                Long start = Long.valueOf(startDate.replace("-", ""));
                Long end = Long.valueOf(endDate.replace("-", ""));
                if(createTime<start||createTime>end){
                    continue;
                }
            }
            //获取组id
            String fdevGroupId = exeTimeInfoUnit.get(Dict.FDEVGROUPID);
            if(Util.isNullOrEmpty(result.get(fdevGroupId))){
                result.put(fdevGroupId, new HashMap<>());
            }
            //案例执行数+1
            if(Util.isNullOrEmpty(result.get(fdevGroupId).get(Dict.EXETIME))){
                result.get(fdevGroupId).put(Dict.EXETIME, Long.valueOf(String.valueOf(exeTimeInfoUnit.get(Dict.TIMES))));
            }else{
                Long exeTime = result.get(fdevGroupId).get(Dict.EXETIME);
                exeTime += Long.valueOf(String.valueOf(exeTimeInfoUnit.get(Dict.TIMES)));
                result.get(fdevGroupId).put(Dict.EXETIME, exeTime);
            }
        }

        //mantisInfo
        for(Map<String, String> mantisInfoUnit : mantisInfo){
            //排除时间外的数据
            Long createTime = Long.valueOf(mantisInfoUnit.get(Dict.CREATETIME));
            Long closeTime = Long.valueOf(String.valueOf(mantisInfoUnit.get(Dict.CLOSETIME)));
            if(!Util.isNullOrEmpty(startDate)&&!Util.isNullOrEmpty(endDate)){
                Long start = Long.valueOf(startDate.replace("-", ""));
                Long end = Long.valueOf(endDate.replace("-", ""));
                boolean createMatch = createTime>=start&&createTime<=end;//true代表创建时间符合要求
                boolean closeMatch = closeTime>=start&&closeTime<=end;//true代表关闭时间符合要求
                boolean couldCollect = createMatch || closeMatch;
                if(!couldCollect){
                    continue;
                }
            }
            //获取组id
            String fdevGroupId = mantisInfoUnit.get(Dict.FDEVGROUPID);
            if(Util.isNullOrEmpty(result.get(fdevGroupId))){
                result.put(fdevGroupId, new HashMap<>());
            }
            //有效缺陷总数+1
            if(Util.isNullOrEmpty(result.get(fdevGroupId).get(Dict.SUMMANTIS))){
                result.get(fdevGroupId).put(Dict.SUMMANTIS, 1L);
            }else{
                Long sumMantis = result.get(fdevGroupId).get(Dict.SUMMANTIS);
                sumMantis++;
                result.get(fdevGroupId).put(Dict.SUMMANTIS, sumMantis);
            }
            //重开次数
            if(!Constants.NUMBER_1.equals(String.valueOf(mantisInfoUnit.get(Dict.OPENTIMES)))){
                if(Util.isNullOrEmpty(result.get(fdevGroupId).get(Dict.REOPENNUM))){
                    result.get(fdevGroupId).put(Dict.REOPENNUM, 1L);
                }else{
                    Long reopenNum = result.get(fdevGroupId).get(Dict.REOPENNUM);
                    reopenNum++;
                    result.get(fdevGroupId).put(Dict.REOPENNUM, reopenNum);
                }
            }

            //如果缺陷解决时长不为0,才统计时长和计数
            if(!Constants.NUMBER_0.equals(String.valueOf(mantisInfoUnit.get(Dict.SOLVETIME)))){
                //已解决缺陷数
                if(Util.isNullOrEmpty(result.get(fdevGroupId).get(Dict.NORMALNUM))){
                    result.get(fdevGroupId).put(Dict.NORMALNUM, 1L);
                }else{
                    Long normalNum = result.get(fdevGroupId).get(Dict.NORMALNUM);
                    normalNum++;
                    result.get(fdevGroupId).put(Dict.NORMALNUM, normalNum);
                }
                //缺陷解决时长
                String s = String.valueOf(mantisInfoUnit.get(Dict.SOLVETIME));
                if(s.contains(".")) {
                    s = s.substring(0, s.lastIndexOf("."));
                }
                if(Util.isNullOrEmpty(result.get(fdevGroupId).get(Dict.SOLVETIME))){
                    result.get(fdevGroupId).put(Dict.SOLVETIME, Long.valueOf(s));
                }else{
                    Long solveTime = result.get(fdevGroupId).get(Dict.SOLVETIME);
                    solveTime += Long.valueOf(s);
                    result.get(fdevGroupId).put(Dict.SOLVETIME, solveTime);
                }
                //严重缺陷解决数量和时长
                String severity = String.valueOf(mantisInfoUnit.get(Dict.SEVERITY));
                String priority = String.valueOf(mantisInfoUnit.get(Dict.PRIORITY));
                if(Constants.severeMantisCode.contains(severity)||Constants.priorityMantisCode.contains(priority)){
                    //严重缺陷数
                    if(Util.isNullOrEmpty(result.get(fdevGroupId).get(Dict.SEVERITY))){
                        result.get(fdevGroupId).put(Dict.SEVERITY, 1L);
                    }else{
                        Long severityNum = result.get(fdevGroupId).get(Dict.SEVERITY);
                        severityNum++;
                        result.get(fdevGroupId).put(Dict.SEVERITY, severityNum);
                    }
                    //严重缺陷解决时长
                    if(Util.isNullOrEmpty(result.get(fdevGroupId).get(Dict.SEVERESOLVETIME))){
                        result.get(fdevGroupId).put(Dict.SEVERESOLVETIME, Long.valueOf(s));
                    }else{
                        Long severeSolveTime = result.get(fdevGroupId).get(Dict.SEVERESOLVETIME);
                        severeSolveTime += Long.valueOf(s);
                        result.get(fdevGroupId).put(Dict.SEVERESOLVETIME, severeSolveTime);
                    }
                }
            }
        }
        //selfMergeInfo
//        for(Map<String, String> selfMergeInfoUnit : selfMergeInfo){
//            //排除时间外的数据
//            Long createTime = Long.valueOf(selfMergeInfoUnit.get(Dict.MERGED_AT));
//            if(!Util.isNullOrEmpty(startDate)&&!Util.isNullOrEmpty(endDate)){
//                Long start = Long.valueOf(startDate.replace("-", ""));
//                Long end = Long.valueOf(endDate.replace("-", ""));
//                if(createTime<start||createTime>end){
//                    continue;
//                }
//            }
//            //获取组id
//            String fdevGroupId = selfMergeInfoUnit.get(Dict.FDEVGROUPID);
//            if(Util.isNullOrEmpty(result.get(fdevGroupId))){
//                result.put(fdevGroupId, new HashMap<>());
//            }
//            //未经审核的代码合并
//            if(Util.isNullOrEmpty(result.get(fdevGroupId).get(Dict.SELF_MERGE))){
//                result.get(fdevGroupId).put(Dict.SELF_MERGE, 1L);
//            }else{
//                Long selfMerge = result.get(fdevGroupId).get(Dict.SELF_MERGE);
//                selfMerge++;
//                result.get(fdevGroupId).put(Dict.SELF_MERGE, selfMerge);
//            }
//        }
        return result;
    }

    @Override
    public Map<String, Object> qualityReportNewUnit(Map map, Map<String, Object> data) throws Exception {
        String fdevGroupId = String.valueOf(map.get(Dict.FDEVGROUPID));
        String isParent = String.valueOf(map.get(Dict.ISPARENT));
        String startDate = String.valueOf(map.getOrDefault(Dict.STARTDATE, ""));
        String endDate = String.valueOf(map.getOrDefault(Dict.ENDDATE, ""));
        List<String> fdevGroupIds = new ArrayList<>();
        fdevGroupIds.add(fdevGroupId);
        if(Constants.NUMBER_1.equals(isParent)){
            List<Map<String, String>> resultList = userService.queryChildGroupById(fdevGroupId);
            if(resultList.size()>1) {
                //如果有子组
                for (Map<String, String> result : resultList) {
                    fdevGroupIds.add(result.get(Dict.ID));
                }
            }
        }
        Map<String, Object> result = getQualityReportNewUnitData(data, startDate, endDate, fdevGroupIds);
        return result;
    }

    private Map<String, Object> getQualityReportNewUnitData(Map<String, Object> data, String startDate, String endDate, List<String> fdevGroupIds) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> submitInfo = (List<Map<String, String>>)data.get(Dict.SUBMITINFO);
        List<Map<String, String>> exeTimeInfo = (List<Map<String, String>>)data.get(Dict.EXETIMEINFO);
        List<Map<String, String>> mantisInfo = (List<Map<String, String>>)data.get(Dict.MANTISINFO);
        List<Map> submitRecord = new ArrayList<>();
        List<Map> rollBackRecord = new ArrayList<>();
        Integer timely = 0;
        Integer total = 0;
        Integer mantisSum = 0;
        Integer exeTimes = 0;
        for(Map<String, String> submitInfoUnit : submitInfo){
            //如果没有计划sit时间，忽略该条数据
            if(Util.isNullOrEmpty(submitInfoUnit.get(Dict.PLANSITDATE))){
                continue;
            }
            //如果用户选择了时间范围，则先根据时间范围筛选
            boolean realSubmit = false;
            Long planSitDate = Long.valueOf(submitInfoUnit.get(Dict.PLANSITDATE));
            Long realSubmitTime = Util.isNullOrEmpty(submitInfoUnit.get(Dict.REALSITTIME))?
                    null:Long.valueOf(submitInfoUnit.get(Dict.REALSITTIME));
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            Long now = Long.valueOf(simpleDateFormat.format(date));
            //如果计划提测晚于今天而且实际还没提测，则不统计
            if(planSitDate>=now&&Util.isNullOrEmpty(realSubmitTime)){
                continue;
            }
            if(!Util.isNullOrEmpty(startDate)&&!Util.isNullOrEmpty(endDate)){
                Long start = Long.valueOf(startDate.replace("-", ""));
                Long end = Long.valueOf(endDate.replace("-", ""));
                boolean planSitTime = planSitDate>=start && planSitDate<=end;//计划sit时间在选择范围内
                if(!Util.isNullOrEmpty(submitInfoUnit.get(Dict.REALSITTIME))){
                    realSubmit = realSubmitTime>=start && realSubmitTime<=end;//实际提测时间在选择范围内
                }
                boolean shouldCollect = planSitTime || realSubmit;
                if(!shouldCollect){
                    continue;
                }
            }

            //根据用户所选择查看详情的组来筛选
            String fdevGroupId = submitInfoUnit.get(Dict.FDEVGROUPID);
            if(!fdevGroupIds.contains(fdevGroupId)){
                continue;
            }
            //提测准时数
            if(!Util.isNullOrEmpty(submitInfoUnit.get(Dict.REALSITTIME))){
                Long realSitTime = Long.valueOf(submitInfoUnit.get(Dict.REALSITTIME));
                if(realSitTime<=planSitDate){
                    //准时
                    timely++;
                    submitInfoUnit.put(Dict.TIMELY, "准时");
                }else{
                    submitInfoUnit.put(Dict.TIMELY, "不准时");
                }
            }else{
                submitInfoUnit.put(Dict.TIMELY, "不准时");
            }
            total++;
            //处理日期格式
            submitInfoUnit.put(Dict.PLANSITDATE, myUtil.restoreDate(String.valueOf(submitInfoUnit.get(Dict.PLANSITDATE))));
            submitInfoUnit.put(Dict.REALSITTIME, myUtil.restoreDate(String.valueOf(submitInfoUnit.get(Dict.REALSITTIME))));
            submitRecord.add(submitInfoUnit);
            //打回数
            if(!Util.isNullOrEmpty(submitInfoUnit.get(Dict.ROLLBACKTIME))){
                String rollBackOpr = submitInfoUnit.get(Dict.ROLLBACKOPR);
                try {
                    submitInfoUnit.put(Dict.ROLLBACKOPR, changeToNameCn(rollBackOpr));
                } catch (Exception e) {
                    submitInfoUnit.put(Dict.ROLLBACKOPR, rollBackOpr);
                }
                rollBackRecord.add(submitInfoUnit);
            }
        }
        //缺陷数据
        List<Map> reopenRecord = new ArrayList<>();
        List<Map> solveTimeRecord = new ArrayList<>();
        List<Map> severeSolveTimeRecord = new ArrayList<>();
        for(Map<String, String> mantisInfoUnit : mantisInfo){
            //排除时间外的数据
            Long createTime = Long.valueOf(mantisInfoUnit.get(Dict.CREATETIME));
            Long closeTime = Long.valueOf(String.valueOf(mantisInfoUnit.get(Dict.CLOSETIME)));
            if(!Util.isNullOrEmpty(startDate)&&!Util.isNullOrEmpty(endDate)){
                Long start = Long.valueOf(startDate.replace("-", ""));
                Long end = Long.valueOf(endDate.replace("-", ""));
                boolean createMatch = createTime>=start&&createTime<=end;//true代表创建时间符合要求
                boolean closeMatch = closeTime>=start&&closeTime<=end;//true代表关闭时间符合要求
                boolean couldCollect = createMatch || closeMatch;
                if(!couldCollect){
                    continue;
                }
            }
            //根据用户所选择查看详情的组来筛选
            String fdevGroupId = mantisInfoUnit.get(Dict.FDEVGROUPID);
            if(!fdevGroupIds.contains(fdevGroupId)){
                continue;
            }
            try {
                mantisInfoUnit.put(Dict.FDEVGROUPID, String.valueOf(userService.queryGroupDetailById(fdevGroupId).get(Dict.NAME)));
            } catch (Exception e) {
                logger.error("fail to get group name cn");
            }
            if(!Constants.NUMBER_0.equals(String.valueOf(mantisInfoUnit.get(Dict.SOLVETIME)))){
                mantisInfoUnit.put(Dict.SOLVETIME,
                        floatToString1(Float.valueOf(String.valueOf(mantisInfoUnit.get(Dict.SOLVETIME)))/60/60/24));
                solveTimeRecord.add(mantisInfoUnit);
                String severity = String.valueOf(mantisInfoUnit.get(Dict.SEVERITY));
                String priority = String.valueOf(mantisInfoUnit.get(Dict.PRIORITY));
                if(Constants.severeMantisCode.contains(severity)||Constants.priorityMantisCode.contains(priority)){
                    severeSolveTimeRecord.add(mantisInfoUnit);
                }
            }
            //筛选重复打开过的缺陷
            if(!Util.isNullOrEmpty(mantisInfoUnit.get(Dict.OPENTIMES))&&Integer.valueOf(String.valueOf(mantisInfoUnit.get(Dict.OPENTIMES)))>1){
                reopenRecord.add(mantisInfoUnit);
            }
            mantisSum++;
        }
        //计算执行数
        for(Map<String, String> exeTimeInfoUnit : exeTimeInfo){
            //排除时间外的数据
            Long createTime = Long.valueOf(exeTimeInfoUnit.get(Dict.EXETIME));
            if(!Util.isNullOrEmpty(startDate)&&!Util.isNullOrEmpty(endDate)){
                Long start = Long.valueOf(startDate.replace("-", ""));
                Long end = Long.valueOf(endDate.replace("-", ""));
                if(createTime<start||createTime>end){
                    continue;
                }
            }
            //根据用户所选择查看详情的组来筛选
            String fdevGroupId = exeTimeInfoUnit.get(Dict.FDEVGROUPID);
            if(!fdevGroupIds.contains(fdevGroupId)){
                continue;
            }
            exeTimes++;
        }
        result.put(Dict.SUBMITINFO, submitRecord.stream().
                sorted(Comparator.comparing(this::sortPlanSit).reversed()).collect(Collectors.toList()));
        Map submitCount = new HashMap();
        submitCount.put(Dict.TIMELY, timely);
        submitCount.put(Dict.NOTTIMELY, total-timely);
        submitCount.put(Dict.TOTAL, total);
        result.put(Dict.SUBMITCOUNT, submitCount);
        result.put(Dict.ROLLBACKINFO, rollBackRecord.stream().
                sorted(Comparator.comparing(this::sortPlanSit).reversed()).collect(Collectors.toList()));
        result.put(Dict.REOPENISSUE, reopenRecord);
        result.put(Dict.SUMMANTIS, mantisSum);
        result.put(Dict.EXETIME, exeTimes);
        result.put(Dict.SOLVETIMERECORD, solveTimeRecord.stream().
                sorted(Comparator.comparing(this::sortSolveTime).reversed()).collect(Collectors.toList()));
        result.put(Dict.SEVERESOLVETIMERECORD, severeSolveTimeRecord.stream().
                sorted(Comparator.comparing(this::sortSolveTime).reversed()).collect(Collectors.toList()));
        return  result;
    }

    private Float sortSolveTime(Map map){
        return  Float.valueOf(String.valueOf(map.get(Dict.SOLVETIME)));
    }
    private Float sortPlanSit(Map map){
        return  Float.valueOf(String.valueOf(map.get(Dict.PLANSITDATE)).replace("-",""));
    }
}

