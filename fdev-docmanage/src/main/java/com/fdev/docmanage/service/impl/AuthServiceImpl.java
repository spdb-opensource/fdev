package com.fdev.docmanage.service.impl;

import com.fdev.docmanage.entity.AuthParameter;
import com.fdev.docmanage.entity.HeaderParameter;
import com.fdev.docmanage.entity.PathParameter;
import com.fdev.docmanage.entity.PreviewParameter;
import com.fdev.docmanage.entity.RequestParameterBody;
import com.fdev.docmanage.entity.TokenParameter;
import com.fdev.docmanage.service.AuthService;
import com.fdev.docmanage.util.HttpClientUtil;
import com.google.common.base.Preconditions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    @Resource
    private HttpClientUtil httpClientUtil;

    @Override
    public String getCode(AuthParameter authParameter, HeaderParameter headerParameter) throws Exception {
        Preconditions.checkNotNull(authParameter);
        Preconditions.checkNotNull(headerParameter);

        Map<String,Object> parameter = new HashMap<>();
        parameter.put("client_id",authParameter.getClientId());
        parameter.put("response_type",authParameter.getResponseType());
        parameter.put("state",authParameter.getState());
        parameter.put("redirect_uri",authParameter.getRedirectUri());
        parameter.put("scope",authParameter.getScope());

        Map<String,String> header = new HashMap<>();
        header.put("Cookie",headerParameter.getCookie());
        return httpClientUtil.doRedirectGetCode("/oauth2/auth",parameter,header);
    }

    @Override
    public String getToken(TokenParameter tokenParameter, HeaderParameter headerParameter) throws Exception {
        Preconditions.checkNotNull(tokenParameter);
        Preconditions.checkNotNull(headerParameter);
        Preconditions.checkArgument(!StringUtils.isEmpty(tokenParameter.getGrantType()));

        checkGrantType(tokenParameter);

        Map<String,Object> parameter = new HashMap<>();
        parameter.put("grant_type",tokenParameter.getGrantType());
        parameter.put("code",tokenParameter.getCode());
        parameter.put("redirect_uri",tokenParameter.getRedirectUri());
        parameter.put("scope",tokenParameter.getScope());
        parameter.put("refresh_token",tokenParameter.getRefreshToken()); 

        Map<String,String> header = new HashMap<>();
        header.put("Authorization",headerParameter.getAuthorization());
        header.put("Content-Type",headerParameter.getContentType());
        return httpClientUtil.doPost("/oauth2/token",parameter,header); 
    }

    private void checkGrantType(TokenParameter tokenParameter){
        switch (tokenParameter.getGrantType()){
            case "authorization_code":
                Preconditions.checkArgument(!StringUtils.isEmpty(tokenParameter.getCode()));
                Preconditions.checkArgument(!StringUtils.isEmpty(tokenParameter.getRedirectUri()));
                break;
            case "client_credentials":
                Preconditions.checkArgument(!StringUtils.isEmpty(tokenParameter.getScope()));
                break;
            case "refresh_token":
                Preconditions.checkArgument(!StringUtils.isEmpty(tokenParameter.getRefreshToken()));
                break;
        }
    }
    

    @Override
    public String retrieveDir(HeaderParameter headerParameter, PathParameter pathParameter,String next) throws Exception {
        Preconditions.checkNotNull(pathParameter);
        Preconditions.checkNotNull(headerParameter);

        Map<String,String> header = new HashMap<>();
        header.put("Authorization",headerParameter.getAuthorization());
        header.put("Content-Type",headerParameter.getContentType());
        
        Map<String,Object> parameter = new HashMap<>();
        if(next != null) {
        	String [] array = next.split("&");
        	String offset = array[0].split("=")[1];
        	String limit = array[1].split("=")[1];
        	parameter.put("offset",offset);
            parameter.put("limit",limit);
        }
        String url = String.format("/api/v1/me/volumes/%s/files/%s/children",pathParameter.getVolume(),pathParameter.getFile());
        return httpClientUtil.doGet(url,parameter,header);
    }

    @Override
    public String createUpload(HeaderParameter headerParameter, PathParameter pathParameter, RequestParameterBody requestBody) throws Exception {
        Preconditions.checkNotNull(headerParameter);
        Preconditions.checkNotNull(pathParameter);
        Preconditions.checkNotNull(requestBody);

        Map<String,String> header = new HashMap<>();
        header.put("Authorization",headerParameter.getAuthorization());
        header.put("Content-Type",headerParameter.getContentType());

        Map<String,Object> parameter = new HashMap<>();
        parameter.put("file_size",requestBody.getFileSize());
        parameter.put("file_hashes",requestBody.getFileHashes());
        parameter.put("file_name",requestBody.getFileName());
        parameter.put("file_name_conflict_behavior",requestBody.getFileNameConflictBehavior());
        parameter.put("upload_method",requestBody.getUploadMethod());

        String url = String.format("/api/v1/me/volumes/%s/files/%s/upload-transactions",pathParameter.getVolume(),pathParameter.getFile());
        return httpClientUtil.doPost(url,parameter,header);

    }

    @Override
    public String commitUpload(HeaderParameter headerParameter, PathParameter pathParameter, RequestParameterBody requestBody) throws Exception {
        Preconditions.checkNotNull(headerParameter);
        Preconditions.checkNotNull(pathParameter);
        Preconditions.checkNotNull(requestBody);

        Map<String,String> header = new HashMap<>();
        header.put("Authorization",headerParameter.getAuthorization());
        header.put("Content-Type",headerParameter.getContentType());

        Map<String,Object> parameter = new HashMap<>();
        parameter.put("feedback",requestBody.getFeedback());
        parameter.put("file_hashes",requestBody.getFileHashes());
        parameter.put("status",requestBody.getStatus());

        String url = String.format("/api/v1/me/volumes/%s/files/%s/upload-transactions/%s",pathParameter.getVolume(),pathParameter.getFile(),pathParameter.getTransaction());
        return httpClientUtil.doPatch(url,parameter,header);
    }

    @Override
    public String download(HeaderParameter headerParameter, PathParameter pathParameter) throws Exception {
        Preconditions.checkNotNull(headerParameter);
        Preconditions.checkNotNull(pathParameter);

        Map<String,String> header = new HashMap<>();
        header.put("Authorization",headerParameter.getAuthorization());

        String url = String.format("/api/v1/me/volumes/%s/files/%s/content",pathParameter.getVolume(),pathParameter.getFile());
        return httpClientUtil.doGet(url,null,header);
    }


    @Override
    public String getPreview(HeaderParameter headerParameter, PathParameter pathParameter, PreviewParameter previewParameter) throws Exception {
        Preconditions.checkNotNull(headerParameter);
        Preconditions.checkNotNull(pathParameter);
        Preconditions.checkNotNull(previewParameter);

        Map<String,String> header = new HashMap<>();
        header.put("Authorization",headerParameter.getAuthorization());

        String url = String.format("/api/v1/me/volumes/%s/files/%s/preview?expiration=%s&copyable=%b&printable=%b&watermark_text=%s&watermark_image_url=%s&ext_userid=%s",
                pathParameter.getVolume(),pathParameter.getFile(),previewParameter.getExpiration(),previewParameter.getCopyable(),
                previewParameter.getPrintable(),previewParameter.getWatermarkText(),previewParameter.getWatermarkImageUrl(),previewParameter.getExtuserid());
        String result = httpClientUtil.doGet(url,null,header);
        return result;
    }
    
    @Override
    public String createDir(HeaderParameter headerParameter,PathParameter pathParameter, RequestParameterBody requestBody) throws Exception {
        Preconditions.checkNotNull(pathParameter);
        Preconditions.checkNotNull(requestBody);
        Preconditions.checkNotNull(headerParameter);

        Map<String,String> header = new HashMap<>();
        header.put("Authorization",headerParameter.getAuthorization());
        header.put("Content-Type",headerParameter.getContentType());

        Map<String,Object> parameter = new HashMap<>();
        parameter.put("file_name",requestBody.getFileName());
        parameter.put("file_type",requestBody.getFileType());
        parameter.put("file_name_conflict_behavior",requestBody.getFileNameConflictBehavior());
        parameter.put("@parent",requestBody.isParents());

        String url = String.format("/api/v1/me/volumes/%s/files/%s/children",pathParameter.getVolume(),pathParameter.getFile());
        return httpClientUtil.doPost(url,parameter,header);
    }
    
    @Override
    public String appVolumes(HeaderParameter headerParameter) throws Exception {
        Preconditions.checkNotNull(headerParameter);
        Preconditions.checkArgument(!StringUtils.isEmpty(headerParameter.getAuthorization()));

        Map<String,String> header = new HashMap<>();
        header.put("Authorization",headerParameter.getAuthorization());

        return httpClientUtil.doGet("/api/v1/me/volumes",null,header);
    }
    
    @Override
	public String queryVersions(HeaderParameter headerParameter, PathParameter pathParameter) throws Exception {
		Preconditions.checkNotNull(headerParameter);
        Preconditions.checkNotNull(pathParameter);

        Map<String,String> header = new HashMap<>();
        header.put("Authorization",headerParameter.getAuthorization());
        header.put("Content-Type",headerParameter.getContentType());

        String url = String.format("/api/v1/me/volumes/%s/files/%s/versions",pathParameter.getVolume(),pathParameter.getFile());
        String result = httpClientUtil.doGet(url,null,header);
        return result;
	}
    
    @Override
   	public String deleteFile(HeaderParameter headerParameter, PathParameter pathParameter) throws Exception {
   		 Preconditions.checkNotNull(headerParameter);
   	     Preconditions.checkNotNull(pathParameter);
   	     
   	     Map<String,String> header = new HashMap<>();
         header.put("Authorization",headerParameter.getAuthorization());

   	     Map<String,Object> parameter = new HashMap<>();
   	     parameter.put("fileId",pathParameter.getFile());
   	     parameter.put("volume",pathParameter.getVolume());

   	     String url = String.format("/api/v1/me/volumes/%s/files/%s",pathParameter.getVolume(),pathParameter.getFile());
   	     String result = httpClientUtil.doDelete(url,header);
   	     return result;
   	   
   	}
}
