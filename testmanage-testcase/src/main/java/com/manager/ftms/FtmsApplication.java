package com.manager.ftms;

import com.test.testmanagecommon.cache.LazyInitAspect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.test.testmanagecommon.vaildate.RequestValidateAspect;

@SpringBootApplication
@Import({RequestValidateAspect.class, LazyInitAspect.class})
@MapperScan(basePackages = {"com.manager.ftms.dao","com.test.testmanagecommon.commonMapper"})
public class FtmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FtmsApplication.class, args);
	} 

}
