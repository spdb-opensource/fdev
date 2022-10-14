package com.spdb.fdev.spdb.service;

import java.util.List;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/10/2-21:59
 * @Description:  caas yaml service接口
 */
public interface ICaasYamlService {

    Map<String, Object> getCaasOldYaml(String deployment_name) throws Exception;

    List<Map> getCaasNewYaml(String deployment_name, String tag) throws Exception;
}
