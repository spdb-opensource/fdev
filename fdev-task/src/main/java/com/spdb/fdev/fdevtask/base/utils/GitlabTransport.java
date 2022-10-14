package com.spdb.fdev.fdevtask.base.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import org.omg.CORBA.COMM_FAILURE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GitlabTransport {
    @Autowired
    RestTemplate restTemplate;

    @Value("${gitlib.path}")
    private String url;// gitlab地址http://xxx/api/v4/

    @Value("${git.token}")
    private String token;

    private Logger logger = LoggerFactory.getLogger("gitlab.api.log");

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fdev调用gitlab接口api
     *
     * @param url  调用api url
     * @param send 发送map数据
     * @return
     * @throws Exception
     */
    public Object submitPost(String url, Map send) throws Exception {
        logger.info(">>>>>>>>> begin request:" + "method:post  url:" + url);
        logger.debug(">>>>>>>>> request data:" + objectMapper.writeValueAsString(send));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        JSONObject parse = JSONObject.parseObject(JSON.toJSONString(send));
        HttpEntity<Object> request = new HttpEntity<Object>(parse, httpHeaders);
        try {
            ResponseEntity<String> result = restTemplate.postForEntity(url, request, String.class);
            Object returnData = result.getBody();
            return returnData;
        } catch (HttpClientErrorException e) {
            logger.error("sumbitPost error");
            String message = new String(e.getResponseBodyAsByteArray());
            logger.error("e:" + e);
            logger.error("error message:" + message);
            if (e.getRawStatusCode() == 400) {
                logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:" + url);
                JSONObject jsonObject = JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,
                        new String[]{" @@@@@ gitlab bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            } else if (e.getRawStatusCode() == 401) {
                logger.error("gitlab error>>>Unauthorized:the role error>>>>url:" + url);
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 403) {
                logger.error("gitlab error>>>Forbidden:this action is forbidden for this user>>>>url:" + url);
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 404) {
                logger.error("gitlab error>>>Not Found:can't find this resource>>>>url:" + url);
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 409) {
                logger.error("gitlab error>>>Conflict:this request already exist>>>>url:" + url);
                throw new FdevException(ErrorConstants.REQUEST_ALREADY_EXIST,
                        new String[]{" @@@@@ gitlab request is already exist" + e.getRawStatusCode()});
            } else {
                logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:" + url);
                JSONObject jsonObject = JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,
                        new String[]{" @@@@@ gitlab bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            }
        } finally {

            logger.debug("<<<<<<<<< response data:" + objectMapper.writeValueAsString(request));

        }
    }


    public Object submitGet(String url, MultiValueMap<String, String> header) {
        logger.info(">>>>>>>>> begin request:" + "method:get  url:" + url);
        try {
            logger.info(">>>>>>>>> request data:" + objectMapper.writeValueAsString(header));
        } catch (JsonProcessingException e) {
        }
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);

        JSONObject jsonObject;
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            if (!url.contains("per_page")) {
                jsonObject = JSONObject.parseObject(result.getBody());
                return jsonObject;
            }
            HttpHeaders headers = result.getHeaders();
            int pageSize = Integer.parseInt(headers.get("X-Total-Pages").get(0));
            JSONArray jsonArray = JSONArray.parseArray(result.getBody());
            JSONArray jsonArrayPage;
            String newUrl = "";
            for (int i = 2; i <= pageSize; i++) {
                newUrl = url + "&page=" + i;
                logger.info(newUrl);
                result = restTemplate.exchange(newUrl, HttpMethod.GET, request, String.class);
                jsonArrayPage = JSONArray.parseArray(result.getBody());
                for (int j = 0; j < jsonArrayPage.size(); j++) {
                    jsonArray.add(jsonArrayPage.get(j));
                }
            }
            return jsonArray;
        } catch (Exception e) {
            logger.error("发送gitlab API错误", e);
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR);
        }
    }

    public Object submitGet2(String url, MultiValueMap<String, String> header) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<>(httpHeaders);
        try {
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return result.getBody();
        } catch (HttpClientErrorException httpClientErrorException) {
            logger.error("code:{} msg:{}", httpClientErrorException.getRawStatusCode(), httpClientErrorException.getMessage());
            throw httpClientErrorException;
        }
    }

    public Object submitGetByURI(URI uri, MultiValueMap<String, String> header) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.addAll(header);
            HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
            String resultContent = restTemplate.exchange(uri, HttpMethod.GET, request, String.class).getBody();
            return resultContent;
        } catch (Exception e) {
            logger.error("发送gitlab API错误", e);
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR);
        }
    }
    /**
     * 查询当前操作成员有无权限信息
     *
     * @param
     */
    public Integer queryProjectMemberPer(Integer gitlab_project_id,String gitlab_user_id) {
        List files = new ArrayList<>();
        //根据提交记录中的id 获取改变的内容
        String protected_branchesUrl = CommonUtils.projectUrl(url) + "/" + gitlab_project_id + "/members";
        MultiValueMap<String, String> header = new LinkedMultiValueMap();
        header.add(Dict.PRIVATE_TOKEN, token);
        String resultArray = submitGetProjectMemberPer(protected_branchesUrl, header);
        //无返回信息报错
        if(CommonUtils.isNullOrEmpty(resultArray)){
            //抛出错误
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR);
        }
        return resultArray.contains(gitlab_user_id)? 0:1;
    }

    public String submitGetProjectMemberPer(String url, MultiValueMap<String, String> header) {
        logger.info(">>>>>>>>> begin request:" + "method:get  url:" + url);
        try {
            logger.info(">>>>>>>>> request data:" + objectMapper.writeValueAsString(header));
        } catch (JsonProcessingException e) {
        }
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return result.getBody();
        } catch (Exception e) {
            logger.error("发送gitlab API错误", e);
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR);
        }
    }
}
