package com.fdev.notify;
import com.spdb.fdev.common.util.TokenMangerUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
@Import(TokenMangerUtil.class)
public class FdevNotifyApplication {
	public static void main(String[] args) {
		SpringApplication.run(FdevNotifyApplication.class, args);
	}

}
