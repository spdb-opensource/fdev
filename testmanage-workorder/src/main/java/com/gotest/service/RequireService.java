package com.gotest.service;

import java.util.List;
import java.util.Map;

/**
 * fdev 数据交互接口
 */
public interface RequireService {

    String createFdevOrder(Map map) throws Exception;

    Map queryFdevOrderState(String taskNo, String orderType,Integer page,Integer pageSize) throws Exception;

    Integer deleteOrder(Map map) throws Exception;

    Integer updateWorkOrder(Map map) throws Exception;

    Integer wasteWorkOrder(Map map) throws  Exception;

    Integer updateUnitNo(Map map) throws Exception;

    void handleMantis(String taskNo, String workNo,String untiNo);

    void handleIssues(List<String> taskIds, String oldWorkNo, String newWorkNo, String untiNo);

}
