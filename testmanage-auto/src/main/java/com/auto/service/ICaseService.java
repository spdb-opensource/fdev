package com.auto.service;

import com.auto.entity.Testcase;

import java.util.List;
import java.util.Map;

public interface ICaseService {

    void addCase(Map<String, String> map) throws Exception;

    List<Map<String, String>> queryCase(Map<String, String> map) throws Exception;

    void deleteCase(Map map) throws Exception;

    void updateCase(Map map) throws Exception;
}
