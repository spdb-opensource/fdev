package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ComponentIssue;
import com.spdb.fdev.component.entity.ComponentRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IComponentIssueService {

    List query(ComponentIssue componentIssue) throws Exception;

    ComponentIssue save(Map<String, String> map) throws Exception;

    void delete(ComponentIssue componentIssue) throws Exception;


    ComponentIssue queryById(ComponentIssue componentIssue) throws Exception;

    ComponentIssue update(ComponentIssue componentIssue) throws Exception;

    void packageTag(Map map) throws Exception;

    List<String> queryBranches(Map map) throws Exception;

    void changeStage(Map<String, String> map) throws Exception;

    Object queryIssueRecord(Map<String, String> map) throws Exception;

    ComponentRecord queryReleaseLog(String componentId, String targetVersion);

    List<ComponentIssue> queryFirstVersion(String component_id);

    void destroyComponentIssue(String id) throws Exception;

    List<HashMap<String, Object>> queryQrmntsData(Map requestParam) throws Exception;

    List<Map>  queryIssueDelay(Map requestParam) throws Exception;

    void judgeTargetVersion(Map<String, String>  map) throws Exception;

    List<ComponentIssue> queryAllVersion(String component_id);

     void saveTargetVersion(Map<String, String> map) throws Exception;
}
