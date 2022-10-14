package com.plan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.test.testmanagecommon.vaildate.RequestValidateAspect;

@SpringBootApplication
@Import({RequestValidateAspect.class})
@MapperScan(basePackages = {"com.plan.dao","com.test.testmanagecommon.commonMapper" })
public class TestmanagePlanApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestmanagePlanApplication.class, args);
    }

}
