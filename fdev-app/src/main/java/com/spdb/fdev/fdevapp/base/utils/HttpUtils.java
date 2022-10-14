package com.spdb.fdev.fdevapp.base.utils;

import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpUtils {

    /**
     * 跨 服务请求
     * @param url
     * @param param
     * @return
     * @throws Exception
     */
    public static JSONObject post(String url, JSONObject param) throws Exception {
        CloseableHttpClient httpClients = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        JSONObject result = null;
        try {
            HttpPost httPost = new HttpPost( url);
            httPost.addHeader("source", "back");
            httPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
            StringEntity entity = new StringEntity(param.toString(),"UTF-8");
            entity.setContentType("application/json");
            entity.setContentEncoding(HTTP.UTF_8);
            httPost.setEntity(entity);
            response = httpClients.execute(httPost);
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();
            if(statusCode==200) {
                HttpEntity httpEntity = response.getEntity();
                String s = EntityUtils.toString(httpEntity, "UTF-8");
                result = JSONObject.fromObject(s);
                return result;
            }
            result = new JSONObject();
            result.put("return_code", String.valueOf(statusCode));
            result.put("message", status.getReasonPhrase());
            result.put("data", "");
            httpClients.close();
            response.close();
        } catch (Exception e) {
            throw e;
        } finally {
            if (httpClients != null) {
                httpClients.close();
            }
            if (response != null) {
                response.close();
            }
        }
        return result;
    }

}
