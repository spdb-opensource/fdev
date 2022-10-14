package com.spdb.fdev.fdevinterface.base.utils;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.spdb.callable.BaseScanCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.spdb.fdev.fdevinterface.base.utils.FileUtil.formatJson;

/**
 * @author c-bucc
 * @description jsonSchemaDraft03转换jsonSchemaDraft04
 * @creatDate 2019-08-18
 */
public class JsonVersionContentChangeUtil extends BaseScanCallable {
    private static Logger logger = LoggerFactory.getLogger(JsonVersionContentChangeUtil.class);

    /**
     * jsonSchema格式转换
     *
     * @param json jsonSchema03内容
     * @return changeAfterJson 更改后 jsonSchema04的内容
     * @author c-bucc
     */
    public static String jsonVersionContentChange(String json) {
        if (json != null && !json.equals("")) {
            JsonParser jsonParser = new JsonParser();
            List<String> erroKeyList = new ArrayList<>();
            List<String> firstLevelKeyList = new ArrayList<>();
            JsonObject jsonObject = new JsonObject();
            try {
                jsonObject = (JsonObject) jsonParser.parse(json);
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.SCHEMA_ERROR, new String[]{"json格式错误，请重新填写"});
            }
            for (String firstLevelKey : jsonObject.keySet()) {
                try {
                    JsonObject keyJsonObject = jsonObject.getAsJsonObject(firstLevelKey);
                    if (keyJsonObject.keySet().contains(Dict.SCHEMA)) {
                        if (keyJsonObject.get(Dict.SCHEMA).getAsString().contains(Dict.DRAFT03)) {
                            keyJsonObject.addProperty(Dict.SCHEMA, "http://json-schema.org/draft-04/schema#");
                            firstLevelKeyList.add(firstLevelKey);
                            if (!keyJsonObject.keySet().contains(Dict.REQUIRED)) {
                                keyJsonObject.addProperty(Dict.REQUIRED, Dict.FALSE);
                            }
                            if (keyJsonObject.keySet().contains(Dict.REQUIRED)) {
                                if (keyJsonObject.get(Dict.REQUIRED).getAsString().equals(Dict.TRUE) || keyJsonObject.get(Dict.REQUIRED).getAsString().equals(Dict.FALSE)) {
                                    analysisItemDraf(keyJsonObject);
                                } else {
                                    erroKeyList.add(firstLevelKey);
                                }
                            }
                        } else {
                            erroKeyList.add(firstLevelKey);
                        }
                    }
                } catch (Exception e) {
                    throw new FdevException(ErrorConstants.SCHEMA_ERROR, new String[]{firstLevelKey + "节点，jsonschema——draft03格式不正确"});
                }
            }
            if (erroKeyList.size() != 0) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("未成功转换的节点有：");
                for (String erroKey : erroKeyList) {
                    stringBuffer.append(erroKey + ",");
                }
                stringBuffer.append("原因：存在$schema属性的版本信息错误或jsonschema——draft03格式不正确");
                String msg = String.valueOf(stringBuffer);
                throw new FdevException(ErrorConstants.SCHEMA_ERROR, new String[]{msg});
            } else {
                return addRequiredFinal(jsonObject , firstLevelKeyList);
            }
        } else {
            throw new FdevException(ErrorConstants.ANALYSIS_FILE_ERROR, new String[]{"转换内容不存在"});
        }
    }

    public static void analysisItemDraf(JsonObject keyNodeBody) {
        if (keyNodeBody != null) {
            JsonParser jsonParser = new JsonParser();
            JsonObject properties = new JsonObject();
            List<String> nodeList = new ArrayList<>();
            if (keyNodeBody.keySet().contains(Dict.PROPERTIES)) {
                properties = keyNodeBody.getAsJsonObject(Dict.PROPERTIES);
            }
            if (keyNodeBody.keySet().contains(Dict.ITEMS)) {
                analysisItemDraf(keyNodeBody.getAsJsonObject(Dict.ITEMS));
            }
            if (!keyNodeBody.keySet().contains(Dict.REQUIRED)) {
                keyNodeBody.addProperty(Dict.REQUIRED, Dict.FALSE);
            }
            for (Map.Entry<String, JsonElement> propEntry : properties.entrySet()) {
                JsonObject value = new JsonObject();
                if (!propEntry.getKey().equals(Dict.TYPE) && !propEntry.getKey().equals(Dict.REQUIRED)) {
                    value = propEntry.getValue().getAsJsonObject();
                }
                Set<String> valueKeySet = value.keySet();
                if (valueKeySet.contains(Dict.REQUIRED)) {
                    String requiredValue = value.getAsJsonPrimitive(Dict.REQUIRED).getAsString();
                    if (requiredValue.equals(Dict.TRUE)) {
                        nodeList.add(propEntry.getKey());
                        value.remove(Dict.REQUIRED);
                    } else {
                        value.remove(Dict.REQUIRED);
                    }
                }
                for (Map.Entry<String, JsonElement> valueEntry : value.entrySet()) {
                    switch (valueEntry.getKey()) {
                        case Dict.PROPERTIES:
                            analysisItemDraf(value);
                            break;
                        case Dict.ITEMS:
                            analysisItemDraf(valueEntry.getValue().getAsJsonObject());
                            break;
                        default:
                            break;
                    }
                }
                if (nodeList.size() > 0) {
                    JsonElement nodeJsonElement = jsonParser.parse(nodeList.toString());
                    keyNodeBody.add(Dict.REQUIREDAFTER, nodeJsonElement);
                }
            }
        }
    }

    public static String addRequiredFinal(JsonObject jsonObject , List<String> firstLevelKeyList) {
        String changedJson = jsonObject.toString();
        DocumentContext context = JsonPath.parse(changedJson);
        for (String firstLevelKey : firstLevelKeyList) {
            context.delete("$."+firstLevelKey+".." + Dict.REQUIRED);
        }
        String changedJsonDeleteRequired = context.jsonString();
        changedJsonDeleteRequired = changedJsonDeleteRequired.replaceAll(Dict.REQUIREDAFTER, Dict.REQUIRED);
        String changeAfterJson = formatJson(changedJsonDeleteRequired);
        return changeAfterJson;
    }
}
