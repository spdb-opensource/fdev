package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.EsfConfiguration;
import com.spdb.fdev.release.entity.EsfRegistration;

import java.util.List;
import java.util.Map;

public interface IEsfRegistration {
    /**
     * 批量插入esf配置中心表
     * */
    void batchAdd(Map<String,Object> req) throws Exception;

    /**
     * 查询esf配置中心
     */
    List<EsfConfiguration> queryEsfConfig(Map<String,Object> req) throws Exception;
    /**
     *  esf新增
     */
    void addEsfRegistration(Map<String,Object> req) throws Exception;
    /**
     *  esf编辑
     */
    void updateEsf(Map<String,Object> req) throws Exception;
    /**
     *  查询
     */
    Map<String,Object> queryAppStatus(Map<String,Object> req) throws Exception;
    /**
     * 删除esf
     * */
    void delEsf(Map<String,Object> req) throws Exception;

    List<EsfRegistration> queryEsfRegistration(Map<String,Object> req) throws Exception;
    /**
     * 查询应用
     * */
    List<Map<String,Object>> queryApps(Map<String,Object> req) throws Exception;
}
