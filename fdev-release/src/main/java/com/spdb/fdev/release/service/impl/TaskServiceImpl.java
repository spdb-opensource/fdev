package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IReleaseRqrmntInfoDao;
import com.spdb.fdev.release.entity.ReleaseRqrmntInfo;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.service.IAppService;
import com.spdb.fdev.release.service.IComponenService;
import com.spdb.fdev.release.service.IReleaseTaskService;
import com.spdb.fdev.release.service.ITaskService;
import com.spdb.fdev.transport.RestTransport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 发送task模块的业务接口实现类
 */
@Service
public class TaskServiceImpl implements ITaskService {

    @Autowired
    private RestTransport restTransport;
    @Autowired
    private IReleaseTaskService releaseTaskService;

    private Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    @Autowired
    private IAppService appService;
    @Autowired
    private IReleaseRqrmntInfoDao releaseRqrmntInfoDao;
    @Autowired
    private IComponenService componenService;
    @Autowired
	private ITaskService taskService;

    @Override
    public Map<String, Object> queryTaskInfo(String taskId) throws Exception {
        //发 task模块 查询任务详情
        Map<String, Object> map = new HashMap<>();
        List<String> ids = new ArrayList<String>();
        ids.add(taskId);
        map.put(Dict.IDS, ids);
        map.put(Dict.REST_CODE, "queryTaskDetailByIds");
        Map<String, Object> result = (Map<String, Object>) restTransport.submit(map);
        if (CommonUtils.isNullOrEmpty(result)) {
            logger.info("can't find this task info !@@@@@taskid={}", taskId);
            throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[]{"无法查询到当前任务信息"});
        }
        return (Map) result.get(taskId);
    }

    @Override
    public Map queryTaskReview(List<String> idlist) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.IDS, idlist);
        map.put(Dict.REST_CODE, "queryTaskReview");
        return (Map) restTransport.submit(map);
    }

    @Override
    public String querySystemName(String id) throws Exception {
        Map param = new HashMap();
        param.put(Dict.ID, id);
        param.put(Dict.REST_CODE, "querySystem");
        List result = (List) restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            logger.info("找不到所属系统 !@@@@@taskid={}", param);
            throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[]{"无法查询到当前系统信息"});
        }
        return (String) ((Map) result.get(0)).get(Dict.NAME);
    }

    @Override
    public Map updateTaskInner(Map<String, Object> map) throws Exception {
        map.put(Dict.REST_CODE, "updateTaskInner");
        return (Map) restTransport.submit(map);
    }

    @Override
    public List<Map> querySystem() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.REST_CODE, "querySystem");
        return (List<Map>) restTransport.submit(map);
    }

    @Override
    public void updateTaskProduction(String task_id) throws Exception {
    	ReleaseTask releaseTask = new ReleaseTask();
    	Map<String, Object> taskInfo = taskService.queryTaskInfo(task_id);
    	String taskType = (String) taskInfo.get("applicationType");
    	if ("componentWeb".equals(taskType) || "componentServer".equals(taskType)) {
    		releaseTask = releaseTaskService.findOneTask(task_id,"4");
		} else if ("archetypeWeb".equals(taskType) || "archetypeServer".equals(taskType)) {
			releaseTask = releaseTaskService.findOneTask(task_id,"5");
		} else if ("image".equals(taskType)) {
			releaseTask = releaseTaskService.findOneTask(task_id,"6");
		} else {
			releaseTask = releaseTaskService.findOneTask(task_id);
		}
        String fire_time = releaseTask.getRelease_node_name().split("_")[0];
        StringBuilder sb = new StringBuilder();
        String release_date = sb.append(fire_time.substring(0, 4)).append("/").append(fire_time.substring(4, 6)).append("/").append(fire_time.substring(6,8)).toString();
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, task_id);
        map.put(Dict.STAGE, Dict.TASK_STAGE_PRODUCTION);
        map.put(Dict.FIRE_TIME, release_date);
        map.put(Dict.REST_CODE, "updateTaskInner");
        restTransport.submit(map);
    }

    @Override
    public Map<String, Object> queryTasksByIds(Set<String> list) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Dict.IDS, list);
        map.put(Dict.REST_CODE, "queryTaskDetailByIds");
        return (Map<String, Object>) restTransport.submit(map);
    }

    @Override
    public Map updateTaskForRel(String id, String stage, String date) throws Exception{
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, id);
        map.put(Dict.STAGE, stage);
        map.put(Dict.START_REL_TEST_TIME, date);
        map.put(Dict.STOP_UAT_TEST_TIME, date);
        map.put(Dict.REST_CODE, "updateTaskInner");
        return (Map) restTransport.submit(map);
    }

    @Override
    public Map rollbackTaskStatus(String id, String stage, String uat_testObject, String date) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, id);
        map.put(Dict.STAGE, stage);
        map.put(Dict.UAT_TESTOBJECT, uat_testObject);
        map.put(Dict.START_UAT_TEST_TIME, date);
        map.put(Dict.START_REL_TEST_TIME, date);
        map.put(Dict.STOP_UAT_TEST_TIME, date);
        map.put(Dict.REST_CODE, "updateTaskInner");
        return (Map) restTransport.submit(map);
    }

    @Override
    public Map<String, Object> queryTaskDetail(String task_id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, task_id);
        map.put(Dict.REST_CODE, "queryTaskDetail");
        try {
            return (Map<String, Object>) restTransport.submit(map);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Map<String, Object> queryTestcaseByTaskId(String task_id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("taskNo", task_id);
        map.put(Dict.REST_CODE, "queryTestcaseByTaskId");
        return (Map<String, Object>) restTransport.submit(map);
    }

    @Override
    public Map<String, Object> querySystemById(String id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, id);
        map.put(Dict.REST_CODE, "querySystem");
        List<Map<String, Object>> result = (List<Map<String, Object>>) restTransport.submit(map);
        if (CommonUtils.isNullOrEmpty(result)) {
            logger.info("找不到所属系统 !@@@@@id={}", id);
            throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[]{"无法查询到当前系统信息"});
        }
        Map<String, Object> returnMap = result.get(0);
        returnMap.put(Dict.NAME_CN, returnMap.get(Dict.NAME));
        return returnMap;
    }

    @Override
    public Map<String, Object> queryTaskRqrmnt(String id) throws Exception {
        Map<String, Object> task = queryTaskDetail(id);
        Map result = new HashMap();
        if(!CommonUtils.isNullOrEmpty(task)){
            Map demand = (Map)task.get(Dict.DEMAND);
            result.put(Dict.RQRMNT_NO ,(String)task.get(Dict.RQRMNT_NO));

            result.put(Dict.RQRMNTNO , (String)demand.get(Dict.OA_CONTACT_NO));
            result.put(Dict.RQRMNT_NAME_U ,(String)demand.get(Dict.OA_CONTACT_NAME));
            String demand_type = (String)demand.get(Dict.DEMAND_TYPE);
            if(!CommonUtils.isNullOrEmpty(demand_type)){
                if(demand_type.equals(Constants.RQRMNT_TYPE_INNER_EN)){
                    result.put(Dict.RQRMNTTYPE , Constants.RQRMNT_TYPE_INNER);
                }else{
                    result.put(Dict.RQRMNTTYPE , Constants.RQRMNT_TYPE_BUSINESS);
                }
            }
            Map review = (Map)task.get(Dict.REVIEW);
            List<Map> otherSystem =  (List<Map>)(review.get(Dict.OTHER_SYSTEM));
            if(!CommonUtils.isNullOrEmpty(otherSystem)){
                List<String> collect = otherSystem.stream().map(e -> (String)e.get(Dict.NAME)).collect(Collectors.toList());
                result.put(Dict.OTHERSYSTEM , String.join(";" ,collect));
                result.put(Dict.OTHER_SYSTEM , collect);
            }
            if(!CommonUtils.isNullOrEmpty(task.get(Dict.PROWANTWINDOW))){
                String releaseDate = (String)task.get(Dict.PROWANTWINDOW);
                result.put(Dict.RELEASE_DATE , releaseDate.replace("/","-"));
            }
            result.put(Dict.PLAN_FIRE_TIME, task.get(Dict.PLAN_FIRE_TIME));
            if(!CommonUtils.isNullOrEmpty(review.get(Dict.COMMONPROFILE))){
                boolean commonProfile = (boolean)review.get(Dict.COMMONPROFILE);
                if(commonProfile){
                    result.put(Dict.COMMONPROFILE , "0");
                }
            }
            //需求包含特殊情况
            if(!CommonUtils.isNullOrEmpty(review.get(Dict.SPECIALCASE))){
               if(!CommonUtils.isNullOrEmpty(review.get(Dict.SPECIALCASE))){
                   result.put(Dict.SPECIALCASE , "0");
               }
            }
            List<Map> date_base_alter = (List<Map>)review.get(Dict.DATA_BASE_ALTER);
            if(!CommonUtils.isNullOrEmpty(date_base_alter)){
                String dateFlag = (String)date_base_alter.get(0).get(Dict.NAME);
                if(dateFlag.equals(Constants.REVIEW_TRUE)){
                    result.put(Dict.DATABASEALTER, "0");
                }
            }
            Map group = (Map)task.get(Dict.GROUP);
            result.put(Dict.GROUP_ID , (String)group.get(Dict.ID));
            if(!CommonUtils.isNullOrEmpty(task.get(Dict.PROJECT_ID))){
            	String applicationType = (String) task.get("applicationType");
            	Map<String, Object> app = new HashMap<String, Object>();
            	if ("componentWeb".equals(applicationType)) {
            		app = componenService.queryMpassComponentDetail((String)task.get(Dict.PROJECT_ID));
				} else if ("componentServer".equals(applicationType)) {
					app = componenService.queryComponenbyid((String)task.get(Dict.PROJECT_ID));
				} else if ("archetypeWeb".equals(applicationType)) {
					app = componenService.queryMpassArchetypeDetail((String)task.get(Dict.PROJECT_ID));
				} else if ("archetypeServer".equals(applicationType)) {
					app = componenService.queryArchetypeDetail((String)task.get(Dict.PROJECT_ID));
				} else if ("image".equals(applicationType)) {
					app = componenService.queryBaseImageDetail((String)task.get(Dict.PROJECT_ID));
				} else {
					app = appService.queryAPPbyid((String)task.get(Dict.PROJECT_ID));
				}
                if(!CommonUtils.isNullOrEmpty(app)){
                    result.put(Dict.NEW_ADD_SIGN , (String)app.get(Dict.NEW_ADD_SIGN));
                }
            }
            return result;
        }
        return  result;
    }

    @Override
    public Map<String, Map> queryRqrmntInfoTasks(List<ReleaseRqrmntInfo> releaseRqrmntInfos) throws Exception {
        Set task_ids = new HashSet();
        for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfos) {
            task_ids.addAll(releaseRqrmntInfo.getTask_ids());
        }
        if(CommonUtils.isNullOrEmpty(task_ids)){
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Dict.IDS, task_ids);
        map.put(Dict.REST_CODE, "queryTaskDetailByIds");
        return (Map<String, Map>) restTransport.submit(map);
    }

    @Override
    public boolean queryTaskReleaseConfirmDoc(ReleaseRqrmntInfo releaseRqrmntInfo, Map<String, Map> tasks) throws Exception {
        //若需求下所有任务 存在一个任务打开  则需求打开
        Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
        List list = new ArrayList();
        boolean flag = false;
        for (String task_id : task_ids) {
            Map<String, Object> task = tasks.get(task_id);
            if(CommonUtils.isNullOrEmpty(task)){
                continue;
            }
            String confirmBtn = (String) task.get(Dict.CONFIRMBTN);
            if(!CommonUtils.isNullOrEmpty(confirmBtn) && "1".equals(confirmBtn)){
                return true;
            }
        }
        return flag;
    }

    @Override
    public List<Map> queryNotConfirmDocTasks(ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception {
        //若任务下所有任务 存在一个任务打开  则需求打开
        Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
        List list = new ArrayList();
        for (String task_id : task_ids) {
            Map<String, Object> task = queryTaskInfo(task_id);
            String confirmBtn = (String) task.get(Dict.CONFIRMBTN);
            if(CommonUtils.isNullOrEmpty(confirmBtn) && "0".equals(confirmBtn)){
                list.add(task);
            }
        }
        return list;
    }

    @Override
    public Map queryTaskTestInfo(String task_id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.TASKNO, task_id);
        map.put(Dict.REST_CODE, "testPlanQuery");
        return  (Map)restTransport.submit(map);
    }


    @Override
    public Map queryTaskConfirmRecord(String task_id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, task_id);
        map.put(Dict.TYPE, Constants.CONFIRM_BTN_OPENED);
        map.put(Dict.REST_CODE, "getConfirmRecord");
        Map<String,String> result = null;
        try {
            result = (Map<String,String>) restTransport.submit(map);
        } catch (Exception e) {
            logger.error("getConfirmRecord error with:{}",e);
            return new HashMap<>();
        }
        return result;
    }

    @Override
    public String queryTaskConfirmRecord(ReleaseRqrmntInfo rqrmntInfo) throws Exception {
        if(CommonUtils.isNullOrEmpty(rqrmntInfo)){
            return null;
        }
        Set<String> task_ids = rqrmntInfo.getTask_ids();
        List<String> dates = new ArrayList<>();
        for (String task_id : task_ids) {
            Map<String, Object> map = new HashMap<>();
            map.put(Dict.ID, task_id);
            map.put(Dict.TYPE, Constants.CONFIRM_BTN_OPENED);
            map.put(Dict.REST_CODE, "getConfirmRecord");
            Map<String,String> result = null;
            try {
                result = (Map<String,String>) restTransport.submit(map);
            } catch (Exception e) {
                logger.error("getConfirmRecord error with:{}",e);
            }
            if(!CommonUtils.isNullOrEmpty(result)){
                dates.add(result.get(Dict.OPERATETIME));
            }
        }
        if(CommonUtils.isNullOrEmpty(dates)){
            return null;
        }
        Collections.sort(dates);
        return dates.get(0);
    }

    @Override
    public String queryRqrmntInfoConfirmRecord(ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception {
        if(releaseRqrmntInfo.getType().equals(Constants.RQRMNT_TYPE_INNER)){
            return "不涉及";
        }
        Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
        List<String> timeList = new ArrayList<>();
        List<String> confirmFileDateList = new ArrayList<>();
        for (String task_id : task_ids) {
            Map map = queryTaskConfirmRecord(task_id);
            Map<String, Object> maps = new HashMap<String, Object>();
            List<String> ids = new ArrayList<String>();
            ids.add(task_id);
            maps.put(Dict.IDS, ids);
            maps.put(Dict.REST_CODE, "queryTaskBaseInfoByIds");// queryTaskDetailByIds queryTaskBaseInfoByIds
			Map<String, Object> taskInfo = new HashMap<String, Object>();
			ArrayList<Map<String, Object>> taskInfoList = (ArrayList<Map<String, Object>>) restTransport.submit(maps);
			for (Map<String, Object> map2 : taskInfoList) {
				String taskId = (String) map2.get(Dict.ID);
				taskInfo.put(taskId, map2);
			}
//            Map<String, Object> taskInfo = taskService.queryTaskInfo(task_id);
            if(!CommonUtils.isNullOrEmpty(map) && !CommonUtils.isNullOrEmpty(taskInfo)){
            	String confirmFileDate = (String) taskInfo.get("confirmFileDate");
            	if (CommonUtils.isNullOrEmpty(confirmFileDate)) {
            		timeList.add ((String) map.get(Dict.OPERATETIME));
				} else {
					confirmFileDateList.add (confirmFileDate);
				}
            }
        }
        if(CommonUtils.isNullOrEmpty(timeList)){
            return "未到达";
        }
        Collections.sort(timeList);
        Collections.sort(confirmFileDateList);
        if (CommonUtils.isNullOrEmpty(confirmFileDateList)) {
        	return timeList.get(0);
		} else {
			return confirmFileDateList.get(0);
		}
    }

    @Override
    public Map<String, String> queryRqrmntInfoApp(ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception {
        Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Dict.IDS, task_ids);
		map.put(Dict.REST_CODE, "queryTaskBaseInfoByIds");// queryTaskDetailByIds queryTaskBaseInfoByIds
		Map<String, Object> tasks = new HashMap<String, Object>();
		ArrayList<Map<String, Object>> taskInfoList = (ArrayList<Map<String, Object>>) restTransport.submit(map);
		for (Map<String, Object> map2 : taskInfoList) {
			String taskId = (String) map2.get(Dict.ID);
			tasks.put(taskId, map2);
		}
//        Map<String, Object> tasks = queryTasksByIds(task_ids);
        Set<String> newAppName = new HashSet<>();
        Set<String> systemNames = new HashSet<>();
        Set<String> appName = new HashSet<>();
        Set<String> specialList = new HashSet<>();
        Map<String, String> result = new HashMap<>();
        for (String task_id : task_ids) {
            Map task = (Map)tasks.get(task_id);
            Map review = (Map)task.get(Dict.REVIEW);
            List<String> list = (List<String>)review.get(Dict.SPECIALCASE);
            if(!CommonUtils.isNullOrEmpty(list)){
                specialList.addAll(list);
            }

            if(!CommonUtils.isNullOrEmpty(task.get(Dict.PROJECT_ID))){
                Map<String, Object> app = appService.queryAPPbyid((String) task.get(Dict.PROJECT_ID));
                Map system = (Map)app.get(Dict.SYSTEM);
                if(!CommonUtils.isNullOrEmpty(system)){
                    systemNames.add((String)system.get(Dict.NAME));
                }
                if("0".equals(app.get(Dict.NEW_ADD_SIGN))){
                    newAppName.add((String)app.get(Dict.NAME_EN));
                }
                appName.add((String)app.get(Dict.NAME_EN));
            }
        }
        result.put(Dict.NEW_ADD_SIGN, String.join(";", newAppName));
        result.put(Dict.SYSNAME_CN, String.join(";", systemNames));
        result.put(Dict.APP, String.join(";", appName));
        result.put(Dict.SPECIALCASE, String.join(";", specialList));
        return result;
    }

    @Override
    public Map queryInfoById(String id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, id);
        map.put(Dict.REST_CODE, "queryInfoById");
        return (Map) restTransport.submit(map);
    }
    
    @Override
    public Map<String, Object> querySecurityTestResult(Set<String> ids) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("taskIds", ids);
        map.put(Dict.REST_CODE, "querySecurityTestResult");
        return  (Map<String, Object>) restTransport.submit(map);
    }

}
