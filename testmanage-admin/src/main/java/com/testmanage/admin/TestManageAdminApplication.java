package com.testmanage.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages={"com.testmanage.admin.mapper","com.test.testmanagecommon.commonMapper"})
public class TestManageAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestManageAdminApplication.class, args);
    }

}
