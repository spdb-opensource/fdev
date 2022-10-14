package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.JsonSchema;

import java.util.List;
import java.util.Map;

public interface JsonSchemaDao {

    String saveJsonSchema(JsonSchema jsonSchema);

    List<JsonSchema> getJsonSchema(Map<String, Object> requestMap);

    JsonSchema getJsonSchema(String id);

    void updateJsonSchema(Map<String, Object> requestMap, String opno);
}
