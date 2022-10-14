package com.fdev.database.spdb.dao;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fdev.database.spdb.entity.DataDict;

import java.util.*;

public interface DataDictDao {
    /**
     * 字典新增
     * @param dataDict
     * @return
     */
    DataDict add(DataDict dataDict);

    /**
     * 字典查询 （根据字段英文模糊匹配）
     * @param dataDict
     * @return
     * @throws Exception
     */
    List<DataDict> query(DataDict dataDict) throws Exception;

    /**
     * 字典查询
     * @param dataDict
     * @return
     * @throws Exception
     */
    List<DataDict> queryByKey(DataDict dataDict) throws Exception;

    /**
     * 字典修改
     * @param dataDict
     */
    void update(DataDict dataDict);

    /**
     * 根据多个字段查询对应的字典信息
     * @param all_field
     * @return
     */
    List<DataDict> queryIdByFields(List<String> all_field);
}
