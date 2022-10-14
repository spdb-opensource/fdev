package com.spdb.fdev.release.service;

import java.util.Map;

/**
 * 发送接口模块接口类
 */
public interface IAppDatService {
    /**
     * 查询最新的appjson
     * @param application_name_en
     * @param branch
     * @return
     */
    Map<String, Object> queryLastAppJson(String application_name_en , String branch) throws Exception;
}
