package com.test.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class NotifyUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static JSONObject objectToJsonObject(Object object) throws JsonProcessingException {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(object);
        return JSONObject.fromObject(json);
    }

    public static String objectToJson(Object object) throws JsonProcessingException {
        JSONObject jsonObject = objectToJsonObject(object);
        return jsonObject.toString();
    }
}
