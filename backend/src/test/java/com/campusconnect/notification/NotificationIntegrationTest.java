package com.campusconnect.notification;

import com.campusconnect.dto.NotificationResponse;
import com.campusconnect.dto.UnreadCountResponse;
import com.campusconnect.entity.NotificationType;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.NotificationRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class NotificationIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        List<User> users = userRepository.findAll();
        assertThat(users).isNotEmpty();
        testUser1 = users.get(0);

        if (users.size() > 1) {
            testUser2 = users.get(1);
        } else {
            testUser2 = userRepository.save(User.builder()
                    .email("other.user@campusconnect.edu")
                    .password("Password@123")
                    .firstName("Other")
                    .lastName("User")
                    .build());
        }
    }

    @Test
    @DisplayName("Should create and retrieve notifications for a user in descending creation order")
    void shouldCreateAndRetrieveNotificationsForUser() {
        notificationService.createNotification(
                testUser1,
                "Event Published",
                "New tech workshop added",
                NotificationType.EVENT,
                "EVENT",
                100L,
                "/events/1"
        );

        notificationService.createNotification(
                testUser1,
                "Club Membership Approved",
                "You are now a member of Robotics Club",
                NotificationType.CLUB_MEMBERSHIP,
                "CLUB",
                200L,
                "/clubs/2"
        );

        List<NotificationResponse> notifications = notificationService.getMyNotifications(testUser1.getEmail());

        assertThat(notifications).hasSize(2);
        assertThat(notifications.get(0).getTitle()).isEqualTo("Club Membership Approved");
        assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.CLUB_MEMBERSHIP);
        assertThat(notifications.get(1).getTitle()).isEqualTo("Event Published");
        assertThat(notifications.get(1).getType()).isEqualTo(NotificationType.EVENT);
    }

    @Test
    @DisplayName("Should calculate unread notification count accurately")
    void shouldCalculateUnreadCountCorrectly() {
        UnreadCountResponse initialCount = notificationService.getUnreadCount(testUser1.getEmail());
        assertThat(initialCount.getUnreadCount()).isEqualTo(0);

        notificationService.createNotification(
                testUser1,
                "Resource Uploaded",
                "New notes available",
                NotificationType.RESOURCE,
                "RESOURCE",
                1L,
                "/resources/1"
        );

        notificationService.createNotification(
                testUser1,
                "Announcement Posted",
                "Exam schedule released",
                NotificationType.ANNOUNCEMENT,
                "ANNOUNCEMENT",
                2L,
                "/announcements/1"
        );

        UnreadCountResponse newCount = notificationService.getUnreadCount(testUser1.getEmail());
        assertThat(newCount.getUnreadCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should mark single notification as read")
    void shouldMarkNotificationAsRead() {
        NotificationResponse created = notificationService.createNotification(
                testUser1,
                "Opportunity Status Updated",
                "Application reviewed",
                NotificationType.OPPORTUNITY_APPLICATION,
                "OPPORTUNITY",
                5L,
                "/opportunities/1"
        );

        NotificationResponse updated = notificationService.markAsRead(created.getId(), testUser1.getEmail());
        assertThat(updated.isRead()).isTrue();

        UnreadCountResponse count = notificationService.getUnreadCount(testUser1.getEmail());
        assertThat(count.getUnreadCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should mark all notifications as read for a user")
    void shouldMarkAllNotificationsAsRead() {
        notificationService.createNotification(testUser1, "N1", "M1", NotificationType.SYSTEM, null, null, null);
        notificationService.createNotification(testUser1, "N2", "M2", NotificationType.SYSTEM, null, null, null);

        int updatedCount = notificationService.markAllAsRead(testUser1.getEmail());
        assertThat(updatedCount).isEqualTo(2);

        UnreadCountResponse count = notificationService.getUnreadCount(testUser1.getEmail());
        assertThat(count.getUnreadCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should isolate notifications between different users")
    void shouldIsolateNotificationsBetweenUsers() {
        NotificationResponse user1Notif = notificationService.createNotification(
                testUser1, "User 1 Notif", "Message 1", NotificationType.SYSTEM, null, null, null
        );

        NotificationResponse user2Notif = notificationService.createNotification(
                testUser2, "User 2 Notif", "Message 2", NotificationType.SYSTEM, null, null, null
        );

        List<NotificationResponse> user1List = notificationService.getMyNotifications(testUser1.getEmail());
        assertThat(user1List).hasSize(1);
        assertThat(user1List.get(0).getTitle()).isEqualTo("User 1 Notif");

        List<NotificationResponse> user2List = notificationService.getMyNotifications(testUser2.getEmail());
        assertThat(user2List).hasSize(1);
        assertThat(user2List.get(0).getTitle()).isEqualTo("User 2 Notif");

        // User 2 should not be able to mark User 1's notification as read
        assertThatThrownBy(() -> notificationService.markAsRead(user1Notif.getId(), testUser2.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("REGRESSION TEST 1-6 & 9: Complete notification mark-as-read persistence flow")
    void testNotificationReadStatePersistenceFlow() {
        // TEST 1: Create notification for Student A. Confirm isRead=false
        NotificationResponse created = notificationService.createNotification(
                testUser1,
                "Assignment Graded",
                "Your CS101 assignment has been graded.",
                NotificationType.ANNOUNCEMENT,
                "ANNOUNCEMENT",
                10L,
                "/announcements/10"
        );
        assertThat(created.isRead()).isFalse();

        // TEST 2: Student A marks notification as read. Confirm API succeeds.
        NotificationResponse markReadResponse = notificationService.markAsRead(created.getId(), testUser1.getEmail());
        assertThat(markReadResponse.isRead()).isTrue();
        assertThat(markReadResponse.getReadAt()).isNotNull();

        // TEST 3: Reload notification from database. Confirm isRead=true in DB.
        com.campusconnect.entity.Notification dbEntity = notificationRepository.findById(created.getId())
                .orElseThrow(() -> new AssertionError("Notification not found in database"));
        assertThat(dbEntity.isRead()).isTrue();
        assertThat(dbEntity.getReadAt()).isNotNull();

        // TEST 4: Fetch Student A notifications. Confirm notification is returned as read.
        List<NotificationResponse> allNotifications = notificationService.getMyNotifications(testUser1.getEmail());
        assertThat(allNotifications).hasSize(1);
        assertThat(allNotifications.get(0).isRead()).isTrue();

        // TEST 5: Fetch Student A unread notifications query. Confirm notification is NOT returned.
        List<com.campusconnect.entity.Notification> unreadEntities =
                notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDescIdDesc(testUser1.getId());
        assertThat(unreadEntities).isEmpty();

        // TEST 6: Fetch Student A unread count. Confirm count decreases to 0.
        UnreadCountResponse countResponse = notificationService.getUnreadCount(testUser1.getEmail());
        assertThat(countResponse.getUnreadCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("REGRESSION TEST 7: Student B cannot mark Student A's notification as read")
    void testUserIsolationOnMarkAsRead() {
        NotificationResponse user1Notif = notificationService.createNotification(
                testUser1, "Private Alert", "Confidential information", NotificationType.SYSTEM, null, null, null
        );

        assertThatThrownBy(() -> notificationService.markAsRead(user1Notif.getId(), testUser2.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);

        // Verify entity remains unread in database
        com.campusconnect.entity.Notification dbEntity = notificationRepository.findById(user1Notif.getId()).orElseThrow();
        assertThat(dbEntity.isRead()).isFalse();
    }

    @Test
    @DisplayName("REGRESSION TEST 8: Mark-all-as-read persists after database reload")
    void testMarkAllAsReadPersistence() {
        notificationService.createNotification(testUser1, "N1", "Msg 1", NotificationType.SYSTEM, null, null, null);
        notificationService.createNotification(testUser1, "N2", "Msg 2", NotificationType.EVENT, null, null, null);
        notificationService.createNotification(testUser1, "N3", "Msg 3", NotificationType.RESOURCE, null, null, null);

        int count = notificationService.markAllAsRead(testUser1.getEmail());
        assertThat(count).isEqualTo(3);

        // Verify all entities persisted as isRead=true in database
        List<com.campusconnect.entity.Notification> allInDb = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(testUser1.getId());
        assertThat(allInDb).hasSize(3);
        assertThat(allInDb).allMatch(com.campusconnect.entity.Notification::isRead);

        // Verify unread count is zero after reload
        assertThat(notificationService.getUnreadCount(testUser1.getEmail()).getUnreadCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("REGRESSION TEST: NotificationResponse serializes boolean field as 'isRead' in JSON")
    void testNotificationResponseJacksonJsonPropertyIsRead() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        NotificationResponse response = NotificationResponse.builder()
                .id(100L)
                .title("Test Title")
                .message("Test Message")
                .isRead(true)
                .build();

        String json = mapper.writeValueAsString(response);
        assertThat(json).contains("\"isRead\":true");
        assertThat(json).doesNotContain("\"read\":true");
    }
}

