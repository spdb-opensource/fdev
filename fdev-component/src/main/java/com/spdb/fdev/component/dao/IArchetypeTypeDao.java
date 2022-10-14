package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.ArchetypeType;

import java.util.List;

public interface IArchetypeTypeDao {
    List query(ArchetypeType archetypeType) throws Exception;

    ArchetypeType save(ArchetypeType archetypeType) throws Exception;

    void delete(ArchetypeType archetypeType) throws Exception;
}
