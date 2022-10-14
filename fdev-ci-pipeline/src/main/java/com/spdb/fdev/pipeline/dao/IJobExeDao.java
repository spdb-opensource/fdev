package com.spdb.fdev.pipeline.dao;

import com.spdb.fdev.pipeline.entity.JobExe;

import java.util.List;
import java.util.Map;

public interface IJobExeDao {

    JobExe saveJobExe(JobExe jobExe) throws Exception;

    JobExe queryPendingJob() throws Exception;

    void updateJobStart(JobExe jobExe) throws Exception;
    void updateSteps(JobExe jobExe) throws Exception;
    void updateJobFinish(JobExe jobExe) throws Exception;

    JobExe queryJobExeByIndex(String pipeline, Integer stageIndex, Integer jobIndex) throws Exception;

    JobExe queryJobExeByExeId(String jobExeId) throws Exception;

    void updateStatusByExeId(String jobExeId, String status) throws Exception;

    List<JobExe> queryByPipelineExeId(String pipelineExeId) throws Exception;
}
