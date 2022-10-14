package com.spdb.fdev.component.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.dao.IApplicationArchetypeDao;
import com.spdb.fdev.component.entity.ApplicationArchetype;
import com.spdb.fdev.component.service.IApplicationArchetypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationArchetypeServiceImpl implements IApplicationArchetypeService {

    @Autowired
    private IApplicationArchetypeDao applicationArchetypeDao;

    @Override
    public List<ApplicationArchetype> query(ApplicationArchetype applicationArchetype) throws JsonProcessingException {
        return applicationArchetypeDao.query(applicationArchetype);
    }

    @Override
    public ApplicationArchetype save(ApplicationArchetype applicationArchetype) {
        return applicationArchetypeDao.save(applicationArchetype);
    }

    @Override
    public ApplicationArchetype update(ApplicationArchetype applicationArchetype) throws JsonProcessingException {
        return applicationArchetypeDao.update(applicationArchetype);
    }

    @Override
    public ApplicationArchetype queryByArchetypeIdAndAppId(ApplicationArchetype applicationArchetype) {
        return applicationArchetypeDao.queryByArchetypeIdAndAppId(applicationArchetype);
    }

    @Override
    public void delete(ApplicationArchetype applicationArchetype) {
        applicationArchetypeDao.delete(applicationArchetype);
    }

    @Override
    public void deleteByAppId(String id) {
        applicationArchetypeDao.deleteByAppId(id);
    }
}
