package com.spdb.fdev.component.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.entity.ImageArchetype;

import java.util.List;

public interface IImageArchetypeService {

    List<ImageArchetype> query(ImageArchetype imageArchetype) throws JsonProcessingException;

    ImageArchetype save(ImageArchetype imageArchetype);

    ImageArchetype update(ImageArchetype imageArchetype) throws JsonProcessingException;

    void delete(ImageArchetype imageArchetype);

    ImageArchetype queryByArchetypeIdAndImageName(ImageArchetype imageArchetype);

}
