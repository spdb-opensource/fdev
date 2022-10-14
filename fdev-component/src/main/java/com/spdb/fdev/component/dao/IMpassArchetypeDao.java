package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.MpassArchetype;

import java.util.List;
import java.util.Map;

public interface IMpassArchetypeDao {
    
    List query(MpassArchetype mpassArchetype) throws Exception;

    MpassArchetype save(MpassArchetype mpassArchetype) throws Exception;

    void delete(MpassArchetype mpassArchetype) throws Exception;

    MpassArchetype queryById(MpassArchetype mpassArchetype) throws Exception;

    MpassArchetype queryById(String id) throws Exception;

    MpassArchetype queryByWebUrl(String web_url);

    MpassArchetype update(MpassArchetype MpassArchetype) throws Exception;

    List<MpassArchetype> queryByUserId(String user_id);

    Integer queryDataByType(String start_date,String end_date);

    List<Map>  queryQrmntsData(String userId);

    Map<String,Integer> queryIssueData(String startTime, String endTime);

    List<Map> queryIssueDelay(Map params);

    List<MpassArchetype> getMpassArchetypeByIds(List params) throws Exception;
}
