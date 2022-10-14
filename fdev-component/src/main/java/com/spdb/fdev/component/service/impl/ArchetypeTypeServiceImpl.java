package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.component.dao.IArchetypeTypeDao;
import com.spdb.fdev.component.entity.ArchetypeType;
import com.spdb.fdev.component.service.IArchetypeTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchetypeTypeServiceImpl implements IArchetypeTypeService {

    @Autowired
    private IArchetypeTypeDao archetypeTypeDao;

    @Override
    public List query(ArchetypeType archetypeType) throws Exception {
        return archetypeTypeDao.query(archetypeType);
    }

    @Override
    public ArchetypeType save(ArchetypeType archetypeType) throws Exception {
        return archetypeTypeDao.save(archetypeType);
    }

    @Override
    public void delete(ArchetypeType archetypeType) throws Exception {
        archetypeTypeDao.delete(archetypeType);
    }
}
