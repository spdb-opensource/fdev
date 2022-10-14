package com.spdb.fdev.pipeline.service;

import com.spdb.fdev.common.User;
import com.spdb.fdev.pipeline.entity.PipelineTemplate;

import java.util.List;
import java.util.Map;

public interface IPipelineTemplateService {

    void delTemplate(String id) throws Exception;

    Map<String, Object> queryMinePipelineTemplateList(String pageNum, String pageSize, User user, String searchContent) throws Exception ;

    String add(PipelineTemplate template) throws Exception;

    PipelineTemplate queryById(String id) throws Exception;

    String update(PipelineTemplate template) throws Exception;

    Map<String, Object> queryPipelineTemplateHistory(String nameId,String pageNum, String pageSize) throws Exception;

    String copy(String id) throws Exception;

    public String saveFromPipeline(String pipelineId) throws Exception;

    PipelineTemplate updateVisibleRange(String id,String visibleRange) throws Exception;

}
