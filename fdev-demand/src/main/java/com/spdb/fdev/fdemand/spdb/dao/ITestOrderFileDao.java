package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.entity.TestOrderFile;

import java.util.Collection;
import java.util.List;

public interface ITestOrderFileDao {

    List<TestOrderFile> queryByIds(Collection<String> ids) throws Exception;

    /**
     * 根据提测单id查询提测单文件
     * @throws Exception
     */
    List<TestOrderFile> queryByTestOrderId(String testOrderId) throws Exception;

    /**
     * 新增插入提测单
     * @param testOrderFile
     * @throws Exception
     */
    void insert(TestOrderFile testOrderFile) throws Exception;

    /**
     * 新增插入提测单
     * @param testOrderFiles
     * @throws Exception
     */
    void insert(List<TestOrderFile> testOrderFiles) throws Exception;

    void delete(Collection<String> ids) throws Exception;

}
