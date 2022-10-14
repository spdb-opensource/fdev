package com.spdb.fdev.fdevtask.spdb.service;


import com.spdb.fdev.common.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文档相关
 */
public interface FdocmanageService {

    List<String> getFilesPath(String moduleName, String path);

    boolean uploadFiletoMinio(String moduleName, String path, MultipartFile multipartFile, User user);
    boolean downloadAndUpload(String url, String path,String fileName) throws Exception;

    boolean uploadFilestoMinio(String moduleName, List<Map<String, String>> doc, MultipartFile[] multipartFile, User user);

    boolean deleteFiletoMinio(String moduleName, String path, User user);

    //        createFolder
    Map createFolder(String taskId) throws Exception;
    //        wpsList
    Map wpsList(String finderId);
    //        content
    Map content(String fileId);
}
