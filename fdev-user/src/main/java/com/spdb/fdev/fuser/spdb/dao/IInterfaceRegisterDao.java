package com.spdb.fdev.fuser.spdb.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.fuser.spdb.entity.user.InterfaceRegister;

import java.util.List;
import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/12/7
 */
public interface IInterfaceRegisterDao {
    void add(InterfaceRegister interfaceRegister) throws Exception;

    void update(InterfaceRegister interfaceRegister) throws JsonProcessingException;

    void delete(String id) throws Exception;

    Map<String, Object> query(Map param);

    InterfaceRegister queryByInterface(String interfacePath);

    InterfaceRegister queryById(String id);
}
