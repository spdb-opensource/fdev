package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.JsonSchema;

import java.util.List;
import java.util.Map;

public interface JsonSchemaService {

    String saveJsonSchema(String modelName, Map<String, Object> requestMap) throws Exception;

    List<JsonSchema> getJsonSchema(Map<String, Object> requestMap);

    JsonSchema getJsonSchema(String id);

    void updateJsonSchema(Map<String, Object> requestMap, String opno);
}
