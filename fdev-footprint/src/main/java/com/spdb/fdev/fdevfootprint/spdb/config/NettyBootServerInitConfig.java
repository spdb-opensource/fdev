package com.spdb.fdev.fdevfootprint.spdb.config;

import javax.annotation.Resource;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.spdb.fdev.fdevfootprint.base.dict.Dict;
import com.spdb.fdev.fdevfootprint.spdb.netty.server.impl.HttpServer;

@Component
public class NettyBootServerInitConfig implements ApplicationListener<ContextRefreshedEvent> {

	
//	@Resource(name = Dict.SWITCH_HTTP_SERVER)
//	HttpServer switchServer;
	
	@Resource(name = Dict.ACQUISITION_HTTP_SERVER)
	HttpServer acquisitionServer;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		//监听应用启动时启动server
		//采集开关服务，不开启
//		switchServer.start();
		acquisitionServer.start();
	}

}
