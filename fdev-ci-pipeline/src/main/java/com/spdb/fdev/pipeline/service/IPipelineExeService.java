package com.spdb.fdev.pipeline.service;

import com.spdb.fdev.pipeline.entity.PipelineExe;

import java.util.List;
import java.util.Map;


public interface IPipelineExeService {

    PipelineExe savePipelineExe(PipelineExe pipelineExe) throws Exception;

    Map<String, Object> queryListByPipelineIdSort(String pipelineId, String pageNum, String pageSize);

    Map<String, Object> queryListRegexSort(String commitId,String branch,String searchContent, String pageNum, String pageSize);

    void save(PipelineExe pi);

    PipelineExe queryPipelineExeByExeId(String id) throws Exception;

    void updateStagesAndStatus(PipelineExe pipelineExe) throws Exception;

    void updateArtifacts(PipelineExe pipelineExe) throws Exception;

    void updateStagesAndStatusAndUser(PipelineExe pipelineExe) throws Exception;

    String getStageFinalStatus(PipelineExe pipelineExe, Integer stageIndex);

    List<PipelineExe> queryExeByPipeLineNumberOrCommitId(String pipleNumber, String commitId);

    String calculatePipelineExeCostTime(PipelineExe pipelineExe) throws Exception;
}
