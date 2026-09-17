package com.campusconnect.controller;

import com.campusconnect.dto.AdminStudentDetailResponse;
import com.campusconnect.dto.AdminStudentResponse;
import com.campusconnect.dto.UpdateStudentStatusRequest;
import com.campusconnect.service.AdminStudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller exposing Administrator Student Management endpoints.
 */
@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentController {

    private final AdminStudentService adminStudentService;

    public AdminStudentController(AdminStudentService adminStudentService) {
        this.adminStudentService = adminStudentService;
    }

    @GetMapping
    public ResponseEntity<List<AdminStudentResponse>> getStudentList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String sem,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        List<AdminStudentResponse> students = adminStudentService.getStudentList(
                adminEmail, status, dept, course, year, sem, search);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminStudentDetailResponse> getStudentById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        AdminStudentDetailResponse student = adminStudentService.getStudentById(adminEmail, id);
        return ResponseEntity.ok(student);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminStudentResponse> updateStudentStatus(
            @PathVariable Long id,
            @RequestBody UpdateStudentStatusRequest request,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        AdminStudentResponse updatedStudent = adminStudentService.updateStudentStatus(adminEmail, id, request.isActive());
        return ResponseEntity.ok(updatedStudent);
    }
}
