package com.fdev.database.spdb.dao;


import com.fdev.database.spdb.entity.TableInfo;

import java.util.List;

public interface TableInfoDao {

    /**
     * 添加库表信息
     * @param tableInfo
     */
    TableInfo add(TableInfo tableInfo);

    /**
     * 删除库表信息
     * @param tableInfo
     */
    void delete(TableInfo tableInfo) throws  Exception;

    /**
     * 根据库类型、库名、表名查询
     * @param tableInfo
     */
    List<TableInfo> query(TableInfo tableInfo) throws Exception;
}
