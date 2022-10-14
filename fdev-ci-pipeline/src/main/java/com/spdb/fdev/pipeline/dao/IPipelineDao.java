package com.spdb.fdev.pipeline.dao;

import com.spdb.fdev.common.User;
import com.spdb.fdev.pipeline.entity.Images;
import com.spdb.fdev.pipeline.entity.Pipeline;
import com.spdb.fdev.pipeline.entity.PipelineDraft;
import com.spdb.fdev.pipeline.entity.TriggerRules;

import java.util.List;
import java.util.Map;

public interface IPipelineDao {

    Pipeline queryById(String id);

    List<Pipeline> queryDetailByProjectId(String id) throws Exception;

    Pipeline queryOneByProjectId(String id) throws Exception;

    String add(Pipeline pipeline) throws Exception;
    /**
     * 流水线删除
     * */
    void delete(String id);
    /**
     * 关注/取消关注
     * */
    long updateFollowStatus(String piperlineId, User user);

    /**
     * 查询所有流水线
     * */
    List<Pipeline> queryAllPipeline();

    Pipeline findActiveVersion(String nameId);

    void updateStatusClose(String id ,User user);

    Map<String, Object> queryPipelineList(long skip, int limit, String applicationId, String userId, List<String> apps, User user, String searchContent);

    String saveDraft(PipelineDraft draft) throws Exception;

    PipelineDraft readDraftByAuthor(String authorID);

    void deleteDraft(String authorId) throws Exception;

    Images findImageById(String id);

    Images findDefaultImage();

    void updateBuildTime(Pipeline pipeline);

    Map<String, Object> findHistoryPipelineList(long skip,int limit,String nameId);

    List<Pipeline> queryByPluginId(String pluginId);

    List<Pipeline> querySchedulePipelines();

    String updateTriggerRules(String pipelineId, TriggerRules triggerRules) throws Exception;

    Pipeline queryPipelineByIdOrNameId(Map param);

    List queryByEntityId(Map request);

    List<Pipeline> queryPipelinesByNameId(String nameId);

    List<Map> queryPipLookDigital();
}
