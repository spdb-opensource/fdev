package com.spdb.fdev.transport;

import com.spdb.fdev.common.interceptor.RequestHeaderInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by xxx on 上午9:57.
 */
@Configuration
public class RestfulAutoConfiguration {
    //请求连接超超时时间,默认10000ms
    @Value("${spdb.rest.ConnectTimeout:10000}")
    int connectionTimeout;
    //请求响应超时时间，默认60000ms
    @Value("${spdb.rest.ReadTimeout:60000}")
    int readTimeout;
    //从连接池中获取连接的超时时间,默认10000ms
    @Value("${spdb.rest.ConnectionRequestTimeout:10000}")
    int connectionRequestTimeout;
    //Http头全局流水号字段定义
    @Value("${spdb.rest.HttpHeader.GlobalSeqNoName:GlobalSeqNo}")
    String globalSeqNoName;
    // http client连接池最大连接数,默认500
    @Value("${spdb.rest.httpclient.maxTotal:500}")
    int maxTotal;
    // http client每个主机的并发,默认100
    @Value("${spdb.rest.httpclient.maxPerRoute:100}")
    int maxPerRoute;

    @Bean
    public RestTemplate getRestTemplate(){
        HttpRequestFactorySupplier httpRequestFactorySupplier = new HttpRequestFactorySupplier(createClientHttpRequestFactory());
        RestTemplate restTemplate = new RestTemplateBuilder()
                .requestFactory(httpRequestFactorySupplier)
                .build();
        RequestHeaderInterceptor requestHeaders = new RequestHeaderInterceptor();
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add(requestHeaders);
        restTemplate.setInterceptors(interceptors);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    private ClientHttpRequestFactory createClientHttpRequestFactory() {
        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        poolingConnectionManager.setMaxTotal(this.maxTotal); // 连接池最大连接数
        poolingConnectionManager.setDefaultMaxPerRoute(maxPerRoute); // 每个主机的并发

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().setConnectionManager(poolingConnectionManager);
        HttpClient httpClient = httpClientBuilder.build();

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        clientHttpRequestFactory.setConnectTimeout(this.connectionTimeout); // 连接超时，单位毫秒
        clientHttpRequestFactory.setReadTimeout(this.readTimeout); // 响应超时，单位毫秒
        clientHttpRequestFactory.setConnectionRequestTimeout(this.connectionRequestTimeout); //从连接池中获取连接的超时时间
        return clientHttpRequestFactory;
    }

    class HttpRequestFactorySupplier implements Supplier{
        public HttpRequestFactorySupplier(){}

        public HttpRequestFactorySupplier(ClientHttpRequestFactory clientHttpRequestFactory){
            this.clientHttpRequestFactory = clientHttpRequestFactory;
        }

        private ClientHttpRequestFactory clientHttpRequestFactory;

        @Override
        public Object get() {
            return clientHttpRequestFactory;
        }
    }
}
