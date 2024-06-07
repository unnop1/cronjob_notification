package com.nt.cronjob_notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CronjobNotificationApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CronjobNotificationApplication.class, args);
    }

    //If you need a traditional war deployment we need to extend the SpringBootServletInitializer
    //This helps us deploy war files to Jboss containers
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder springApplicationBuilder) {
        return springApplicationBuilder.sources(CronjobNotificationApplication.class);
    }
}