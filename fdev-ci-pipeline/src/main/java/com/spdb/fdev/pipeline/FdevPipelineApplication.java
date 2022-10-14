package com.spdb.fdev.pipeline;

import com.spdb.fdev.base.annotation.nonull.RunnerTokenAspect;
import com.spdb.fdev.base.listener.PipelineKafkaListener;
import com.spdb.fdev.base.utils.VaildateUtil;
import com.spdb.fdev.base.annotation.nonull.NoNullAspect;
import com.spdb.fdev.base.listener.SaveMongoEventListener;
import com.spdb.fdev.base.utils.MinioUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
//@Import({SaveMongoEventListener.class, MinioUtils.class, VaildateUtil.class, RunnerTokenAspect.class, NoNullAspect.class, PipelineKafkaListener.class, TokenMangerUtil.class, FdevUserCacheUtil.class})
@Import({SaveMongoEventListener.class, MinioUtils.class, VaildateUtil.class, RunnerTokenAspect.class, NoNullAspect.class, PipelineKafkaListener.class})
public class FdevPipelineApplication {

    public static void main(String[] args) {
        SpringApplication.run(FdevPipelineApplication.class, args);
    }

}
