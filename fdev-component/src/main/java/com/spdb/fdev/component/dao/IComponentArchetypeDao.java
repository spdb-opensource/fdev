package com.spdb.fdev.component.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.entity.ComponentArchetype;

import java.util.List;

public interface IComponentArchetypeDao {
    List<ComponentArchetype> query(ComponentArchetype componentArchetype) throws JsonProcessingException;

    ComponentArchetype save(ComponentArchetype componentArchetype);

    ComponentArchetype update(ComponentArchetype componentArchetype) throws JsonProcessingException;

    void delete(ComponentArchetype componentArchetype);

    ComponentArchetype queryByArchetypeIdAndAppId(ComponentArchetype componentArchetype);

    List<ComponentArchetype> queryByArchetypeIdAndVersion(String archetypeId, String version);

    List<ComponentArchetype> queryByArcIdAndVersionCopId(String archetypeId, String version, String componentId);
}
