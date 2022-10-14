package com.spdb.fdev.component.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.entity.ApplicationArchetype;

import java.util.List;

public interface IApplicationArchetypeService {
    List<ApplicationArchetype> query(ApplicationArchetype applicationArchetype) throws JsonProcessingException;

    ApplicationArchetype save(ApplicationArchetype applicationArchetype);

    ApplicationArchetype update(ApplicationArchetype applicationArchetype) throws JsonProcessingException;

    ApplicationArchetype queryByArchetypeIdAndAppId(ApplicationArchetype applicationArchetype);

    void delete(ApplicationArchetype applicationArchetype);

    void deleteByAppId(String id);
}
