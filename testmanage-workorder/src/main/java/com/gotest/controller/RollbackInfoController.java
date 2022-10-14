package com.gotest.controller;

import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.domain.WorkOrder;
import com.gotest.service.RollbacksInfoService;
import com.gotest.service.WorkOrderService;
import com.gotest.utils.MyUtil;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import com.test.testmanagecommon.vaildate.RequestValidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/rollback")
public class RollbackInfoController {
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private MyUtil util;
    @Autowired
    private RollbacksInfoService rollbacksInfoService;


    /**
     * 工单打回
     *
     * @param map
     * @return
     */
    @RequestValidate(NotEmptyFields = {Dict.WORKNO, Dict.REASON, Dict.TASKNO})
    @RequestMapping("/workOrderRollback")
    public JsonResult workOrderRollback(@RequestBody Map<String, String> map) throws Exception {
        String workNo = map.get(Dict.WORKNO);
        String reason = map.get(Dict.REASON);
        String taskId = map.get(Dict.TASKNO);
        String detailInfo = map.get(Dict.DETAILINFO);
        WorkOrder workOrder = workOrderService.queryWorkOrderByNo(workNo);
        if (Util.isNullOrEmpty(workOrder)) {
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST);
        }
        String stage = workOrder.getStage();
        //1、判断工单状态，只有开发中和 sit可打回
        if (!"1".equals(stage) && !"2".equals(stage)) {
            throw new FtmsException(ErrorConstants.ERROR_STAGE_ORDER_ROLLBACK);
        }
        //2、判断人员权限，工单的测试组长和工单负责人可操作
        String userName = util.getCurrentUserEnName();
        String groupLeader = workOrder.getGroupLeader();
        String workManager = workOrder.getWorkManager();
        if (!workManager.equals(userName) && !groupLeader.contains(userName)) {
            throw new FtmsException(ErrorConstants.ROLE_ERROR, new String[]{"仅工单负责人和测试组长可进行打回操作"});
        }
        rollbacksInfoService.workOrderRollback(workOrder, userName, reason, detailInfo, taskId);
        return JsonResultUtil.buildSuccess(null);
    }

   // @RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE})
    @RequestMapping("/queryRollbackReport")
    public JsonResult queryRollbackReport(@RequestBody Map<String, String> map) throws Exception {
        String mainTaskName = map.get(Dict.MAINTASKNAME);//主任务名
        String groupId = map.get(Dict.GROUPID);//组id
        String startDate = map.get(Dict.STARTDATE);// 开始时间
        String endDate = map.get(Dict.ENDDATE);// 结束时间
        String developer = map.get(Dict.DEVELOPER);// 开发人员英文名
        String childGroupFlag = map.get(Dict.CHILDGROUPFLAG);// 是否包含子组标志
        String reason = map.get(Dict.REASON);// 打回原因
        return JsonResultUtil.buildSuccess(rollbacksInfoService.queryRollbackReport(mainTaskName, groupId, startDate, endDate, childGroupFlag, developer, reason));
    }

    @RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE})
    @PostMapping("/exportRollbackReport")
    public void exportRollbackReport(@RequestBody Map<String, String> map, HttpServletResponse resp) throws Exception {
        String mainTaskName = map.get(Dict.MAINTASKNAME);//主任务名
        String groupId = map.get(Dict.GROUPID);//组id
        String startDate = map.get(Dict.STARTDATE);// 开始时间
        String endDate = map.get(Dict.ENDDATE);// 结束时间
        String developer = map.get(Dict.DEVELOPER);// 开发人员英文名
        String childGroupFlag = map.get(Dict.CHILDGROUPFLAG);// 是否包含子组标志
        String reason = map.get(Dict.REASON);// 打回原因
        rollbacksInfoService.exportRollbackReport(mainTaskName, groupId, startDate, endDate, childGroupFlag, resp, developer, reason);
    }

    /**
     * 拒绝任务
     *
     * @param requestMap
     * @throws Exception
     */
    @RequestMapping("/refuseTask")
    @RequestValidate(NotEmptyFields = {Dict.WORKNO, Dict.TASKIDS})
    public JsonResult refuseTask(@RequestBody Map requestMap) throws Exception {
        List<String> taskIds = (List<String>) requestMap.get(Dict.TASKIDS);
        String workNo = String.valueOf(requestMap.get(Dict.WORKNO));
        rollbacksInfoService.refuseTask(taskIds, workNo);
        return JsonResultUtil.buildSuccess();
    }
}
