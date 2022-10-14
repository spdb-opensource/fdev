package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.base.utils.ServiceUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.JsonSchemaDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.JsonSchema;
import com.spdb.fdev.fdevenvconfig.spdb.service.JsonSchemaService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JsonSchemaServiceImpl implements JsonSchemaService {

    @Autowired
    private ServiceUtil serviceUtil;
    @Autowired
    private JsonSchemaDao jsonSchemaDao;

    @Override
    public String saveJsonSchema(String modelName, Map<String, Object> requestMap) throws Exception{
        // 实体属性英文名
        String nameEn = (String) requestMap.get(Dict.NAME_EN);
        JsonSchema jsonSchema = new JsonSchema();
        jsonSchema.setTitle(modelName + "." + nameEn);
        jsonSchema.setDescription((String) requestMap.get(Dict.NAME_CN));
        // 暂时只支持新增非模板的高级属性
        jsonSchema.setTemplate(0);
        jsonSchema.setDataType((String) requestMap.get(Dict.DATA_TYPE));
        String jsonSchemaString = (String) requestMap.get(Dict.JSON_SCHEMA);
        jsonSchemaString = jsonSchemaAddProperties(jsonSchemaString, Dict.ADDITIONAL_PROPERTIES, Boolean.FALSE);
        // 验证json schema格式
        checkJsonSchema(jsonSchemaString, nameEn);
        jsonSchema.setJsonSchema(jsonSchemaString);
        jsonSchema.setOpno(serviceUtil.getOpno());
        // 保存json schema，并返回id
        return jsonSchemaDao.saveJsonSchema(jsonSchema);
    }

    /**
     * jsonSchema添加
     *
     * @param jsonSchema
     * @return
     */
    private String jsonSchemaAddProperties(String jsonSchema, String properties, Object value) {
        JSONObject jsonObject = JSON.parseObject(jsonSchema);
        if (jsonObject.get(Dict.TYPE).equals(Dict.ARRAY)) {
            JSONObject jsonObj = (JSONObject) jsonObject.get(Dict.ITEMS);
            jsonObj.put(properties, value);
        } else if (jsonObject.get(Dict.TYPE).equals(Dict.OBJECT)) {
            jsonObject.put(properties, value);
        }
        return jsonObject.toJSONString();
    }

    /**
     * 获取高级属性模板
     *
     * @param requestMap
     * @return
     */
    @Override
    public List<JsonSchema> getJsonSchema(Map<String, Object> requestMap) {
        requestMap.put(Dict.IS_TEMPLATE, 1);
        return jsonSchemaDao.getJsonSchema(requestMap);
    }

    @Override
    public JsonSchema getJsonSchema(String id) {
        return jsonSchemaDao.getJsonSchema(id);
    }

    @Override
    public void updateJsonSchema(Map<String, Object> requestMap, String opno) {
        // 判断是否为高级属性模板，若是，则直接返回
        String id = (String) requestMap.get(Dict.JSON_SCHEMA_ID);
        String nameEn = (String) requestMap.get(Dict.NAME_EN);
        JsonSchema jsonSchema = jsonSchemaDao.getJsonSchema(id);
        if (jsonSchema.getTemplate() == 1) {
            return;
        }
        String jsonSchemaString = (String) requestMap.get(Dict.JSON_SCHEMA);
        jsonSchemaString = jsonSchemaAddProperties(jsonSchemaString, Dict.ADDITIONAL_PROPERTIES, Boolean.FALSE);
        // 验证json schema是否变化，没有变化，直接返回
        if (StringUtils.isNotEmpty(jsonSchemaString) && jsonSchemaString.equals(jsonSchema.getJsonSchema())) {
            return;
        }
        // 验证json schema格式
        checkJsonSchema(jsonSchemaString, nameEn);
        jsonSchemaDao.updateJsonSchema(requestMap, opno);
    }

    /**
     * 验证json schema格式
     *
     * @param jsonSchemaString
     */
    private void checkJsonSchema(String jsonSchemaString, String nameEn) {
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(jsonSchemaString);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"实体的高级属性" + nameEn + "不符合json格式"});
        }
        String type = (String) jsonObject.get(Dict.TYPE);
        Object schema = jsonObject.get(Dict.$SCHEMA);
        Object properties = jsonObject.get(Dict.PROPERTIES);
        Object items = jsonObject.get(Dict.ITEMS);
        if ((StringUtils.isEmpty(type) || schema == null)
                || (Dict.OBJECT.equals(type) && properties == null)
                || (Dict.ARRAY.equals(type) && items == null)) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"实体的高级属性" + nameEn + "不符合json schema规范"});
        }
    }

}
