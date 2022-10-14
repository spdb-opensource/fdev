package com.spdb.fdev.pipeline.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface IFileService {

    String downloadDocumentFile(String bucket, String minioPath) throws Exception;

    Boolean minioUploadFiles(String bucket,String path, MultipartFile file) throws Exception;

    InputStream downloadFileStream(String bucket, String minioPath) throws Exception;

}
