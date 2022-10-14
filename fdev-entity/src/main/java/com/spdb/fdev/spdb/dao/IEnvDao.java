package com.spdb.fdev.spdb.dao;

import com.spdb.fdev.spdb.entity.Env;
import com.spdb.fdev.spdb.entity.EnvType;

import java.util.List;
import java.util.Map;

public interface IEnvDao {

    void save(List<Env> envs) throws Exception;

    List<Env> queryList(Map<String, Object> req) throws Exception;

    Env queryEnv(Map<String, Object> req) throws Exception;

    void saveEnvType(List<EnvType> envTypes) throws Exception;

    List<EnvType> queryEnvTypeList(Map<String, Object> req) throws Exception;
}

