package com.spdb.fdev.fdevfootprint.spdb.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spdb.fdev.fdevfootprint.base.dict.Dict;
import com.spdb.fdev.fdevfootprint.spdb.netty.server.impl.HttpServer;

@Configuration
@EnableAutoConfiguration
public class HttpServerConfig {
	
	@Bean(name=Dict.SWITCH_HTTP_SERVER)
	@ConfigurationProperties(prefix = Dict.HTTP_SERVER_SWITCH)
	public HttpServer getSwitchServer(){
		HttpServer server = new HttpServer();
		return server;
	}
	
	@Bean(name=Dict.ACQUISITION_HTTP_SERVER)
	@ConfigurationProperties(prefix = Dict.HTTP_SERVER_ACQUISITION)
	public HttpServer getAcquisitionServer(){
		HttpServer server = new HttpServer();
		return server;
	}

}
