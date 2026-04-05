package com.jmp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Jitsi Management Platform.
 * 
 * This is the entry point for the Spring Boot application that provides
 * centralized administration, monitoring, and management of Jitsi video conferences.
 * 
 * @author JMP Team
 * @since 1.0.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class JmpApplication {

    public static void main(String[] args) {
        SpringApplication.run(JmpApplication.class, args);
    }
}
