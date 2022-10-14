package com.spdb.fdev.pipeline.service.impl;

import com.spdb.fdev.base.utils.MinioUtils;
import com.spdb.fdev.pipeline.service.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class FileServiceImpl implements IFileService {

    @Autowired
    MinioUtils minioUtils;

    @Override
    public String downloadDocumentFile(String bucket, String minioPath) throws Exception {
        return minioUtils.minioDowloadFiles(bucket, minioPath);
    }

    @Override
    public Boolean minioUploadFiles(String bucket, String path, MultipartFile file) throws Exception {
        return minioUtils.minioUploadFiles(bucket, path, file);
    }

    @Override
    public InputStream downloadFileStream(String bucket, String minioPath) throws Exception {
        return minioUtils.downloadFileStream(bucket, minioPath);
    }
}
