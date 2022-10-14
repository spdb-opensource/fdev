package com.manager.ftms.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.manager.ftms.service.*;
import com.manager.ftms.util.*;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.manager.ftms.entity.PlanlistTestcaseRelation;
import com.manager.ftms.entity.Testcase;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import com.test.testmanagecommon.vaildate.RequestValidate;

@RestController
@RequestMapping("/testcase")
public class TestCaseController {

	@Autowired
	private TestCaseService testCaseService;
	private static final Logger logger = LoggerFactory.getLogger(TestCaseController.class);
	@Autowired
	private IPlanlistTestcaseRelationServier planlistTestcaseRelationServier;
	@Autowired
	private ExportExcelService exportExcelService;
	@Autowired
	private IUserService userCheckService;
	@Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private Utils utils;
	@Autowired
	private ISendEmailService sendEmailService;
	@Autowired
	private IMantisService mantisService;
	@Autowired
    private TestcaseUtils testcaseUtils;
	@Autowired
	private RedisUtils redisUtils;



    /**
     * 查询菜单下案例 无菜单时查询所有生效案例（案例库查询）
     *
     * @param map
     * @return Testcase
     */
    @RequestMapping("/queryTestcaseByFuncId")
    public JsonResult queryTestcaseByFuncId(@RequestBody Map map) throws Exception {
        // 根据菜单id查询生效案例
        List<Testcase> list = testCaseService.queryTestcaseByFuncId(map);
        return JsonResultUtil.buildSuccess(list);
    }

    /**
     * 查询计划下的案例列表
     *
     * @param map
     * @return Testcase
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID})
    @RequestMapping("/queryTestcaseByPlanId")
    public JsonResult queryTestcaseByPlanId(@RequestBody Map map) throws Exception {
        // 根据计划id 关联查询案例表和关系表 查询案例信息列表
        List list;
        try {
            // 调用分页接口
            list = this.testCaseService.queryTestByPlanId(map);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
        }
        return JsonResultUtil.buildSuccess(list);
    }

    /**
     * 查询计划下的案例列表
     *
     * @param map
     * @return Testcase
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID})
    @RequestMapping("/queryAllTestcaseByPlanId")
    public JsonResult queryAllTestcaseByPlanId(@RequestBody Map map) throws Exception {
        // 根据计划id 关联查询案例表和关系表 查询案例信息列表
        List list;
        try {
            list = this.testCaseService.queryTestcaseByPlanId(map);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
        }
        return JsonResultUtil.buildSuccess(list);
    }

    /**
     * 查询计划下的案例列表总数
     *
     * @param map
     * @return Testcase
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID})
    @RequestMapping("/queryTestCount")
    public JsonResult queryTestCount(@RequestBody Map map) throws Exception {
        // 根据计划id 关联查询案例表和关系表 查询案例信息列表
        Integer count;
        Integer planId = (Integer) map.get(Dict.PLANID);
        String testcaseStatus = String.valueOf(map.getOrDefault(Dict.TESTCASESTATUS, ""));//案例审核状态
        String testcaseNature = String.valueOf(map.getOrDefault(Dict.TESTCASENATURE, ""));//案例性质（正反）
        String testcaseType = String.valueOf(map.getOrDefault(Dict.TESTCASETYPE, ""));//案例类型
        String testcaseExecuteResult = String.valueOf(map.getOrDefault(Dict.TESTCASEEXECUTERESULT, ""));//案例执行结果
        String planlistTestcaseId = String.valueOf(map.getOrDefault(Dict.PLANLISTTESTCASEID, "")).trim();//关系id
        String testcaseName = String.valueOf(map.getOrDefault(Dict.TESTCASENAME,"")).trim();//测试案例名称
        try {
            count = this.testCaseService.queryTestCount(planId, testcaseStatus, testcaseNature, testcaseType,
                    testcaseExecuteResult, testcaseName, planlistTestcaseId);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
        }
        return JsonResultUtil.buildSuccess(count);
    }

    /**
     * 新增单条案例
     *
     * @param map 案例实体 系统id 计划id
     * @return int num 受影响行数
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID, Dict.SYSID, Dict.TESTCASE, Dict.WORKNO})
    @RequestMapping("/addTestcase")
    public JsonResult addTestcase(@RequestBody Map map) throws Exception {
        // 获取testcase对象和计划id planId
        // 先添加案例 再添加关系表数据
        Testcase testcase = testCaseService.addTestcase(map);
        String testcaseNo = testcase.getTestcaseNo();
        int planId = Integer.valueOf(map.get(Dict.PLANID).toString());
        String workNo = map.get(Dict.WORKNO).toString();
        Map paramMap = new HashMap();
        paramMap.put(Dict.TESTCASENO, testcaseNo);
        paramMap.put(Dict.PLANID, planId);
        paramMap.put(Dict.WORKNO, workNo);
        paramMap.put(Dict.TESTCASEEXECUTERESULT, "0");
        paramMap.put(Dict.CREATEOPR,testcase.getTestcasePeople());
        // 当前时间戳
        SimpleDateFormat sdf = new SimpleDateFormat(Utils.FULL_TIME);
        String createTm = sdf.format(new Date());
        paramMap.put(Dict.CREATETM, createTm);
        int num2 = 0;
        try {
            num2 = planlistTestcaseRelationServier.addPlanlistTsetcaseRelation(paramMap);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
        }
        if (testcase != null && num2 > 0) {
            return JsonResultUtil.buildSuccess("新增案例信息成功!");
        }
        return JsonResultUtil.buildError(ErrorConstants.ADD_ERROR, "新增案例信息失败!");
    }

    /**
     * 根据testcaseNo查询单条案例的详细信息
     *
     * @param map
     * @return Testcase
     */
    @RequestMapping("/queryDetailByTestcaseNo")
    public JsonResult queryDetailByTestcaseNo(@RequestBody Map map) throws Exception {
        String testcaseNo = (String) map.get(Dict.TESTCASENO);
        if (Util.isNullOrEmpty(testcaseNo)) {
            throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{" 案例编号 "});
        }
        Testcase testcase = null;
        try {
            testcase = testCaseService.queryDetailByTestcaseNo(testcaseNo);
            if (!Util.isNullOrEmpty(testcase.getTestcaseFuncId())) {
                testcase = testCaseService.queryCaseByTestcaseNo(testcaseNo);
            }
        } catch (Exception e) {
            logger.error("e:" + e);
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
        }
        return JsonResultUtil.buildSuccess(testcase);
    }

    /**
     * 编辑案例信息
     *
     * @param map
     * @return Testcase
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANLISTTESTCASEID, Dict.TESTCASE, Dict.PLANID, Dict.WORKNO})
    @RequestMapping("/updateTestcaseByTestcaseNo")
    public JsonResult updateTestcaseByTestcaseNo(@RequestBody Map map) throws Exception {
        String userName = utils.getCurrentUserEnName();
        // 关系表中计划id
        String planlistTestcaseId = String.valueOf(map.get(Dict.PLANLISTTESTCASEID));
        // 检查案例是否已执行过，执行过则报错
        testcaseUtils.checkCaseExe(planlistTestcaseId);
        // 获取testcase对象和计划id planId
        Map map1 = (Map) map.get(Dict.TESTCASE);
        map1.put(Dict.SYSID, map.get(Dict.SYSID));
        String testcaseNo = (String) map1.get(Dict.TESTCASENO);
        int planId = (Integer) map.get(Dict.PLANID);
        String workNo = (String) map.get(Dict.WORKNO);
        int count;
        try {
            count = planlistTestcaseRelationServier.queryCountByTestcaseNo(testcaseNo);
        } catch (Exception e1) {
            logger.error(e1.toString());
            throw new FtmsException(ErrorConstants.SELECT_DATA_EXCEPTION);
        }
        // 先查询是否在其他计划中也存在 若存在则新增一个案例实体，将原关系数据中的案例编号改为新的； 若不存在直接修改案例实体
        if (count > 1) {
            // 存在 新增案例并修改关系
            try {
                Testcase testCase = testCaseService.addTestcase(map);
                String newTestcaseNo = testCase.getTestcaseNo();
                planlistTestcaseRelationServier.updateTestcaseByPlanlistTestcaseId(newTestcaseNo, planlistTestcaseId);
                planlistTestcaseRelationServier.addRelationModifyRecord(planlistTestcaseId);
            } catch (Exception e) {
                logger.error(e.toString());
                throw new FtmsException(ErrorConstants.UPDATE_ERROR, new String[]{",案例编辑失败!"});
            }
            return JsonResultUtil.buildSuccess();
        } else {
            try {
                int t = testCaseService.updateTestcaseByTestcaseNo((Map) map.get(Dict.TESTCASE), planlistTestcaseId);
                planlistTestcaseRelationServier.addRelationModifyRecord(planlistTestcaseId);
                return JsonResultUtil.buildSuccess();
            } catch (Exception e) {
                logger.error(e.toString());
                throw new FtmsException(ErrorConstants.UPDATE_ERROR, new String[]{",案例编辑失败!"});
            }
        }
    }

    /**
     * 删除单条案例 如果只存在一个计划下,删除关系和案例;如果存在多个计划下,只删除当前关系;生效案例不能删除,只删除挂载关系
     *
     * @param map
     * @return int
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANLISTTESTCASEID, Dict.PLANID, Dict.TESTCASENO})
    @RequestMapping("/deleteTestcaseByTestcaseNo")
    public JsonResult deleteTestcaseByTestcaseNo(@RequestBody Map map) throws Exception {
        String testcaseNo = (String) map.get(Dict.TESTCASENO);
        String planlistTestcaseId = String.valueOf(map.get(Dict.PLANLISTTESTCASEID));
        int planId = (Integer) map.get(Dict.PLANID);
        Testcase queryCase = testCaseService.queryDetailByTestcaseNo(testcaseNo);
        if (Utils.isEmpty(queryCase)) {
            logger.error("案例不存在!");
            throw new FtmsException(ErrorConstants.SELECT_DATA_EXCEPTION);
        }
        //查询当前案例关系下是否存在缺陷，若有缺陷则不允许删除
        List<Map> issues = mantisService.queryIssuesByFprId(planlistTestcaseId);
        if(!Util.isNullOrEmpty(issues) && issues.size() != 0){
            throw new FtmsException(ErrorConstants.DELETE_CASE_INCLUDE_ISSUE_ERROR);
        }
        // 查询是否在多计划下
        int count = planlistTestcaseRelationServier.queryCountByTestcaseNo(testcaseNo);
        if (1 < count) {// 存在多计划下,只删除当前挂载关系
            int delNum = planlistTestcaseRelationServier.deletePlanlistTsetcaseRelationById(planlistTestcaseId);
            if (1 > delNum) {
                logger.error("案例关系删除失败!");
                throw new FtmsException(ErrorConstants.DATA_DEL_ERROR);
            }
            return JsonResultUtil.buildSuccess(delNum);
        } else {// 只存在一个计划下,先删除关系,后删除案例
            //查询当前案例关系下是否存在缺陷，若有缺陷则不允许删除
            int delNum = planlistTestcaseRelationServier.deletePlanlistTsetcaseRelationById(planlistTestcaseId);
            if (1 > delNum) {
                logger.error("案例关系删除失败!");
                throw new FtmsException(ErrorConstants.DATA_DEL_ERROR);
            }
            // 生效,通用案例不能删除(2020/7/13案例逻辑删除改造，案例本体永不删除)
//            if (!Constants.TESTCASE_EFFECT_THROUGH.equals(queryCase.getTestcaseStatus())
//                    && !Constants.TESTCASE_EFFECT_GENERAL.equals(queryCase.getTestcaseStatus())) {
//                int delCase = testCaseService.deleteTestcaseByTestcaseNo(testcaseNo);
//                if (1 > delCase) {
//                    logger.error("案例删除失败!");
//                    throw new FtmsException(ErrorConstants.DATA_DEL_ERROR);
//                }
//                return JsonResultUtil.buildSuccess(delCase);
//            }
            return JsonResultUtil.buildSuccess(delNum);
        }
    }

    /**
     * 根据testcaseNo 提交审批
     *
     * @param map
     * @return int
     */
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENO})
    @RequestMapping("/updateTestcaseByStatusWaitPass")
    public JsonResult updateTestcaseByStatusWaitPass(@RequestBody Map map) {
        String testcaseNo = map.get(Dict.TESTCASENO).toString();
        // 根据案例编号查询案例状态
        Testcase testcase;
        try {
            testcase = testCaseService.queryDetailByTestcaseNo(testcaseNo);
        } catch (Exception e) {
            logger.error("e:" + e);
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
        }
        String status = testcase.getTestcaseStatus();
        if (status.equals(Constants.TESTCASE_NEW) || status.equals(Constants.TESTCASE_AUDITING_REJECT)) {
            // 状态为0即新建案例状态下 和审核拒绝12下案例 才能提交审核 提交审核后状态为待审核状态10
            try {
                int num = testCaseService.updateTestcaseByStatusWaitPass(testcaseNo);
            } catch (Exception e) {
                throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
            }
            return JsonResultUtil.buildSuccess();
        } else {
            return JsonResultUtil.buildError(ErrorConstants.STATUS_ERROR, "该状态下案例不能提交审核!");
        }
    }

    /**
     * 根据testcaseNo 提交生效
     *
     * @param map
     * @return int
     */
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENO})
    @RequestMapping("/updateTestcaseByStatusWaitEffect")
    public JsonResult updateTestcaseByStatusWaitEffect(@RequestBody Map map) {
        String testcaseNo = map.get(Dict.TESTCASENO).toString();
        // 根据案例编号查询案例状态
        Testcase testcase;
        try {
            testcase = testCaseService.queryDetailByTestcaseNo(testcaseNo);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
        }
        String status = testcase.getTestcaseStatus();
        if (status.equals(Constants.TESTCASE_AUDITING_THROUGH) || status.equals(Constants.TESTCASE_EFFECT_REJECT)) {
            // 状态为11即审核通过下案例 和生效拒绝22下案例 才能提交审核 提交审核后状态为待生效状态20
            try {
                int num = testCaseService.updateTestcaseByStatusWaitEffect(testcaseNo);
            } catch (Exception e) {
                throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
            }
            return JsonResultUtil.buildSuccess();
        } else {
            return JsonResultUtil.buildError(ErrorConstants.STATUS_ERROR, "该状态下案例不能提交审核!");
        }
    }

    // 复制功能 复制(相当于手机图片复制到的功能
    // 当前案例复制到其他计划下面 点击复制时先展示详细信息并且值不能修改)

    /**
     * 根据计划id 和 案例编号新增关系
     *
     * @param map 计划id 和 案例编号
     * @return int
     */
    @RequestValidate(NotEmptyFields = {Dict.ORDERPLANID, Dict.TARGETPLANID, Dict.WORKNO, Dict.TESTCASE})
    @RequestMapping("/copyTestcaseToOtherPlan")
    public JsonResult copyTestcaseToOtherPlan(@RequestBody Map map) throws Exception {
        Integer orderPlanId = (Integer) (map.getOrDefault(Dict.ORDERPLANID, 0));
        Integer targetPlanId = (Integer) (map.getOrDefault(Dict.TARGETPLANID, 0));
        String orderWorkNo = (String) map.getOrDefault(Dict.WORKNO, "");
        Map tMap = (Map) map.get(Dict.TESTCASE);
        String testcaseNo = (String) tMap.getOrDefault(Dict.TESTCASENO, "");
        Map paramMap = new HashMap();
        // 新增关系 如果当前id和目的id一致进行新增案例操作 不一致新增关系
        Map addMap = new HashMap();
        if (orderPlanId.equals(targetPlanId)) {
            Testcase testcase;
            try {// 案例实体 系统id 计划id
                testcase = testCaseService.addTestcase(map);
            } catch (Exception e) {
                throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
            }
            testcaseNo = testcase.getTestcaseNo();
            targetPlanId = orderPlanId;
        }
        int count = planlistTestcaseRelationServier.queryCountByPlanIdandTestcaseNo(testcaseNo, targetPlanId);
        if (count > 0) {
            throw new FtmsException("该计划中已经存在此条案例!");
        }
        paramMap.put("planId", targetPlanId);
        paramMap.put("workNo", orderWorkNo);
        paramMap.put(Dict.TESTCASENO, testcaseNo);
        paramMap.put(Dict.TESTCASEEXECUTERESULT, "0");
        // 当前时间戳
        SimpleDateFormat sdf = new SimpleDateFormat(Utils.FULL_TIME);
        String createTm = sdf.format(new Date());
        paramMap.put("createTm", createTm);
        int num;
        try {
            num = planlistTestcaseRelationServier.addPlanlistTsetcaseRelation(paramMap);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
        }
        if (num > 0) {
            return JsonResultUtil.buildSuccess(paramMap);
        }
        return JsonResultUtil.buildError(ErrorConstants.STATUS_ERROR, "参数传递异常!");
    }

    /*
     * *********************************************************案例执行模块
     *******************************************************/

    /**
     * 模糊匹配查询案例
     *
     * @param map
     * @return
     */
    @RequestMapping("/selectTestCaseFuzzy")
    public JsonResult selectTestCaseFuzzy(@RequestBody Map map) throws Exception {
        List<Testcase> list;
        try {
            list = this.testCaseService.selectTestCaseFuzzy((String) map.get(Dict.PARAMETERS));
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.SELECT_DATA_EXCEPTION);
        }

        return JsonResultUtil.buildSuccess(list);
    }

    /**
     * 根据计划id删除计划下面的案例关系
     *
     * @param map
     * @return
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID})
    @RequestMapping(value = "/delPlanlistTestcaseIdByPlanId", method = RequestMethod.POST)
    public JsonResult delPlanlistTestcaseIdByPlanId(@RequestBody Map map) throws Exception {
        int row = -1;
        try {
            row = this.planlistTestcaseRelationServier.deleteByPlanId(((Integer) map.get(Dict.PLANID)));
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.DATA_DEL_ERROR);
        }
        return JsonResultUtil.buildSuccess(row);
    }

    /**
     * 查询计划下面的案例关系
     *
     * @param map
     * @return
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID})
    @RequestMapping("/queryPlanTestCaseStatusAll")
    public JsonResult queryPlanTestCaseStatusAll(@RequestBody Map map) throws FtmsException {
        List<PlanlistTestcaseRelation> list;
        try {
            list = this.planlistTestcaseRelationServier.selectByPlanId(map);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.SELECT_DATA_EXCEPTION);
        }
        return JsonResultUtil.buildSuccess(list);

    }

    /**
     * 根据计划id 查询当前计划下面的所有案例（一）
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID})
    @RequestMapping("/queryByPlanIdAll")
    public JsonResult queryByPlanIdAll(@RequestBody Map map) throws Exception {
        List<Testcase> list;
        try {
            Integer planId = (Integer) map.get(Dict.PLANID);
            list = this.testCaseService.queryTestcaseByPlanIdOrStatus(planId);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.SELECT_DATA_EXCEPTION);
        }
        return JsonResultUtil.buildSuccess(list);
    }

    /**
     * 根据案例编号查询( 二 )
     *
     * @param map
     * @return
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID, Dict.TESTCASENO})
    @RequestMapping("/testCaseDetailByTestcaseNoAndplanId")
    public JsonResult testCaseDetailByTestcaseNo(@RequestBody Map map) throws Exception {
        List<Testcase> list;
        try {
            list = new ArrayList<>();
            Integer planId = (Integer) map.get(Dict.PLANID);
            String testCaseNo = (String) map.get(Dict.TESTCASENO);
            list.add(this.testCaseService.queryDetailByTestcaseNoAndPlanId(testCaseNo, planId));
            return JsonResultUtil.buildSuccess(list);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
        }

    }

    /**
     * 根据案例计划关系表Id 修改案例状态 并给案例增加备注 ( 三 )
     *
     * @param
     * @return 返回受影响行数
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANLISTTESTCASEID, Dict.TESTCASENO, Dict.TESTCASEEXECUTERESULT})
    @RequestMapping(value = "/updatePlanlistTestcaseRelation")
    public JsonResult updatePlanlistTestcaseRelation(@RequestBody Map map) throws Exception {
        Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
        if(Util.isNullOrEmpty(user)){
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        String userEnName = (String)user.get(Dict.USER_NAME_EN);
        String id = String.valueOf(map.get(Dict.PLANLISTTESTCASEID));
        String testcaseExecuteResult = String.valueOf(map.get(Dict.TESTCASEEXECUTERESULT));
        String remark = (String)map.get(Dict.REMARK);
        int row = this.planlistTestcaseRelationServier.updateTestCaseExecuteStatus(id, testcaseExecuteResult, remark, userEnName);
        return JsonResultUtil.buildSuccess(row == 2 ? 1 : 0);
    }


    /**
     * 批量执行案例
     *
     * @param
     * @return 返回受影响行数
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.IDS, Dict.TESTCASEEXECUTERESULT})
    @RequestMapping(value = "/batchExecuteTestcase")
    public JsonResult batchExecuteTestcase(@RequestBody Map map) throws Exception {
        List<String> ids = (List<String>)map.get(Dict.IDS);
        String testcaseExecuteResult = (String)map.get(Dict.TESTCASEEXECUTERESULT);
        planlistTestcaseRelationServier.batchExecuteTestcase(ids, testcaseExecuteResult);
        return JsonResultUtil.buildSuccess(null);
    }


    /**************************************
     * 案例审核
     ************************************/

    /**
     * 案例审核,根据传入选项查询案例
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryTestcaseByOption")
    @ResponseBody
    public JsonResult queryTestcaseByOption(@RequestBody TestcaseAuditQueryObject requestParam) throws Exception {
        // 如果该用户包含master角色,查询全部审批案例,否则查询工单下的案例
        if (userCheckService.isAdmin()) {
            List<Testcase> list = testCaseService.queryTestcaseByOption(requestParam, null);
         Integer count=   testCaseService.queryTestcaseByOptionCount(requestParam,null);
            Map map=new HashMap();
            map.put("total",count);
            map.put("list",list);
            return JsonResultUtil.buildSuccess(map);
        } else {
            List<Testcase> list = testCaseService.queryTestcaseByOption(requestParam,
                    userCheckService.getUserEnName());
            Integer count=   testCaseService.queryTestcaseByOptionCount(requestParam, userCheckService.getUserEnName());
            Map map=new HashMap();
            map.put("total",count);
            map.put("list",list);
            return JsonResultUtil.buildSuccess(map);
        }

    }

    /**
     * 根据用户名和状态查询案例
     *
     * @param requestParam
     * @return
     */
    @RequestValidate(NotEmptyFields = {Dict.TESTCASESTATUS})
    @RequestMapping("/queryTestcaseByStatusAndUserEnName")
    @ResponseBody
    public JsonResult queryTestcaseByStatusAndUserEnName(@RequestBody Map<String, Object> requestParam)
            throws Exception {
        String testcaseStatus = (String) requestParam.get(Dict.TESTCASESTATUS);
        List<Testcase> testcases = this.testCaseService.queryTestcaseByUserEnName
                (userCheckService.getUserEnName(), testcaseStatus);
        return JsonResultUtil.buildSuccess(testcases);

    }

    // 案例同意生效
    @RequestMapping(value = "/agreeEffect", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENOS})
    @ResponseBody
    public JsonResult agreeEffect(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<String> list = updateCaseStatus(requestParam, Constants.TESTCASE_EFFECT, "该案例已生效通过,请勿重复进行生效审批", "案例生效失败!");
        if (!Utils.isEmpty(list)) {
            throw new FtmsException(ErrorConstants.EXIST_TESTCASE_APPROVAL_FAIL, new String[]{list.toString()});
        }
        return JsonResultUtil.buildSuccess(null);
    }

    // 案例拒绝生效
    @RequestMapping(value = "/rejectEffect", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENOS})
    @ResponseBody
    public JsonResult rejectEffect(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<String> list = updateCaseStatus(requestParam, Constants.TESTCASE_EFFECT, "该案例已生效拒绝,请勿重复进行生效审批", "案例拒绝生效失败!");
        if (!Utils.isEmpty(list)) {
            throw new FtmsException(ErrorConstants.EXIST_TESTCASE_APPROVAL_FAIL, new String[]{list.toString()});
        }
        return JsonResultUtil.buildSuccess(null);
    }

    // 修改生效案例变成通用
    @RequestMapping(value = "/updateToGeneralCase", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENOS})
    @ResponseBody
    public JsonResult agreeGeneral(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<String> list = updateCaseStatus(requestParam, Constants.TESTCASE_EFFECT_THROUGH, "该案例已通用,请勿重复进行通用审批", "案例通用失败!");
        if (!Utils.isEmpty(list)) {
            throw new FtmsException(ErrorConstants.EXIST_TESTCASE_APPROVAL_FAIL, new String[]{list.toString()});
        }
        return JsonResultUtil.buildSuccess(null);
    }

    // 修改生效案例变成废弃
    @RequestMapping(value = "/updateToDiscardCase", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENOS})
    @ResponseBody
    public JsonResult agreeDiscard(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<String> list = updateCaseStatus(requestParam, Constants.TESTCASE_EFFECT_THROUGH, "该案例已废弃,请勿重复进行废弃审批", "案例废弃失败!");
        if (!Utils.isEmpty(list)) {
            throw new FtmsException(ErrorConstants.EXIST_TESTCASE_APPROVAL_FAIL, new String[]{list.toString()});
        }
        return JsonResultUtil.buildSuccess(null);
    }

    // 案例同意通过
    @RequestMapping(value = "/agreeThrough", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENOS})
    @ResponseBody
    public JsonResult agreeThrough(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<String> list = updateCaseStatus(requestParam, Constants.TESTCASE_AUDITING, "该案例已评审成功,请勿重复进行评审审批", "案例评审失败!");
        if (!Utils.isEmpty(list)) {
            throw new FtmsException(ErrorConstants.EXIST_TESTCASE_APPROVAL_FAIL, new String[]{list.toString()});
        }
        return JsonResultUtil.buildSuccess(null);
    }

    // 案例拒绝通过
    @RequestMapping(value = "/rejectThrough", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENOS})
    @ResponseBody
    public JsonResult rejectThrough(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<String> list = updateCaseStatus(requestParam, Constants.TESTCASE_AUDITING, "该案例已评审拒绝,请勿重复进行评审审批", "案例拒绝失败!");
        if (!Utils.isEmpty(list)) {
            throw new FtmsException(ErrorConstants.EXIST_TESTCASE_APPROVAL_FAIL, new String[]{list.toString()});
        }
        return JsonResultUtil.buildSuccess(null);
    }

    /***************************** 案例复用 *********************************/
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENO, Dict.PLANID, Dict.WORKNO})
    @RequestMapping(value = "/repeatedTestcaseForPlans", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult repeatedTestcaseForPlans(@RequestBody Map<String, Object> requestParam) throws Exception {
        String testcaseNo = (String) requestParam.get(Dict.TESTCASENO);
        Integer planId = (Integer) requestParam.get(Dict.PLANID);
        String workNo = (String) requestParam.get(Dict.WORKNO);
        Testcase testcase = testCaseService.queryDetailByTestcaseNo(testcaseNo);
        String userName = utils.getCurrentUserEnName();
        if (Util.isNullOrEmpty(testcase)) {
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST_ERROR);
        }
        if (!Constants.TESTCASE_EFFECT_THROUGH.equals(testcase.getTestcaseStatus())) {
            throw new FtmsException(ErrorConstants.TESTCASE_CAN_NOT_USE);
        }
        PlanlistTestcaseRelation planlistTestcase = planlistTestcaseRelationServier
                .queryRelationByTestcaseNoAndPlanId(testcaseNo, planId);
        if (!Util.isNullOrEmpty(planlistTestcase)) {
            throw new FtmsException(ErrorConstants.DATA_IS_EXIST, new String[]{"该案例已复用,请勿重复添加"});
        }
        PlanlistTestcaseRelation pl = new PlanlistTestcaseRelation();
        pl.setPlanId(planId);
        pl.setTestcaseNo(testcaseNo);
        pl.setWorkNo(workNo);
        // 当前时间戳
        SimpleDateFormat sdf = new SimpleDateFormat(Utils.FULL_TIME);
        String createTm = sdf.format(new Date());
        pl.setCreateTm(createTm);
        pl.setTestcaseExecuteResult("0");
        pl.setExeNum(0);
        pl.setCreateOpr(userName);
        try {
            // 案例复用
            planlistTestcaseRelationServier.savePlanlistTsetcaseRelation(pl);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.UPDATE_ERROR, new String[]{"该案例复用失败!"});
        }
        return JsonResultUtil.buildSuccess("添加成功,该案例已复用");
    }

    // 批量复用
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENO, Dict.PLANID, Dict.WORKNO})
    @RequestMapping(value = "/batchRepeatedRelation", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult batchRepeatedRelation(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<Map<String, Object>> testcases = (List<Map<String, Object>>) requestParam.get(Dict.TESTCASENO);
        Integer planId = (Integer) requestParam.get(Dict.PLANID);
        String workNo = (String) requestParam.get(Dict.WORKNO);
        List<String> list = new ArrayList<>();
        for (Map<String, Object> map : testcases) {
            Testcase testcase = testCaseService.queryDetailByTestcaseNo((String) map.get(Dict.TESTCASENO));
            if (Util.isNullOrEmpty(testcase)) {
                throw new FtmsException(ErrorConstants.DATA_NOT_EXIST_ERROR);
            }
            if (!Constants.TESTCASE_EFFECT_THROUGH.equals(testcase.getTestcaseStatus())) {
                throw new FtmsException(ErrorConstants.TESTCASE_CAN_NOT_USE);
            }
            PlanlistTestcaseRelation planlistTestcaseRelation = planlistTestcaseRelationServier
                    .queryRelationByTestcaseNoAndPlanId((String) map.get(Dict.TESTCASENO), planId);
            if (!Util.isNullOrEmpty(planlistTestcaseRelation)) {
                continue;
            }
            list.add((String) map.get(Dict.TESTCASENO));
        }
        if (0 == list.size()) {
            throw new FtmsException(ErrorConstants.DATA_IS_EXIST, new String[]{",批量案例已在计划下,请勿重复添加"});
        }
        try {
            // 案例批量复用
            planlistTestcaseRelationServier.batchRepeatedRelation(list, planId, workNo);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new FtmsException(ErrorConstants.UPDATE_ERROR, new String[]{"案例批量复用失败!"});
        }
        return JsonResultUtil.buildSuccess("添加成功,案例已批量复用");
    }

    /**
     * 批量删除
     *
     * @return
     */
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENOS, Dict.PLANID})
    @RequestMapping("/delBatchRelationCase")
    public JsonResult delBatchRelationCase(@RequestBody Map map) throws Exception{
        this.planlistTestcaseRelationServier.delBatchRelationCase(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * funcation_point 功能点 testcase_func_id 功能模块 testcase_name 案例名 testcase_people
     * 案例编写人 计划Id planId
     *
     * @return
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID})
    @RequestMapping("/selTestCaseCondition")
    public JsonResult selTestCaseCondition(@RequestBody Map map) {
        List<Testcase> testcases;
        try {
            testcases = this.testCaseService.selTestCaseCondition(map);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.SELECT_DATA_EXCEPTION);
        }
        return JsonResultUtil.buildSuccess(testcases);
    }

    // 批量提交审核
    @RequestMapping(value = "/batchCommitAudit", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult batchCommitAudit(@RequestBody Map<String, Object> requestParam) throws Exception {
        batchCaseStatus(requestParam, Constants.TESTCASE_NEW, Constants.TESTCASE_AUDITING_REJECT, "存在状态不允许提交审批的案例!",
                "案例批量评审失败!");
        return JsonResultUtil.buildSuccess("案例批量评审成功!");
    }

    // 批量生效
    @RequestMapping(value = "/batchEffectAudit", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult batchEffectAudit(@RequestBody Map<String, Object> requestParam) throws Exception {
        batchCaseStatus(requestParam, Constants.TESTCASE_AUDITING_THROUGH, Constants.TESTCASE_EFFECT_REJECT,
                "存在状态不允许提交生效审批的案例!", "案例批量生效失败!");
        return JsonResultUtil.buildSuccess("案例批量生效成功!");
    }

    // 批量复制
    @RequestValidate(NotEmptyFields = {Dict.PLANID, Dict.TESTCASENO, Dict.WORKNO})
    @RequestMapping("/batchCopyTestcaseToOtherPlan")
    @ResponseBody
    public JsonResult batchCopyTestcaseToOtherPlan(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<Map<String, Object>> testcases = (List<Map<String, Object>>) requestParam.get(Dict.TESTCASENO);
        Integer planId = (Integer) requestParam.get(Dict.PLANID);
        String workNo = (String) requestParam.get(Dict.WORKNO);
        List<String> list = new ArrayList<>();
        for (Map<String, Object> map : testcases) {
            String testcaseNo = (String) map.get(Dict.TESTCASENO);
            Testcase queryDetailByTestcase = testCaseService.queryDetailByTestcaseNo(testcaseNo);
            if (Util.isNullOrEmpty(queryDetailByTestcase)) {
                throw new FtmsException(ErrorConstants.DATA_NOT_EXIST_ERROR);
            }
            PlanlistTestcaseRelation planlistTestcaseRelation = planlistTestcaseRelationServier
                    .queryRelationByTestcaseNoAndPlanId(testcaseNo, planId);
            if (!Util.isNullOrEmpty(planlistTestcaseRelation)) {
                continue;
            }
            list.add((String) map.get(Dict.TESTCASENO));
        }
        if (0 == list.size()) {
            throw new FtmsException(ErrorConstants.DATA_IS_EXIST, new String[]{",批量案例已在计划下,请勿重复添加"});
        }
        // 当前时间戳
        SimpleDateFormat sdf = new SimpleDateFormat(Utils.FULL_TIME);
        String createTm = sdf.format(new Date());
        try {
            planlistTestcaseRelationServier.batchCopyTestcaseToOtherPlan(list, planId, workNo, createTm);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new FtmsException(ErrorConstants.UPDATE_ERROR, new String[]{"批量复制失败"});
        }
        return JsonResultUtil.buildSuccess("案例批量复制成功!");
    }

    @RequestMapping(value = "/queryPlanAllStatus")
    public JsonResult queryPlanAllStatus(@RequestBody Map map) throws Exception {
        try {
            Map returnMap = this.testCaseService.queryPlanAllStatus(map);
            return JsonResultUtil.buildSuccess(returnMap);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR);
        }
    }

    // 导出执行计划下所有案例
    @RequestMapping(value = "/exportExcelTestcase")
    public void exportExcelTestcase(@RequestBody Map map, HttpServletResponse resp) throws Exception {
        try {
            exportExcelService.generateProdExcel(map, resp);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_EXPORT_ERROR, new String[]{"计划页面案例导出失败!"});
        }
    }

    /**
     * 查询菜单下案例 无菜单时查询所有生效案例（案例库查询）
     *
     * @param map 案例库导出
     * @return Testcase
     */
    @RequestMapping("/exportCase")
    public void exportCase(@RequestBody Map map, HttpServletResponse resp) throws Exception {
        try {
            exportExcelService.generateCaseExcel(map, resp);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_EXPORT_ERROR, new String[]{"案例库导出失败!"});
        }
    }

    /**
     * 查询计划下案例执行情况
     * @param map
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID})
    @RequestMapping(value = "/queryAllStatus")
    public JsonResult queryPlanStatus(@RequestBody Map map) throws Exception {
        Integer planId = (Integer) map.get(Dict.PLANID);
        map = testCaseService.queryAllStatus(planId);
        return JsonResultUtil.buildSuccess(map);
    }

    @RequestMapping("/countTestcase")
    @ResponseBody
    public JsonResult countTestcase(@RequestBody TestcaseAuditQueryObject requestParam) throws Exception {
        try {
            // 如果该用户包含master角色,查询全部审批案例,否则查询工单下的案例
            if (userCheckService.isAdmin()) {
                int count = testCaseService.countTestcase(requestParam, null);
                return JsonResultUtil.buildSuccess(count);
            } else {
                int count = testCaseService.countTestcase(requestParam, userCheckService.getUserEnName());
                return JsonResultUtil.buildSuccess(count);
            }
        } catch (Exception e) {
            logger.error("e" + e);
            throw new FtmsException(ErrorConstants.COUNT_TESTCASE_NUM_EXCEPTION);
        }
    }

    /**
     * 案例库案例统计
     */
    @RequestMapping("/countTestcaseByFuncId")
    public JsonResult countTestcaseByFuncId(@RequestBody Map map) throws Exception {
        int total;
        try {
            total = testCaseService.countTestcaseByFuncId(map);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new FtmsException(ErrorConstants.SELECT_DATA_EXCEPTION);
        }
        return JsonResultUtil.buildSuccess(total);
    }

    /**
     * 案例 评审、审批、拒绝、废弃、通用、拒绝生效、生效 操作
     */
    private List<String> updateCaseStatus(Map<String, Object> requestParam, String judgeDict, String errorStr1,
                                          String errorStr2) throws Exception {
        List<String> testcaseNos = (List<String>) requestParam.get(Dict.TESTCASENOS);
        String testcaseStatus = (String) requestParam.get(Dict.TESTCASESTATUS);
        List list = new ArrayList<>();
        for (String testcaseNo : testcaseNos) {
            Testcase testcase = testCaseService.queryDetailByTestcaseNo(testcaseNo);
            if (Util.isNullOrEmpty(testcase)) {
                list.add(testcaseNo);
                continue;
            }
            if (!judgeDict.equals(testcase.getTestcaseStatus())) {
                list.add(testcaseNo);
                continue;
            }
            try {
                testCaseService.auditTestcase(testcaseNo, testcaseStatus);
            } catch (Exception e) {
                list.add(testcaseNo);
                continue;
            }
        }
        return list;

    }

    /**
     * 案例 批量 审批， 审批生效 操作
     */
    private void batchCaseStatus(Map<String, Object> requestParam, String judgeDict1, String judgeDict2,
                                 String errorStr1, String errorStr2) throws Exception {
        List<Map<String, Object>> testcases = (List<Map<String, Object>>) requestParam.get(Dict.TESTCASENO);
        String testcaseStatus = (String) requestParam.get(Dict.TESTCASESTATUS);
        List<String> list = new ArrayList<>();
        if (Util.isNullOrEmpty(testcases)) {
            throw new FtmsException(ErrorConstants.PARAM_ERROR, new String[]{"选择至少1个案例进行操作!"});
        }
        for (Map<String, Object> map : testcases) {
            Testcase queryDetailByTestcase = testCaseService
                    .queryDetailByTestcaseNo((String) map.get(Dict.TESTCASENO));
            if (Util.isNullOrEmpty(queryDetailByTestcase)) {
                throw new FtmsException(ErrorConstants.DATA_NOT_EXIST_ERROR, new String[]{"该案例不存在!"});
            }
            if (!(judgeDict1.equals(queryDetailByTestcase.getTestcaseStatus())
                    || judgeDict2.equals(queryDetailByTestcase.getTestcaseStatus()))) {
                throw new FtmsException(errorStr1);
            }
            list.add((String) map.get(Dict.TESTCASENO));
        }
        try {
            testCaseService.batchAudit(list, testcaseStatus);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new FtmsException(ErrorConstants.UPDATE_ERROR, new String[]{errorStr2});
        }
    }

    @RequestValidate(NotEmptyFields = {Dict.TESTCASENO, Dict.NECESSARYFLAG})
    @RequestMapping("/changeNecessary")
    @ResponseBody
    public JsonResult changeNecessary(@RequestBody Map<String, String> requestParam) throws Exception {
        // 管理员以上权限才可修改案例必测标志
        if (!userCheckService.isAdmin()) {
            throw new FtmsException(ErrorConstants.ROLE_ERROR);
        }
        String testcaseNo = requestParam.get(Dict.TESTCASENO);
        if (Utils.isEmpty(testCaseService.queryDetailByTestcaseNo(testcaseNo))) {
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST_ERROR);
        }
        ;
        // 1为必测 0或null为非必测
        String necessaryFlag = requestParam.get(Dict.NECESSARYFLAG);
        testCaseService.changeNecessary(testcaseNo, necessaryFlag);
        return JsonResultUtil.buildSuccess(null);
    }

    @RequestMapping("/queryUserApprovalList")
    @ResponseBody
    public JsonResult queryUserApprovalList(@RequestBody Map<String, String> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(testCaseService.queryUserApprovalList());
    }



    /**
     * 根据计划案例结果Id查询计划Id(校验权限)
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryPlanIdByPlanlistTestcaseId", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.PLANLISTTESTCASEID})
    @ResponseBody
    public JsonResult queryPlanIdByPlanlistTestcaseId(@RequestBody Map<String, String> requestParam) throws Exception {
        String planlistTestcaseId = requestParam.get(Dict.PLANLISTTESTCASEID);
        String workOrderNo = requestParam.get(Dict.WORKNO);
        String planId = planlistTestcaseRelationServier.queryPlanIdByPlanlistTestcaseId(planlistTestcaseId, workOrderNo);
        return JsonResultUtil.buildSuccess(planId);
    }


    /**
     * 根据工单号返回工单信息
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryTaskNameTestersByNo", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    @ResponseBody
    public JsonResult queryTaskNameTestersByNo(@RequestBody Map<String, String> requestParam) throws Exception {
        String workOrderNo = requestParam.get(Dict.WORKNO);
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap = workOrderService.queryTaskNameTestersByNo(workOrderNo);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new FtmsException(ErrorConstants.SELECT_DATA_EXCEPTION);
        }
        return JsonResultUtil.buildSuccess(resultMap);
    }

    /**
     * 发送案例评审邮件
     * @param requestParam
     * @return
     * @throws Exception
     */
	@RequestValidate(NotEmptyFields = {Dict.WORKNO})
	@RequestMapping("/sendCaseEmail")
	@ResponseBody
	public JsonResult sendCaseEmail(@RequestBody Map requestParam) throws Exception {
		sendEmailService.sendCaseEmail(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@RequestValidate(NotEmptyFields = {Dict.PLANID})
	@RequestMapping("/testCaseCustomSort")
	@ResponseBody
	public JsonResult testcaseCustomSort(@RequestBody Map<String, Object> requestParam) throws Exception {
		boolean flag = false;
		if (Util.isNullOrEmpty(requestParam.get("allCase"))) {
	        throw new FtmsException(ErrorConstants.PARAM_ERROR, new String[]{"该计划下无案例!"});
	    }
		int i = testCaseService.updateTestcaseOrder(requestParam);
		if(i>0) {
			flag = true;
		}
		return JsonResultUtil.buildSuccess(flag);
	}

    /**
     * 根据工单号查询其下任务联系人
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryRelativePeople")
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
	public JsonResult queryRelativePeople(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(testCaseService.queryRelativePeople(map));
    }
}