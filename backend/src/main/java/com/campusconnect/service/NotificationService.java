package com.campusconnect.service;

import com.campusconnect.dto.NotificationResponse;
import com.campusconnect.dto.UnreadCountResponse;
import com.campusconnect.entity.Notification;
import com.campusconnect.entity.NotificationType;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.NotificationRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reusable Service for creating, querying, and updating user Notifications.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Internal/Service helper to create a single notification for a recipient user.
     * Ready for Phase 10.2 triggers.
     */
    @Transactional
    public NotificationResponse createNotification(
            User recipient,
            String title,
            String message,
            NotificationType type,
            String relatedEntityType,
            Long relatedEntityId,
            String actionUrl
    ) {
        if (recipient == null) {
            throw new IllegalArgumentException("Recipient user cannot be null");
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .actionUrl(actionUrl)
                .build();

        Notification saved = notificationRepository.save(notification);
        return mapToResponse(saved);
    }

    /**
     * Internal/Service helper to create bulk notifications for multiple recipient users.
     * Ready for Phase 10.2 event/announcement broadcasting.
     */
    @Transactional
    public List<NotificationResponse> createNotifications(
            List<User> recipients,
            String title,
            String message,
            NotificationType type,
            String relatedEntityType,
            Long relatedEntityId,
            String actionUrl
    ) {
        if (recipients == null || recipients.isEmpty()) {
            return Collections.emptyList();
        }

        List<Notification> notifications = recipients.stream()
                .filter(u -> u != null && u.getId() != null)
                .map(recipient -> Notification.builder()
                        .recipient(recipient)
                        .title(title)
                        .message(message)
                        .type(type)
                        .isRead(false)
                        .relatedEntityType(relatedEntityType)
                        .relatedEntityId(relatedEntityId)
                        .actionUrl(actionUrl)
                        .build())
                .collect(Collectors.toList());

        List<Notification> savedList = notificationRepository.saveAll(notifications);
        return savedList.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Get all notifications belonging to the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        List<Notification> list = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(user.getId());
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Get unread notification count for the authenticated user.
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
        return new UnreadCountResponse(count);
    }

    /**
     * Mark a single notification as read (must belong to authenticated user).
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);

        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return mapToResponse(notification);
    }

    /**
     * Mark all notifications belonging to the authenticated user as read.
     */
    @Transactional
    public int markAllAsRead(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        return notificationRepository.markAllAsReadForUser(user.getId(), LocalDateTime.now());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .actionUrl(notification.getActionUrl())
                .build();
    }
}
