package com.spdb.fdev.component.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.entity.ImageApplication;

import java.util.List;

public interface IImageApplicationDao {
    List<ImageApplication> query(ImageApplication imageApplication) throws JsonProcessingException;

    ImageApplication save(ImageApplication imageApplication);

    ImageApplication update(ImageApplication imageApplication) throws JsonProcessingException;

    void delete(ImageApplication imageApplication);

    void deleteAllByApplicationId(String applicationId);

    void deleteAllByImageName(String imageName);

    ImageApplication queryByApplicationAndImage(String id, String image_name);

    List<ImageApplication> queryByImage(String imageName);
}
