package com.spdb.fdev.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RequestMethodFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(TokenFilter.class);

    @Value("${requestMethodFilter.forbid.methods:PUT,DELETE}")
    private String forbidMethods;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        List<String> methods = Arrays.asList(forbidMethods.split(","));
        if (!methods.contains(request.getMethod())) {
            chain.doFilter(request, response);
        } else {
            logger.error("request methods is not allowed");
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "请求方法异常！");
            return;
        }
    }

}
