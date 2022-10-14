package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.fdevtask.spdb.entity.MateData;

import java.util.List;
import java.util.Map;

public interface MateDataService {
    List<String> queryMateDataByType(Map requestParam);

    MateData addMateDataByType(MateData mateData);

    MateData updateMateDataByType(MateData mateData);

    MateData queryMateDataByTaskId(String taskId);
}
