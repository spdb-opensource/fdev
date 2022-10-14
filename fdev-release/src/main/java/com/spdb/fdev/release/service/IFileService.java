package com.spdb.fdev.release.service;

import com.spdb.fdev.common.User;
import com.spdb.fdev.release.entity.ReleaseRqrmntInfo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IFileService {

    void uploadFiles(String path, MultipartFile file, String fileName, String moduleName) throws Exception;

    void deleteFiles(String path, String moduleName) throws Exception;

    void uploadWord(String path, File file, String moduleName) throws Exception;

    void downloadDocumentFile(String filePath, String minioPath, String moduleName);

    byte[] downloadDocumentFileToByte(String moduleName, String minioPath);

    Map queryWpsFileById(String fileId) throws Exception;

    void putRarmntInfosMinioFile(List<ReleaseRqrmntInfo> releaseRqrmntInfoList, String type, User user) throws Exception;

    List queryRqrmntDoc(String rqrmnt_id) throws Exception;

    void uploadWarFile(String path, Resource file, String moduleName) throws Exception;

    MultipartFile[] getFiles(List<String> filePaths);
}
