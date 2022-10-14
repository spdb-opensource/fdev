package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.spdb.dao.ITodoListDao;
import com.spdb.fdev.fdevtask.spdb.entity.ToDoList;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;
import com.spdb.fdev.fdevtask.spdb.service.TodoListApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TodoListApiImpl implements TodoListApi {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

    @Autowired
    private ITodoListDao iTodoListDao;

    @Autowired
    private IUserApi userApi;

    @Override
    public ToDoList save(ToDoList toDoList) throws Exception {
        return iTodoListDao.save(toDoList);
    }

    @Override
    public ToDoList query(ToDoList toDoList) throws Exception {
        return iTodoListDao.query(toDoList);
    }

    @Override
    public List<ToDoList> queryByParam(ToDoList toDoList) throws Exception {
        return iTodoListDao.queryByParam(toDoList);
    }

    @Override
    public ToDoList update(ToDoList toDoList) throws Exception {
        return iTodoListDao.update(toDoList);
    }

    @Override
    public Long remove(ToDoList toDoList) throws Exception {
        List<ToDoList> toDoLists = this.iTodoListDao.queryByParam(toDoList);
        if (toDoLists.size() == 0 || null == toDoLists) {
            logger.info("任务" + toDoList.getTask_id() + "不存在有相关的待办");
            return 0L;
        }
        Long removeCount = iTodoListDao.remove(toDoList);
        for (ToDoList entity : toDoLists) {
            Map<String, Object> param = new HashMap<>();
            param.put(Dict.MODULE, Dict.TASK);
            param.put(Dict.TYPE, entity.getType());
            param.put(Dict.TARGET_ID, entity.getTarget_id());
            //调用用户模块删除待办接口
            userApi.deleteTodoList(param);
        }

        return removeCount;
    }
}
