package com.example.auth;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AuthApplication {

    // MySql
    // public static void main(String[] args) {
    // EnvLoader.loadEnv();
    // SpringApplication.run(AuthApplication.class, args);
    // }

    // PostgreSQL
    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        EnvLoader.loadEnv();
        SpringApplication.run(AuthApplication.class, args);
    }

}
