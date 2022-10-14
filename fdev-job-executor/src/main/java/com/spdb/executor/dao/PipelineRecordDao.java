package com.spdb.executor.dao;

import com.csii.pe.pojo.PipelineRecord;

import java.util.List;
import java.util.Set;

public interface PipelineRecordDao {

    void savePipelineRecord(PipelineRecord pipelineRecord);

    void updatePipelineRecord(String id, Set<Integer> pipelineIds);

    PipelineRecord getPipelineRecord(Integer projectId, String branch, String startTime, String endTime);

    List<PipelineRecord> listPipelineRecords(String startTime, String endTime);

}
