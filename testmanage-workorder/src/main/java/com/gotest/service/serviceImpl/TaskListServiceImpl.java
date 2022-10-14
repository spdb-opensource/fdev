package com.gotest.service.serviceImpl;

import com.gotest.dao.FdevSitMsgMapper;
import com.gotest.dao.RollbackInfoMapper;
import com.gotest.dao.TaskListMapper;
import com.gotest.dao.WorkOrderMapper;
import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.domain.TaskList;
import com.gotest.domain.WorkOrder;
import com.gotest.service.IDemandService;
import com.gotest.service.ITaskApi;
import com.gotest.service.TaskListService;
import com.gotest.utils.CommonUtils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import ognl.ObjectElementsAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务列表实现类，
 * 1、根据给定的workNo，taskName（模糊查询用）查询任务总数
 * 2、根据给定的workNo，taskName（模糊查询用）,start, pageSize查询任务
 */
@SuppressWarnings("all")
@Service
public class TaskListServiceImpl implements TaskListService {

    @Autowired
    private TaskListMapper taskListMapper;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private FdevSitMsgMapper fdevSitMsgMapper;
    @Autowired
    private IDemandService demandService;
    @Autowired
    private RollbackInfoMapper rollbackInfoMapper;
    @Autowired
    private ITaskApi taskApi;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private NewAppServiceImpl newAppService;

    private Logger logger = LoggerFactory.getLogger(TaskListServiceImpl.class);

    /**
     * 根据给定Map中的workNo及taskName(模糊查询用)查询出任务总数
     * @return 返回一个结果Map
     */
    @Override
    public Map queryTaskListCountByWorkNo(Map map) throws Exception {
        String workNo = String.valueOf(map.get(Dict.WORKNO));
        String taskName = "%" + (String)map.get(Dict.TASKNAME) + "%";
        map.put(Dict.TOTAL, taskListMapper.queryTaskListCountByWorkNo(workNo, taskName));
        map.put(Dict.ISNEW, taskListMapper.queryNewOrOldOrder(workNo)>=1?false:true);
        return map;
    }

    /**
     * 根据给定Map中的workNo，taskName,页面显示数据条数，当前页数
     *  查询出任务集合
     */

    @Override
    public List<TaskList> queryTaskList(Map map) throws Exception {
        String workNo = String.valueOf(map.get(Dict.WORKNO));
        String taskName = "%" + map.get(Dict.TASKNAME) + "%";
        Integer pageSize = (Integer)(map.getOrDefault(Dict.PAGESIZE, Constants.PAGESIZE_DEF));
        Integer currentPage = (Integer)(map.getOrDefault(Dict.CURRENTPAGE, Constants.CURRENTPAGE_DEF));
        Integer start = pageSize * (currentPage - 1);//起始
        List<TaskList> myTaskList = taskListMapper.queryTaskListByWorkNo(workNo, taskName, start, pageSize);
        return myTaskList;
    }

    @Override
    public Map<String,Object> queryTasks(String workNo, String taskNo) throws Exception{
        //根据工单分出三种情况分别返回值
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workNo);
        if(CommonUtils.isNullOrEmpty(workOrder)){
            return null;
        }
        Map<String,Object> returnData=new HashMap<>();
        List<Map<String,Object>> taskDetails=new ArrayList<>();
        String mainTaskNo = workOrder.getMainTaskNo();
        String workOrderFlag = workOrder.getWorkOrderFlag();
        String fdevNew = workOrder.getFdevNew();
        //新fdev的工单的查询
        if (CommonUtils.isNullOrEmpty(mainTaskNo)&&Constants.NUMBER_1.equals(workOrderFlag)&&Constants.NUMBER_1.equals(fdevNew)){
            List<TaskList> taskLists = taskListMapper.queryTaskListByWorkNoAndTaskNo(workNo, taskNo);
            for (TaskList taskList : taskLists) {
                //查询任务详情
                Map<String, Object> taskMap = getNewTaskById(taskList.getTaskno());
                if (Util.isNullOrEmpty(taskMap)){
                    continue;
                }
                //获取关联应用的id
                String appId =(String) taskMap.get("relatedApplication");
                //获取任务关联应用的信息详情
                Map appMap=new HashMap();
                String nameEN=null;
                if (!CommonUtils.isNullOrEmpty(appId)){
                     appMap = newAppService.queryAppInfoById(appId);
                     nameEN =(String)appMap.get("nameEN");
                }
                taskMap.put("project_id",appId);
                taskMap.put("project_name",nameEN);
                //获取任务的执行人员id集合
                List<String> assigneeList =(List<String>) taskMap.get("assigneeList");
                //根据用户id获取用户信息
                List<Map> mapList=new ArrayList<>();
                if (!CommonUtils.isNullOrEmpty(assigneeList)){
                    for (String userId : assigneeList) {
                        Map<String, Object> map = userService.queryUserCoreDataById(userId);
                        String  user_name_en =(String) map.get("user_name_en");
                        String user_name_cn = (String) map.get("user_name_cn");
                        Map map1=new HashMap();
                        map1.put("user_name_en",user_name_en);
                        map1.put("user_name_cn",user_name_cn);
                        map1.put("id",userId);
                        mapList.add(map1);
                    }
                }
                //根据任务关联需求id查询需求详情
                String  requirementId =(String)taskMap.get("requirementId");
                Map map = queryNewDemandById(requirementId);
                String oaRealNo =(String) map.get("oaRealNo");
                String oaContactName =(String) map.get("oaContactName");
                String demandType =(String) map.get("demandType");
                Map demandMap=new HashMap();
                demandMap.put("oa_contact_name",oaContactName);
                demandMap.put("demand_type",demandType);
                demandMap.put("oa_contact_no",oaRealNo);
                taskMap.put("demand",demandMap);
                taskMap.put("developer",mapList); //封装开发人员
                taskDetails.add(taskMap);
            }
           //根据实施单元id查询实施单元详情
            Map map = demandService.queryNewUnitInfoById(workOrder.getUnit());
            List<Map<String,String>> implLeaderInfo =(List) map.get("implLeaderInfo");
            if (CommonUtils.isNullOrEmpty(implLeaderInfo)){
                returnData.put(Dict.UNITLEADEREN,null);
                returnData.put(Dict.UNITLEADERCN,null);
            }
            Map<String, String> map1 = implLeaderInfo.get(0);
            String unitNameEn = map1.get(Dict.USER_NAME_EN);
            String unitNameCn = map1.get(Dict.USER_NAME_CN);
            if (unitNameCn==unitNameEn){
                returnData.put(Dict.UNITLEADEREN,unitNameEn);
                returnData.put(Dict.UNITLEADERCN,unitNameCn);
            }else {
                returnData.put(Dict.UNITLEADEREN,null);
                returnData.put(Dict.UNITLEADERCN, null);
            }
        }
        //老fdev的工单
        else  if (CommonUtils.isNullOrEmpty(mainTaskNo) && Constants.NUMBER_1.equals(workOrderFlag)&&!Constants.NUMBER_1.equals(fdevNew)){
            //根据工单号和任务id查询任务列表
            List<TaskList> taskLists = taskListMapper.queryTaskListByWorkNoAndTaskNo(workNo, taskNo);
            for (TaskList taskList : taskLists) {
                //查询任务详情
                String taskId = taskList.getTaskno();
                Map<String,Object> map = queryTaskDetail(taskId);
                if (Util.isNullOrEmpty(map)){
                    continue;
                }
                String stage = (String)map.get(Dict.STAGE);
                if(CommonUtils.isNullOrEmpty(stage)){
                    logger.error("!!!!!!!!!!!!!!!!"+taskList.getTaskno());
                }
                //若任务状态为 已投产  废弃
                if(stage.equals("production")||stage.equals("abort")||stage.equals("discard")||stage.equals("file")){
                    continue;
                }
                taskDetails.add(map);
            }
            //查询工单的负责人信息
            returnData = buildUnitLeader(workOrder.getUnit(), returnData);
            //存量的fdev创建的任务查询
        }else if(!CommonUtils.isNullOrEmpty(mainTaskNo) && Constants.NUMBER_1.equals(workOrderFlag)){
            Map<String, Object> map = queryTaskDetail(mainTaskNo);
            taskDetails.add(map);
        }
        returnData.put(Dict.TASKLIST,taskDetails);
        return returnData;
    }


    /**
     * 根据实施单元编号查询实施单元负责人
     *
     */
    private Map<String,Object> buildUnitLeader(String unitNo,Map<String,Object> map) throws Exception{
        Map result = new HashMap<>();
        try {
            result = demandService.queryByFdevNoAndDemandId(unitNo);
        } catch (Exception e) {
            logger.error("fail to query fdev demand info");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{e.getMessage()});
        }
        //获取实施单元信息
        Map<String,Object> unitInfo = (Map<String, Object>) result.get("implement_unit_info");
        if(CommonUtils.isNullOrEmpty(unitInfo)){
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST,new String[]{"实施单元不存在！"});
        }
        //获取实施单元的负责人
        List<Map<String,String>> unitLeaders = (List<Map<String,String>>) unitInfo.get("implement_leader_all");
        if(CommonUtils.isNullOrEmpty(unitLeaders)){
            map.put(Dict.UNITLEADEREN, null);
            map.put(Dict.UNITLEADERCN,null);
            return map;
    }
        Map<String, String> unitLeader = unitLeaders.get(0);
        map.put(Dict.UNITLEADEREN, unitLeader.get(Dict.USER_NAME_EN));
        map.put(Dict.UNITLEADERCN,unitLeader.get(Dict.USERNAMECN));
        return map;
    }


    @Override
    public String queryWorkNoByTaskNo(String taskNo) {
        TaskList taskList = taskListMapper.queryTaskByTaskNo(taskNo);
        return CommonUtils.isNullOrEmpty(taskList) ? "" : taskList.getWorkno();
    }

    @Override
    public List<Map> queryTasksByWorkNo(String workNo) throws Exception {
        List<TaskList> taskLists = taskListMapper.queryTaskByNo(workNo);
        List<Map> result=new ArrayList<>();
        for (TaskList taskList : taskLists) {
            Map map = queryTaskDetail(taskList.getTaskno());
            result.add(map);
        }
        return result;
    }

    @Override
    public boolean isAllTasksInSitByWorkNo(String workNo) throws Exception {
        //根据工单号判断工单是否为存量数据
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workNo);
        String mainTaskNo = workOrder.getMainTaskNo();
        String workOrderFlag = workOrder.getWorkOrderFlag();
        if(!Util.isNullOrEmpty(mainTaskNo) && Constants.NUMBER_1.equals(workOrderFlag)){
            int fdevSitCount = fdevSitMsgMapper.querySitMsgCount(mainTaskNo).intValue();
            int rollBackCount = rollbackInfoMapper.queryRollbackCount(mainTaskNo,workNo).intValue();
            if(fdevSitCount > rollBackCount){
                return true;
            }
        }else if (Util.isNullOrEmpty(mainTaskNo) && Constants.NUMBER_1.equals(workOrderFlag)) {
            //根据工单号查询任务列表
            List<TaskList> taskLists = taskListMapper.queryTaskByNo(workNo);
            if (CommonUtils.isNullOrEmpty(taskLists)) {
                return false;
            }
            //根据工单查询提测记录
            List<String> taskNos = fdevSitMsgMapper.queryTaskNoByWorkNo(workNo);
            if (CommonUtils.isNullOrEmpty(taskNos)) {
                return false;
            }
            Map<String, TaskList> tasksMap = taskLists.stream().collect(Collectors.toMap(TaskList::getTaskno, taskList -> taskList));
            //提测记录有一条任务记录删除工单下的对应任务
            for (String taskNo : taskNos) {
                TaskList taskList = tasksMap.get(taskNo);
                int fdevSitCount = fdevSitMsgMapper.querySitMsgCount(taskNo).intValue();
                int rollBackCount = rollbackInfoMapper.queryRollbackCount(taskNo,workNo).intValue();
                if (!CommonUtils.isNullOrEmpty(taskList) && fdevSitCount > rollBackCount) {
                    taskLists.remove(taskList);
                }
            }
            //任务列表为空代表工单下所有任务提测
            if (CommonUtils.isNullOrEmpty(taskLists)) {
                return true;
            }
        }
        return false;
    }

    private Map<String,Object> queryTaskDetail(String taskId){
        Map<String,Object> map=new HashMap();
        try {
            map.put(Dict.ID, taskId);
            map.put(Dict.REST_CODE, "queryTaskDetail");
            map = (Map<String,Object>)restTransport.submitSourceBack(map);
            return map;
        } catch (Exception e) {
            logger.error("fail to query fdev subtask info");
        }
        return null;
    }

    @Override
    public List<Map> queryFdevTaskByWorkNo(String workNo) throws Exception {
        List<String> taskIds = taskListMapper.queryTaskNoByOrder(workNo);
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workNo);
        String mainTaskNo = workOrder.getMainTaskNo();
        if(!Util.isNullOrEmpty(mainTaskNo)){
            taskIds.add(mainTaskNo);
        }
        taskIds = taskIds.stream().distinct().collect(Collectors.toList());
        Map sendTask;
        List<Map> result = new ArrayList<>();
        if(!Util.isNullOrEmpty(taskIds)){
            if(Constants.NUMBER_1.equals(workOrder.getFdevNew())){
                //新fdev任务
                List<Map<String, Object>> newTaskInfos = (List<Map<String, Object>>)taskApi.queryNewTaskDetail(taskIds);
                if(!CommonUtils.isNullOrEmpty(newTaskInfos)){
                    for(Map<String, Object> taskInfo : newTaskInfos) {
                        List<String> assignIds = (List<String>)taskInfo.get("assigneeList");
                        List<Map> assigns = this.formatAssignList(assignIds);
                        taskInfo.put(Dict.MASTER, assigns);
                        taskInfo.put(Dict.SPDBMASTER, assigns);
                        taskInfo.put(Dict.DEVELOPER, assigns);
                        taskInfo.put(Dict.STAGE, taskInfo.get(Dict.STATUS));
                        Map group = userService.queryGroupDetailById((String)taskInfo.get(Dict.ASSIGNEEGROUPID));
                        taskInfo.put(Dict.GROUP, group);
                        Map appInfo = newAppService.queryAppInfoById((String)taskInfo.get("relatedApplication"));
                        taskInfo.put("project_name",CommonUtils.isNullOrEmpty(appInfo)?"":appInfo.get("nameEN"));
                        result.add(taskInfo);
                    }
                }
            } else {
                //老fdev任务
                Map<String, Object> taskInfos = null;
                try {
                    taskInfos = taskApi.queryTaskDetailByIds(taskIds);
                } catch (Exception e) {
                    logger.error("fail to get task info");
                }
                if(!CommonUtils.isNullOrEmpty(taskInfos)){
                    for(String taskId : taskInfos.keySet()){
                        result.add((Map)taskInfos.get(taskId));
                    }
                }
            }
        }
        return result;
    }

    private List<Map> formatAssignList(List<String> assignIds) throws Exception{
        List<Map> result = new ArrayList<>();
        for(String id : assignIds) {
            Map user = null;
            try {
                user = userService.queryUserCoreDataById(id);
            } catch (Exception e) {
                logger.error("fail to get user info");
            }
            if(CommonUtils.isNullOrEmpty(user))
                continue;
            result.add(user);
        }
        return result;
    }

    //查询新fdev的任务详情
    private Map<String,Object> getNewTaskById(String id) {
        Map send = new HashMap();
        send.put(Dict.ID, id);
        send.put(Dict.REST_CODE, "getTaskById");
        try {
          return  (Map<String, Object>) restTransport.submitSourceBack(send);
        } catch (Exception e) {
            logger.error("fail to get new Task info");
        }
        return null;
    }

    /**
     * 根据需求id查询新需求信息
     *
     * @param demandId
     * @return
     * @throws Exception
     */
    public Map queryNewDemandById(String demandId) throws Exception {
        try {
            Map send = new HashMap();
            send.put(Dict.DEMANDID, demandId);
            send.put(Dict.REST_CODE, "queryDemandInfoDetailNew");
            Map result = (Map) restTransport.submitSourceBack(send);
            return result;
        }
        catch (Exception e) {
            logger.error("fail to get new demandId info");
        }
        return null;
    }
}
