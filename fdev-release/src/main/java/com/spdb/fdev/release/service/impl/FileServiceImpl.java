package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.release.dao.IReleaseRqrmntInfoDao;
import com.spdb.fdev.release.entity.ReleaseRqrmntInfo;
import com.spdb.fdev.release.service.IFileService;
import com.spdb.fdev.transport.RestTransport;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RefreshScope
public class FileServiceImpl implements IFileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private RestTransport restTransport;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${upload.local.tempdir}")
    private String tempDir;

    @Value("${docmanage.file.url}")
    private String docFileUrl;

    @Value("${spring.profiles.active}")
    private String env;
    @Autowired
    private IReleaseRqrmntInfoDao releaseRqrmntInfoDao;

    @Override
    public void uploadFiles(String path, MultipartFile file, String fileName, String moduleName) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.MODULE_NAME, moduleName);
        param.put(Dict.PATH, path);
        param.put(Dict.FILES, file.getResource());
        try {
            restTransport.submitUpload("filesUpload", param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
    }

    @Override
    public void deleteFiles(String path, String moduleName) throws Exception {
        User u1 = CommonUtils.getSessionUser();
        User user = new User();
        user.setUser_name_en(u1.getUser_name_en());
        user.setUser_name_cn(u1.getUser_name_cn());
        Map<String, Object> param = new HashMap<>();
        List<String> pathList = new ArrayList<>();
        pathList.add(path);
        param.put(Dict.REST_CODE, "filesDelete");
        param.put(Dict.MODULE_NAME, moduleName);
        param.put(Dict.PATH, pathList);
        param.put(Dict.USER, user);
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
    }

    @Override
    public void uploadWord(String path, File file, String moduleName) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.MODULE_NAME, moduleName);
        param.put(Dict.PATH, path);
        param.put(Dict.FILES, new FileSystemResource(file));
        try {
            restTransport.submitUpload("filesUpload", param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
    }

//    @Override
//    public void downloadDocumentFile(String filePath, String minioPath, String moduleName) {
//        Map<String,String> param=new HashMap<>();
//        param.put(Dict.MODULE_NAME,moduleName);
//        param.put(Dict.PATH,minioPath);
//        byte[] result=null;
//        try {
//            result = (byte[]) restTransport.submitGet("filesDownload", param);
//        } catch (Exception e) {
//            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
//        }
//        File file = new File(filePath);
//        FileOutputStream os = null;
//        try {
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            os = new FileOutputStream(file);
//            os.write(result);
//            os.flush();
//        } catch (IOException e) {
//            logger.info("{}文件生成失败", filePath);
//        } catch (Exception e) {
//            if (os != null) {
//                try {
//                    os.close();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }finally {
//            if (os != null) {
//                try {
//                    os.close();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }
//    }

    @Override
    public void downloadDocumentFile(String filePath, String minioPath, String moduleName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("source", "back");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(docFileUrl)
                .queryParam(Dict.MODULE_NAME, moduleName).queryParam(Dict.PATH, minioPath);
        byte[] result = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET,
                new HttpEntity(headers), byte[].class).getBody();
        File file = new File(filePath);
        FileOutputStream os = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            os = new FileOutputStream(file);
            os.write(result);
            os.flush();
        } catch (IOException e) {
            logger.info("{}文件生成失败", filePath);
        } catch (Exception e) {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public byte[] downloadDocumentFileToByte(String moduleName, String minioPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("source", "back");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(docFileUrl)
                .queryParam(Dict.MODULE_NAME, moduleName).queryParam(Dict.PATH, minioPath);
        byte[] result = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET,
                new HttpEntity(headers), byte[].class).getBody();
        return result;
    }

    @Override
    public Map queryWpsFileById(String fileId) throws Exception {
        Map sendMap = new HashMap();
        sendMap.put(Dict.REST_CODE, "wpsFileQuery");
        sendMap.put(Dict.FILE, fileId);
        return  (Map)restTransport.submit(sendMap);
    }

    @Async
    @Override
    public void putRarmntInfosMinioFile(List<ReleaseRqrmntInfo> releaseRqrmntInfoList, String type, User user) throws Exception {
        //整包提测
        if("1".equals(type)){
            for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfoList) {
                List<Map> docs = queryRqrmntDoc(releaseRqrmntInfo.getRqrmnt_id());
                if (!Util.isNullOrEmpty(docs)) {
                    for (Map doc : docs) {
                        readDemandFileStream((String) doc.get(Dict.DOC_PATH), (String) doc.get(Dict.DOC_NAME), releaseRqrmntInfo.getRelease_date(), user);
                    }
                }
                releaseRqrmntInfoDao.updateRqrmntInfoPackageSubmitTest(releaseRqrmntInfo.getId(), "2");
            }
        }else{
            for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfoList) {
                releaseRqrmntInfoDao.updateRqrmntInfoRelTest(releaseRqrmntInfo.getId(), "2");
            }
        }
    }

    @Override
    public List queryRqrmntDoc(String rqrmnt_id) throws Exception {
        List<Map> result = new ArrayList<>();
        if(CommonUtils.isNullOrEmpty(rqrmnt_id)){
            return result;
        }
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.DEMAND_ID, rqrmnt_id);
        map.put(Dict.TYPE, Dict.DEMANDPLANINSTRUCTION);
        map.put(Dict.REST_CODE, "queryDemandDoc");
        Map date = (Map)restTransport.submit(map);
        result = (List<Map>) date.get(Dict.DATA);
        return result;
    }

    private void readDemandFileStream(String path, String name, String release_date, User user) throws Exception{
        Map sendMap = new HashMap();
        sendMap.put(Dict.REST_CODE, "filesDownload");
        sendMap.put(Dict.PATH, path);
        sendMap.put(Dict.MODULE_NAME, "fdev-demand");
        byte[] result = (byte[]) restTransport.submitGet("filesDownload", sendMap);
        InputStream inputStream = new ByteArrayInputStream(result);
        MultipartFile multipartFile = null;
        try {
            multipartFile = new MockMultipartFile(name, name, ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.info("文件IO错:{}Error Trace:{}",
                    sw.toString());
        }
        sendMap.put("moduleName", "demandfiles");
        StringBuilder filePath = new StringBuilder();
        if(env.equals(Dict.SIT)||env.equals(Dict.REL_LOWERCASE)){
            filePath.append(env).append("/").append(release_date).append("/").append(name);
        }else{
            filePath.append(release_date).append("/").append(name);
        }
        sendMap.put("path",  filePath.toString());
        sendMap.put("files", multipartFile.getResource());
        sendMap.put("user", user);
        restTransport.submitUpload("filesUpload", sendMap);
    }

    /**
     * 上传文件到minio
     *
     * @param path
     * @param file
     * @param moduleName
     */
    @Override
    public void uploadWarFile(String path, Resource file, String moduleName) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.MODULE_NAME, moduleName);
        param.put(Dict.PATH, path);
        param.put(Dict.FILES, file);
        try {
            restTransport.submitUpload("filesUpload", param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
    }

	@Override
	public MultipartFile[] getFiles(List<String> filePaths) {
		MultipartFile[] multipartFiles = new MultipartFile[filePaths.size()];
		for(int i=0; i < filePaths.size(); i++){
            String[] strings = filePaths.get(i).split("/");
		    String name = strings[strings.length-1];
            MultipartFile multipartFile = getfile(filePaths.get(i), name);
            multipartFiles[i] = multipartFile;
        }
		return multipartFiles;
	}

    private MultipartFile getfile(String path, String name) {
        MultipartFile multipartFile = null;
        try {
            Map sendMap = new HashMap();
            sendMap.put(Dict.REST_CODE, "filesDownload");
            sendMap.put(Dict.PATH, path);
            sendMap.put(Dict.MODULE_NAME, "fdev-release");
            byte[] result = (byte[]) restTransport.submitGet("filesDownload", sendMap);
            InputStream inputStream = new ByteArrayInputStream(result);
            multipartFile = new MockMultipartFile(name, name, ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.info("文件IO错:{}Error Trace:{}", sw.toString());
        }
        return multipartFile;
    }


}
