package com.spdb.fdev.component.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.dao.IImageApplicationDao;
import com.spdb.fdev.component.entity.ImageApplication;
import com.spdb.fdev.component.service.IImageApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageApplicationServiceImpl implements IImageApplicationService {

    @Autowired
    private IImageApplicationDao imageApplicationDao;

    @Override
    public List<ImageApplication> query(ImageApplication imageApplication) throws JsonProcessingException {
        return imageApplicationDao.query(imageApplication);
    }

    @Override
    public ImageApplication save(ImageApplication imageApplication) {
        return imageApplicationDao.save(imageApplication);
    }

    @Override
    public ImageApplication update(ImageApplication imageApplication) throws JsonProcessingException {
        return imageApplicationDao.update(imageApplication);
    }

    @Override
    public void delete(ImageApplication imageApplication) {
        imageApplicationDao.delete(imageApplication);
    }

    @Override
    public void deleteAllByApplicationId(String applicationId) {
        imageApplicationDao.deleteAllByApplicationId(applicationId);
    }

    @Override
    public void deleteAllByImageName(String iamgeName) {
        imageApplicationDao.deleteAllByImageName(iamgeName);
    }

    @Override
    public ImageApplication queryByApplicationAndImage(String id, String image_name) {
        return imageApplicationDao.queryByApplicationAndImage(id, image_name);
    }
}
