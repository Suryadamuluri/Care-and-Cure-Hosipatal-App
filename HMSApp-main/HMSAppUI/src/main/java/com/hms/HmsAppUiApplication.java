package com.hms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling
@SpringBootApplication
public class HmsAppUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HmsAppUiApplication.class, args);
	}

}
