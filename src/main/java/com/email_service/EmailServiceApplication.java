package com.email_service;

import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@EnableCaching
@SpringBootApplication
public class EmailServiceApplication {

	private static long appStartTime = 0L;

	public static void main(String[] args) {
		SpringApplication.run(EmailServiceApplication.class, args);
		appStartTime = System.currentTimeMillis();
		log.info("Email Service Application started successfully at {}", new Date(appStartTime));
	}

	@GetMapping("/")
	String home() {
		if (appStartTime == 0L)
			return "Email Service Application is starting.....";
		return "Email Service Application. Running since: " + new Date(appStartTime);
	}
}
