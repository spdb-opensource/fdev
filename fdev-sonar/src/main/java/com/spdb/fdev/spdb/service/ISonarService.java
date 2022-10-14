package com.spdb.fdev.spdb.service;

import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

public interface ISonarService {
    /**
     * 获取 分析记录查询(分析时间，版本信息)
     *
     * @param app
     * @return
     * @throws Exception
     */
    Map projectAnalyses(Map<String, Object> app, String branch_name);

    /**
     * Sonar 扫描 排行榜
     *
     * @return
     * @throws Exception
     */
    Map searchProject() throws Exception;

    List<Map<String, String>> searchTotalProjectMeasures(Map<String, Object> requestMap) throws Exception;

    /**
     * 查询 项目信息
     *
     * @param app
     * @return
     */
    Map getProjectInfo(Map<String, Object> app, String branch_name) throws Exception;

    /**
     * 获取bugs，漏洞，异味三者的增长趋势图
     *
     * @param app
     * @return
     * @throws Exception
     */
    Map<String, Object> getAnalysesHistory(Map<String, Object> app, String branch_name) throws Exception;

    List<Map> componentTree(Map<String, Object> app, String branch_name) throws Exception;

    String scanningFeatureBranch(Map app, String branch_name);

    String getSonarStatus(String sonar_id);
}
