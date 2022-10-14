package com.spdb.fdev.spdb.service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Author:guanz2
 * @Date:2021/10/5-13:43
 * @Description:scc_yaml service 层
 */
public interface ISccYamlService {
    List<LinkedHashMap<String, Object>> getSccOldYaml(String resource_code) throws Exception;

    List<LinkedHashMap<String, Object>> getSccNewYaml(String namespace_code, String resource_code) throws Exception;
}
