package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ArchetypeInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IArchetypeInfoService {
    List query(ArchetypeInfo archetypeInfo) throws Exception;

    ArchetypeInfo save(ArchetypeInfo archetypeInfo) throws Exception;

    void delete(ArchetypeInfo archetypeInfo) throws Exception;

    ArchetypeInfo queryById(ArchetypeInfo archetypeInfo) throws Exception;

    ArchetypeInfo queryById(String id) throws Exception;

    ArchetypeInfo queryByNameEn(String name_en) throws Exception;

    ArchetypeInfo queryByGroupIdAndArtifact(String groupId, String artifactId);

    ArchetypeInfo queryByWebUrl(String web_url);

    List<ArchetypeInfo> queryByType(String type) throws Exception;

    ArchetypeInfo update(ArchetypeInfo archetypeInfo) throws Exception;

    List<ArchetypeInfo> queryByUserId(String user_id);

    Object saveConfigTemplate(Map hashMap) throws Exception;

    String queryConfigTemplate(Map hashMap) throws Exception;

    List<HashMap<String, Object>>  queryQrmntsData(Map requestParam) throws Exception;

    void relDevops(Map<String, String> map) throws Exception;

    void saveTargetVersion(Map<String, String> map) throws Exception;

    String queryLatestVersion(Map<String, String> map) throws Exception;

    List<String> queryReleaseVersion(Map<String, String> map) throws Exception;
}
