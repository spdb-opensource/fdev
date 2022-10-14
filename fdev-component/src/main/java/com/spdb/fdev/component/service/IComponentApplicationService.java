package com.spdb.fdev.component.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.entity.ComponentApplication;
import com.spdb.fdev.component.entity.ComponentInfo;

import java.util.List;
import java.util.Map;

public interface IComponentApplicationService {
    List<ComponentApplication> query(ComponentApplication componentApplication) throws JsonProcessingException;

    ComponentApplication save(ComponentApplication componentApplication);

    ComponentApplication update(ComponentApplication componentApplication) throws JsonProcessingException;

    ComponentApplication queryByComponentIdAndAppId(ComponentApplication componentApplication);

    void scanApplication(String application_id) throws Exception;
    
    void delete(ComponentApplication componentApplication) throws Exception;

    void deleteAllByApplicationId(ComponentApplication componentApplication) throws Exception;

    void deleteAllByComponentId(ComponentApplication componentApplication) throws Exception;

    List<Map> queryApplicatonsByComponent(ComponentApplication componentApplication) throws Exception;

    List<Map> queryFrameByComponent(Map<String, String> requestMap) throws Exception;

    void scanMpassComponentByApplication(String application_id) throws Exception;

    List<Map> queryApplicationByImage(String imageName);

    List<Map> queryFrameByImage(Map requestMap) throws Exception;
}
