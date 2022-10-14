package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.enums.OperateTaskTypeEnum;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.FiledChangeValueUtils;
import com.spdb.fdev.fdevtask.spdb.dao.OperateChangeLog;
import com.spdb.fdev.fdevtask.spdb.entity.ChangeValueLog;
import com.spdb.fdev.fdevtask.spdb.entity.IgnoreFields;
import com.spdb.fdev.fdevtask.spdb.entity.Task;
import com.spdb.fdev.fdevtask.spdb.service.DemandManageApi;
import com.spdb.fdev.fdevtask.spdb.service.OperateChangeLogService;
import com.spdb.fdev.fdevtask.spdb.service.ProcessApi;
import com.spdb.fdev.fdevtask.spdb.service.ServiceApi;
import org.apache.commons.lang.ArrayUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

/**
 * @author patrick
 * @date 2021/3/15 上午10:54
 * @Des 操作日志服务类
 * 最簡單的事是堅持，最難的事還是堅持
 */
@Service
public class OperateChangeLogServiceImpl implements OperateChangeLogService {

    public static String[] ignoreFields = new String[]{IgnoreFields.UPDATE_TIME,
            IgnoreFields.ID, IgnoreFields.DOC, IgnoreFields.IDD ,IgnoreFields.COMMIT_SHA,IgnoreFields.BRANCH_TYPE,IgnoreFields.BK_INFO};

    @Autowired
    private OperateChangeLog operateChangeLog;

    @Autowired
    private DemandManageApi demandManageApi;

    @Autowired
    private ProcessApi processApi;

    @Autowired
    private ServiceApi serviceApi;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Override
    public void asyncLogCreate(Task task) throws Exception{
        try{
            ChangeValueLog changeValueLog = ChangeValueLog.builder().taskId(task.getId())
                    .afterValue(task.getName())
                    .type(OperateTaskTypeEnum.CREATE.getValue() + "").build();
            User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
            autoUser(changeValueLog,user);
            operateChangeLog.saveOperateChangeLog(changeValueLog);
        }catch (Exception e){
            exceptionHandler(e);
        }

    }

    @Override
    public void asyncLogUpload(List<String> docNames, String taskId)  throws Exception{
        ChangeValueLog changeValueLog = ChangeValueLog.builder().taskId(taskId)
                .afterValue(docNames)
                .type(OperateTaskTypeEnum.UPLOAD_FILE.getValue() + "").build();
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        autoUser(changeValueLog,user);
        operateChangeLog.saveOperateChangeLog(changeValueLog);
    }

    @Override
    public void asyncUpdateLog(Object newObj, Object oldObj, String taskId) throws Exception{
        try{
            List<ChangeValueLog> fieldValueChangeRecords =
                    FiledChangeValueUtils.getFieldValueChangeRecords(newObj, oldObj, OperateTaskTypeEnum.UPDATE,
                            filed -> ArrayUtils.contains(ignoreFields, filed.getName()));
            User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
            fieldValueChangeRecords.forEach(changeValueLog -> {
                changeValueLog.setTaskId(taskId);
                autoUser(changeValueLog,user);
                implName(changeValueLog);
                statusName(changeValueLog);
                relateApplication(changeValueLog);
                branch(changeValueLog);
                version(changeValueLog);
                operateChangeLog.saveOperateChangeLog(changeValueLog);
            });
        }catch (Exception e){
            exceptionHandler(e);
        }

    }

    @Override
    public void asyncDeleteLog(Task task)  throws Exception{
        try{
            ChangeValueLog changeValueLog = ChangeValueLog.builder().taskId(task.getId())
                    .afterValue(task.getName())
                    .type(OperateTaskTypeEnum.DELETE.getValue() + "").build();
            User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
            autoUser(changeValueLog,user);
            operateChangeLog.saveOperateChangeLog(changeValueLog);
        }catch (Exception e){
            exceptionHandler(e);
        }

    }

    @Override
    public List<ChangeValueLog> getOperateChangeLogByTaskId(String taskId) {
        return operateChangeLog.getOperateChangeLogByTaskId(taskId);
    }

    @Override
    public void asyncUpdateTaskListLog(List<Task> newTaskList, List<Task> oldTaskList)  throws Exception{
        try {
            ArrayList<Map<String, Object>> taskList = new ArrayList<>();
            HashMap<String, Object> taskMap = new HashMap<>();
            for (Task newTask:newTaskList) {
                for (Task oldTask:oldTaskList) {
                    if(newTask.getId().equals(oldTask.getId())){
                        taskMap.put(Dict.NEW_TASK,newTask);
                        taskMap.put(Dict.OLD_TASK,oldTask);
                        taskList.add(taskMap);
                    }
                }
            }
            for (Map task:taskList){
                List<ChangeValueLog> fieldValueChangeRecords =
                        FiledChangeValueUtils.getFieldValueChangeRecords(task.get(Dict.NEW_TASK), task.get(Dict.OLD_TASK), OperateTaskTypeEnum.UPDATE,
                                filed -> ArrayUtils.contains(ignoreFields, filed.getName()));
                User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                        .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
                fieldValueChangeRecords.forEach(changeValueLog -> {
                    changeValueLog.setTaskId(((Task)task.get(Dict.NEW_TASK)).getId());
                    implName(changeValueLog);
                    statusName(changeValueLog);
                    relateApplication(changeValueLog);
                    branch(changeValueLog);
                    version(changeValueLog);
                    autoUser(changeValueLog,user);
                    operateChangeLog.saveOperateChangeLog(changeValueLog);
                });
            }
        }catch (Exception e){
            exceptionHandler(e);
        }

    }

    public void implName(ChangeValueLog changeValueLog){
        if (changeValueLog.getField().equals(Dict.IMPL_UNIT_ID)){
            ArrayList<String> implIds = new ArrayList<>();
            Object beforeValue = changeValueLog.getBeforeValue();
            Object afterValue = changeValueLog.getAfterValue();
            if(!CommonUtils.isNullOrEmpty(beforeValue)){
                implIds.add((String)beforeValue);
            }
            implIds.add((String)afterValue);
            if(!CommonUtils.isNullOrEmpty(implIds)){
                List<Map> implUnits = demandManageApi.queryIpmpListByid(implIds);
                if (!CommonUtils.isNullOrEmpty(implUnits)){
                    for (Object implUnit:implUnits){
                        if(!CommonUtils.isNullOrEmpty(implUnit)){
                            Map implUnitMap = (Map) implUnit;
                            if(implUnitMap.get(Dict.ID).equals(beforeValue)){
                                changeValueLog.setBeforeValue(implUnitMap.get(Dict.IMPL_CONTENT));
                            }
                            if(implUnitMap.get(Dict.ID).equals(afterValue)){
                                changeValueLog.setAfterValue(implUnitMap.get(Dict.IMPL_CONTENT));
                            }
                        }

                    }
                }
            }
        }
    }

    public void statusName(ChangeValueLog changeValueLog){
        if (changeValueLog.getField().equals(Dict.STATUS)){
            ArrayList<String> status = new ArrayList<>();
            Object beforeValue = changeValueLog.getBeforeValue();
            Object afterValue = changeValueLog.getAfterValue();
            changeValueLog.setType(OperateTaskTypeEnum.UPDATE_STATUS.getValue() + "");
            if(!CommonUtils.isNullOrEmpty(beforeValue)){
                status.add((String)beforeValue);
            }
            status.add((String)afterValue);
            if(!CommonUtils.isNullOrEmpty(status)){
                Map instance = processApi.queryInstanceByTaskId(changeValueLog.getTaskId());
                List<Map> processStatusList = CommonUtils.castList(instance.get(Dict.PROCESS_STATUS_LIST),Map.class);
                for (Map process: processStatusList) {
                    if(process.get(Dict.ID).equals(beforeValue)){
                        changeValueLog.setBeforeValue(process.get(Dict.NAME));
                    }
                    if(process.get(Dict.ID).equals(afterValue)){
                        changeValueLog.setAfterValue(process.get(Dict.NAME));
                    }
                }
            }
        }
    }

    public void relateApplication(ChangeValueLog changeValueLog){
        if (changeValueLog.getField().equals(Dict.RELATED_APPLICATION)){
            String afterValue = (String)changeValueLog.getAfterValue();
            if(!CommonUtils.isNullOrEmpty(afterValue)){
                Map service = serviceApi.queryApp(afterValue);
                if(service.get(Dict.ID).equals(afterValue)){
                    changeValueLog.setAfterValue(service.get(Dict.NAMECN));
                }
            }
            changeValueLog.setType(OperateTaskTypeEnum.RELATED_APPLICATION.getValue() + "");
        }
    }

    public void branch(ChangeValueLog changeValueLog){
        if (changeValueLog.getField().equals(Dict.BRANCH_NAME)){
            changeValueLog.setType(OperateTaskTypeEnum.CREATE_BRANCH.getValue() +"");
        }
    }

    public void version(ChangeValueLog changeValueLog){
        if (changeValueLog.getField().equals(Dict.VERSION_ID)){
            Object afterValue = changeValueLog.getAfterValue();
            Object beforeValue = changeValueLog.getBeforeValue();
            if(!CommonUtils.isNullOrEmpty(afterValue)){
                Map version = serviceApi.queryVersionDetail((String)afterValue);
                if(version.get(Dict.ID).equals(afterValue)){
                    changeValueLog.setAfterValue(version.get(Dict.NAME));
                }
            }
            if(!CommonUtils.isNullOrEmpty(beforeValue)){
                Map version = serviceApi.queryVersionDetail((String)beforeValue);
                if(version.get(Dict.ID).equals(beforeValue)){
                    changeValueLog.setBeforeValue(version.get(Dict.NAME));
                }
            }
        }
    }
    void autoUser(ChangeValueLog changeValueLog,User user){
        if (user == null) {
            changeValueLog.setUserId("fdev_id");
            changeValueLog.setUserName("系统操作");
        } else {
            changeValueLog.setUserId(user.getId());
            changeValueLog.setUserName(user.getUser_name_cn());
        }
        ObjectId objectId = new ObjectId();
        changeValueLog.set_id(objectId);
        changeValueLog.setId(objectId.toString());
        changeValueLog.setExecTime(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));
    }

    void exceptionHandler(Exception e)throws Exception{
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        ChangeValueLog changeValueLog = new ChangeValueLog();
        autoUser(changeValueLog,user);
        changeValueLog.setFieldName(e.getMessage());
        operateChangeLog.saveOperateChangeLog(changeValueLog);
    }
}
