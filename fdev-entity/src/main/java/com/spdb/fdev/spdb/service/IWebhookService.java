package com.spdb.fdev.spdb.service;

import java.util.Map;

public interface IWebhookService {

    /**
     * 保存外部配置文件引用的实体信息
     *
     * @param parse
     * @throws Exception 
     */
    void saveAppConfigMap(Map<String, Object> parse) throws Exception;

}
