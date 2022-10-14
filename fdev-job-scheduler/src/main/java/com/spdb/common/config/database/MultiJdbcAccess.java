package com.spdb.common.config.database;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * *@author lizz
 * Created by lettuce on 2017/6/22.
 * 配置多数据源，以及默认数据源
 */
@Component
public class MultiJdbcAccess {

    private Map<String, SqlSession> sqlMapRegister = new HashMap<>();

    /**
     * 默认数据源名称
     */
    public static final String DB_DEFAULT = "default";

    @Autowired
    public MultiJdbcAccess(SqlSession sqlSession) {
        sqlMapRegister.put(DB_DEFAULT, sqlSession);
    }

    public SqlSession getSqlMap() {
        return sqlMapRegister.get(DB_DEFAULT);
    }

    public SqlSession getSqlMap(String jdbcId) {
        return sqlMapRegister.get(jdbcId);
    }

}
