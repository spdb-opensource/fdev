package com.spdb.fdev.fdevtask.spdb.dao;

import com.spdb.fdev.fdevtask.spdb.entity.MateData;

public interface MateDataDao {
    MateData queryMateDataByType(String type);

    MateData addMateDataByType(MateData mateData);

    MateData queryMateDataByTaskId(String taskId);

    MateData updateMateDataByType(MateData mateData);

}
