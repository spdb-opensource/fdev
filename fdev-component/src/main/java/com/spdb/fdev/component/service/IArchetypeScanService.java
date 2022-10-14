package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ApplicationArchetype;
import com.spdb.fdev.component.entity.ArchetypeInfo;
import com.spdb.fdev.component.entity.ComponentArchetype;
import com.spdb.fdev.component.entity.ComponentInfo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.scheduling.annotation.Async;

import java.util.Date;
import java.util.List;

public interface IArchetypeScanService {


    void initArchetypeHistory(ArchetypeInfo archetypeInfo);

    void initArchetypeApplication(ArchetypeInfo archetypeInfo) throws Exception;

    ApplicationArchetype getApplicationArchetype(String application_id, String dir, ArchetypeInfo archetypeInfo) throws Exception;

    void scanComponentArchetype(String archetype_id, String version) throws Exception;

    ComponentArchetype getComponentArchetype(String archetype_id, String archetype_version, String dir, ComponentInfo component)
            throws Exception;

    void scanImageArchetype(String archetype_id, String version) throws Exception;
}
