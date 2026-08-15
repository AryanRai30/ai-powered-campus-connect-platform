package com.campusconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * Main entry point for the Ai Powered Campus Connect Platform backend.
 * 
 * Note for Phase 3: SecurityAutoConfiguration is excluded temporarily so health endpoints
 * are accessible without credential prompts. Spring Security configuration filters and JWT
 * tokens will be fully enabled in Phase 4 (Authentication).
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class CampusConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusConnectApplication.class, args);
    }
}
