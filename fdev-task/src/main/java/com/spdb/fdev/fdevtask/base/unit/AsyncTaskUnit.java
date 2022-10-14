package com.spdb.fdev.fdevtask.base.unit;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.ToDoList;
import com.spdb.fdev.fdevtask.spdb.service.FdocmanageService;
import com.spdb.fdev.fdevtask.spdb.service.INotifyApi;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;
import com.spdb.fdev.fdevtask.spdb.service.TodoListApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.Future;

/**
 * 异步处理组建
 */
@Component
@RefreshScope
public class AsyncTaskUnit {
    private static final Logger log = LoggerFactory.getLogger(AsyncTaskUnit.class);

    @Value("${ui.review.link}")
    private String baseLink;

    @Resource
    private IUserApi iUserApi;

    @Resource
    private INotifyApi iNotifyApi;

    @Resource
    private FdocmanageService fdocmanageService;

    @Autowired
    private TodoListApi todoListApi;

    @Async
    public void dealNoReturnTask() {

    }

    /**
     * 获取文件列表
     *
     * @return Future
     */
    @Async
    public Future<List<Map<String, String>>> dealTaskDocList(String moduleName, String path) {
        List<String> filesPath = fdocmanageService.getFilesPath(moduleName, path);
        //处理相关逻辑
        List<Map<String, String>> lists = new ArrayList<>();
        filesPath.forEach((str) -> {
            List<String> list = Lists.reverse(Arrays.asList(str.split("/")));
            if (list.size() >= 2) {
                HashMap<String, String> map = Maps.newHashMap();
                map.put("name", list.get(0));
                map.put("type", list.get(1));
                map.put("path", str.replaceFirst("fdev-task/", ""));
                lists.add(map);
            }
        });
//        if (!CommonUtils.isNullOrEmpty(filesPath)) {
//            List<String> collect = filesPath.stream()
//                    .flatMap((str) ->
//                            Lists.reverse(Arrays.asList(str.split("/"))).stream())
//                    .limit(2).filter(str -> !"".equals(str))
//                    .collect(Collectors.toList());
//            if (collect.size() >= 2) {
//                map.put("name", collect.get(0));
//                map.put("type", collect.get(1));
//            }
//        }
        return new AsyncResult<>(lists);
    }

    @Async
    public void sendToList(FdevTask fdevTask, String userId, String desc, String type) throws Exception {
        String link = baseLink + fdevTask.getId() + "/design";
        HashMap todoMap = new HashMap<>();
        todoMap.put(Dict.USER_IDS, Arrays.asList(userId));
        todoMap.put(Dict.MODULE, Dict.TASK);
        todoMap.put(Dict.DESCRIPTION, desc);
        todoMap.put(Dict.LINK, link);
        todoMap.put(Dict.TYPE, type);
        todoMap.put(Dict.TARGET_ID, fdevTask.getId());
        todoMap.put(Dict.CREATE_USER_ID, fdevTask.getReviewer());
        ToDoList toDoList = CommonUtils.mapToBean(todoMap, ToDoList.class);
        toDoList.setTask_id(fdevTask.getId());
        toDoList.setCreate_date(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));
        toDoList.setModule(Constants.TASK_MODULE);
        toDoList.setProject_id(fdevTask.getProject_id());
        this.todoListApi.save(toDoList);
        try {
            iUserApi.addTodoList(todoMap);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            log.info("发送todoList失败,请求参数:{}Error Trace:{}", todoMap, sw.toString());
        }
    }

    @Async
    public void sendNotify(FdevTask fdevTask, String targetId, String desc) {
        String link = baseLink + fdevTask.getId() + "/design";
        try {
            Map map = iUserApi.queryUser(new HashMap() {{
                put(Dict.ID, targetId);
            }});
            String user_name_en = (String) map.get("user_name_en");
            //发送通知
            iNotifyApi.sendFdevNotify(desc, "0", new String[]{user_name_en}, link, "设计还原审核任务审核");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            log.info("发送sendNotify失败,审核人id:{}Error Trace:{}", targetId, sw.toString());
        }
    }

    @Async
    public void deleteTodolist(String taskId, String type, String userId) throws Exception {
        Map param = new HashMap();
        param.put(Dict.TARGET_ID, taskId);
        param.put(Dict.TYPE, type);
        param.put(Dict.EXECUTOR_ID, userId);
        param.put(Dict.MODULE, "task");
        iUserApi.updateTodoList(param);
    }
}
