package com.fdev.database.spdb.service.Impl;

import com.fdev.database.spdb.dao.DictUseRecordDao;
import com.fdev.database.spdb.entity.DictUseRecord;
import com.fdev.database.spdb.service.DictUseRecordService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Service
public class DictUseRecordServiceImpl implements DictUseRecordService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Autowired
    private RestTransport restTransport;

    @Resource
    private DictUseRecordDao dictUseRecordDao;


    @Override
    public DictUseRecord queryByKey(DictUseRecord dictUseRecord) {
        return dictUseRecordDao.queryByKey(dictUseRecord);
    }

    @Override
    public void add(DictUseRecord dictUseRecord) {
        dictUseRecordDao.add(dictUseRecord);
    }

    @Override
    public List<DictUseRecord> queryUseRecord(DictUseRecord dictUseRecord) throws Exception {
        return dictUseRecordDao.query(dictUseRecord);
    }

    @Override
    public Map<String, Object> queryUseRecordByPage(String sys_id, String database_type, String database_name, String table_name, int page, int per_page) {
        return dictUseRecordDao.queryUseRecordByPage(sys_id, database_type, database_name, table_name, page, per_page);
    }

    @Override
    public DictUseRecord queryById(String useRecord_id) {
        return dictUseRecordDao.queryById(useRecord_id);
    }

    @Override
    public void updateUseRecord(DictUseRecord dictUseRecord) {
        dictUseRecordDao.updateUseRecord(dictUseRecord);
    }


}
