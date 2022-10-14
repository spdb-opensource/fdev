package com.spdb.fdev.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean someFilterRegistration(@Autowired Filter tokenFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(tokenFilter);
        registration.addUrlPatterns("/*");
        registration.addInitParameter("paramName", "paramValue");
        registration.setName("tokenFilter");
        return registration;
    }

//    @Bean
//    public FilterRegistrationBean paramFilterRegistration(@Autowired Filter paramTrimFilter) {
//        FilterRegistrationBean registration = new FilterRegistrationBean();
//        registration.setFilter(paramTrimFilter);
//        registration.addUrlPatterns("/*");
//        registration.addInitParameter("paramName", "paramValue");
//        registration.setName("paramTrimFilter");
//        return registration;
//    }

    @Bean
    public FilterRegistrationBean requestMethodFilterRegistration(@Autowired Filter requestMethodFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(requestMethodFilter);
        registration.addUrlPatterns("/*");
        registration.addInitParameter("paramName", "paramValue");
        registration.setName("requestMethodFilter");
        return registration;
    }

//    @Bean
//    public FilterRegistrationBean csrfFilterRegistration(@Autowired Filter csrfFilter) {
//        FilterRegistrationBean registration = new FilterRegistrationBean();
//        registration.setFilter(csrfFilter);
//        registration.addUrlPatterns("/*");
//        registration.addInitParameter("paramName", "paramValue");
//        registration.setName("csrfFilter");
//        return registration;
//    }

    @Bean(name = "tokenFilter")
    public Filter tokenFilter() {
        return new TokenFilter();
    }

//    @Bean(name = "paramTrimFilter")
//    public Filter ParamTrimFilter() {
//        return new ParamTrimFilter();
//    }

    @Bean(name = "requestMethodFilter")
    public Filter requestMethodFilter() {
        return new RequestMethodFilter();
    }

//    @Bean(name = "csrfFilter")
//    public Filter csrfFilter() {
//        return new CsrfFilter();
//    }

}
