package com.gotest.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IDocService {
    /**
     * 上传文档
     * @param file
     * @param fileType
     * @param workNo
     * @return
     * @throws Exception
     */
    Map<String, String> uploadDocFile(MultipartFile file, String fileType, String workNo) throws Exception;
}
