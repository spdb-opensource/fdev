package com.spdb.fdev.fdevtask.spdb.service;

import java.util.Map;

public interface IEnvConfigApi {

    Map queryEnvByNameEn(String name, Integer project_id) throws Exception;

    Map queryEnv(String id) throws Exception;

    Map mapFilter(Map map) throws Exception;
}
