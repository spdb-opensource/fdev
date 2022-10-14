package com.spdb.fdev.fdevinterface.base.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import static com.spdb.fdev.fdevinterface.base.utils.FileUtil.formatJson;


/**
 * @author c-bucc
 * @description yapi数据格式修改
 * @creatDate 2020-08-18
 */

public class YapiExportDataUtil {
    private static Logger logger = LoggerFactory.getLogger("yapi.api.log");
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @author c-bucc
     * @description 修改yapi导出的jsonschema
     * @creatDate 2020-08-18
     */
    public static String yapiDataContentChange(Map request) {
        try {
            String json = (String) request.get("strInterdaceSchema");
            String projectName = (String) request.get("projectName");
            DocumentContext context = JsonPath.parse(json);
            JsonPath pathDeleteAll = JsonPath.compile("$.data");
            JsonPath pathDataTitleValue = JsonPath.compile("$.data.title");
            JsonPath pathDataPathValue = JsonPath.compile("$.data.path");
            JsonPath pathDataDescValue = JsonPath.compile("$.data.desc");
            JsonPath pathDataReqType = JsonPath.compile("$.data.req_body_type");
            JsonPath pathDataResType = JsonPath.compile("$.data.res_body_type");
            String dataTitleValue = context.read(pathDataTitleValue);
            String dataPathValue = context.read(pathDataPathValue);
            String dataDescValue = context.read(pathDataDescValue);
            String dataReqTypeValue = context.read(pathDataReqType);
            String dataResTypeValue = context.read(pathDataResType);
            if (dataReqTypeValue.equals("json") && dataResTypeValue.equals("json")) {
                JsonPath pathAddRe = JsonPath.compile("$");
                JsonPath pathDeleteErrcode = JsonPath.compile("$.errcode");
                JsonPath pathDeleteErrmsg = JsonPath.compile("$.errmsg");
                context.delete(pathDeleteErrcode);
                context.delete(pathDeleteErrmsg);
                context.delete(pathDeleteAll);
                context.put(pathAddRe, "Metadata", new JSONObject());
                JsonPath pathMetadata = JsonPath.compile("$.Metadata");
                context.put(pathMetadata, "ServiceId", projectName);
                context.put(pathMetadata, "InterfaceName", dataTitleValue);
                context.put(pathMetadata, "Uri", dataPathValue);
                context.put(pathMetadata, "RequestType", "json");
                context.put(pathMetadata, "RequestProtocol", "http");
                context.put(pathMetadata, "InterfaceDescription", dataDescValue);
                String reqObjectStr = getResReqjsonSchema(json, "req_body_other");
                String resObjectStr = getResReqjsonSchema(json, "res_body");
                if ((resObjectStr == null || resObjectStr.equals("")) && (reqObjectStr == null || reqObjectStr.equals(""))) {
                    DocumentContext contextRes = addNullContent(context, "Request", pathAddRe);
                    DocumentContext contextFinal = addNullContent(contextRes, "Response", pathAddRe);
                    context = contextFinal;
                } else if (reqObjectStr == null || reqObjectStr.equals("")) {
                    DocumentContext contextRes = addNullContent(context, "Request", pathAddRe);
                    DocumentContext contextFinal = addContent(contextRes, resObjectStr, "Response", pathAddRe);
                    context = contextFinal;
                } else if (resObjectStr == null || resObjectStr.equals("")) {
                    DocumentContext contextRes = addContent(context, reqObjectStr, "Request", pathAddRe);
                    DocumentContext contextFinal = addNullContent(contextRes, "Response", pathAddRe);
                    context = contextFinal;
                } else {
                    DocumentContext contextRes = addContent(context, reqObjectStr, "Request", pathAddRe);
                    DocumentContext contextFinal = addContent(contextRes, resObjectStr, "Response", pathAddRe);
                    context = contextFinal;
                }
                String changedJson = context.jsonString();
                String s = formatJson(changedJson);
                return s;
            } else {
                return "no_json";
            }
        } catch (Exception e) {
            return "no_json";
        }
    }

    /**
     * @author c-bucc
     * @description 获取jsonSchema数据
     * @creatDate 2020-08-18
     */
    public static String getResReqjsonSchema(String json, String resOrReq) {
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(json);
        String resOrReqKey = jsonObject.getString("data");
        com.alibaba.fastjson.JSONObject jsonObject2 = com.alibaba.fastjson.JSONObject.parseObject(resOrReqKey);
        String resOrReqStr = jsonObject2.getString(resOrReq);
        return resOrReqStr;
    }

    /**
     * @author c-bucc
     * @description 向jsonSchema添加数据
     * @creatDate 2020-08-18
     */
    public static DocumentContext addContent(DocumentContext context, String resObjectStr, String resOrReq, JsonPath pathroot) {
        JSONObject reObject = JSONObject.fromObject(resObjectStr);
        context.put(pathroot, resOrReq, new JSONObject());
        JsonPath pathRe = JsonPath.compile("$." + resOrReq);
        context.put(pathRe, "$schema", "http://json-schema.org/draft-04/schema#");
        context.put(pathRe, "type", "object");
        context.put(pathRe, "properties", new JSONObject());
        JsonPath pathAddBody = JsonPath.compile("$." + resOrReq + ".properties");
        if ("Response".equals(resOrReq)) {
            context.put(pathAddBody, "RspBody", reObject);
            JsonPath pathDeleteSchema = JsonPath.compile("$." + resOrReq + ".properties.RspBody.$schema");
            context.delete(pathDeleteSchema);
        } else if ("Request".equals(resOrReq)) {
            context.put(pathAddBody, "ReqBody", reObject);
            JsonPath pathDeleteSchema = JsonPath.compile("$." + resOrReq + ".properties.ReqBody.$schema");
            context.delete(pathDeleteSchema);
        }
        context.put(pathRe, "required", new JSONArray());
        JsonPath pathAddReqResuidValue = JsonPath.compile("$." + resOrReq + ".required");
        if ("Response".equals(resOrReq)) {
            context.add(pathAddReqResuidValue, "RspBody");
        } else if ("Request".equals(resOrReq)) {
            context.add(pathAddReqResuidValue, "ReqBody");
        }
        JsonPath pathDeleteErrmsgDefault = JsonPath.compile("$..default");
        JsonPath pathDeleteErrmsgEnumDesc = JsonPath.compile("$..enumDesc");
        context.delete(pathDeleteErrmsgDefault);
        context.delete(pathDeleteErrmsgEnumDesc);
        return context;
    }

    /**
     * @author c-bucc
     * @description 当request或response 为空时 只添加不为空的数据
     * @creatDate 2020-08-18
     */
    public static DocumentContext addNullContent(DocumentContext context, String resOrReq, JsonPath pathroot) {
        context.put(pathroot, resOrReq, new JSONObject());
        JsonPath pathRe = JsonPath.compile("$." + resOrReq);
        context.put(pathRe, "$schema", "http://json-schema.org/draft-04/schema#");
        context.put(pathRe, "type", "object");
        context.put(pathRe, "properties", new JSONObject());
        JsonPath pathAddBody = JsonPath.compile("$." + resOrReq + ".properties");
        if ("Response".equals(resOrReq)) {
            context.put(pathAddBody, "RspBody", new JSONObject());
            JsonPath pathAddBodyContent = JsonPath.compile("$." + resOrReq + ".properties.RspBody");
            context.put(pathAddBodyContent, "type", "object");
            context.put(pathAddBodyContent, "title", "empty object");
            context.put(pathAddBodyContent, "properties", new JSONObject());
        } else if ("Request".equals(resOrReq)) {
            context.put(pathAddBody, "ReqBody", new JSONObject());
            JsonPath pathAddBodyContent = JsonPath.compile("$." + resOrReq + ".properties.ReqBody");
            context.put(pathAddBodyContent, "type", "object");
            context.put(pathAddBodyContent, "title", "empty object");
            context.put(pathAddBodyContent, "properties", new JSONObject());
        }
        context.put(pathRe, "required", new JSONArray());
        JsonPath pathAddReqResuidValue = JsonPath.compile("$." + resOrReq + ".required");
        if ("Response".equals(resOrReq)) {
            context.add(pathAddReqResuidValue, "RspBody");
        } else if ("Request".equals(resOrReq)) {
            context.add(pathAddReqResuidValue, "ReqBody");
        }
        return context;
    }

    /**
     * @author c-bucc
     * @description 通过YaopiApi获取数据
     * @creatDate 2020-08-18
     */
    public static String getURLResource(String url) {
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            String returnData = result.getBody();
            DocumentContext context = JsonPath.parse(returnData);
            JsonPath pathDeleteErrcode = JsonPath.compile("$.errcode");
            Integer read = context.read(pathDeleteErrcode);
            if (read != 40011) {
                return returnData;
            } else {
                logger.error("yapi error>>>Bad Request:wrong request data>>>>url:" + url);
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
        } catch (HttpClientErrorException e) {
            logger.error("sumbitGet error");
            String message = new String(e.getResponseBodyAsByteArray());
            logger.error("e:" + e);
            logger.error("error message:" + message);
            if (e.getRawStatusCode() == 401) {
                logger.error("yapi error>>>Unauthorized:the role error>>>>url:" + url);
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ yapi role error" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 404) {
                logger.error("yapi error>>>Not Found:can't find this resource>>>>url:" + url);
                throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,
                        new String[]{"@@@@@" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 405) {
                logger.error("yapi error>>>Not Found:can't find this resource>>>>url:" + url);
                throw new FdevException("方法不被允许",
                        new String[]{"@@@@@" + e.getRawStatusCode()});
            } else {
                logger.error("yapi error>>>Bad Request:wrong request data>>>>url:" + url);
                com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException("服务器异常",
                        new String[]{" @@@@@ yapi bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            }
        } catch (ResourceAccessException e) {
            logger.error("sumbitGet error");
            String message = e.getMessage();
            logger.error("e:" + e);
            logger.error("error message:" + message);
            throw new FdevException(ErrorConstants.ROLE_ERROR,
                    new String[]{"@@@@@ yapi role error" + e.getMessage()});
        } finally {
            try {
                logger.debug("<<<<<<<<< response data:" + objectMapper.writeValueAsString(result));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
