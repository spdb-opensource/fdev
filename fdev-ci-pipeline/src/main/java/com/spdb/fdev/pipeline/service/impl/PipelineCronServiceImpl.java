package com.spdb.fdev.pipeline.service.impl;

import com.spdb.fdev.pipeline.dao.IPipelineCronDao;
import com.spdb.fdev.pipeline.entity.PipelineCronBatch;
import com.spdb.fdev.pipeline.service.IPipelineCronService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PipelineCronServiceImpl implements IPipelineCronService {

    @Autowired
    private IPipelineCronDao pipelineCronDao;

    @Override
    public List<PipelineCronBatch> getAllPipelineCrons() {
        return this.pipelineCronDao.queryAllCron();
    }

    /**
     * 获取上一次的批次（即最后一次）
     *
     * @return
     */
    @Override
    public PipelineCronBatch getLastBatch() {
        return this.pipelineCronDao.getLastBatch();
    }

    @Override
    public void upsertCronBatch(PipelineCronBatch pipelineCronBatch) {
        this.pipelineCronDao.upsertCron(pipelineCronBatch);
    }
}
