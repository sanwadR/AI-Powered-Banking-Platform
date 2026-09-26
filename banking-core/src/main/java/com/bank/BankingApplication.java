package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The entry point of the entire application.
 *
 * @SpringBootApplication is a shortcut for 3 annotations:
 *   - @Configuration        : This class can define Spring beans
 *   - @EnableAutoConfiguration : Spring Boot auto-configures things based on what's in pom.xml
 *   - @ComponentScan        : Spring scans this package and all sub-packages for components
 *
 * When you run this class, it starts an embedded Tomcat web server on port 8080.
 */
@SpringBootApplication
@EnableScheduling   // Needed for @Scheduled tasks (we'll use this in Phase 2)
public class BankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
        System.out.println("""
                
                ================================================
                 🏦 AI-Driven Banking Platform is RUNNING!
                 API:     http://localhost:8080
                 Docs:    http://localhost:8080/swagger-ui.html
                 Health:  http://localhost:8080/actuator/health
                ================================================
                """);
    }
}
