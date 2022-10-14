package com.spdb;


import com.csii.pe.config.JobThreadConfig;
import com.csii.pe.config.ServletConfiguration;
import com.csii.pe.config.aspect.CoreControllerImplAspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableDiscoveryClient
@Import({ServletConfiguration.class, JobThreadConfig.class, CoreControllerImplAspect.class})
@PropertySource(value = "${SPRING_CLOUD_CONFIG_URI}")
@ImportResource({
        "classpath*:/config/core/**/*.xml",
        "classpath*:/config/channel/**/*.xml",
        "classpath*:/config/spdb/trans/**/*.xml"
})
// 禁止spring boot默认加载数据源配置
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, FlywayAutoConfiguration.class})
@EnableKafka
@EnableAsync
public class JobExecutorApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobExecutorApplication.class, args);
    }

}
