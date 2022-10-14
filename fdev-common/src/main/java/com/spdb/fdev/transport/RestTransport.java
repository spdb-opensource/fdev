package com.spdb.fdev.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.dict.Constants;
import com.spdb.fdev.common.dict.Dict;
import com.spdb.fdev.common.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by xxx on 上午10:41.
 */
@Component
public class RestTransport implements InitializingBean {

    @Value("${fdev.transport.log.req.data.enabled:true}")
    private boolean logReqDataEnabled;

    @Value("${fdev.transport.log.rsp.data.enabled:false}")
    private boolean logRsqDataEnabled;

    private Logger logger = LoggerFactory.getLogger(RestTransport.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 后端请求交易码、url映射
     */
    private Properties urlMapping = new Properties();

    /**
     * 后端请求接口属性映射
     */
    private Map interfaceMapping = new HashMap();

    @Resource(name = "getRestTemplate")
    RestTemplate restTemplate;

    @Autowired
    Environment env;

    private static final String LOG_TRANSPORT_BEGIN_FORMAT = ">>>>>>>>> begin transport: {} , random: {}";
    private static final String LOG_TRANSPORT_REQ_DATA_FORMAT = ">>>>>>>>> send data: {} , random: {}";
    private static final String LOG_TRANSPORT_RSP_DATA_FORMAT = ">>>>>>>>> receive data: {} , random: {}";
    private static final String LOG_TRANSPORT_END_FORMAT = ">>>>>>>>> end transport: {}  on time: {}ms, random: {}";
    private static final String LOG_TRANSPORT_ERROR_FORMAT = ">>>>>>>>> end transport {} with error: {}({}) on time: {}ms, random: {}";

    /**
     * 后端请求交易码、url映射properties配置文件地址
     */
    @Value("${fdev.transport.urlmapping.config.path:urlmapping.properties}")
    private String urlMappingConfigPath = "";

    /**
     * 后端请求交易属性映射配置文件地址
     */
    @Value("${fdev.transport.interfaceMapping.config.path:interfaceMapping.yml}")
    private String interfaceMappingConfigPath = "";

    public Object submit(Object send) throws Exception {
        String url = getRequestUrl(send);
        return submitInner(url, getSendData(url, send));
    }

    public byte[] submitForByte(Object send) throws Exception {
        String url = getRequestUrl(send);
        return submitInnerForByte(url, getSendData(url, send));
    }

    public Object submitUpload(String tranCode, Map params) throws Exception {
        long startTime = System.currentTimeMillis();
        String backEndUrl = (String) urlMapping.get(tranCode);
        printRequestInfos(backEndUrl, getSendData(backEndUrl, params), startTime);
        //设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("multipart/form-data"));
        //设置请求体
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.setAll(params);
        //用HttpEntity封装整个请求报文
        HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers);
        Map result;
        try {
            result = restTemplate.exchange(backEndUrl, HttpMethod.POST, files, Map.class).getBody();
        } catch (RestClientException ex) {
            logger.error("rest request error, url: {}", backEndUrl, ex);
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        return resultProcess(backEndUrl, result, startTime);
    }

    public Object submitPut(Object send) throws Exception {
        String backEndUrl = getRequestUrl(send);
        long startTime = System.currentTimeMillis();
        printRequestInfo(backEndUrl, getSendData(backEndUrl, send), startTime);
        Map result = null;
        try {
            ResponseEntity<Map> exchange = restTemplate.exchange(
                    backEndUrl,
                    HttpMethod.PUT,
                    new HttpEntity<Map>((Map) send),
                    Map.class);
            result = exchange.getBody();
        } catch (RestClientException ex) {
            logger.error("rest request error, url: {}", backEndUrl, ex);
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        return resultProcess(backEndUrl, result, startTime);
    }

    public Object submitGet(Object send) throws Exception {
        Map result = null;
        String backEndUrl = getRequestUrl(send);
        long startTime = System.currentTimeMillis();
        printRequestInfo(backEndUrl, getSendData(backEndUrl, send), startTime);
        Object forObject;
        try {
            forObject = restTemplate.getForObject(backEndUrl, Map.class);
        } catch (RestClientException ex) {
            logger.error("rest request error, url: {}", backEndUrl, ex);
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        if (Util.isNullOrEmpty(forObject) && forObject instanceof Map) {
            result = (Map) forObject;
            return resultProcess(backEndUrl, result, startTime);
        }
        return forObject;
    }

    /**
     * @param params   GET请求url参数列表
     * @param tranCode GET请求对应后端交易
     * @return
     * @throws Exception
     */
    public Object submitGet(String tranCode, Object params) throws Exception {
        String backEndUrl = (String) urlMapping.get(tranCode);
        long startTime = System.currentTimeMillis();
        printRequestInfo(backEndUrl, getSendData(backEndUrl, params), startTime);
        if (params instanceof Map) {
            backEndUrl = addUrlParams(backEndUrl, (Map) params);
        } else {
            logger.error("rest request error, url: {}", "请求参数有误不是map类型");
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"sendDate", "数据结构应为map"});
        }
        byte[] forObject;
        try {
            forObject = restTemplate.getForObject(backEndUrl, byte[].class);
        } catch (RestClientException ex) {
            logger.error("rest request error, url: {}", backEndUrl, ex);
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        return forObject;
    }

    public Object submitDelete(Object send) throws Exception {
        String backEndUrl = getRequestUrl(send);
        long startTime = System.currentTimeMillis();
        printRequestInfo(backEndUrl, getSendData(backEndUrl, send), startTime);
        Map result = null;
        try {
            ResponseEntity<Map> exchange = restTemplate.exchange(
                    backEndUrl,
                    HttpMethod.DELETE,
                    new HttpEntity<Map>((Map) send),
                    Map.class);
            result = exchange.getBody();
        } catch (RestClientException ex) {
            logger.error("rest request error, url: {}", backEndUrl, ex);
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        return resultProcess(backEndUrl, result, startTime);
    }

    public Object submitDeleteWithBody(Object send) throws Exception {
        String backEndUrl = getRequestUrl(send);
        long startTime = System.currentTimeMillis();
        printRequestInfo(backEndUrl, getSendData(backEndUrl, send), startTime);
        Map result = null;
        try {
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory() {
                @Override
                protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
                    if (HttpMethod.DELETE == httpMethod) {
                        return new HttpEntityEnclosingDeleteRequest(uri);
                    }
                    return super.createHttpUriRequest(httpMethod, uri);
                }
            });

            ResponseEntity<Map> exchange = restTemplate.exchange(
                    backEndUrl,
                    HttpMethod.DELETE,
                    new HttpEntity<Map>((Map) send),
                    Map.class);
            result = exchange.getBody();
        } catch (Exception ex) {
            logger.error("rest request error, url: {}", backEndUrl, ex);
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        return resultProcess(backEndUrl, result, startTime);
    }

    public Object submitGetWithBody(Object send) throws Exception {
        String backEndUrl = getRequestUrl(send);
        long startTime = System.currentTimeMillis();
        printRequestInfo(backEndUrl, getSendData(backEndUrl, send), startTime);
        Map result = null;
        try {
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory() {
                @Override
                protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
                    if (httpMethod == HttpMethod.GET) {
                        return new HttpGetRequestWithEntity(uri);
                    }
                    return super.createHttpUriRequest(httpMethod, uri);
                }
            });

            ResponseEntity<Map> exchange = restTemplate.exchange(
                    backEndUrl,
                    HttpMethod.GET,
                    new HttpEntity<Map>((Map) send),
                    Map.class);
            result = exchange.getBody();
        } catch (Exception ex) {
            logger.error("rest request error, url: {}", backEndUrl, ex);
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        return resultProcess(backEndUrl, result, startTime);
    }


    /**
     * Fdev调用第三方后台api
     *
     * @param url  调用api url
     * @param send 发送map数据
     * @return
     * @throws Exception
     */
    private Object submitInner(String url, Object send) throws Exception {
        Map result = null;
        long startTime = System.currentTimeMillis();
        String authorization = "";
        if(!Util.isNullOrEmpty(RequestContextHolder.getRequestAttributes())) {
            authorization = ((ServletRequestAttributes) RequestContextHolder.
                    getRequestAttributes()).getRequest().getHeader(Dict.TOKEN_AUTHORIZATION_KEY);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(Dict.TOKEN_AUTHORIZATION_KEY, authorization);
        HttpEntity requestEntity = new HttpEntity(send, headers);
        printRequestInfo(url, send, startTime);
        try {
            result = restTemplate.postForObject(url, requestEntity, Map.class);
        } catch (RestClientException ex) {
            logger.error("rest request error, url: {}", url, ex);
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        return resultProcess(url, result, startTime);

    }

    private byte[] submitInnerForByte(String url, Object send) throws Exception {
        byte[] result = null;
        long startTime = System.currentTimeMillis();
        printRequestInfo(url, send, startTime);
        try {
            result = restTemplate.postForObject(url, send, byte[].class);
        } catch (RestClientException ex) {
            logger.error("rest request error, url: {}", url, ex);
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        return result;

    }

    private void printRequestInfo(String url, Object send, long startTime) throws Exception {
        logger.info(LOG_TRANSPORT_BEGIN_FORMAT, url, startTime);
        if (logReqDataEnabled)
            logger.info(LOG_TRANSPORT_REQ_DATA_FORMAT, objectMapper.writeValueAsString(send), startTime);
    }

    private void printRequestInfos(String url, Object send, long startTime) {
        logger.info(LOG_TRANSPORT_BEGIN_FORMAT, url, startTime);
        if (logReqDataEnabled)
            logger.info(LOG_TRANSPORT_REQ_DATA_FORMAT, send.toString(), startTime);
    }

    private Object resultProcess(String url, Map result, long startTime) throws Exception {
        if (logRsqDataEnabled)
            logger.info(LOG_TRANSPORT_RSP_DATA_FORMAT, objectMapper.writeValueAsString(result), startTime);
        long lastTime = System.currentTimeMillis() - startTime;
        if (result != null && result.get(Dict.CODE) != null) {
            String returnCode = (String) result.get(Dict.CODE);
            String returnMsg = (String) result.get(Dict.MESSAGE);
            if (Constants.I_SUCCESS.equals(returnCode)) {
                Object returnData = result.get(Dict.DATA);
                if ("".equals(returnData)) {
                    returnData = null;
                }
                logger.info(LOG_TRANSPORT_END_FORMAT, url, lastTime, startTime);
                return getReturnData(url, returnData);
            } else {
                FdevException fex = new FdevException(returnCode);
                fex.setMessage(returnMsg);
                logger.error(LOG_TRANSPORT_ERROR_FORMAT, url, returnCode, returnMsg, lastTime, startTime);
                throw fex;
            }
        } else {
            logger.error("<<<<<<<<< end request error!!! on time: " + lastTime + "ms");
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
    }

    private void resultProcess(String url, long startTime) throws Exception {
        long lastTime = System.currentTimeMillis() - startTime;
        logger.info(LOG_TRANSPORT_END_FORMAT, url, lastTime, startTime);
    }


    /**
     * 根据请求数据获取请求url
     *
     * @param send
     * @return requestUrl
     */
    private String getRequestUrl(Object send) {
        String tranCode = (String) ((Map) send).remove(Dict.REST_CODE);
        if (Util.isNullOrEmpty(tranCode)) {
            logger.error("transaction code not defined:{}", tranCode);
            throw new FdevException(ErrorConstants.TRANS_CODE_CANNOT_BE_EMPTY);
        }
        String backEndUrl = (String) urlMapping.get(tranCode);
        if (Util.isNullOrEmpty(backEndUrl)) {
            logger.error("transaction code not defined:{}", tranCode);
            throw new FdevException(ErrorConstants.TRANS_CODE_UNDEFINED, new String[]{tranCode});
        }
        Object[] args = (Object[]) ((Map) send).remove(Dict.ARGS);
        if (!Util.isNullOrEmpty(args)) {
            backEndUrl = MessageFormat.format(backEndUrl, args);
        }
        return backEndUrl;
    }

    /**
     * 根据请求数据转换真实的请求数据
     *
     * @param send
     * @return requestData
     */
    private Object getSendData(String urlString, Object send) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            if (!Util.isNullOrEmpty(this.interfaceMapping) && this.interfaceMapping.keySet().contains(path)) {
                if (Util.isNullOrEmpty(((Map) this.interfaceMapping.get(path)).get("req"))) {
                    return send;
                } else {
                    replaceKey(send, ((Map) this.interfaceMapping.get(path)).get("req"));
                    return send;
                }
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        return send;
    }

    /**
     * 根据响应数据转换真实的返回数据
     *
     * @param res
     * @return responseData
     */
    private Object getReturnData(String urlString, Object res) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            if (!Util.isNullOrEmpty(this.interfaceMapping) && this.interfaceMapping.keySet().contains(path)) {
                if (Util.isNullOrEmpty(((Map) this.interfaceMapping.get(path)).get("res"))) {
                    return res;
                } else {
                    replaceKey(res, ((Map) this.interfaceMapping.get(path)).get("res"));
                    return res;
                }
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        return res;
    }

    private Object replaceKey(Object sourceData, Object mapping) {
        if (sourceData instanceof String) {
            return sourceData;
        }
        if (sourceData instanceof List) {
            for (int i = 0; i < ((List) sourceData).size(); i++) {
                replaceKey(((List) sourceData).get(i), ((List) mapping).get(0));
            }
        }
        if (sourceData instanceof Map) {
            Map mappingKey = (Map) mapping;
            for (Object key : mappingKey.keySet()) {
                String str = (String) key;
                String keyName = str.replaceAll("-key", "");
                if (str.contains("-key") && ((Map) sourceData).keySet().contains(keyName)) {
                    ((Map) sourceData).put(mappingKey.get(key), ((Map) sourceData).remove(keyName));
                }
                if (((Map) sourceData).keySet().contains(key)) {
                    Object object = mappingKey.get(key);
                    if (object instanceof String) {
                        ((Map) sourceData).put(mappingKey.get(key), ((Map) sourceData).remove(key));
                    }
                    if (object instanceof List) {
                        List list = (List) (((Map) sourceData).get(key));
                        for (int i = 0; i < list.size(); i++) {
                            replaceKey(list.get(i), ((List) object).get(0));
                        }
                    }
                    if (object instanceof Map) {
                        replaceKey(((Map) sourceData).get(key), object);
                    }
                }
            }
        }
        return sourceData;
    }

    /**
     * url 拼接请求参数
     *
     * @param backEndUrl
     * @param params
     * @return
     */
    public String addUrlParams(String backEndUrl, Map params) {
        StringBuffer urlBuffer = new StringBuffer(backEndUrl);
        Iterator iterator = params.entrySet().iterator();
        if (iterator.hasNext()) {
            urlBuffer.append("?");
            Object arg;
            while (iterator.hasNext()) {
                arg = iterator.next();
                Map.Entry<String, Object> entry = (Map.Entry) arg;
                //过滤value为null，value为null时进行拼接字符串会变成 "null"字符串
                if (entry.getValue() != null) {
                    urlBuffer.append(arg).append("&");
                }
                backEndUrl = urlBuffer.substring(0, urlBuffer.length() - 1);
            }
        }
        return backEndUrl;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!Util.isNullOrEmpty(this.urlMappingConfigPath)) {
            InputStream urlMappingInputStream = null;
            InputStreamReader inputStreamReader = null;
            try {
                if (this.urlMappingConfigPath.startsWith("http://")) {
                    urlMappingInputStream = new SecurityUrlResource(new URL(urlMappingConfigPath)).getInputStream();
                } else if (this.urlMappingConfigPath.startsWith("file://")) {
                    urlMappingInputStream = new FileInputStream(new File(urlMappingConfigPath));
                } else {
                    urlMappingInputStream = new ClassPathResource(this.urlMappingConfigPath).getInputStream();
                }
                inputStreamReader = new InputStreamReader(urlMappingInputStream, Charset.forName("UTF-8"));
                this.urlMapping.load(inputStreamReader);
            } catch (Exception exception) {
                logger.warn("RestTransport urlMapping init error, urlMappingConfigPath: {}",
                        this.urlMappingConfigPath, exception);
            } finally {
                if(null != urlMappingInputStream) {
                    urlMappingInputStream.close();
                }
                if(null != inputStreamReader) {
                    inputStreamReader.close();
                }
            }
            PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver =
                    new PropertyPlaceholderHelper.PlaceholderResolver() {
                        @Nullable
                        @Override
                        public String resolvePlaceholder(String placeHolder) {
                            return env.getProperty(placeHolder);
                        }
                    };

            for (Iterator iterator = this.urlMapping.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry entry = (Map.Entry) iterator.next();
                entry.setValue(Util.parseStringValue((String) entry.getValue(), placeholderResolver, new HashSet<String>()));
                logger.info("resolved fdev url mapping: {}={}", entry.getKey(), entry.getValue());
            }
        }
        if (!Util.isNullOrEmpty(this.interfaceMappingConfigPath)) {
            InputStream interfaceMappingInputStream = null;
            InputStreamReader inputStreamReader = null;
            try {
                Yaml yaml = new Yaml();
                if (this.interfaceMappingConfigPath.startsWith("http://")) {
                    interfaceMappingInputStream = new SecurityUrlResource(new URL(interfaceMappingConfigPath)).getInputStream();
                } else if (this.urlMappingConfigPath.startsWith("file://")) {
                    interfaceMappingInputStream = new FileInputStream(new File(interfaceMappingConfigPath));
                } else {
                    interfaceMappingInputStream = new ClassPathResource(this.interfaceMappingConfigPath).getInputStream();
                }
                inputStreamReader = new InputStreamReader(interfaceMappingInputStream, Charset.forName("UTF-8"));
                interfaceMapping = yaml.load(inputStreamReader);
            } catch (Exception exception) {
                logger.warn("RestTransport urlMapping init error, urlMappingConfigPath: {}",
                        this.urlMappingConfigPath, exception);
            } finally {
                if(null != interfaceMappingInputStream) {
                    interfaceMappingInputStream.close();
                }
                if(null != inputStreamReader) {
                    inputStreamReader.close();
                }
            }
        }
    }


    public Properties getUrlMapping() {
        return urlMapping;
    }

    public void setUrlMapping(Properties urlMapping) {
        this.urlMapping = urlMapping;
    }

    public Map getInterfaceMapping() {
        return interfaceMapping;
    }

    public void setInterfaceMapping(Map interfaceMapping) {
        this.interfaceMapping = interfaceMapping;
    }
}

class HttpEntityEnclosingDeleteRequest extends HttpEntityEnclosingRequestBase {

    public HttpEntityEnclosingDeleteRequest(final URI uri) {
        super();
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return HttpMethod.DELETE.name();
    }
}

class HttpGetRequestWithEntity extends HttpEntityEnclosingRequestBase {
    public HttpGetRequestWithEntity(final URI uri) {
        super();
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return HttpMethod.GET.name();
    }
}