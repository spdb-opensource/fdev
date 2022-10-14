package com.spdb.fdev.spdb.service;

import java.util.List;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2022/2/23-17:11
 * @Description:
 */
public interface IBatchModifyService {

    //修改esf应用的env，云上云下环境的所有生产环境，不包括灰度
    void updateEsfEnv(List<Map<String, Object>> list) throws Exception;

    //修改云上yaml文件
    void updateSccYamlBatch(List<Map<String, Object>> list) throws Exception;

    //云上、云下、生产和灰度添加initContainers
    void addInitContainers(List<String> deploy) throws  Exception;

    //云上修改滚动发布参数、dnsConfig,configMap
    void updateSccRollingParams(List<String> proDeployments, List<String> grayDeployments) throws Exception;
}
