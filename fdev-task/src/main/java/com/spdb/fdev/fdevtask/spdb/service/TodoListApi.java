package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.fdevtask.spdb.entity.ToDoList;

import java.util.List;

public interface TodoListApi {
    public ToDoList save(ToDoList toDoList)throws Exception;

    public ToDoList query(ToDoList toDoList)throws Exception;

    public ToDoList update(ToDoList toDoList)throws Exception;

    List<ToDoList> queryByParam(ToDoList toDoList) throws Exception;

    Long remove(ToDoList toDoList) throws Exception;
}
