package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.service.FdocmanageService;
import com.spdb.fdev.transport.RestTransport;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.util.*;
@RefreshScope
@Service
public class FdocmanageServiceImpl implements FdocmanageService{
	
	private static final Logger log = LoggerFactory.getLogger(FdocmanageService.class);
	
	@Autowired
    private RestTransport restTransport;
	
	@Autowired
    private RestTemplate restTemplate;
	
	@Value("${fdev.docmanage.domain}")
    private String url;
	
	 /**
     * 上传文档文件
     *
     * @param moduleName 实体
     * @param pathCommon       路径
     * @return
     */
	
	
	public List<String> uploadFilestoMinio(String moduleName,String pathCommon, MultipartFile[] multipartFile, User user) {
        List<String> listPathAlList = new ArrayList<>();
        try {
        	for (MultipartFile file : multipartFile) {
                String fileNameString = file.getOriginalFilename();
                String pathAll = pathCommon  + fileNameString;
                Map<String, Object> param = new HashMap<>();
                param.put("moduleName", moduleName);
                param.put("path",pathAll);
                param.put("user", user);
                param.put("files", file.getResource());
                restTransport.submitUpload("filesUpload", param);
                listPathAlList.add(pathAll);
            }
		} catch (Exception e) {
			throw new FdevException("需求文档上传失败");
		}
            
        return listPathAlList;
    }
	
	
	/**
     * 删除文档文件
     *
     * @param moduleName 实体
     * @param path       路径
     * @return
     */
	@Override
	public boolean deleteFiletoMinio(String moduleName, String path, User user) {
		return deleteFiletoMinio(moduleName, Arrays.asList(path), user);
    }

    /**
     * 删除文档文件
     *
     * @param moduleName 实体
     * @param paths       路径
     * @return
     */
    @Override
    public boolean deleteFiletoMinio(String moduleName, List<String> paths, User user) {

        try {
            restTransport.submit(new HashMap() {{
                put("moduleName", moduleName);
                put("path", paths);
                put("user", user);
                put(Dict.REST_CODE, "filesDelete");
            }});
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            StringBuilder sb = new StringBuilder();
            for (String path : paths) sb.append(path).append(",");
            log.info("调用文档模块失败！请求参数:{},{}Error Trace:{}",
                    moduleName,
                    sb.toString(),
                    sw.toString());
            return false;
        }
        return true;
    }

    @Override
    public Map createFolder(String fileId) {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute("_USER");
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
    public Map<String, List> wpsList(String folderId) {
        Map submit;
        try {
            submit = (Map)restTransport.submit(new HashMap() {{
                put("file", folderId);
                put(Dict.REST_CODE, "wpsList");
            }});
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            log.info("调用文档模块失败list！请求参数:{},Error Trace:{}",
                    folderId,
                    sw.toString());
            return new HashMap();
        }
        if(null == submit){
            return new HashMap();
        }
        return submit;
    }

    @Override
    public Map content(String id) {
        Map submit;
        try {
            submit = (Map)restTransport.submit(new HashMap() {{
                put("file", id);
                put(Dict.REST_CODE, "content");
            }});
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            log.info("调用文档模块失败content！请求参数:{},Error Trace:{}",
                    id,
                    sw.toString());
            return new HashMap();
        }
        if(null == submit){
            return new HashMap();
        }
        return submit;
    }

    @Override
    public boolean downloadAndUpload(String url, String pathMinio, String name) {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute("_USER");
        HttpHeaders httpHeaders1 = new HttpHeaders();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = builder.build(true).toUri();
        ResponseEntity<byte[]> exchange = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(httpHeaders1), byte[].class);
        byte[] body = exchange.getBody();
        InputStream inputStream = new ByteArrayInputStream(body);
        MultipartFile multipartFile = null;
        try {
            multipartFile = new MockMultipartFile(name, name, ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            log.info("文件IO错:{}Error Trace:{}",
                    pathMinio,
                    sw.toString());
            return false;
        }
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("moduleName", "fdev-design");
            param.put("path", pathMinio);
            param.put("files", multipartFile.getResource());
            param.put("user", user);
            restTransport.submitUpload("filesUpload",param);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            log.info("调用文档模块失败！请求参数:{}Error Trace:{}",
                    pathMinio,
                    sw.toString());
            return false;
        }
        return true;
    }

    @Override
    public InputStreamResource downloadFileInputStream(String moduleName, String filePath) {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("moduleName", moduleName);
            param.put("path", filePath);
            Object filesDownload = restTransport.submitGet("filesDownload", param);
            byte[] bytes = (byte[]) filesDownload;
            return new InputStreamResource(new ByteArrayInputStream(bytes)){

                @Override
                public String getFilename() {
                    return filePath.substring(filePath.lastIndexOf("/") + 1);
                }

                @Override
                public long contentLength() throws IOException {
                    return bytes.length == 0 ? 1 : bytes.length;
                }
            };
        } catch (Exception e) {
            log.error("下载文件异常------" + e.getMessage());
            return null;
        }
    }

    @Override
    public byte[] downloadFileByte(String moduleName, String filePath) {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("moduleName", moduleName);
            param.put("path", filePath);
            Object filesDownload = restTransport.submitGet("filesDownload", param);
            return (byte[]) filesDownload;
        } catch (Exception e) {
            log.error("下载文件异常------" + e.getMessage());
            return null;
        }
    }

}