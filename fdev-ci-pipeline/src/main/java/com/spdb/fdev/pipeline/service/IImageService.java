package com.spdb.fdev.pipeline.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.spdb.fdev.pipeline.entity.Images;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface IImageService {

    Page<? extends Map> queryAllImages(JsonNode objectNode) throws Exception;

    Map addImage(Images image) throws Exception;

    Map updateImage(Map requestParam) throws Exception;

    Images findImageById(String id);
}
