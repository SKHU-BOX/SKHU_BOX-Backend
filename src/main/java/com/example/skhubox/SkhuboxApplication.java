package com.example.skhubox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class SkhuboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkhuboxApplication.class, args);
    }

}
