package com.fdev.docmanage;

import com.fdev.docmanage.util.MinIoUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(MinIoUtils.class)
@ComponentScan(basePackages = {"com"})
public class FdevDocmanageApplication {

	public static void main(String[] args) {
			SpringApplication.run(FdevDocmanageApplication.class, args);
	}

}
