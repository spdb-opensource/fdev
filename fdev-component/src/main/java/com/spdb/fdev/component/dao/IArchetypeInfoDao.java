package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.ArchetypeInfo;

import java.util.List;
import java.util.Map;


public interface IArchetypeInfoDao {

     List<ArchetypeInfo> getArchetypeByIds(List params) throws Exception;

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

    Integer queryDataByType(String start_date, String end_date);

    List<Map> queryQrmntsData(String userId);
}
