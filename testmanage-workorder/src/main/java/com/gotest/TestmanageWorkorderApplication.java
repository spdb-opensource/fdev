package com.gotest;

import com.test.testmanagecommon.cache.LazyInitAspect;
import com.test.testmanagecommon.vaildate.RequestValidateAspect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({RequestValidateAspect.class,LazyInitAspect.class})
@MapperScan({"com.gotest.dao","com.test.testmanagecommon.commonMapper"})
public class TestmanageWorkorderApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestmanageWorkorderApplication.class, args);
    }

}


