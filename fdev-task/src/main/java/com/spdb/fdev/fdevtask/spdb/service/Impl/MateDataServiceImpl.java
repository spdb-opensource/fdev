package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.fdevtask.spdb.dao.MateDataDao;
import com.spdb.fdev.fdevtask.spdb.entity.MateData;
import com.spdb.fdev.fdevtask.spdb.service.MateDataService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MateDataServiceImpl implements MateDataService {
    @Autowired
    private MateDataDao mateDataDao;

    @Override
    public List<String> queryMateDataByType(Map requestParam) {
        return mateDataDao.queryMateDataByType((String) requestParam.get("type")).getListMate();
    }

    @Override
    public MateData addMateDataByType(MateData mateData) {
        mateData.set_id(new ObjectId());
        mateData.setId(new ObjectId().toString());
        return mateDataDao.addMateDataByType(mateData);
    }

    @Override
    public MateData updateMateDataByType(MateData mateData) {
        return mateDataDao.updateMateDataByType(mateData);
    }

    @Override
    public MateData queryMateDataByTaskId(String taskId) {
        return mateDataDao.queryMateDataByTaskId(taskId);
    }
}
