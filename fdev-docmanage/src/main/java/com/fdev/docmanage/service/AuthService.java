package com.fdev.docmanage.service;

import com.fdev.docmanage.entity.AuthParameter;
import com.fdev.docmanage.entity.HeaderParameter;
import com.fdev.docmanage.entity.PathParameter;
import com.fdev.docmanage.entity.PreviewParameter;
import com.fdev.docmanage.entity.RequestParameterBody;
import com.fdev.docmanage.entity.TokenParameter;

public interface AuthService {
    String getCode(AuthParameter authParameter,HeaderParameter headerParameter) throws Exception;
    String getToken(TokenParameter tokenParameter,HeaderParameter headerParameter) throws Exception;
    
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
    //获取文件版本列表
    String queryVersions(HeaderParameter headerParameter, PathParameter pathParameter) throws Exception;
    //删除文件
    String deleteFile(HeaderParameter headerParameter,PathParameter pathParameter) throws Exception;
}
