package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IDocService {
    void testReportCreate(Map request) throws Exception;

    String batchUpdateTaskDoc(String moduleName, String path) throws Exception;

    String batchUpdateTaskDoc(String moduleName) throws Exception;

    FdevTask uploadDesignFile(MultipartFile file, String fileName, String taskId, String fileType, String uploadStage, String remark) throws Exception;

    Long updateTaskDoc(String taskId, List<Map<String, String>> doc) throws Exception;

    Long deleteTaskDoc(String taskId, String path) throws Exception;

    Long deleteDesignDoc(String taskId, String path) throws Exception;

    Long uploadFile(String taskId,List<Map<String, String>> doc, MultipartFile[] file) throws Exception;

    String uploadNoCodeDbReview(String name,MultipartFile file,FdevTask fdevTask) throws Exception;

    Map<String,Object> uploadSecurityTestDoc(MultipartFile file, String fileType, String taskId) throws Exception;

    void downloadTemplateFile(HttpServletResponse resp, String fileType);
}
