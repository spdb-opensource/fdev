package com.fdev.database.spdb.dao;


import com.fdev.database.spdb.entity.DictUseRecord;

import java.util.List;
import java.util.Map;

public interface DictUseRecordDao {


    void add(DictUseRecord dictUseRecord);

    /**
     * 根据系统、数据库类型、数据库名、表名进行查询
     * @param dictUseRecord
     * @return
     */
    DictUseRecord queryByKey(DictUseRecord dictUseRecord);

    /**
     * 根据所有参数进行查询
     * @param dictUseRecord
     * @return
     * @throws Exception
     */
    List<DictUseRecord> query(DictUseRecord dictUseRecord) throws Exception;

    /**
     * 根据查询条件进行数据库库表的变动信息查询（分页）
     * @param sys_id
     * @param database_type
     * @param database_name
     * @param table_name
     * @param page
     * @param per_page
     * @return
     */
    Map<String, Object> queryUseRecordByPage(String sys_id, String database_type, String database_name, String table_name, int page, int per_page);

    /**
     * 根据库表信息id进行查询
     * @param useRecord_id
     * @return
     */
    DictUseRecord queryById(String useRecord_id);

    /**
     * 库表字段使用记录修改
     * @param dictUseRecord
     */
    void updateUseRecord(DictUseRecord dictUseRecord);
}
