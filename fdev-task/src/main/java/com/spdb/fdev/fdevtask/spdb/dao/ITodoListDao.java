package com.spdb.fdev.fdevtask.spdb.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.fdevtask.spdb.entity.ToDoList;

import java.util.List;

public interface ITodoListDao {

    public ToDoList save(ToDoList toDoList);

    public ToDoList query(ToDoList toDoList);

    public ToDoList update(ToDoList toDoList) throws JsonProcessingException;

    List<ToDoList> queryByParam(ToDoList toDoList) throws JsonProcessingException;

    Long remove(ToDoList toDoList) throws JsonProcessingException;
}
