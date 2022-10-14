package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ComponentInfo;
import com.spdb.fdev.component.entity.TagRecord;

import java.text.ParseException;
import java.util.List;
import java.util.Map;


public interface IComponentInfoService {

    List query(ComponentInfo componentInfo) throws Exception;

    ComponentInfo save(Map<String, Object> map) throws Exception;

    ComponentInfo create(ComponentInfo componentInfo) throws Exception;

    void delete(ComponentInfo componentInfo) throws Exception;

    ComponentInfo queryById(ComponentInfo componentInfo) throws Exception;

    ComponentInfo queryById(String id) throws Exception;

    ComponentInfo queryByNameEn(String name_en) throws Exception;

    List<ComponentInfo> queryByType(String type) throws Exception;

    ComponentInfo update(Map<String, Object> map) throws Exception;

    ComponentInfo queryByWebUrl(String web_url) throws Exception;

    List<ComponentInfo> queryByUserId(String user_id) throws Exception;

    Map<String, Object> queryDataByType(ComponentInfo componentInfo) throws ParseException;

    List queryAllIssue(Map requestParm) throws Exception;

    Map defaultBranchAndVersion(String component_id, String issue_type) throws Exception;

    void saveMergeRequest(TagRecord tagRecord) throws Exception;

    void relDevops(Map<String, String> map) throws Exception;

    List<Map>  getDetailByIds(Map requestParam) throws Exception;

    String queryLatestVersion(Map<String, String> map) throws Exception;

    List<String>  queryReleaseVersion(Map<String, String> map) throws Exception;
}
