package com.test;

import com.test.testmanagecommon.vaildate.RequestValidateAspect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({RequestValidateAspect.class})
@MapperScan({"com.test.websocket","com.test.dao","com.test.testmanagecommon.commonMapper"})
public class TestmanageUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestmanageUserApplication.class, args);
    }
}