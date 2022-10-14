package com.spdb.fdev.fdevapp;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * Swagger2
 * 访问地址http://localhost:8080/fapp/swagger-ui.html
 * */

@Configuration
@EnableSwagger2
public class Swagger2Config {
	@Bean
	public Docket api() {
		ParameterBuilder tiketPar = new ParameterBuilder();
		List<Parameter> pars  = new ArrayList<Parameter>();
		tiketPar.name("Authorization").description("登录认证")
				.modelRef(new ModelRef("string")).parameterType("header")
				.required(false).build();
		ParameterBuilder sourcePar = new ParameterBuilder();
		sourcePar.name("source").description("请求来源")
				.modelRef(new ModelRef("string")).parameterType("header")
				.required(false).build();
		pars.add(tiketPar.build());
		pars.add(sourcePar.build());
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.spdb.fdev.fdevapp"))
				.paths(PathSelectors.any())
				.build()
				.globalOperationParameters(pars)
				.apiInfo(apiInfo());
	}
	//
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("fdev-app-api文档")
				.version("1.0")
				.build();
	}
}
