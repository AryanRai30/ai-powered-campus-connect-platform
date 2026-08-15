package com.campusconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller exposing minimal protected endpoints for verifying authentication and role-based authorization.
 */
@RestController
@RequestMapping("/api/protected")
public class ProtectedTestController {

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testProtected() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Access granted to authenticated test endpoint");
        response.put("user", auth.getName());
        response.put("authorities", auth.getAuthorities());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student")
    public ResponseEntity<Map<String, Object>> testStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Access granted to Student protected area");
        response.put("user", auth.getName());
        response.put("role", "STUDENT");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/faculty")
    public ResponseEntity<Map<String, Object>> testFaculty() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Access granted to Faculty protected area");
        response.put("user", auth.getName());
        response.put("role", "FACULTY");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> testAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Access granted to Admin protected area");
        response.put("user", auth.getName());
        response.put("role", auth.getAuthorities());
        return ResponseEntity.ok(response);
    }
}
