package com.spdb.fdev.codeReview.spdb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.common.User;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/11/12
 */
public interface IFileService {

    void uploadFile(String orderId,Integer orderStatus, String fileType, MultipartFile[] files, User user) throws Exception;

    void delete(String id) throws Exception;

    Map<String,Object> queryFiles(String orderId) throws Exception;

    void downloadAll(String orderId, HttpServletResponse response) throws FileNotFoundException, UnsupportedEncodingException, Exception;
}
