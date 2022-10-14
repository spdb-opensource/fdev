package com.spdb.fdev.fdevtask.base.utils;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import org.apache.xmlbeans.impl.xb.xsdschema.PatternDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RefreshScope
public class GitHttpServer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;
    @Value("${git.token}")
    private String token;
    @Value("${git.home}") // xxx/ebank/
    private String home;
    @Value("${git.commitUrl:xxx/api/v4/projects/81/repository/commits}")
    private String commitUrl;

    private String ipRegex = "(\\w+):\\/\\/([^/:]+)(:\\d*)?";

    public Object getMergeInfo(Map param){
        Object result= null;
        ResponseEntity responseEntity = null;
        try {
            String gitPId = (String) param.remove(Dict.GITLAB_PROJECT_ID);
            Matcher matcher = Pattern.compile(ipRegex).matcher(home);
            responseEntity = null;
            if(matcher.find()){
                StringBuffer sb = new StringBuffer(matcher.group());
                StringJoiner stringJoiner = new StringJoiner("&");
                param.forEach((k, v)->stringJoiner.add(k+"="+v));
                sb.append("/api/v4/projects/").append(gitPId).append("/merge_requests?").append(stringJoiner.toString());
                HttpHeaders headers = new HttpHeaders();
                headers.add(Dict.PRIVATE_TOKEN,token);
                HttpEntity<String> request = new HttpEntity<String>(headers);
                responseEntity = restTemplate.exchange(sb.toString(), HttpMethod.GET,request,Object.class);
            }
        } catch (RestClientException e) {
            logger.error(e.getMessage());
            throw new FdevException(ErrorConstants.GITLAB_REQUEST_ERROR);
        }
        if(null != responseEntity && responseEntity.getStatusCodeValue() == 200){
            result = responseEntity.getBody();
        }
        return result;
    }

    private void gitRespHandler(ResponseEntity responseEntity) throws Exception{
        if(responseEntity == null){
            throw new RuntimeException("responseEntity 不能为空！");
        }
        int status = responseEntity.getStatusCodeValue();
        String msg = "";
        switch(status){
            case 200:
                msg = "request success";
                break;
            case 201:
                msg = "created success";
                break;
            case 204:
                msg = "no content";
                break;
            case 304:
            case 400:
            case 401:
            case 403:
            case 404:
            case 405:
            case 409:
            case 412:
            case 422:
            case 500:
                break;
            default:
                break;
        }

    }

    // /projects/:id/repository/commits
    private ResponseEntity gitRequest(String url,Map param) throws Exception {
        ResponseEntity responseEntity = null;
        HttpHeaders headers = new HttpHeaders();
        headers.add(Dict.PRIVATE_TOKEN,token);
        headers.add("Content-Type","application/json");
        HttpEntity<Map> request = new HttpEntity(param,headers);
        responseEntity = restTemplate.exchange(url, HttpMethod.POST,request,Object.class);
        return responseEntity;
    }

    public void deleteFile(List action) {
        Map param = new HashMap();
//        StringBuilder url = new StringBuilder(commitUrl);
//        url.append("/projects/").append(projectId).append("/repository/commits");
        param.put(Dict.BRANCH,Dict.MASTER);
        param.put(Dict.COMMIT_MESSAGE,"delete file");
        param.put("actions",action);
        try {
            gitRequest(commitUrl,param);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
