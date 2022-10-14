package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.ComponentIssue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IComponentIssueDao {
    List query(ComponentIssue componentIssue) throws Exception;

    ComponentIssue save(ComponentIssue componentIssue) throws Exception;

    void delete(ComponentIssue componentIssue) throws Exception;

    ComponentIssue queryById(ComponentIssue componentIssue) throws Exception;

    ComponentIssue queryByComponentIdAndVersion(String component_id,String version);

    ComponentIssue queryById(String id) throws Exception;

    ComponentIssue update(ComponentIssue componentIssue) throws Exception;

    List<ComponentIssue> queryDevIssues(String component_id);

    List<ComponentIssue> queryAllDevIssues(String component_id);

    List<Map>  queryQrmntsData(String userId);

    List<Map> queryIssueDelay(Map params);

    List<ComponentIssue> queryLatestVersion(String componentId, String releaseNodeName);
}
