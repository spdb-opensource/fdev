package com.spdb.fdev.component.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.entity.ImageApplication;

import java.util.List;

public interface IImageApplicationService {

    List<ImageApplication> query(ImageApplication imageApplication) throws JsonProcessingException;

    ImageApplication save(ImageApplication imageApplication);

    ImageApplication update(ImageApplication imageApplication) throws JsonProcessingException;

    void delete(ImageApplication imageApplication);

    void deleteAllByApplicationId(String applicationId);

    void deleteAllByImageName(String imageName);

    ImageApplication queryByApplicationAndImage(String id, String image_name);
}
