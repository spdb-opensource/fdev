package com.auto.service;

import com.auto.entity.Data;

import java.util.List;
import java.util.Map;

public interface IDataService {

    void addData(Data data) throws Exception;

    List<Map<String, String>> queryData(Map<String, String> map) throws Exception;

    List<Map<String, String>> queryDataByTestCaseNo(Map<String, String> map) throws Exception;

    List<Map<String, String>> queryDataByModuleId(Map<String, String> map) throws Exception;

    void deleteData(Map map) throws Exception;

    void updateData(Map map) throws Exception;
}
