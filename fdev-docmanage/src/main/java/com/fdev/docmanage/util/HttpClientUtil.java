package com.fdev.docmanage.util;

import com.alibaba.fastjson.JSONObject;
import com.fdev.docmanage.entity.Constants;
import com.google.common.base.Preconditions;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * 构造通用的httlclient
 */
@Component
public class HttpClientUtil {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private Constants constants;


    /**
     * 发送HttpGet请求
     * @param url
     * @return
     */
    public String doGet(String url,Map<String,Object> parameter ,Map<String,String> header) throws Exception{
        // url不为空
        Preconditions.checkArgument(!StringUtils.isEmpty(url));

        // 给url添加prefix
        if(!url.contains("https") && !url.contains("http")){
            url = constants.getGraphPrefix() + url;
        }

        HttpGet httpget = new HttpGet(getUriBuilder(url,parameter).build());

        // 给http请求添加head信息
        addHeader(httpget,header);

        return executeRequestReturnString(httpget);
    }

    
    public HttpResponse doGetReturnResponse(String url,Map<String,Object> parameter ,Map<String,String> header) throws Exception{
        // url不为空
        Preconditions.checkArgument(!StringUtils.isEmpty(url));

        // 给url添加prefix
        if(!url.contains("https") && !url.contains("http")){
            url = constants.getGraphPrefix() + url;
        }

        HttpGet httpget = new HttpGet(getUriBuilder(url,parameter).build());

        // 给http请求添加head信息
        addHeader(httpget,header);

        return executeRequestReturnResponse(httpget);
    }
 
    /**
     * 发送HttpPost请求，参数为parameter
     * @return
     */
    public String doPost(String url, Map<String,Object> parameter, Map<String, String> header) throws Exception{
        // url不为空
        Preconditions.checkArgument(!StringUtils.isEmpty(url));

        // 给url添加prefix
        if(!url.contains("https") && !url.contains("http")){
            url = constants.getGraphPrefix() + url;
        }

        HttpPost httppost = new HttpPost(url);
        setEntity(httppost,parameter,header);

        // 给请求添加head信息
        addHeader(httppost,header);

        return executeRequestReturnString(httppost);
    }

    public String doPostWithFile(String url, Map<String,Object> parameter, Map<String, String> header) {
        // url不为空
        Preconditions.checkArgument(!StringUtils.isEmpty(url));

        // 给url添加prefix
        if(!url.contains("https") && !url.contains("http")){
            url = constants.getGraphPrefix() + url;
        }

        //文件转换 MultipartFile文件转化为 File对象
        MultipartFile file = (MultipartFile)parameter.get("file");
        File tofile = new File(file.getOriginalFilename());
        try (InputStream  ins = file.getInputStream();
             OutputStream os = new FileOutputStream(tofile);) {
            int byteRead =0;
            byte[] buffer = new byte[8192];
            while((byteRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer,0,byteRead);
            }
        }catch (Exception e) {
            logger.error("文件转换 MultipartFile文件转化为 File对象错误", e);
        }

        RequestConfig config = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(2000000000).build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(config);
        MultipartEntityBuilder mutipartEntityBuilder = MultipartEntityBuilder.create();
        mutipartEntityBuilder.addBinaryBody("file", tofile);
        mutipartEntityBuilder.addTextBody("key",(String) parameter.get("key"));
        mutipartEntityBuilder.addTextBody("x-amz-credential",(String) parameter.get("x-amz-credential"));
        mutipartEntityBuilder.addTextBody("x-amz-date",(String) parameter.get("x-amz-date"));
        mutipartEntityBuilder.addTextBody("x-amz-algorithm",(String) parameter.get("x-amz-algorithm"));
        mutipartEntityBuilder.addTextBody("x-amz-signature",(String) parameter.get("x-amz-signature"));
        mutipartEntityBuilder.addTextBody("policy",(String) parameter.get("policy"));
        HttpEntity httpEntity = mutipartEntityBuilder.build();
        httpPost.setEntity(httpEntity);
        BufferedReader reader = null;
        InputStreamReader inputStreamReader = null;
        try(CloseableHttpClient httpclient = HttpClientBuilder.create().build();
            CloseableHttpResponse httpResponse = httpclient.execute(httpPost);
            ) {
            HttpEntity responseEntity = httpResponse.getEntity();
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if(statusCode == 200) {
                inputStreamReader = new InputStreamReader(responseEntity.getContent());
                reader = new BufferedReader(inputStreamReader);
                StringBuffer stringBuffer = new StringBuffer();
                String str = "";
                while((str = reader.readLine())!=null) {
                    stringBuffer.append(str);
                }
                return stringBuffer.toString();
            }
        } catch (ClientProtocolException e) {
            logger.error("client protocol exception", e);
        } catch (IOException e) {
            logger.error("io exception", e);
        } finally {
            if(null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("close reader stream error", e);
                }
            }
            if(null != inputStreamReader) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    
    public HttpResponse doPostReturnResponse(String url, Map<String,Object> parameter, Map<String, String> header) throws Exception{
        // url不为空
        Preconditions.checkArgument(!StringUtils.isEmpty(url));

        // 给url添加prefix
        if(!url.contains("https") && !url.contains("http")){
            url = constants.getGraphPrefix() + url;
        }

        HttpPost httppost = new HttpPost(url);
        setEntity(httppost,parameter,header);

        // 给请求添加head信息
        addHeader(httppost,header);

        return executeRequestReturnResponse(httppost);
    }

    /**
     * 发送patch请求，参数为parameter
     * @return
     */
    public String doPatch(String url, Map<String,Object> parameter, Map<String, String> header) throws Exception{
        // url不为空
        Preconditions.checkArgument(!StringUtils.isEmpty(url));

        // 给url添加prefix
        if(!url.contains("https") && !url.contains("http")){
            url = constants.getGraphPrefix() + url;
        }

        HttpPatch httpPatch = new HttpPatch(url);
        setEntity(httpPatch,parameter,header);

        // 给请求添加head信息
        addHeader(httpPatch,header);

        return executeRequestReturnString(httpPatch);
    }

    public HttpResponse doPutReturnResponse(String url, Map<String,Object> parameter, Map<String, String> header) throws Exception{
        // url不为空
        Preconditions.checkArgument(!StringUtils.isEmpty(url));

        // 给url添加prefix
        if(!url.contains("https") && !url.contains("http")){
            url = constants.getGraphPrefix() + url;
        }

        HttpPut httpPut = new HttpPut(url);
        setEntity(httpPut,parameter,header);

        // 给请求添加head信息
        addHeader(httpPut,header);

        return executeRequestReturnResponse(httpPut);
    }

    /**
     * 发送Redirect请求
     * @param url
     * @return
     */
    public String doRedirectGetCode(String url,Map<String,Object> parameter ,Map<String,String> header) throws Exception{
        // url不为空
        Preconditions.checkArgument(!StringUtils.isEmpty(url));

        // 给url添加prefix
        if(!url.contains("https") && !url.contains("http")){
            url = constants.getGraphPrefix() + url;
        }
        RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        HttpGet httpget = new HttpGet(getUriBuilder(url,parameter).build());
        httpget.setConfig(defaultConfig);

        // 给http请求添加head信息
        addHeader(httpget,header);

        String code = null;
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpClientContext context = HttpClientContext.create();
            CloseableHttpResponse response = httpclient.execute(httpget,context);
            HttpHost target = context.getTargetHost();
            List<URI> redirectLocations = context.getRedirectLocations();
            URI location = URIUtils.resolve(httpget.getURI(), target, redirectLocations);
            if(location.toString().contains("code")){
                code = location.toASCIIString();
            }
            response.close();
        } catch (IOException e) {
        	logger.error(e.getMessage());
        }
        return code;
    }


    private URIBuilder getUriBuilder(String url , Map<String,Object> parameter) throws Exception{
        /*
         * 由于GET请求的参数都是拼装在URL地址后方，所以我们要构建一个URL，带参数
         */
        URIBuilder uriBuilder = new URIBuilder(url);
        List<NameValuePair> list = new LinkedList<>();
        if(!CollectionUtils.isEmpty(parameter)){
            for (Map.Entry<String, Object> entry : parameter.entrySet()) {
                BasicNameValuePair param = new BasicNameValuePair(entry.getKey(),String.valueOf(entry.getValue()));
                list.add(param);
            }
        }
        uriBuilder.setParameters(list);
        return uriBuilder;
    }

    private void addHeader(HttpRequestBase httpRequestBase,Map<String,String> header) throws Exception{
        Preconditions.checkNotNull(httpRequestBase);
        if(!CollectionUtils.isEmpty(header)){
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpRequestBase.addHeader(entry.getKey(),entry.getValue());
            }
        }
    }

    private UrlEncodedFormEntity getFormEntity(Map<String,Object> map){
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        if(!CollectionUtils.isEmpty(map)){
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                formparams.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
            }
        }
        return new UrlEncodedFormEntity(formparams, Consts.UTF_8);
    }

    private StringEntity getJsonEntity(Map<String,Object> map){
        JSONObject jsonParam = new JSONObject();
        if(!CollectionUtils.isEmpty(map)){
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                jsonParam.put(entry.getKey(),entry.getValue());
            }
        }
        return new StringEntity(jsonParam.toJSONString(), ContentType.APPLICATION_JSON);
    }

    private void setEntity(HttpEntityEnclosingRequestBase request, Map<String,Object> parameter , Map<String,String> header){
        if(header.containsKey("Content-Type")){
            switch (header.get("Content-Type")){
                // form表单提交数据
                case Constants.CONTENT_TYPE_FORM:
                    UrlEncodedFormEntity entity = getFormEntity(parameter);
                    entity.setContentType(Constants.CONTENT_TYPE_FORM);
                    request.setEntity(entity);
                    break;
                    
                case Constants.CONTENT_TYPE_FORM1:
                    UrlEncodedFormEntity entity1 = getFormEntity(parameter);
                    entity1.setContentType(Constants.CONTENT_TYPE_FORM1);
                    request.setEntity(entity1);
                    break;
                 
                // json表单提交数据
                case Constants.CONTENT_TYPE_JSON:
                    StringEntity stringEntity = getJsonEntity(parameter);
                    request.setEntity(stringEntity);
                    break;
            }
        }
    }

    private String executeRequestReturnString(HttpUriRequest request){
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            response = httpclient.execute(request);
        } catch (IOException e1) {
        	logger.error(e1.getMessage());
        }
        String result = null;
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
            response.close();
        } catch (ParseException | IOException e) {
        	logger.error(e.getMessage());
        }
        return result;
    }

    private HttpResponse executeRequestReturnResponse(HttpUriRequest request){
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            response = httpclient.execute(request);
        } catch (Exception e) {
        	logger.error(e.getMessage());
        }
        return response;
    }
    
    
    /**
     * 发送delete请求
     * @return
     */
    public String doDelete(String url, Map<String, String> header) throws Exception{
        // url不为空
        Preconditions.checkArgument(!StringUtils.isEmpty(url));

        // 给url添加prefix
        if(!url.contains("https") && !url.contains("http")){
            url = constants.getGraphPrefix() + url;
        }
        HttpDelete httpDelete = new HttpDelete(url);
        // 给请求添加head信息
        addHeader(httpDelete,header);

        return executeRequestReturnString(httpDelete);
    }
}
