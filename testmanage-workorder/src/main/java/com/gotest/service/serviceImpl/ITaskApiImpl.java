package com.gotest.service.serviceImpl;

import com.gotest.dao.WorkOrderMapper;
import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.service.ITaskApi;
import com.gotest.utils.MyUtil;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ITaskApiImpl implements ITaskApi {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Override
    public Map queryByJobId(String id) throws Exception {
        Map param = new HashMap<>();
        param.put(Dict.JOBID, id);
        param.put(Dict.REST_CODE, "fdev.task.queryByJobId");
        Map result = null;
        try {
            result = (Map) restTransport.submitSourceBack(param);
        } catch (Exception e) {
            throw new FtmsException(com.gotest.dict.ErrorConstants.DATA_QUERY_ERROR, new String[]{"数据查询失败"});
        }
        return result;
    }

    @Override
    public Map queryTaskDetail(String id) throws Exception {
        Map param = new HashMap<>();
        param.put(Dict.ID, id);
        param.put(Dict.REST_CODE, "fdev.task.query");
        List<Map> result = null;
        try {
            result = (List) restTransport.submitSourceBack(param);
        } catch (Exception e) {
            throw new FtmsException(com.gotest.dict.ErrorConstants.DATA_QUERY_ERROR, new String[]{"数据查询失败"});
        }
        if (Util.isNullOrEmpty(result)) {
            return null;
        }
        return result.get(0);
    }

    @Override
    public String queryTaskDevelopNameCns(String taskId, String type) throws Exception {
        if (MyUtil.isNullOrEmpty(taskId)) {
            return "";
        }
        //根据任务id查询工单 ，判断是否是新老工单
        String fdevNew = workOrderMapper.queryNewFdevBytaskId(taskId, Constants.ORDERTYPE_FUNCTION);
        if ("1".equals(fdevNew)){
            Map<String, Object> task = getNewTaskById(taskId);
            if (Util.isNullOrEmpty(task)){
                return  "";
            }
            List<String> developers = (List<String>) task.get("assigneeList");
            String developer = developers.get(0);
            List names = new ArrayList();
            if (type.equals(Dict.USER_NAME_EN)){
                String name = (String) userService.queryUserCoreDataById(developer).get(Dict.USER_NAME_EN);
                names.add(name);
            }else {
                String name = (String) userService.queryUserCoreDataById(developer).get(Dict.USER_NAME_CN);
                names.add(name);
            }
            return StringUtils.join(names, ",");
        }else {
            Map task = (Map) queryTaskDetailByIds(Arrays.asList(taskId)).get(taskId);
            if (Util.isNullOrEmpty(task)) {
                return "";
            }
            List<Map> developers = (List) task.get(Dict.DEVELOPER);
            List names = new ArrayList();
            if (type.equals(Dict.USER_NAME_EN)) {
                for (Map developer : developers) {
                    names.add(developer.get(Dict.USER_NAME_EN));
                }
            } else {
                for (Map developer : developers) {
                    names.add(developer.get(Dict.USERNAMECN));
                }
            }
            return StringUtils.join(names, ",");
        }
    }

    @Override
    public Map<String, String> queryTasksDeveloper(List<String> taskIds, String type) {
        if (Util.isNullOrEmpty(taskIds)) {
            return null;
        }
        Map<String, Object> tasks = queryTaskDetailByIds(taskIds);
        Map<String, String> nameMap = new HashMap<>();
        for (String taskId : taskIds) {
            Map<String, Object> task = (Map<String, Object>) tasks.get(taskId);
            if (Util.isNullOrEmpty(task)) {
                continue;
            }
            List<Map> developers = (List) task.get(Dict.DEVELOPER);
            List names = new ArrayList();
            if (type.equals(Dict.USER_NAME_EN)) {
                for (Map developer : developers) {
                    names.add(developer.get(Dict.USER_NAME_EN));
                }
            } else {
                for (Map developer : developers) {
                    names.add(developer.get(Dict.USERNAMECN));
                }
            }
            nameMap.put(taskId, StringUtils.join(names, ","));
        }
        return nameMap;
    }

    @Override
    public Map<String, Object> queryTaskDetailByIds(List<String> taskIds) {
        Map param = new HashMap<>();
        param.put(Dict.IDS, taskIds);
        param.put(Dict.REST_CODE, "fdev.task.queryTaskDetailByIds");
        Map<String, Object> result = null;
        try {
            result = (Map) restTransport.submitSourceBack(param);
        } catch (Exception e) {
            logger.error("fail to get task infos by ids");
            throw new FtmsException(com.gotest.dict.ErrorConstants.DATA_QUERY_ERROR, new String[]{"数据查询失败"});
        }
        if (Util.isNullOrEmpty(result)) {
            return null;
        }
        return result;
    }

    /**
     * 根据新任务id查任务详情
     *
     * @param taskId
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> queryNewTaskDetail(List<String> taskId) throws Exception {
        Map send = new HashMap();
        send.put(Dict.IDS, taskId);
        send.put(Dict.REST_CODE, "getTaskByIds");
        try {
            return (List<Map<String, Object>>) restTransport.submitSourceBack(send);
        } catch (Exception e) {
            logger.error("fail to get new Task info" + e);
        }
        return null;
    }

    /**
     * 查询重构fdev任务指派人
     *
     * @param task
     * @return
     * @throws Exception
     */
    @Override
    public List queryNewTaskAssignEns(Map task) throws Exception {
        List<String> result = new ArrayList<>();
        List<String> assigns = (List<String>) task.get("assigneeList");
        for (String assign : assigns) {
            try {
                String name = (String) userService.queryUserCoreDataById(assign).get(Dict.USER_NAME_EN);
                result.add(name);
            } catch (Exception e) {
                logger.error("fail to find user info");
            }
        }
        return result;
    }

    /**
     * 变更新任务模块玉衡插件状态
     *
     * @param taskId
     * @param s
     */
    @Override
    public void changeTestComponentStatus(String taskId, String s) {
        Map send = new HashMap();
        send.put(Dict.TASKID, taskId);
        send.put(Dict.STATUS, s);
        send.put(Dict.REST_CODE, "changeTestComponentStatus");
        try {
            restTransport.submitSourceBack(send);
        } catch (Exception e) {
            logger.error("fail to update task test status");
        }
    }

    //查询新fdev的任务详情
    @Override
    public Map<String,Object> getNewTaskById(String id) {
        Map send = new HashMap();
        send.put(Dict.ID, id);
        send.put(Dict.REST_CODE, "getTaskById");
        Map<String,Object> map=new HashMap<>();
      try {
          map=  (Map<String, Object>) restTransport.submitSourceBack(send);
      }catch (Exception e){
          logger.error("fail to update task test status");
      }finally {
          return map;
      }
    }

    @Override
    public List<Map> queryTaskBaseInfoByIds(List<String> taskIds, List<String> fields, List<String> responseFields) {
        Map<String, Object> param = new HashMap<String, Object>(){{
            put(Dict.IDS, taskIds);
            put(Dict.FIELDS, fields);
            put(Dict.RESPONSEFIELDS, responseFields);
            put(Dict.REST_CODE, "queryTaskBaseInfoByIds");
        }};
        try {
            return (List<Map>) restTransport.submitSourceBack(param);
        }catch (Exception e) {
            logger.info(">>>queryTaskBaseInfoByIds批量查询任务基础信息失败");
        }
        return new ArrayList<>();
    }
}
