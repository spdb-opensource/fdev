package com.spdb.fdev.pipeline.service.impl;

import com.spdb.fdev.pipeline.dao.IJobExeDao;
import com.spdb.fdev.pipeline.entity.JobExe;
import com.spdb.fdev.pipeline.service.IJobExeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobExeServiceImpl implements IJobExeService {

    @Autowired
    private IJobExeDao jobExeDao;

    @Override
    public JobExe saveJobExe(JobExe jobExe) throws Exception {
        return jobExeDao.saveJobExe(jobExe);
    }

    @Override
    public List<JobExe> queryByPipelineExeId(String pipelineExeId) throws Exception {
        return jobExeDao.queryByPipelineExeId(pipelineExeId);
    }

    public JobExe queryJobExeByExeId(String jobExeId ) throws Exception{
        return jobExeDao.queryJobExeByExeId(jobExeId);
    }
}
