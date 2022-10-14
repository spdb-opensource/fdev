package com.spdb.fdev.fdevenvconfig.spdb.cache.impl;

import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.cache.IConfigFileCache;
import com.spdb.fdev.fdevenvconfig.spdb.service.IDeployFileService;
import com.spdb.fdev.fdevenvconfig.spdb.service.impl.ConfigFileServiceImpl;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @date 2019/8/5 13:58
 */
@Component
@RefreshScope
public class ConfigFileCacheImpl implements IConfigFileCache {

    @Value("${user.api}")
    private String userApi;

    @Autowired
    private ConfigFileServiceImpl configFileService;
    @Autowired
    private IDeployFileService deployFileService;
    @Autowired
    private RestTransport restTransport;

    /***
     * 对每一个应用中的用户信息进行懒加载
     * @param key 每个应用的idkey/userNameENskey
     * @param param 向用户模块传入的数据
     * @return 最新的用户信息
     * @throws Exception
     */
    @Override
    @LazyInitProperty(redisKeyExpression = "fenvconfig.user.{key}")
    public Map<String, Map> getUsersByIdsOrUserNameEn(String key, Map<String, List<String>> param) throws Exception {
        Map<String, Object> data = new HashMap<>(param);
        data.put(Dict.REST_CODE, "queryByUserCoreData");
        Object submit = this.restTransport.submit(data);
        return (Map<String, Map>) submit;
    }

    @Override
    public List<Map> preQueryConfigDependency(Map<String, Object> requestParam) throws Exception {
        // 实体英文名
        String modelNameEn = ((String) requestParam.get(Constants.MODEL_NAME_EN)).trim();
        // 实体的一条属性字段
        String fieldNameEn = (String) requestParam.get(Constants.FIELD_NAME_EN);
        // 查询分支范围
        List<String> range = (List<String>) requestParam.get(Constants.RANGE);
        if (modelNameEn.startsWith("ci_")) {
            // 不区分分支
            return deployFileService.queryDeployDependency(modelNameEn, fieldNameEn);
        }
        return configFileService.queryConfigDependency(modelNameEn, fieldNameEn, range);
    }
}
