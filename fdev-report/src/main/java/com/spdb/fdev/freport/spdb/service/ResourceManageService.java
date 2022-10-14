package com.spdb.fdev.freport.spdb.service;

import java.util.List;
import java.util.Map;

public interface ResourceManageService {

    Map<String, Object> queryPersonStatistics(List<String> groupIds, Boolean includeChild) throws Exception;

    List<List<Object>> queryPersonFreeStatistics(List<String> groupIds, Boolean includeChild) throws Exception;
}
