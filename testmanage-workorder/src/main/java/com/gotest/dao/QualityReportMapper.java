package com.gotest.dao;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface QualityReportMapper {

    List<Map<String, String>> querySubmitInfo() throws Exception;

    List<Map<String, String>> queryExeTime() throws Exception;

    List<Map<String, String>> querySubmitInfoByDateAndGroup(String startDate, String endDate, List<String> groupIds);
}
