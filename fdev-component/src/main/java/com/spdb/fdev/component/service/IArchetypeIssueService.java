package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ArchetypeIssue;
import com.spdb.fdev.component.entity.ArchetypeRecord;

import java.util.List;
import java.util.Map;

public interface IArchetypeIssueService {

    List query(ArchetypeIssue archetypeIssue) throws Exception;

    ArchetypeIssue save(Map<String, String> map) throws Exception;

    void delete(ArchetypeIssue archetypeIssue) throws Exception;

    void packageTag(Map map) throws Exception;

    ArchetypeIssue queryById(ArchetypeIssue archetypeIssue) throws Exception;

    ArchetypeIssue queryByArchetypeIdAndVersion(String archetype_id, String version);

    ArchetypeIssue queryById(String id) throws Exception;

    ArchetypeIssue update(ArchetypeIssue archetypeIssue) throws Exception;

    void changeStage(Map<String, String> map) throws Exception;

    Object queryIssueRecord(Map<String, String> map) throws Exception;

    ArchetypeRecord queryReleaseLog(String archetypeId, String targetVersion);

    List<ArchetypeIssue> queryFirstVersion(String archetypeId);

    void destroyArchetypeIssue(String id) throws Exception;

    List<Map>  queryIssueDelay(Map requestParam) throws Exception;

    void judgeTargetVersion(Map<String, String> map) throws Exception;

    List<ArchetypeIssue> queryAllVersion(String archetypeId);
}
