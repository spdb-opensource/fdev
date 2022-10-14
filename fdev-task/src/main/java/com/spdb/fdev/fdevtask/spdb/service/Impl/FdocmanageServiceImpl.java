package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.service.FdocmanageService;
import com.spdb.fdev.transport.RestTransport;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RefreshScope
public class FdocmanageServiceImpl implements FdocmanageService {

    private static final Logger log = LoggerFactory.getLogger(FdocmanageService.class);

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${fdev.docmanage.domain}")
    private String url;

    /**
     * 获取文件
     *
     * @param moduleName 实体
     * @param path       路径
     * @return
     */
    @Override
    public List<String> getFilesPath(String moduleName, String path) {
        List<String> submit = null;
        try {
            submit = (List) restTransport.submit(new HashMap() {{
                put("moduleName", moduleName);
                put("path", path);
                put(Dict.REST_CODE, "getFilesList");
            }});
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            log.info("调用文档模块失败！请求参数:{},{}Error Trace:{}",
                    moduleName,
                    path,
                    sw.toString());
            return Collections.emptyList();
        }
        return submit;
    }

    public boolean uploadFiletoMinio(String moduleName, String path, MultipartFile multipartFile, User user) {
        String authorization = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader("Authorization");
        try {
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("moduleName", moduleName);
            param.add("path", path);
            param.add("files", multipartFile.getResource());
            param.add("user", user);
            HttpEntity httpHeaders = setHttpHeader(param, authorization);
            String ula = url + "/api/file/filesUpload";
            restTemplate.exchange(ula, HttpMethod.POST, httpHeaders, String.class);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            log.info("调用文档模块失败！请求参数:{},{}Error Trace:{}",
                    moduleName,
                    path,
                    sw.toString());
            return false;
        }
        return true;
    }

    public boolean downloadAndUpload(String url, String path, String fileName) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        String authorization = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader("Authorization");
        HttpHeaders httpHeaders1 = new HttpHeaders();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = builder.build(true).toUri();
        ResponseEntity<byte[]> exchange = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(httpHeaders1), byte[].class);
        byte[] body = exchange.getBody();
        InputStream inputStream = new ByteArrayInputStream(body);
        MultipartFile multipartFile = null;
        try {
            multipartFile = new MockMultipartFile(fileName, fileName, ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            log.info("文件IO错:{}Error Trace:{}",
                    path,
                    sw.toString());
            return false;
        }
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("moduleName", "fdev-design");
            param.put("path", path);
            param.put("files", multipartFile.getResource());
            param.put("user", user);
            restTransport.submitUpload("uploadMinio",param);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            log.info("调用文档模块失败！请求参数:{}Error Trace:{}",
                    path,
                    sw.toString());
            return false;
        }
        return true;
    }

    public boolean uploadFilestoMinio(String moduleName, List<Map<String, String>> doc, MultipartFile[] multipartFile, User user) {
        String authorization = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader("Authorization");
        try {
            for (MultipartFile file : multipartFile) {
                MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
                String path = doc.stream().flatMap(docMap -> {
                    if (CommonUtils.isNullOrEmpty(docMap.get("type")) || "undefined".equals(docMap.get("type"))) {
                        throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"请选择文件类型后上传!"});
                    }
                    if (file.getOriginalFilename().equals(docMap.get("name"))) {
                        return Arrays.asList(docMap.get("path")).stream();
                    }
                    return Stream.empty();
                }).collect(Collectors.joining());
                param.add("moduleName", moduleName);
                param.add("path", path);
                param.add("files", file.getResource());
                param.add("user", user);
                HttpEntity httpHeaders = setHttpHeader(param, authorization);
                String ula = url + "/api/file/filesUpload";
                restTemplate.exchange(ula, HttpMethod.POST, httpHeaders, String.class);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            log.info("调用文档模块失败！请求参数:{},{}Error Trace:{}",
                    moduleName,
                    JSON.toJSONString(doc, true),
                    sw.toString());
            return false;
        }
        return true;
    }

    public boolean deleteFiletoMinio(String moduleName, String path, User user) {
        try {
            restTransport.submit(new HashMap() {{
                put("moduleName", moduleName);
                put("path", Arrays.asList(path));
                put("user", user);
                put(Dict.REST_CODE, "filesDelete");
            }});
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            log.info("调用文档模块失败！请求参数:{},{}Error Trace:{}",
                    moduleName,
                    path,
                    sw.toString());
            return false;
        }
        return true;
    }

    @Override
    public Map createFolder(String fileId) throws Exception{
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        Map submit;
        try {
            submit = (Map)restTransport.submit(new HashMap() {{
                put("file_name", fileId);
                put("file", "root");
                put("user", JSON.toJSONString(user));
                put(Dict.REST_CODE, "createFolder");
            }});
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            log.info("调用文档模块失败createFolder！请求参数:{},Error Trace:{}",
                    fileId,
                    sw.toString());
            return new HashMap();
        }
        if(null == submit){
            return new HashMap();
        }
        return submit;
    }

    @Override
    public Map<String,List> wpsList(String finderId) {
        Map submit;
        try {
            submit = (Map)restTransport.submit(new HashMap() {{
                put("file", finderId);
                put(Dict.REST_CODE, "wpsList");
            }});
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            log.info("调用文档模块失败list！请求参数:{},Error Trace:{}",
                    finderId,
                    sw.toString());
            return new HashMap();
        }
        if(null == submit){
            return new HashMap();
        }
        return submit;
    }

    @Override
    public Map content(String fileId) {
        Map submit;
        try {
            submit = (Map)restTransport.submit(new HashMap() {{
                put("file", fileId);
                put(Dict.REST_CODE, "content");
            }});
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            log.info("调用文档模块失败content！请求参数:{},Error Trace:{}",
                    fileId,
                    sw.toString());
            return new HashMap();
        }
        if(null == submit){
            return new HashMap();
        }
        return submit;
    }

    public HttpEntity setHttpHeader(MultiValueMap<String, Object> param, String auth) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", auth);
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "multipart/form-data");
        HttpEntity request = new HttpEntity<Object>(param, httpHeaders);
        return request;
    }
}
