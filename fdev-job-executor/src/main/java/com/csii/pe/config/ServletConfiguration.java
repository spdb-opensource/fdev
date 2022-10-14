package com.csii.pe.config;

import com.csii.pe.channel.http.servlet.ext.MainServlet0;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.servlet.Servlet;

@Configuration
public class ServletConfiguration {
    @Bean
    public Servlet mainServlet() {
        MainServlet0 servlet = new MainServlet0();
        return servlet;
    }

    @Bean
    public ServletRegistrationBean mainServletRegistration() {
        ServletRegistrationBean registration = new ServletRegistrationBean(mainServlet());
        registration.getUrlMappings().clear();
        registration.addUrlMappings("/rest/*");
        return registration;
    }

}
