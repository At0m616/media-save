package com.example.url_media_save;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties()
public class UrlMediaSaveApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlMediaSaveApplication.class, args);
    }

}
