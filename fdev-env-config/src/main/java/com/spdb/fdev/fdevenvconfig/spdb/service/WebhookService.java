package com.spdb.fdev.fdevenvconfig.spdb.service;

import java.util.Map;

public interface WebhookService {

    /**
     * 保存Node应用引用的实体信息
     *
     * @param parse
     */
    void saveNodeAppConfigMap(Map<String, Object> parse);

    /**
     * 保存外部配置文件引用的实体信息
     *
     * @param parse
     */
    void saveAppConfigMap(Map<String, Object> parse);

}
