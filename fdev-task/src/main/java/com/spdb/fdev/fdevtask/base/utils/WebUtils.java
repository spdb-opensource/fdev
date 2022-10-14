package com.spdb.fdev.fdevtask.base.utils;

import com.google.common.base.Charsets;
import com.google.common.net.HttpHeaders;

import javax.servlet.http.HttpServletResponse;

public class WebUtils {
    /**
     * 设置下载的header 浏览器弹出下载对话框
     *
     * @param fileName 下载后的文件名字
     */
    public static void setFileDownloadHeader(HttpServletResponse response, String fileName) {
        String encodeFileName = new String(fileName.getBytes(), Charsets.UTF_8);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + encodeFileName);
        response.setContentType("application/octet-stream");
        response.setHeader("Access-Control-Expose-Headers","Content-Disposition");
        response.setCharacterEncoding("UTF-8");

    }
}
