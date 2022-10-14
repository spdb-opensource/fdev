package com.fdev.database.spdb.service;


import com.fdev.database.spdb.entity.Database;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DatabaseService {

    Database add(Database database) throws Exception;

    Map<String, Object> queryInfo(String appid, String database_type, String database_name, String table_name, String status, Map spdb_manager, int page, int per_page) throws Exception;

    void exportExcel(List<Map> databases, HttpServletResponse resp) throws IOException, Exception;

    List<Database> queryName(Database database)throws Exception;

    List<Map> query(Database database)throws Exception;

    void delete(Database database) throws Exception;

    Database update(Database database) throws Exception;

    void Confirm(Database database);

    List<Database> queryAll(Database database) throws Exception;

    Map queryDetail(Database database) throws Exception;

    List<Database> queryTbName(Database database) throws Exception;
}
