package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.ToDoList;
import com.spdb.fdev.fdevtask.spdb.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MergeInfoCallBackImpl implements MergeInfoCallBack {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志
    @Autowired
    private TodoListApi todoListApi;

    @Autowired
    private IFdevTaskService fdevTaskService;

    @Autowired
    private IUserApi iUserApi;

    @Autowired
    private IAppApi appApi;

    @Override
    public JsonResult mergeInfoCallBack(String project_id, String iid, String state, String username) throws Exception {
        ToDoList toDoList = todoListApi.query(new ToDoList(iid, project_id));
        if (!CommonUtils.isNullOrEmpty(toDoList) && !CommonUtils.isNullOrEmpty(toDoList.getTask_id())) {
            String date = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1);
            List<FdevTask> tmp = fdevTaskService.query(new FdevTask.FdevTaskBuilder().id(toDoList.getTask_id()).init());
            FdevTask task = CommonUtils.isNullOrEmpty(tmp) ? null : tmp.get(0);
            if (!CommonUtils.isNullOrEmpty(task)  && "uatMergeRequest".equals(toDoList.getType())&& "merged".equals(state)) {
                //合并代码不再自动更新阶段，只保存合并代码时间
                task.setUat_merge_time(date);
//                if(Dict.TASK_STAGE_SIT.equals(task.getStage())){
//                    task.setStage(Dict.TASK_STAGE_UAT);
//                    task.setStart_uat_test_time(date);
//                }
                //是否涉及内测,不涉及补充sit时间
                Map<String, Object> projectInfo = fdevTaskService.getProjectInfo(task.getProject_id(), task.getApplicationType());
                String testSwitch = CommonUtils.isNullOrEmpty(projectInfo.get(Dict.ISTEST)) ? "1" : (String) projectInfo.get(Dict.ISTEST);
                if(!"2".equals(String.valueOf(task.getTaskType())) && "0".equals(testSwitch)){
                    task.setStage(Dict.TASK_STAGE_UAT);
                    task.setStart_uat_test_time(date);
                    task.setStart_inner_test_time(date);
                }
                fdevTaskService.update(task);
            }
        }
        
        if (CommonUtils.isNullOrEmpty(toDoList)) {
            logger.info("未记录代办的分支合并数据！");
            return JsonResultUtil.buildSuccess();
        }
        Map userParam = new HashMap();
        userParam.put(Dict.GIT_USER, username);
        Map result = iUserApi.queryUser(userParam);
        if (CommonUtils.isNullOrEmpty(result)) {
            logger.error("用户不存在！");
        }
        Map param = new HashMap();
        param.put(Dict.TARGET_ID, toDoList.getTarget_id());
        param.put(Dict.TYPE, toDoList.getType());
        param.put(Dict.EXECUTOR_ID, result.get(Dict.ID));
        param.put(Dict.MODULE, toDoList.getModule());
        iUserApi.updateTodoList(param);
        toDoList.setExecutor_id((String) result.get(Dict.ID));
        toDoList.setStatus(state);
        toDoList.setExecutor_name((String) result.get(Dict.USER_NAME_EN));
        toDoList.setExecutor_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));
        todoListApi.update(toDoList);

        return JsonResultUtil.buildSuccess();
    }
}
