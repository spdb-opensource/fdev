package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ArchetypeIssue;
import com.spdb.fdev.component.entity.ArchetypeIssueTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IMpassArchetypeIssueService {

    List<ArchetypeIssue> query(ArchetypeIssue archetypeIssue) throws Exception;

    ArchetypeIssue save(ArchetypeIssue archetypeIssue) throws Exception;

    void delete(ArchetypeIssue archetypeIssue) throws Exception;

    void packageTag(Map map) throws Exception;

    ArchetypeIssue queryById(String id) throws Exception;

    List<ArchetypeIssueTag> queryIssueTag(Map param);

    void changeStage(Map<String, String> map) throws Exception;

    void destroyIssue(Map param) throws Exception;

    List<ArchetypeIssueTag> queryMpassArchetypeHistory(Map param);

    List<HashMap<String, Object>>  queryQrmntsData(Map requestParam);

    Map<String,Integer> queryIssueData(String startTime, String endTime);

    List<Map>  queryIssueDelay(Map requestParam) throws Exception;

    void mergedCallBack(String state, Integer iid, Integer projectId) throws Exception;
}
