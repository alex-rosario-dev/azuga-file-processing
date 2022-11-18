package com.example.azuga;

import com.example.azuga.service.FileProcessService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AzugaApplication {

	
	public static void main(String[] args) {
		SpringApplication.run(AzugaApplication.class, args);
	}
	

}
