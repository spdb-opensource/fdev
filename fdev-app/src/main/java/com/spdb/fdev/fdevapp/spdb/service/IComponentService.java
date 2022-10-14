package com.spdb.fdev.fdevapp.spdb.service;

import java.util.Map;

public interface IComponentService {
    String queryConfigTemplate(String branch, String archetype_id);

    // 获取最新镜像版本
    String queryBaseImageVersion(Map<String, String> requestMap);
}
