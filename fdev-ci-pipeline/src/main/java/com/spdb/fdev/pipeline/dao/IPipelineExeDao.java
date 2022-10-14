package com.spdb.fdev.pipeline.dao;

import com.spdb.fdev.pipeline.entity.PipelineExe;

import java.util.List;
import java.util.Map;


public interface IPipelineExeDao {

    PipelineExe savePipelineExe(PipelineExe pipelineExe) throws Exception;

    Map<String, Object> queryListByPipelineIdSort(String pipelineId, long skip, int limit, String nameId);

    Map<String, Object> queryListRegexSort(String commitId,String branch,String searchContent, long skip, int limit);

    void save(PipelineExe pi);

    PipelineExe queryPipelineExeByExeId(String id) throws Exception;

    void updateStagesAndStatus(PipelineExe pipelineExe) throws Exception;

    void updateArtifacts(PipelineExe pipelineExe) throws Exception;

    void updateStagesAndStatusAndUser(PipelineExe pipelineExe) throws Exception;

    PipelineExe queryOneByPipelineIdSort(String id);

    List<PipelineExe> queryExeByPipeLineNumberOrCommitId(String pipleNumber, String commitId);

    List<PipelineExe> findRunningJobPipeline();

    List<PipelineExe> queryExeByPipelineNameId(String pipelineNameId);
}
