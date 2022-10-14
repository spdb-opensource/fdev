package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.AppConfigMapping;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelField;

import java.util.List;
import java.util.Map;

public interface IAppConfigMappingService {
    /**
     * 扫描 gitlab 对应分支的配置文件 保存到数据库
     *
     * @param requestParam
     * @return
     */
    void saveAppConfigByWebHook(Map<String, Object> requestParam) throws Exception;

    void saveOrUpdate(Integer projectId, String branch, List<ModelField> modelField, String node) throws Exception;

    /**
     * 根据实体英文名及实体属性英文名，获得应用Gitlab id及分支信息
     *
     * @param modelName
     * @param field
     * @return
     */
    List<AppConfigMapping> getAppIdAndBranch(String modelName, String field);

    /**
     * 通过 应用 gitlab id ，分支名，环境 查询 实体环境映射值
     * @param requestParam
     */
    Map queryConfigVariables(Map<String, Object> requestParam) throws Exception;
}
