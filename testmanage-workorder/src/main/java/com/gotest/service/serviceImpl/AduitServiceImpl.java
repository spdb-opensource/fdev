package com.gotest.service.serviceImpl;

import com.gotest.dao.AduitRecordMapper;
import com.gotest.dao.TaskListMapper;
import com.gotest.dao.WorkOrderMapper;
import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.WorkOrderStage;
import com.gotest.domain.AduitRecord;
import com.gotest.domain.TaskList;
import com.gotest.domain.WorkOrder;
import com.gotest.domain.WorkOrderUser;
import com.gotest.service.AduitService;
import com.gotest.service.IDemandService;
import com.gotest.service.ITaskApi;
import com.gotest.service.WorkOrderService;
import com.gotest.utils.MyUtil;
import com.gotest.utils.OrderUtil;
import com.test.testmanagecommon.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@Service
public class AduitServiceImpl implements AduitService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private AduitRecordMapper aduitRecordMapper;

    @Autowired
    private MyUtil myUtil;
    @Autowired
    private OrderUtil orderUtil;
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private ITaskApi iTaskApi;
    @Autowired
    private IDemandService demandService;
    @Autowired
    private TaskListMapper taskListMapper;

    /**
     * 根据人名，查询用户待审核工工单
     */
    @Override
    public List<WorkOrderUser> queryAduitOrder(Map map) throws Exception {
        String userEnName = (String) map.getOrDefault(Dict.USER_EN_NAME, Constants.FAIL_GET);
        String taskName = "%" +((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)) + "%";
        Integer pageSize = (Integer)(map.get(Dict.PAGESIZE));
        Integer currentPage = (Integer)(map.get(Dict.CURRENTPAGE));
        Integer start = pageSize * (currentPage - 1);//起始
        //超级管理员权限判定
        Integer userRole = Integer.parseInt((String) map.getOrDefault(Dict.USERROLE, Constants.NUMBER_0));
        //工单类型，默认查询功能工单
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        List<WorkOrder> aduitOrderList = workOrderMapper.queryAduitOrder(userEnName, taskName, start, pageSize, userRole, orderType);
        //英文名转中文
        List<WorkOrderUser> orderUserList = myUtil.makeEnNameToChName(aduitOrderList);
        return orderUserList;
    }


    @Override
    public Integer queryAduitOrderCount(Map map) throws Exception {
        String userEnName = (String) map.getOrDefault(Dict.USER_EN_NAME, Constants.FAIL_GET);
        String taskName = "%" +((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)) + "%";
        //超级管理员权限判定
        Integer userRole = Integer.parseInt((String) map.getOrDefault(Dict.USERROLE, Constants.NUMBER_0));
        //工单类型，默认查询功能工单
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        Integer count = workOrderMapper.queryAduitOrderCount(userEnName, taskName, userRole, orderType);

        return count;
    }


    /**
     * 通过审核
     *  工单通过审核，1、该工单状态修改为3，uat
     *          2、记录该审批记录
     *              工单编号，测试组长，测试组员，创建时间
     */
    @Override
    public Integer passAduit(Map map) throws Exception {
        String workOrderNo = (String) map.getOrDefault(Dict.WORKORDERNO, Constants.FAIL_GET);
        WorkOrder order = workOrderMapper.queryWorkOrderByNo(workOrderNo);
        String workStage = order.getStage();
        String fdevNew = order.getFdevNew();
        //判断该工单是否是待审批阶段
        if(!"6".equals(workStage) && !"10".equals(workStage) && !"13".equals(workStage)){
            return -1;
        }
        Integer count = workOrderMapper.passAduit(workOrderNo);
        if(count == null || count != 1){
            return count;
        }
        //推任务uat提测时间
        if(!Constants.NUMBER_1.equals(fdevNew) && ("6".equals(workStage) || "10".equals(workStage))){
            workOrderService.changeUatSubmit(workOrderNo);
        }
        //新工单推任务提测插件状态
        if (Constants.NUMBER_1.equals(fdevNew) && ("6".equals(workStage) || "10".equals(workStage))) {
            //如果新工单更新为uat或者分包测试（内测完成）成功，则改变所有任务玉衡提测插件状态为成功
            List<String> tasks = taskListMapper.queryTaskNoByOrder(workOrderNo);
            for (String taskId : tasks) {
                iTaskApi.changeTestComponentStatus(taskId, "2");
            }
        }
        //新增记录
        String createTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        AduitRecord aduitRecord = new AduitRecord();
        aduitRecord.setAduitWorkNo(workOrderNo);
        aduitRecord.setWorkLeader(order.getGroupLeader());
        aduitRecord.setTesters(order.getTesters());
        aduitRecord.setCreateTime(createTime);
        count = aduitRecordMapper.addRecordPassOrder(aduitRecord);
        //老工单发邮件
        Set<String> peoples = new HashSet<>();
        if(!Util.isNullOrEmpty(order.getTesters())){
            peoples.addAll(Arrays.asList(order.getTesters().split(",")));
        }
        if(!Util.isNullOrEmpty(order.getGroupLeader())){
            peoples.addAll(Arrays.asList(order.getGroupLeader().split(",")));
        }
        myUtil.makeUserToCh(order);
        HashMap sendDate = (HashMap) myUtil.beanToMap(order);
        sendDate.forEach((k,v) ->{
            if (v == null || "".equals(v))
                sendDate.put(k,"无");
        });
        if(order.getStage().equals("6")){
            sendDate.put(Dict.STAGE, WorkOrderStage.getStage("6"));
        }else if(order.getStage().equals("10")){
            sendDate.put(Dict.STAGE, WorkOrderStage.getStage("10"));
        }else if(order.getStage().equals("13")){
            sendDate.put(Dict.STAGE, WorkOrderStage.getStage("13"));
        }
        String rqrmntName = order.getDemandName();
        if(Util.isNullOrEmpty(rqrmntName)){
            rqrmntName="无";
        }
        sendDate.put(Dict.RQRMNTNAME,rqrmntName);
        List<String> taskNames = new ArrayList<>();
        List<TaskList> taskLists = taskListMapper.queryTaskByNo(workOrderNo);
        if(!Constants.NUMBER_1.equals(fdevNew)){
            if(!Util.isNullOrEmpty(order.getMainTaskNo())){
                Map result = (Map)iTaskApi.queryTaskDetailByIds(Arrays.asList(order.getMainTaskNo()));
                if (!Util.isNullOrEmpty(result)){
                    Map taskMap=(Map) result.get(order.getMainTaskNo());
                    taskNames.add((String) taskMap.get(Dict.NAME));
                }
            }
            for (TaskList taskList : taskLists) {
                Map result = (Map)iTaskApi.queryTaskDetailByIds(Arrays.asList(taskList.getTaskno()));
                if (!Util.isNullOrEmpty(result)) {
                    Map  taskmap =(Map) result.get(taskList.getTaskno());
                    taskNames.add((String) taskmap.get(Dict.NAME));
                }
            }
            if ( Util.isNullOrEmpty(peoples) || (peoples.size()==1&&peoples.contains("admin")) ){
                return count;
            }
        } else {
            taskNames = taskLists.stream().map(TaskList::getTaskname).collect(Collectors.toList());
        }
        sendDate.put(Dict.TASKLIST,String.join("、",taskNames));
        myUtil.sendFastMail(sendDate, peoples, Constants.UPDATETASK, order.getMainTaskName());
        return count;
    }


    /**
     * 审核拒绝
     *  工单审核拒绝，1、该工单状态修改为1，开发中
     */
    @Override
    public Integer refuseAduit(Map map) throws Exception {
        String workOrderNo = (String) map.getOrDefault(Dict.WORKORDERNO, Constants.FAIL_GET);
        //判断该工单是否是待审批阶段
        String workStage = workOrderMapper.queryOrderStateByWorkNo(workOrderNo);
        //判断该工单是否是待审批阶段
        if(!("6".equals(workStage) || "10".equals(workStage) || "13".equals(workStage))){
            return -1;
        }
        //将工单状态置为sit
        Integer count = workOrderMapper.refuseAduit(workOrderNo);
        return count;
    }


}
