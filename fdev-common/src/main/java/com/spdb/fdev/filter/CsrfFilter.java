package com.spdb.fdev.filter;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.FilterConfig;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * CSRF过滤器
 *
 * @author CL
 */
public class CsrfFilter implements Filter {

    @Value("${spring.profiles.active}")
    private String active;

    /**
     * 过滤器配置对象
     */
    FilterConfig filterConfig = null;

    /**
     * 初始化
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    /**
     * 拦截
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String referer = request.getHeader("Referer");
        String ip = getIpAddress(request);

        // 判断是否存在外链请求本站
        if (!active.contains("sit") && null != referer && referer.indexOf(ip) < 0) {
            Map map = new HashMap<>();
            map.put("code", "9999999");
            map.put("msg", "系统不支持当前域名的访问！");
            map.put("data", "");
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(JSONObject.toJSONString(map).getBytes());
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    /**
     * 销毁
     */
    @Override
    public void destroy() {
        this.filterConfig = null;
    }

    /**
     * 从request中获取请求方IP
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1") ? "xxx" : ip;
    }

}