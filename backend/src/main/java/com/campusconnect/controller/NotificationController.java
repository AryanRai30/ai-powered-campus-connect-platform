package com.campusconnect.controller;

import com.campusconnect.dto.NotificationResponse;
import com.campusconnect.dto.UnreadCountResponse;
import com.campusconnect.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing Notification endpoints for authenticated users.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        List<NotificationResponse> response = notificationService.getMyNotifications(currentUserEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        UnreadCountResponse response = notificationService.getUnreadCount(currentUserEmail);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();
        NotificationResponse response = notificationService.markAsRead(id, currentUserEmail);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        int updatedCount = notificationService.markAllAsRead(currentUserEmail);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read", "updatedCount", updatedCount));
    }
}
