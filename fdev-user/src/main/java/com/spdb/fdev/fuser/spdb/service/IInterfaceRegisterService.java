package com.spdb.fdev.fuser.spdb.service;

import com.spdb.fdev.fuser.spdb.entity.user.InterfaceRegister;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author liux81
 * @DATE 2021/12/7
 */
public interface IInterfaceRegisterService {
    void add(InterfaceRegister interfaceRegister) throws Exception;

    void update(InterfaceRegister interfaceRegister) throws Exception;

    void delete(Map param) throws Exception;

    Map<String, Object> query(Map param) throws Exception;

    Set<String> getRolesByInterface(Map param);

    void export(Map param, HttpServletResponse resp) throws Exception;
}
