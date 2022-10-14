package com.fdev.docmanage.transport;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdev.docmanage.dict.Dict;
import com.fdev.docmanage.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class GitLabTransport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RestTemplate restTemplate;

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
            int pageSize = Integer.parseInt(headers.get(Dict.X_TOTAL_PAGES).get(0));
            JSONArray jsonArray = JSONArray.parseArray(result.getBody());
            JSONArray jsonArrayPage;
            for (int i = 2; i <= pageSize; i++) {
                url += url + "&page=" + i;
                result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
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
}
