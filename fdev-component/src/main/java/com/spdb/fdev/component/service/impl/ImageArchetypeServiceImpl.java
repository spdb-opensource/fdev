package com.spdb.fdev.component.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.dao.IImageArchetypeDao;
import com.spdb.fdev.component.entity.ImageArchetype;
import com.spdb.fdev.component.service.IImageArchetypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageArchetypeServiceImpl implements IImageArchetypeService {

    @Autowired
    private IImageArchetypeDao imageArchetypeDao;

    @Override
    public List<ImageArchetype> query(ImageArchetype imageArchetype) throws JsonProcessingException {
        return imageArchetypeDao.query(imageArchetype);
    }

    @Override
    public ImageArchetype save(ImageArchetype imageArchetype) {
        return imageArchetypeDao.save(imageArchetype);
    }

    @Override
    public ImageArchetype update(ImageArchetype imageArchetype) throws JsonProcessingException {
        return imageArchetypeDao.update(imageArchetype);
    }

    @Override
    public void delete(ImageArchetype imageArchetype) {
        imageArchetypeDao.delete(imageArchetype);
    }

    @Override
    public ImageArchetype queryByArchetypeIdAndImageName(ImageArchetype imageArchetype) {
        return imageArchetypeDao.queryByArchetypeIdAndImageName(imageArchetype);
    }
}
