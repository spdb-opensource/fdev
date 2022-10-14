package com.spdb.fdev.component.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.entity.ArchetypeIssueTag;

import java.util.List;

public interface IArchetypeIssueTagDao {

    List<ArchetypeIssueTag> query(ArchetypeIssueTag archetypeIssueTag) throws JsonProcessingException;

    ArchetypeIssueTag save(ArchetypeIssueTag archetypeIssueTag) throws Exception;

    void delete(ArchetypeIssueTag archetypeIssueTag) throws Exception;

    ArchetypeIssueTag queryById(String id);

    List<ArchetypeIssueTag> queryByIssueId(String issue_id);

    List<ArchetypeIssueTag> queryByArchetypeId(String archetype_id);
}
