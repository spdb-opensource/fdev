package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ArchetypeIssue;
import com.spdb.fdev.component.entity.MpassArchetype;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IMpassArchetypeService {

    List query(MpassArchetype mpassArchetype) throws Exception;

    MpassArchetype save(MpassArchetype mpassArchetype) throws Exception;

    void delete(MpassArchetype mpassArchetype) throws Exception;

    MpassArchetype queryById(MpassArchetype mpassArchetype) throws Exception;

    MpassArchetype queryById(String id) throws Exception;

    MpassArchetype queryByWebUrl(String web_url);

    MpassArchetype update(MpassArchetype mpassArchetype) throws Exception;

    List<MpassArchetype> queryByUserId(String user_id);

}
