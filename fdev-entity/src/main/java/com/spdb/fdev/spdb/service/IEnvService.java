package com.spdb.fdev.spdb.service;

import com.spdb.fdev.spdb.entity.Env;
import com.spdb.fdev.spdb.entity.EnvType;

import java.util.List;
import java.util.Map;

public interface IEnvService {
    /**
     * 新增环境
     */
    void add(Map<String, Object> req) throws Exception;

    /**
     * 查询环境
     */
    List<Env> queryList(Map<String, Object> req) throws Exception;




    /**
     * 新增环境类型
     * @param req
     * @throws Exception
     */
    void addEnvType(Map<String, Object> req) throws Exception;


    /**
     * 查询环境类型
     * @param req
     * @return
     * @throws Exception
     */
    List<EnvType> queryEnvTypeList(Map<String, Object> req) throws Exception;
}
