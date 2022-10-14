package com.gotest.service;

import java.util.List;
import java.util.Map;


public interface MantisService {

    Map<String, String> queryWorkNoByTaskNo(List<String> list) throws Exception;

    String judgeByMantisBeforeUat(String workNo) throws Exception;

    List<Map<String, String>> queryQualityAll() throws Exception;

    Map<String, Integer> countMantisByGroup(String startDate, String endDate, List<String> groupIds);
}
