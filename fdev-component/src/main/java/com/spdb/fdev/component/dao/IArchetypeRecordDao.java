package com.spdb.fdev.component.dao;


import com.spdb.fdev.component.entity.ArchetypeRecord;
import com.spdb.fdev.component.entity.ComponentRecord;

import java.util.List;
import java.util.Map;

public interface IArchetypeRecordDao {

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

    ArchetypeRecord queryLatestRelease(String archetypeId);

    void delete(ArchetypeRecord archetypeRecord) throws Exception;

    Map<String,Integer> queryIssueData(String startTime, String endTime);

    List<ArchetypeRecord> queryReleaseRecordByArchetypeId(String archetypeId);
}
