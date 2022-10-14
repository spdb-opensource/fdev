package com.spdb.fdev.fdevapp.spdb.service.impl;

import com.spdb.fdev.fdevapp.base.dict.Constant;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.service.IComponentService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ComponentServiceImpl implements IComponentService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    private RestTransport restTransport;

    /**
     * 获取骨架环境配置模版文件
     *
     * @param branch
     * @param archetype_id
     * @return
     */
    @Override
    public String queryConfigTemplate(String branch, String archetype_id) {
        try {
            Map param = new HashMap<>();
            param.put(Dict.BRANCH, branch);
            param.put(Dict.ARCHETYPE_ID, archetype_id);
            param.put(Dict.REST_CODE, Dict.QUERYCONFIGTEMPLATE);
            Object submit = restTransport.submit(param);
            return (String) submit;
        } catch (Exception e) {
            logger.error("获取骨架模版配置文件为空", e.getStackTrace());
        }
        return null;
    }

    @Override
    public String queryBaseImageVersion(Map<String, String> requestMap) {
        Map<String, Object> responseMap = null;
        requestMap.put(Dict.REST_CODE, "queryBaseImageVersion");
        try {
            responseMap = (Map<String, Object>) this.restTransport.submit(requestMap);
        } catch (Exception e) {
            logger.error("请求组件模块queryBaseImageVersion接口出错:{}", e.getMessage());
        }
        if (responseMap != null) {
            return (String) responseMap.get(Constant.FDEV_CAAS_BASE_IMAGE_VERSION);
        } else {
            return "";
        }
    }
}
