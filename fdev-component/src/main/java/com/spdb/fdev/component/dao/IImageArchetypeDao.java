package com.spdb.fdev.component.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.entity.ImageArchetype;

import java.util.List;

public interface IImageArchetypeDao {

    List<ImageArchetype> query(ImageArchetype imageArchetype) throws JsonProcessingException;

    ImageArchetype save(ImageArchetype imageArchetype);

    ImageArchetype update(ImageArchetype imageArchetype) throws JsonProcessingException;

    void delete(ImageArchetype imageArchetype);

    ImageArchetype queryByArchetypeIdAndImageName(ImageArchetype imageArchetype);

    List<ImageArchetype> queryByImage(String imageName);
}
