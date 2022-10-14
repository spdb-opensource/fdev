package com.spdb.fdev.component.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.dao.IComponentArchetypeDao;
import com.spdb.fdev.component.entity.ComponentArchetype;
import com.spdb.fdev.component.service.IComponentArchetypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComponentArchetypeServiceImpl implements IComponentArchetypeService {

    @Autowired
    private IComponentArchetypeDao componentArchetypeDao;

    @Override
    public List<ComponentArchetype> query(ComponentArchetype componentArchetype) throws JsonProcessingException {
        return componentArchetypeDao.query(componentArchetype);
    }

    @Override
    public ComponentArchetype save(ComponentArchetype componentArchetype) {
        return componentArchetypeDao.save(componentArchetype);
    }

    @Override
    public ComponentArchetype update(ComponentArchetype componentArchetype) throws JsonProcessingException {
        return componentArchetypeDao.update(componentArchetype);
    }

    @Override
    public void delete(ComponentArchetype componentArchetype) {
        componentArchetypeDao.delete(componentArchetype);
    }

    @Override
    public ComponentArchetype queryByArchetypeIdAndAppId(ComponentArchetype componentArchetype) {
        return componentArchetypeDao.queryByArchetypeIdAndAppId(componentArchetype);
    }

    @Override
    public List<ComponentArchetype> queryByArchetypeIdAndVersion(String archetypeId, String version) {
        return componentArchetypeDao.queryByArchetypeIdAndVersion(archetypeId, version);
    }

    @Override
    public List<ComponentArchetype> queryByArcIdAndVersionCopId(String archetypeId, String version,String componentId) {
        return componentArchetypeDao.queryByArcIdAndVersionCopId(archetypeId, version,componentId);
    }
}
