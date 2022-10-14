package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnvUpdateApply;

import java.util.List;
import java.util.Map;

public interface ModelEnvUpdateApplyDao {

    void save(ModelEnvUpdateApply modelEnvUpdateApply);

    ModelEnvUpdateApply get(String id);

    void updateStatus(String id, String status);

    void update(String id, List<Map<String, Object>> variables, String status);

    List<ModelEnvUpdateApply> listCheckingModelEnvUpdateApplys(String modelId, String email);

    List<ModelEnvUpdateApply> listCheckingModelEnvUpdateApplys(String modelId, String envId, String email);

    Map<String, Object> listByPage(String modelId, String appUserId, String status, int page, int perPage);

    List<ModelEnvUpdateApply> listCheckingByDate(String date);

    void updateStatus(List<String> ids);
}
