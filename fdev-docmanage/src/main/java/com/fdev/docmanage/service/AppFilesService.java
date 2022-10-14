package com.fdev.docmanage.service;

import com.fdev.docmanage.entity.HeaderParameter;
import com.fdev.docmanage.entity.PathParameter;
import com.fdev.docmanage.entity.PreviewParameter;
import com.fdev.docmanage.entity.RequestParameterBody;

public interface AppFilesService {
    String retrieveDir(HeaderParameter headerParameter,PathParameter pathParameter,String next) throws Exception;
    String createUpload(HeaderParameter headerParameter,PathParameter pathParameter,RequestParameterBody requestBody) throws Exception;
    String commitUpload(HeaderParameter headerParameter,PathParameter pathParameter,RequestParameterBody requestBody) throws Exception;
    String download(HeaderParameter headerParameter,PathParameter pathParameter) throws Exception;
    //文件预览
    String getPreview(HeaderParameter headerParameter, PathParameter pathParameter, PreviewParameter previewParameter) throws Exception;
    //创建文件夹
    String createDir(HeaderParameter headerParameter,PathParameter pathParameter, RequestParameterBody requestBody) throws Exception;
    //获取卷列表
    String appVolumes(HeaderParameter headerParameter) throws Exception;
    //删除文件
    String deleteFile(HeaderParameter headerParameter,PathParameter pathParameter) throws Exception;
    //在线编辑
    String getEditor(HeaderParameter headerParameter, PathParameter pathParameter, PreviewParameter previewParameter) throws Exception;
    //获取文件版本列表
    String queryVersions(HeaderParameter headerParameter, PathParameter pathParameter) throws Exception;
    //根据文件id查询文件信息
    String queryFileByFileId(HeaderParameter headerParameter, PathParameter pathParameter) throws Exception;

}
