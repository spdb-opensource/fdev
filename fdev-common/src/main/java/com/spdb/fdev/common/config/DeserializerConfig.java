package com.spdb.fdev.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class DeserializerConfig {
	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter httpMessageConverter = new MappingJackson2HttpMessageConverter();
		SimpleModule simpleModule =new SimpleModule();
		simpleModule.addDeserializer(String.class, new ParamTrimDeserializer());
		httpMessageConverter.getObjectMapper().registerModule(simpleModule);
		return httpMessageConverter;
	}
}
