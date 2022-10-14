package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ArchetypeType;

import java.util.List;

public interface IArchetypeTypeService {
    List query(ArchetypeType archetypeType) throws Exception;

    ArchetypeType save(ArchetypeType archetypeType) throws Exception;

    void delete(ArchetypeType archetypeType) throws Exception;
}
