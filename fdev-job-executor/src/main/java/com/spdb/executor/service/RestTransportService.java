package com.spdb.executor.service;

import java.util.List;
import java.util.Map;

public interface RestTransportService {

    /**
     * 调应用模块接口，获取自动测试环境
     * YApi地址：xxx/project/98/interface/api/6664
     *
     * @param gitlabProjectId
     * @param branch
     * @return
     */
    Map<String, Object> getAutoTestEnv(Integer gitlabProjectId, String branch);

    /**
     * 调环境配置模块接口，获取环境变量
     *
     * @param gitlabProjectId
     * @param env
     * @return
     */
    Map<String, Object> getVariablesMapping(Integer gitlabProjectId, String env);

    List querySitMergeInfo(String app);

    Object createTestPipeline(String application, String pipelineId, String mergeId);
}
