package com.fdev.database.spdb.dao;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fdev.database.spdb.entity.Database;

import java.util.List;
import java.util.Map;

public interface DatabaseDao {

    Database add(Database database) throws Exception;

    List<Map> query(Database database) throws  Exception;

    Database update(Database database) throws Exception;

    Map<String, Object> queryInfo(String appid, String database_type, String database_name, String table_name, String status, Map spdb_manager, int page, int per_page) throws Exception;

    List<Database> queryName(Database database) throws  Exception;

    List<Database> queryAll(Database database) throws  Exception;

    void delete(Database database) throws Exception;

    void collectByTableName(Database database) throws Exception;

    void deleteBydb(Database database) throws  Exception;

    void updateByTbNameAndAppid(Database database1);

    void updateById(Database database1);

    void Confirm(Database database);

    List<Database> queryByAutoflag(Database database) throws Exception;

    List<Database> queryTbName(Database database) throws Exception;
}
