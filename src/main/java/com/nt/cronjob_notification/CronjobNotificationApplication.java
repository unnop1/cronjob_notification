package com.nt.cronjob_notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Enable Spring's scheduled task execution
public class CronjobNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(CronjobNotificationApplication.class, args);
	}

}
