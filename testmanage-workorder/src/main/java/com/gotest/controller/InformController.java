package com.gotest.controller;

import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.domain.FdevSitMsg;
import com.gotest.domain.MessageFdev;
import com.gotest.domain.WorkOrder;
import com.gotest.service.*;
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

import java.util.*;

@SuppressWarnings("all")
@RestController
@RequestMapping(value = "/inform")
public class InformController {

    private Logger logger = LoggerFactory.getLogger(InformController.class);

    @Autowired
    private InformService informService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private INotifyApi iNotifyApi;

    @Autowired
    private OrderDimensionService odService;

    @Autowired
    private ITaskApi taskApi;

    @Autowired
    private TaskListService taskListService;

    @Autowired
    private RequireService requireService;

    @Autowired
    private IDemandService demandService;

    @Autowired
    private NewFdevService newFdevService;


    /**
     * 查询fdev消息详情
     */
    @RequestMapping("/queryMessage")
    public JsonResult queryMessage(@RequestBody Map map) {
        List<MessageFdev> myInformList;
        try {
            myInformList = informService.queryMessage(map);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询消息失败！"});
        }
        return JsonResultUtil.buildSuccess(myInformList);
    }

    /**
     * 查询fdev消息总数，对应人英文名。。
     */
    @RequestMapping("/queryMessageCount")
    public JsonResult queryMessageCount(@RequestBody Map map) {
        Map myInformCount;
        try {
            myInformCount = informService.queryMsgCountByUserEnName(map);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询消息总数失败！"});
        }
        return JsonResultUtil.buildSuccess(myInformCount);
    }

    /**
     * 查询 消息记录 详情
     */
    @RequestMapping("/queryMessageRecord")
    public JsonResult queryMessageRecord(@RequestBody Map map) {
        if (Util.isNullOrEmpty(map.get(Dict.USER_EN_NAME))) {
            return JsonResultUtil.buildError(ErrorConstants.DATA_NOT_EXIST, "用户名不能为空，请检查是否登录");
        }
        List<MessageFdev> myInformList;
        try {
            myInformList = informService.queryMessageRecord(map);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询消息失败！"});
        }
        return JsonResultUtil.buildSuccess(myInformList);
    }

    /**
     * 查询消息记录总数，对应人英文名。。
     */
    @RequestMapping("/queryMessageRecordCount")
    public JsonResult queryMessageRecordCount(@RequestBody Map map) {
        if (Util.isNullOrEmpty(map.get(Dict.USER_EN_NAME))) {
            return JsonResultUtil.buildError(ErrorConstants.DATA_NOT_EXIST, "用户名不能为空，请检查是否登录");
        }
        Map myInformCount;
        try {
            myInformCount = informService.queryMessageRecordCount(map);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询消息总数失败！"});
        }
        return JsonResultUtil.buildSuccess(myInformCount);
    }

    /**
     * 已读 一条数据
     */
    @RequestMapping("/ignoreMessage")
    public JsonResult ignoreMessage(@RequestBody Map map) {
        Map myResult;
        try {
            myResult = informService.updateOneMsgByMsgId(map);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"忽略单条数据失败！"});
        }
        return JsonResultUtil.buildSuccess(myResult);
    }

    /**
     * 已读 所有数据。
     */
    @RequestMapping("/ignoreAllMessage")
    public JsonResult ignoreAllMessage(@RequestBody Map map) {
        Map myResult;
        try {
            myResult = informService.updateAllMsgByUserEnName(map);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"忽略所有数据失败！"});
        }
        return JsonResultUtil.buildSuccess(myResult);
    }

    /**
     * fdev提测
     */
    @RequestMapping("/informTest")
    public JsonResult informTest(@RequestBody Map map) throws Exception {
        //查询任务对应工单号
        String taskNo = String.valueOf(map.get(Dict.JOBNO));
        String workNo = taskListService.queryWorkNoByTaskNo(taskNo);
        //判断是否为存量数据
        if (Util.isNullOrEmpty(workNo)) {
            String oldWorkNo = workOrderService.queryWorkNoByTaskId(taskNo, Constants.ORDERTYPE_FUNCTION);
            if (!Util.isNullOrEmpty(oldWorkNo)) {
                workNo = oldWorkNo;
            } else {
                try {
                    //任务没进玉衡,查任务对应的实施单元
                    Map taskInfo = (Map) taskApi.queryTaskDetailByIds(Arrays.asList(taskNo)).get(taskNo);
                    String unitNo = (String) taskInfo.get(Dict.FDEV_IMPLEMENT_UNIT_NO);
                    unitNo = demandService.getUnitNo(unitNo);
                    //在根据实施单元查工单
                    List<String> orderNos = (List<String>) workOrderService.queryWorkNoByUnitNo(unitNo).get(Dict.WORKNOS);
                    if (!Util.isNullOrEmpty(orderNos)) {
                        //如果工单存在，说明只有任务没同步过来
                        Map create = new HashMap();
                        create.put(Dict.UNITNO, unitNo);
                        create.put(Dict.TASKNO, taskInfo.get(Dict.ID));
                        create.put(Dict.TASKNAME, taskInfo.get(Dict.NAME));
                        workNo = requireService.createFdevOrder(create);
                    } else {
                        //如果工单不存在说明实施单元连工单都没同步到玉衡，先查实施单元
                        Map unitInfo = (Map) demandService.queryByFdevNoAndDemandId(unitNo).get(Dict.IMPLEMENT_UNIT_INFO);
                        Map createOrder = new HashMap();
                        createOrder.put(Dict.UNITNO, unitNo);
                        createOrder.put(Dict.GROUP_ID, unitInfo.get(Dict.GROUP));
                        createOrder.put(Dict.INTERNALTESTSTART, unitInfo.get(Dict.PLAN_INNER_TEST_DATE));
                        createOrder.put(Dict.INTERNALTESTEND, unitInfo.get(Dict.PLAN_TEST_DATE));
                        createOrder.put(Dict.EXPECTEDPRODUCTDATE, unitInfo.get(Dict.PLAN_PRODUCT_DATE));
                        workOrderService.createWorkOrder(createOrder);
                        Map create = new HashMap();
                        create.put(Dict.UNITNO, unitNo);
                        create.put(Dict.TASKNO, taskInfo.get(Dict.ID));
                        create.put(Dict.TASKNAME, taskInfo.get(Dict.NAME));
                        workNo = requireService.createFdevOrder(create);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"工单不存在！"});
                }
            }
        }
        map.put(Dict.JOBID, workNo);//工单号
        Collection<String> resultList = null;
        Collection<String> allUser = null;
        //新增fdev提测记录 返回提测id
        String id = informService.addFdevSitMsg(map);
        //发送玉衡通知
        resultList = informService.addMsgFromFdev(map);
        //更新tag
        try {
            informService.fdevSubmitSitTag(map);
        } catch (Exception e) {
            logger.error("fail to update tag in fdev");
        }
        allUser = informService.queryAllUser(map);
        //新增玉衡弹窗消息
        WorkOrder order = workOrderService.queryWorkOrderByNo(workNo);
        String unit = "";
        if (!Util.isNullOrEmpty(order)) {
            unit = order.getUnit();
        }
        Map messageMap = new HashMap();
        messageMap.put("content", unit + "/" + map.get("taskName"));
        messageMap.put("type", "提交SIT测试");
        messageMap.put("target", allUser);
        messageMap.put("id", id);
        try {
            iNotifyApi.sendUserNotify(messageMap);
        } catch (Exception e) {
            logger.error("ftms notify send error:send sit test");
        }
        if (Util.isNullOrEmpty(resultList)) {
            return JsonResultUtil.buildError(Constants.I_FAILED, "人员为空，新增消息失败");
        }
        //查询工单下的所有任务是否都已经提测，即所有任务都已经提测，更新工单sit标识和sit提测时间
        if (taskListService.isAllTasksInSitByWorkNo(workNo)) {
            workOrderService.updateSitFlag(map);
        }
        return JsonResultUtil.buildSuccess(resultList);
    }

    /**
     * 根据任务id查询fdev提测信息
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryFdevSitMsg")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryFdevSitMsg(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(informService.queryFdevSitMsg(map));
    }

    /**
     * 根据消息id查询提测信息详情
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/querySitMsgDetail")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult querySitMsgDetail(@RequestBody Map<String, String> map) throws Exception {
        String id = map.get(Dict.ID);
        return JsonResultUtil.buildSuccess(informService.querySitMsgDetail(id));
    }

    /**
     * 查询提测信息列表
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/querySitMsgList")
    @RequestValidate(NotEmptyFields = {Dict.PAGESIZE, Dict.CURRENTPAGE})
    public JsonResult querySitMsgList(@RequestBody Map<String, Object> map) throws Exception {
        //新增搜索维度：组，测试人员，提测日期，需求编号（和工单名搜索合并）
        String workNo = (String) map.getOrDefault(Dict.WORKNO, Constants.FAIL_GET);
        Integer pageSize = (Integer) map.get(Dict.PAGESIZE);
        Integer currentPage = (Integer) map.get(Dict.CURRENTPAGE);
        Integer startPage = pageSize * (currentPage - 1);//起始
        String stage = String.valueOf(map.getOrDefault(Dict.STAGE, Constants.FAIL_GET));
        List<String> groupId = (List<String>) map.get(Dict.GROUPID);
        List<String> orderGroupId = (List<String>) map.get(Dict.ORDERGROUPID);
        String tester = String.valueOf(map.getOrDefault(Dict.TESTER, Constants.FAIL_GET));
        String startDate = String.valueOf(map.getOrDefault(Dict.STARTDATE, Constants.FAIL_GET));
        String endDate = String.valueOf(map.getOrDefault(Dict.ENDDATE, Constants.FAIL_GET));
        String orderType = String.valueOf(map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION));
        Boolean isIncludeChildren = (Boolean) map.getOrDefault(Dict.ISINCLUDECHILDREN, false);
        return JsonResultUtil.buildSuccess(informService.querySitMsgList(workNo, pageSize, startPage, groupId, orderGroupId, tester, startDate, endDate, stage, orderType, isIncludeChildren));
    }

    /**
     * 查询提测信息列表
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/countSitMsgList")
    public JsonResult countSitMsgList(@RequestBody Map<String, Object> map) throws Exception {
        String workNo = (String) map.getOrDefault(Dict.WORKNO, Constants.FAIL_GET);
        //String userNameEn = (String)map.get(Dict.USERNAMEEN);
        List<String> groupId = (List<String>) map.get(Dict.GROUPID);
        List<String> orderGroupId = (List<String>) map.get(Dict.ORDERGROUPID);
        String tester = String.valueOf(map.getOrDefault(Dict.TESTER, Constants.FAIL_GET));
        String startDate = String.valueOf(map.getOrDefault(Dict.STARTDATE, Constants.FAIL_GET));
        String endDate = String.valueOf(map.getOrDefault(Dict.ENDDATE, Constants.FAIL_GET));
        String stage = String.valueOf(map.getOrDefault(Dict.STAGE, Constants.FAIL_GET));
        String orderType = String.valueOf(map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION));
        Boolean isIncludeChildren = (Boolean) map.getOrDefault(Dict.ISINCLUDECHILDREN, false);
        return JsonResultUtil.buildSuccess(informService.countSitMsgList(workNo, groupId, orderGroupId, tester, startDate, endDate, stage, orderType, isIncludeChildren));
    }

    /**
     * 发送提交业测邮件
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/sendStartUatMail")
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    public JsonResult sendStartUatMail(@RequestBody Map map) throws Exception {
        informService.sendStartUatMail(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 发送sit测试报告邮件和玉衡通知
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/sendSitDoneMail")
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    public JsonResult sendSitDoneMail(@RequestBody Map map) throws Exception {
        String workNo = String.valueOf(map.get(Dict.WORKNO));
        map = odService.exportSitReportData(workNo, map);
        informService.sendSitDoneMail(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询任务相关提测信息列表
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryTaskSitMsg")
    @RequestValidate(NotEmptyFields = {Dict.TASKNO})
    public JsonResult queryTaskSitMsg(@RequestBody Map map) throws Exception {
        String taskNo = String.valueOf(map.get(Dict.TASKNO));
        List<FdevSitMsg> result = informService.queryTaskSitMsg(taskNo);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 查询业测邮件正文信息
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryUatMailInfo")
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    public JsonResult queryUatMailInfo(@RequestBody Map map) throws Exception {
        Map<String, String> result = informService.queryUatMailInfo(map);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 跑批提测信息表任务组id
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/getSitSubmitGroup")
    public JsonResult getSitSubmitGroup() throws Exception {
        informService.getSitSubmitGroup();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 新fdev提测
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/testInform")
    public JsonResult testInform(@RequestBody Map map) throws Exception {
        //查询任务对应工单号
        String taskNo = String.valueOf(map.get(Dict.JOBNO));
        String unitNo = (String) map.get(Dict.UNITNO);
        //查询任务id对应的工单
        String workNo = taskListService.queryWorkNoByTaskNo(taskNo);
        if (Util.isNullOrEmpty(workNo)) {
            //该任务还没有进入玉衡，根据实施单元id查询该实施单元下的工单
            List<String> orderNos = (List<String>) workOrderService.queryWorkNoByUnitNo(unitNo).get(Dict.WORKNOS);
            //如果该实施单元下的工单列表不为空
            if (!Util.isNullOrEmpty(orderNos)) {
                //如果工单存在，说明只有任务没同步过来
                Map create = new HashMap();
                String taskNmae = (String) map.get(Dict.TASKNAME);
                workNo = newFdevService.newCreateTask(taskNo, unitNo, taskNmae);
            } else {
                throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"该实施单元未同步工单"});
            }
        }
        map.put(Dict.JOBID, workNo);//工单号
        //新增fdev提测记录 返回提测id
        String id = informService.addFdevSitMsg(map);
        //发送玉衡通知
        Collection<String> resultList = informService.addMsgFromFdev(map);
        //新增玉衡弹窗消息
        Collection<String> allUser = informService.queryAllUser(map);
        WorkOrder order = workOrderService.queryWorkOrderByNo(workNo);
        if (!Util.isNullOrEmpty(allUser)) {
            String demandName = Util.isNullOrEmpty(order.getDemandName()) ? "" : "【需求】" + order.getDemandName() + "/";
            Map messageMap = new HashMap();
            messageMap.put(Dict.CONTENT, demandName + map.get(Dict.TASKNAME));
            messageMap.put(Dict.TYPE, "提交SIT测试");
            messageMap.put(Dict.TARGET, allUser);
            messageMap.put(Dict.ID, id);
            try {
                iNotifyApi.sendUserNotify(messageMap);
            } catch (Exception e) {
                logger.error("fail to send ftms notify for topic : test sit");
            }
        }
        //查询工单下的所有任务是否都已经提测，即所有任务都已经提测，更新工单sit标识和sit提测时间
        if (taskListService.isAllTasksInSitByWorkNo(workNo)) {
            workOrderService.updateSitFlag(map);
        }
        //更新工单状态到开发中
        //修改玉衡的状态为开发中，修改sitFlag为提内侧
      //  workOrderService.updateOrderStage(order.getWorkOrderNo(),"2");
        workOrderService.updateOrderStageAndSitFlag(order.getWorkOrderNo(),"1","1");
        return JsonResultUtil.buildSuccess(resultList);
    }

    /**
     * 查询工单对应的最后一次提测信息中的交易清单文件路径
     * @param param
     * @return
     */
    @PostMapping("/queryLastTransFilePath")
    public JsonResult queryLastTransFilePath(@RequestBody Map<String,String> param) throws Exception {
        return JsonResultUtil.buildSuccess(informService.queryLastTransFilePath(param.get(Dict.WORKNO)));
    }


    /**
     * 查询指定时间段内每个时间节点内提测准时率、提测任务数和缺陷数
     * @param param
     * @return
     * @throws Exception
     */
    @RequestMapping("/querySubmitTimelyAndMantis")
    @RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE, Dict.GROUPIDS, Dict.ISINCLUDECHILDREN})
    public JsonResult querySubmitTimelyAndMantis(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(informService.querySubmitTimelyAndMantis((String)param.get(Dict.STARTDATE),
                (String)param.get(Dict.ENDDATE), (List<String>) param.get(Dict.GROUPIDS), (Boolean) param.get(Dict.ISINCLUDECHILDREN)));
    }

    /**
     * 查询需求下工单的内部测试数据
     * @param param
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryInnerTestData")
    @RequestValidate(NotEmptyFields = {Dict.DEMANDNO})
    public JsonResult queryInnerTestData(@RequestBody Map<String, String> param) throws Exception {
        return JsonResultUtil.buildSuccess(informService.queryInnerTestData(param.get(Dict.DEMANDNO)));
    }
}
