package com.spdb.fdev.common.interceptor;

import com.spdb.fdev.common.dict.Constants;
import com.spdb.fdev.common.dict.Dict;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class RequestHeaderInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
                                        ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        httpRequest.getHeaders().set(Dict.BACKEND_REQUEST_FLAG_KEY, Constants.BACKEND_REQUEST_FLAG_VALUE);
        httpRequest.getHeaders().set(Dict.HTTPHEADER_USERNAME_EN, MDC.get(Dict.MDC_USERNAME_EN));
        return clientHttpRequestExecution.execute(httpRequest, bytes);
    }
}
