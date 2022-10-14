package com.auto.service;

import java.util.List;
import java.util.Map;

public interface ICaseDetailService {

    void addCaseDetail(Map<String, String> map) throws Exception;

    List<Map<String, String>> queryCaseDetail(Map<String, String> map) throws Exception;

    List<Map<String, String>> queryCaseDetailByTestCaseNo(String testcaseNo) throws Exception;

    void deleteCaseDetail(Map<String, String[]> map) throws Exception;

    void updateCaseDetail(Map<String, String> map) throws Exception;
}
