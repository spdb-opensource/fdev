package com.fdev.database.spdb.dao;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fdev.database.spdb.entity.DictRecord;

import java.util.List;
import java.util.Map;

public interface DictRecordDao {


    void add(DictRecord dictRecord);

    /**
     * 数据字典登记信息页面分页查询
     * @param sys_id
     * @param database_type
     * @param database_name
     * @param field_en_name
     * @param per_page
     * @param page
     * @return
     * @throws Exception
     */
    Map queryDictRecord(String sys_id, String database_type, String database_name, String field_en_name, int per_page, int page) throws Exception;

    /**
     *  数据字典登记信息查询
     * @param dictRecord
     * @return
     */
    List<DictRecord> query(DictRecord dictRecord) throws JsonProcessingException, Exception;

    /**
     * 数据字典登记信息修改
     * @param dictRecord
     */
    void update(DictRecord dictRecord);
}
