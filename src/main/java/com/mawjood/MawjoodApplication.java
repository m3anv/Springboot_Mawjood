package com.mawjood;

import com.mawjood.service.OfficeWorkflowService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Entry point for the Mawjood Spring Boot application.
 */
@SpringBootApplication
public class MawjoodApplication {

    public static void main(String[] args) {
        SpringApplication.run(MawjoodApplication.class, args);
    }

    /**
     * Runs once at startup to backfill any match records that may be missing
     * for existing inventory items.
     */
    @Bean
    CommandLineRunner scanMatchesOnStartup(OfficeWorkflowService officeWorkflowService) {
        return args -> officeWorkflowService.scanExistingForMatches();
    }
}
