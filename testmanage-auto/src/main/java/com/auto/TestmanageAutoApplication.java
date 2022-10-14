package com.auto;

import com.test.testmanagecommon.vaildate.RequestValidateAspect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({RequestValidateAspect.class})
@MapperScan(basePackages = {"com.auto.dao","com.test.testmanagecommon.commonMapper" })
public class TestmanageAutoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestmanageAutoApplication.class, args);
    }

}
