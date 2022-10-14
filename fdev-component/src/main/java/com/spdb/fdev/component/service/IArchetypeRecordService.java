package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ArchetypeRecord;

import java.util.List;
import java.util.Map;

public interface IArchetypeRecordService {
    List query(ArchetypeRecord archetypeRecord) throws Exception;

    List queryByArchetypeIdAndVersion(ArchetypeRecord archetypeRecord);

    List queryByArchetypeIdAndRegexVersion(ArchetypeRecord archetypeRecord);

    ArchetypeRecord queryByArchetypeIdAndVersion(String archetype_id, String version);

    ArchetypeRecord queryByArchetypeIdAndType(String archetype_id, String type);

    ArchetypeRecord save(ArchetypeRecord archetypeRecord) throws Exception;

    ArchetypeRecord update(ArchetypeRecord archetypeRecord) throws Exception;

    ArchetypeRecord queryById(ArchetypeRecord archetypeRecord) throws Exception;

    ArchetypeRecord queryByType(String archetypeId, String type);

    long deleteByArchetypeId(String archetypeId);

    ArchetypeRecord upsert(ArchetypeRecord archetypeRecord) throws Exception;

    List queryRecordByArchetypeIdAndVersion(String archetypeId, String tagName);

    ArchetypeRecord queryByPipId(String pipid);

    int queryArchetypeRecoreds(String archetypeId);

    void mergedCallBack(String state, Integer iid, Integer project_id) throws Exception;

    void pipiCallBack(String version, String pipid) throws Exception;

    ArchetypeRecord queryLatestRelease(String archetypeId);

    void mergedSitAndMasterCallBack(String state, Integer iid, Integer project_id, String target_branch) throws Exception;
}
