package com.spdb.fdev.pipeline.service;

import com.spdb.fdev.common.User;
import com.spdb.fdev.pipeline.entity.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IPipelineService {

    /**
     * 手动触发流水线
     * @param pipelineId 流水线id
     * @throws Exception
     */
    String triggerPipeline(String pipelineId, String branch, Boolean tagFlag, String branchType,List<Map> runVariables,List<Map> exeSkippedSteps, String userId) throws Exception;

    void continueRunPipeline(Map request) throws Exception;

    /**
     * 新增流水线
     * @param pipeline
     * @return id
     * @throws Exception
     * @author dengf5
     */
    String add(Pipeline pipeline) throws Exception;

    Map addByTemplateNameIdAndBindProject(String nameId,BindProject bindProject, String userId) throws Exception;

    String update(Pipeline pipeline) throws Exception;

    Pipeline queryById(Map request) throws Exception;

    void webhookPipeline(Map<String, Object> param) throws Exception;

    List<Pipeline> queryDetailByProjectId(String id) throws Exception;

    void delete(String id) throws Exception;

    String updateFollowStatus(String pipelineId) throws Exception;

    String executePipeline(Pipeline pipeline, String type, Author author, String branch, Integer gitlabId, Boolean tagFlag) throws Exception;

    Map<String, Object> queryPipelineByPluginCode(String pluginCode) throws Exception;

    Map<String, Object> queryAllPipelineList(String pageNum, String pageSize, User user, String searchContent) throws Exception;

    Map<String, Object> queryAppPipelineList(String pageNum, String pageSize, String userId, String applicationId, String searchContent);

    Map<String, Object> queryCollectionPipelineList(String pageNum, String pageSize, String userId, String searchContent);

    Map<String, Object> queryMinePipelineList(String pageNum, String pageSize, String userId, String searchContent) throws Exception;

    String saveDraft(PipelineDraft draft) throws Exception;

    PipelineDraft readDraft()throws Exception;

    void retryPipeline(String pipelineExeId) throws Exception;

    List<Images> queryAllImages() throws Exception;

    Pipeline querySimpleObjectById(String pipelineId);

    Map<String, Object> queryPipelineHistory(String nameId,String pageNum, String pageSize) throws Exception;

    void retryPipeline(String pipelineExeId, Integer stageIndex, Integer jobIndex) throws Exception;

    String copy(String id) throws Exception;

    Step prepareStep(Step step) throws Exception;

    Step prepareJobTemplateStep(Step step) throws Exception;

    PipelineExe stopPipeline(String pipelineExeId, Boolean skipManualReview) throws Exception;

    JobExe stopJob(String pipelineExeId, Integer stageIndex, Integer jobIndex) throws Exception;

    List<Pipeline> getSchedulePipelines();

    PluginInfo prePareResultShowPluginInfo(PluginInfo pluginInfo) throws Exception;

    Images findImageById(String id);

    Map queryTriggerRules(String id) throws Exception;

    String updateTriggerRules(String pipelineId,TriggerRules triggerRules) throws Exception;

    Pipeline queryByNameId(Map request);

    void downLoadArtifacts(String name, HttpServletResponse response) throws Exception;

    List getPipelineByEntityId(Map request);

    List<Map> getPipelineHistoryVersion(Map request);

    String setPipelineRollBack(Map request) throws Exception;

    Boolean checkGroupidInUserGroup(String dataGroupId) throws Exception;

    void cronStopPipeline(Map request) throws Exception;

    void countPipDigital() throws Exception;
}
