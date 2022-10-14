package com.spdb.fdev.fdevenvconfig.spdb.dao;

import java.util.List;
import java.util.Map;

public interface ICommonDao {
    /**
     * 通用单表查询
     *
     * @param paramMap
     * @param operator
     * @param clazz
     * @return
     * @throws Exception
     */
    <T> List<T> commonQuery(Map<Object, Object> paramMap, String operator, Class<T> clazz);

}
