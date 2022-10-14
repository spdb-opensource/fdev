package com.spdb.fdev.pipeline.dao;

import com.spdb.fdev.common.User;
import com.spdb.fdev.pipeline.entity.PipelineTemplate;

import java.util.List;
import java.util.Map;

public interface IPipelineTemplateDao {

    /**
     * 删除流水线模板
     */
    public void delTemplate(String id) throws Exception;

    Map<String, Object> queryMinePipelineTemplateList(long skip, int limit, User user, String searchContent) throws Exception;

    String add(PipelineTemplate template) throws Exception;

    PipelineTemplate queryById(String id);

    PipelineTemplate findActiveVersion(String nameId);

    void setStatusClose(String id);

    void updateStatusClose(String id, User user);

    Map<String, Object> findHistoryPipelineTemplateList(long skip, int limit, String nameId);

    String save(PipelineTemplate pipelineTemplate) throws Exception;

    PipelineTemplate updateVisibleRange(String id,PipelineTemplate pipelineTemplate) throws Exception;

    PipelineTemplate queryByNameId(String nameId) throws Exception;

    boolean checkAdminRole(String userNameEn);

    List<PipelineTemplate> queryByPluginCodeList(List<String> pluginCodeList);

    void updateStages(PipelineTemplate pipelineTemplate);
}
