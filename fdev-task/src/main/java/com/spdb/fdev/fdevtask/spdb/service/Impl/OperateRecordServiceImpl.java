package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.dao.OperateRecordDao;
import com.spdb.fdev.fdevtask.spdb.entity.OperateRecords;
import com.spdb.fdev.fdevtask.spdb.service.OperateRecordService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OperateRecordServiceImpl  implements OperateRecordService {
    @Autowired
    private OperateRecordDao operateRecordDao;

    @Override
    public OperateRecords save(OperateRecords operateRecord) {
        ObjectId objectId = new ObjectId();
        operateRecord.set_id(objectId);
        operateRecord.setOperateTime(CommonUtils.dateFormat(new Date(),CommonUtils.DATE_TIME_PATTERN));
        operateRecord.setType(1);
        return operateRecordDao.save(operateRecord);
    }

    @Override
    public OperateRecords queryConfirmRecordByTaskId(String id) {
        List<OperateRecords> operateRecords = operateRecordDao.queryConfirmRecordByTaskId(id);
        if (!CommonUtils.isNullOrEmpty(operateRecords)) {
            return operateRecords.get(0);
        }else {
            return null;
        }
    }

    @Override
    public List<OperateRecords> getAllConfirmRecordByTaskIdContainHistory(String id) {
        return operateRecordDao.getAllConfirmRecordByTaskIdContainHistory(id);
    }
}
