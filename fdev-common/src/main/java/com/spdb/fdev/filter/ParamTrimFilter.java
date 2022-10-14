package com.spdb.fdev.filter;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.config.ParamRequestWrapper;
import com.spdb.fdev.common.exception.FdevException;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ParamTrimFilter implements Filter {
    @Value("${fdev.param.trim.enabled:true}")
    private boolean paramTrimEnabled = true;
    @Value("${fdev.param.trim.exclude:}")
    private String excludeTrans;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        boolean excludeFlag = true;
        String[] excludeTransList = excludeTrans.split(",");
        for (String trans : excludeTransList) {
            if (request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1).equals(trans)) {
                excludeFlag = false;
            }
        }
        if (paramTrimEnabled && excludeFlag) {
            try {
                ParamRequestWrapper requestWrapper = new ParamRequestWrapper(request);
                chain.doFilter(requestWrapper, response);
            } catch (Exception e) {
                if (e instanceof FdevException) {
                    Map map = new HashMap<>();
                    map.put("code", ((FdevException) e).getCode());
                    map.put("msg", "请求参数异常");
                    map.put("data", "");
                    ServletOutputStream outputStream = response.getOutputStream();
                    outputStream.write(JSONObject.toJSONString(map).getBytes());
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }

}
