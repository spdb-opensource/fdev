package com.gotest.controller;

import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.domain.PlanList;
import com.gotest.domain.Testcase;
import com.gotest.domain.WorkOrder;
import com.gotest.service.NewFdevService;
import com.gotest.utils.CommonUtils;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import com.test.testmanagecommon.vaildate.RequestValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@RestController
@RequestMapping(value = "/fdev")
public class NewFdevController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NewFdevService newFdevService;

    /**
     * 新fdev创建工单
     *
     * @param requestMap
     * @return
     */
    @RequestMapping("/newCreateWorkOrder")
    @RequestValidate(NotEmptyFields = {Dict.USERID, Dict.LIST, Dict.DEMANDID, Dict.OAREALNO, Dict.OACONTACTNAME})
    public JsonResult newCreateWorkOrder(@RequestBody Map map) throws Exception {
        List<Map<String, Object>> list = (List<Map<String, Object>>) map.get(Dict.LIST);
        String id = (String) map.get(Dict.USERID);
        String demandId = (String) map.get(Dict.DEMANDID);
        String oaRealNo = (String) map.get(Dict.OAREALNO);
        String oaContactName = (String) map.get(Dict.OACONTACTNAME);
        String paramError = checkDataNotNull(list);
        if (!CommonUtils.isNullOrEmpty(paramError)) {
            logger.error("param empty error");
            throw new FtmsException(ErrorConstants.PARAM_ERROR, new String[]{paramError});
        }
        List<Map> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean error = false;
        List<Map> update = new ArrayList<>();
        for (Map<String, Object> requestMap : list) {
            Map updateOrderNoForUnit = new HashMap();
            String unitId = (String) requestMap.get(Dict.UNITID);//实施单元id
            String unitName = (String) requestMap.get(Dict.IMPLCONTENT);//实施单元内容
            String groupId = (String) requestMap.get(Dict.GROUPID);//组id
            String sitStart = (String) requestMap.get(Dict.INTERNALTESTSTART);//计划sit时间
            String sitEnd = (String) requestMap.get(Dict.INTERNALTESTEND);//计划uat时间
            String proDate = (String) requestMap.get(Dict.EXPECTEDPRODUCTDATE);//计划投产时间
            String remark = (String) requestMap.get(Dict.REQUIREREMARK);//备注
            updateOrderNoForUnit.put(Dict.UNITID, unitId);
            updateOrderNoForUnit.put(Dict.GROUPID, groupId);
            updateOrderNoForUnit.put(Dict.INTERNALTESTSTART, sitStart);
            String resultInfo = newFdevService.newCreateWorkOrder(unitId, groupId, unitName, sitStart, sitEnd, proDate, remark, id,
                    demandId, oaRealNo, oaContactName);
            if (!resultInfo.startsWith("创建工单失败")) {
                requestMap.put(Dict.WORKNO, resultInfo);
                result.add(requestMap);
                updateOrderNoForUnit.put("workNo", resultInfo);
                update.add(updateOrderNoForUnit);
            } else {
                error = true;
                sb.append(resultInfo + "/");
                if (resultInfo.contains("已存在工单")) {
                    updateOrderNoForUnit.put("workNo", resultInfo.substring(resultInfo.indexOf("已存在工单") + 5));
                    update.add(updateOrderNoForUnit);
                }
            }
        }
        //更新实施单元工单号，包括新建的和报已存在的
        newFdevService.updateOrderNum(update);
        if (error)
            return JsonResultUtil.buildError(ErrorConstants.DATA_INSERT_ERROR, sb.toString());
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 检查必输字段
     *
     * @param list
     * @throws Exception
     */
    private String checkDataNotNull(List<Map<String, Object>> list) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> map : list) {
            StringBuilder sb1 = new StringBuilder();
            sb1.append("实施单元id为" + map.get(Dict.UNITID) + "的请求数据的");
            boolean flag = false;
            for (String key : map.keySet()) {
                if (Dict.EXPECTEDPRODUCTDATE.equals(key) || Dict.REQUIREREMARK.equals(key) || Dict.INTERNALTESTEND.equals(key))
                    continue;
                if (CommonUtils.isNullOrEmpty(map.get(key))) {
                    flag = true;
                    sb1.append(key + "/");
                }
            }
            sb1.append("字段值为空;");
            if (flag)
                sb.append(sb1);
        }
        return sb.toString();
    }

    /**
     * 重构fdev新增任务
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    @RequestMapping("/newCreateTask")
    @RequestValidate(NotBothEmptyFields = {Dict.TASKNO, Dict.UNITID, Dict.TASKNAME})
    public JsonResult newCreateTask(@RequestBody Map requestMap) throws Exception {
        String taskNo = (String) requestMap.get(Dict.TASKNO);//任务id
        String unitId = (String) requestMap.get(Dict.UNITID);//实施单元id
        String taskName = (String) requestMap.get(Dict.TASKNAME);//任务名
        requestMap.put(Dict.WORKNO, newFdevService.newCreateTask(taskNo, unitId, taskName));
        return JsonResultUtil.buildSuccess(requestMap);
    }


    /**
     * 根据测试计划查询测试案例集合
     */
    @RequestMapping("/queryTestcaseByPlanId")
    @RequestValidate(NotEmptyFields = {Dict.PLANID})
    public JsonResult queryTestcaseByPlanId(@RequestBody Map map) throws Exception {
        //测试计划
        Integer planId = Integer.valueOf(map.get(Dict.PLANID).toString());
        Integer index = Integer.parseInt(map.get("index").toString());
        Integer pageSize = Integer.parseInt(map.get("pageSize").toString());
        //根据测试计划查询测试用例
        List<Map> mapList = newFdevService.queryTestcaseByPlanId(planId, index, pageSize);
        return JsonResultUtil.buildSuccess(mapList);
    }

    /**
     * 根据testcaseNo查询单条案例的详细信息
     */
    @RequestMapping("/querytestCaseByTestCaseNo")
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENO})
    public JsonResult querytestCaseByTestCaseNo(@RequestBody Map map) throws Exception {
        String testcaseNo = (String) map.get(Dict.TESTCASENO);
        String planId = (String) map.get(Dict.PLANID);
        if (Util.isNullOrEmpty(testcaseNo)) {
            throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{" 案例编号 "});
        }
        Testcase testcase = null;
        try {
            testcase = newFdevService.queryDetailByTestcaseNo(testcaseNo,planId);
            if (testcase != null && !Util.isNullOrEmpty(testcase.getTestcaseFuncId())) {
                testcase = newFdevService.queryCaseByTestcaseNo(testcaseNo,planId);
            }
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return JsonResultUtil.buildSuccess(testcase);
    }

    /**
     * 根据实施单元id列表批量查询测试计划
     */
    @RequestMapping("/queryPlanByUnitNo")
    public JsonResult queryPlanByUnitNo(@RequestBody Map<String, Object> map) throws Exception {
        //实施单元id
        List unitNos = (List) map.get(Dict.UNITNO);
        if (CommonUtils.isNullOrEmpty(unitNos)) {
            return null;
        }
        Integer index = Integer.parseInt(map.get("index").toString());
        Integer pageSize = Integer.parseInt(map.get("pageSize").toString());
        //需求编号查询对应的工单
        Map<String, Object> map1 = newFdevService.queryPlanByUnitNo(unitNos, index, pageSize);
        return JsonResultUtil.buildSuccess(map1);
    }

    /**
     * 根据实施单元id集合批量查询工单
     */
    @RequestMapping("/queryWorkNoByUnitNos")
    @RequestValidate(NotEmptyFields = {Dict.UNITNO})
    public JsonResult queryWorkNoByUnitNos(@RequestBody Map<String, Object> map) throws Exception {
        //实施单元id
        List unitNos = (List) map.get(Dict.UNITNO);
        //需求编号查询对应的工单
        List<WorkOrder> workNos = newFdevService.queryWorkNoByUnitNos(unitNos);
        List<String> stringList = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(workNos)) {
            stringList = workNos.stream().map(e -> e.getWorkOrderNo()).collect(Collectors.toList());
        }
        return JsonResultUtil.buildSuccess(stringList);
    }

    /**
     * 根据PlanId查询单条数据
     *
     * @param map
     * @return 返回一个条数据
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID})
    @PostMapping(value = "/queryByPlanId")
    public JsonResult queryByPlanId(@RequestBody Map map) throws Exception {
        Integer planId = (Integer) map.get(Dict.PLANID);
        PlanList planList = new PlanList();
        try {
            planList = newFdevService.queryByPlanId(planId);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return JsonResultUtil.buildSuccess(planList);
    }

    /**
     * 根据实施单元ids废弃玉衡工单
     *
     * @param map
     * @return 返回响应数量
     */
    @PostMapping(value = "/deleteWorkOrderByUnitNo")
    @RequestValidate(NotEmptyFields = {Dict.UNITNO})
    public JsonResult deleteWorkOrderByUnitNo(@RequestBody Map map) throws Exception {
        List unitNo = (List) map.get(Dict.UNITNO);//获取实施单元id集合
        return JsonResultUtil.buildSuccess(newFdevService.deleteWorkOrderByUnitNo(unitNo));
    }

    /**
     * 根据任务ids删减玉衡任务集
     *
     * @param map
     * @return 返回响应数量
     */
    @PostMapping(value = "/deleteTask")
    @RequestValidate(NotEmptyFields = {Dict.TASKIDS})
    public JsonResult deleteTask(@RequestBody Map map) throws Exception {
        List<String> tasks = (List<String>) map.get(Dict.TASKIDS);//获取任务id集合
        return JsonResultUtil.buildSuccess(newFdevService.deleteTask(tasks));
    }

    /**
     * 任务变更实施单元
     *
     * @param map
     * @return
     */
    @PostMapping(value = "/updateTaskUnitNo")
    @RequestValidate(NotEmptyFields = {Dict.TASKNO, Dict.TASKNAME, Dict.UNITNO})
    public JsonResult updateTaskUnitNo(@RequestBody Map map) throws Exception {
        String taskNo = (String) map.get(Dict.TASKNO);
        String taskName = (String) map.get(Dict.TASKNAME);
        String unitNo = (String) map.get(Dict.UNITNO);
        return JsonResultUtil.buildSuccess(newFdevService.updateTaskUnitNo(taskNo, taskName, unitNo));
    }
    /**
     * 任务id查询任务
     *
     * @param map
     * @return
     */
    @PostMapping(value = "/queryFdevTaskDetail")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryFdevTaskDetail(@RequestBody Map map) throws Exception {
        String taskNo=(String) map.get(Dict.ID);
        return JsonResultUtil.buildSuccess(newFdevService.queryFdevTaskDetail(taskNo));
    }

    /**
     * 更新工单名
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateOrderName")
    @RequestValidate(NotEmptyFields = {Dict.WORKNO, Dict.NAME})
    public JsonResult updateOrderName(@RequestBody Map map) throws Exception {
        String workNo = (String) map.get(Dict.WORKNO);
        String name = (String) map.get(Dict.NAME);
        return JsonResultUtil.buildSuccess(newFdevService.updateOrderName(workNo, name));
    }

    /**
     * 同步实时单元更新工单名
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/synIpmpUpdateOrderName")
    @RequestValidate(NotEmptyFields = {Dict.UNITNO, Dict.NAME})
    public JsonResult synIpmpUpdateOrderName(@RequestBody Map map) throws Exception {
        String unitNo = (String) map.get(Dict.UNITNO);
        String name = (String) map.get(Dict.NAME);
        return JsonResultUtil.buildSuccess(newFdevService.synIpmpUpdateOrderName(unitNo, name));
    }


    /**
     * 同步需求更新工单信息
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/synDemandUpdateOrderInfo")
    @RequestValidate(NotEmptyFields = {Dict.DEMANDID, Dict.NAME})
    public JsonResult synDemandUpdateOrderInfo(@RequestBody Map map) throws Exception {
        String demandId = (String) map.get(Dict.DEMANDID);
        String name = (String) map.get(Dict.NAME);
        return JsonResultUtil.buildSuccess(newFdevService.synDemandUpdateOrderInfo(demandId, name));
    }
}
