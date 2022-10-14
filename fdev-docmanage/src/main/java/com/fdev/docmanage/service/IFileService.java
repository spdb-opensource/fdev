package com.fdev.docmanage.service;

import com.spdb.fdev.common.exception.FdevException;
import org.apache.http.HttpResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IFileService {

    /**
     * 文件上传
     *
     * @param files
     * @param path
     * @throws Exception
     */
    void fileUpload(String[] path, MultipartFile[] files) throws Exception;

    /**
     * 获取指定路径下全部文件的目录结构
     *
     * @param path
     * @return
     * @throws Exception
     */
    List<String> getFiles(String path) throws Exception;

    /**
     * 文件下载
     *
     * @throws Exception
     */
    void fileDownload(HttpServletResponse response, String path) throws Exception;

    /**
     * 文件删除
     *
     * @param path
     * @throws Exception
     */
    void fileDelete(String[] path) throws Exception;

    void minioDowloadFolder(HttpServletResponse response, Map<String, Object> fileMap) throws Exception;

}
