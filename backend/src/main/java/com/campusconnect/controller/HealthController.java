package com.campusconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health Controller to verify that the Spring Boot backend application is running properly.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "Ai Powered Campus Connect Platform");
        return ResponseEntity.ok(response);
    }
}
