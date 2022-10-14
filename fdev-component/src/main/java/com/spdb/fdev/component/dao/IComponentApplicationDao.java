package com.spdb.fdev.component.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.entity.ComponentApplication;

import java.util.List;

public interface IComponentApplicationDao {

    List<ComponentApplication> query(ComponentApplication componentApplication) throws JsonProcessingException;

    ComponentApplication save(ComponentApplication componentApplication);

    ComponentApplication update(ComponentApplication componentApplication) throws JsonProcessingException;

    ComponentApplication queryByComponentIdAndAppId(ComponentApplication componentApplication);
    
    void delete(ComponentApplication componentApplication);

    void deleteAllByApplicationId(ComponentApplication componentApplication);

    void deleteAllByComponentId(ComponentApplication componentApplication);

}
