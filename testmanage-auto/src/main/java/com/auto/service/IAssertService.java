package com.auto.service;

import java.util.List;
import java.util.Map;

import com.auto.entity.Assert;

public interface IAssertService {

    void addAssert(Assert asrt) throws Exception;

    List<Map<String, String>> queryAssert(Map<String, String> map) throws Exception;
    
    List<Map<String, String>> queryAssertByTestCaseNo(Map<String, String> map) throws Exception;

    void deleteAssert(Map map) throws Exception;

    void updateAssert(Map map) throws Exception;
}
