package com.spdb.fdev.pipeline.service;

import com.spdb.fdev.pipeline.entity.JobExe;

import java.util.List;

public interface IJobExeService {

    JobExe saveJobExe(JobExe jobExe) throws Exception;

    JobExe queryJobExeByExeId(String jobExeId) throws Exception;

    List<JobExe> queryByPipelineExeId(String pipelineExeId) throws Exception;
}
