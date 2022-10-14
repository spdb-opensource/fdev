package com.spdb.fdev.component.dao;


import com.spdb.fdev.component.entity.ArchetypeIssue;
import com.spdb.fdev.component.entity.ComponentIssue;

import java.util.List;
import java.util.Map;

public interface IArchetypeIssueDao {
    List query(ArchetypeIssue archetypeIssue) throws Exception;

    ArchetypeIssue save(ArchetypeIssue archetypeIssue) throws Exception;

    void delete(ArchetypeIssue archetypeIssue) throws Exception;

    ArchetypeIssue queryById(ArchetypeIssue archetypeIssue) throws Exception;

    ArchetypeIssue queryByArchetypeIdAndVersion(String archetype_id,String version);

    ArchetypeIssue queryById(String id) throws Exception;

    ArchetypeIssue update(ArchetypeIssue archetypeIssue) throws Exception;

    List queryDevIssues(String archetypeId);

    List<Map> queryIssueDelay(Map params);

    List<ArchetypeIssue> queryAllDevIssues(String archetypeId);

    List<ArchetypeIssue> queryLatestVersion(String archetypeId, String releaseNodeName);
}
