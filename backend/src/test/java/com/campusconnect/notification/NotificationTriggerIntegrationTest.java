package com.campusconnect.notification;

import com.campusconnect.dto.*;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.exception.EventAlreadyRegisteredException;
import com.campusconnect.exception.OpportunityAlreadyAppliedException;
import com.campusconnect.repository.NotificationRepository;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.StudentProfileRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class NotificationTriggerIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private FacultyAnnouncementService facultyAnnouncementService;

    @Autowired
    private FacultyEventService facultyEventService;

    @Autowired
    private EventService eventService;

    @Autowired
    private FacultyClubService facultyClubService;

    @Autowired
    private ClubService clubService;

    @Autowired
    private FacultyResourceService facultyResourceService;

    @Autowired
    private FacultyOpportunityService facultyOpportunityService;

    @Autowired
    private OpportunityService opportunityService;

    private User facultyUser;
    private User cseStudentUser;
    private User eceStudentUser;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role")));
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("STUDENT", "Student role")));

        facultyUser = userRepository.findByEmail("prof.trigger@campusconnect.edu")
                .orElseGet(() -> {
                    User u = User.builder()
                            .email("prof.trigger@campusconnect.edu")
                            .password("Password@123")
                            .firstName("Prof")
                            .lastName("Trigger")
                            .phone("5551112222")
                            .isActive(true)
                            .build();
                    u.addRole(facultyRole);
                    return userRepository.save(u);
                });

        cseStudentUser = userRepository.findByEmail("student.cse@campusconnect.edu")
                .orElseGet(() -> {
                    User u = User.builder()
                            .email("student.cse@campusconnect.edu")
                            .password("Password@123")
                            .firstName("CSE")
                            .lastName("Student")
                            .phone("5553334444")
                            .isActive(true)
                            .build();
                    u.addRole(studentRole);
                    User saved = userRepository.save(u);
                    studentProfileRepository.save(StudentProfile.builder()
                            .user(saved)
                            .studentId("STU-CSE-001")
                            .department("CSE")
                            .course("BTech CSE")
                            .year("3")
                            .semester("5")
                            .build());
                    return saved;
                });

        eceStudentUser = userRepository.findByEmail("student.ece@campusconnect.edu")
                .orElseGet(() -> {
                    User u = User.builder()
                            .email("student.ece@campusconnect.edu")
                            .password("Password@123")
                            .firstName("ECE")
                            .lastName("Student")
                            .phone("5555556666")
                            .isActive(true)
                            .build();
                    u.addRole(studentRole);
                    User saved = userRepository.save(u);
                    studentProfileRepository.save(StudentProfile.builder()
                            .user(saved)
                            .studentId("STU-ECE-002")
                            .department("ECE")
                            .course("BTech ECE")
                            .year("2")
                            .semester("3")
                            .build());
                    return saved;
                });
    }

    // --- ANNOUNCEMENT TRIGGERS ---

    @Test
    @DisplayName("Published targeted announcement should notify matching students and exclude non-matching")
    void testAnnouncementPublicationNotification() {
        FacultyAnnouncementRequest request = FacultyAnnouncementRequest.builder()
                .title("CSE Department Seminar")
                .content("Seminar details")
                .category("Departmental")
                .targetDepartment("CSE")
                .published(true)
                .build();

        facultyAnnouncementService.createAnnouncement(request, facultyUser.getEmail());

        List<NotificationResponse> cseNotifs = notificationService.getMyNotifications(cseStudentUser.getEmail());
        List<NotificationResponse> eceNotifs = notificationService.getMyNotifications(eceStudentUser.getEmail());

        assertThat(cseNotifs).hasSize(1);
        assertThat(cseNotifs.get(0).getTitle()).isEqualTo("New announcement");
        assertThat(cseNotifs.get(0).getMessage()).contains("CSE Department Seminar");
        assertThat(cseNotifs.get(0).getActionUrl()).isEqualTo("/announcements");

        assertThat(eceNotifs).isEmpty();
    }

    @Test
    @DisplayName("Draft announcement should not create any notifications")
    void testDraftAnnouncementNoNotification() {
        FacultyAnnouncementRequest request = FacultyAnnouncementRequest.builder()
                .title("Draft Announcement")
                .content("Draft content")
                .category("General")
                .published(false)
                .build();

        facultyAnnouncementService.createAnnouncement(request, facultyUser.getEmail());

        assertThat(notificationService.getMyNotifications(cseStudentUser.getEmail())).isEmpty();
        assertThat(notificationService.getMyNotifications(eceStudentUser.getEmail())).isEmpty();
    }

    // --- EVENT TRIGGERS ---

    @Test
    @DisplayName("Published targeted event notifies matching students; draft creates none")
    void testEventPublicationNotification() {
        FacultyEventRequest draftReq = FacultyEventRequest.builder()
                .title("Draft Hackathon")
                .description("Draft desc")
                .eventDate(LocalDate.now().plusDays(5))
                .venue("Main Hall")
                .published(false)
                .build();
        facultyEventService.createEvent(draftReq, facultyUser.getEmail());
        assertThat(notificationService.getMyNotifications(cseStudentUser.getEmail())).isEmpty();

        FacultyEventRequest pubReq = FacultyEventRequest.builder()
                .title("Public Coding Contest")
                .description("Contest desc")
                .eventDate(LocalDate.now().plusDays(10))
                .venue("Auditorium B")
                .targetDepartment("CSE")
                .published(true)
                .build();
        facultyEventService.createEvent(pubReq, facultyUser.getEmail());

        List<NotificationResponse> cseNotifs = notificationService.getMyNotifications(cseStudentUser.getEmail());
        assertThat(cseNotifs).hasSize(1);
        assertThat(cseNotifs.get(0).getTitle()).isEqualTo("New event available");
        assertThat(cseNotifs.get(0).getMessage()).contains("Public Coding Contest");

        assertThat(notificationService.getMyNotifications(eceStudentUser.getEmail())).isEmpty();
    }

    @Test
    @DisplayName("Student event registration notifies faculty owner; duplicate registration throws exception and creates no duplicate notification")
    void testStudentEventRegistrationNotification() {
        FacultyEventRequest pubReq = FacultyEventRequest.builder()
                .title("AI Workshop 2026")
                .description("AI Workshop")
                .eventDate(LocalDate.now().plusDays(7))
                .venue("Seminar Room 1")
                .published(true)
                .build();
        FacultyEventResponse event = facultyEventService.createEvent(pubReq, facultyUser.getEmail());

        eventService.registerForEvent(event.getId(), cseStudentUser.getEmail());

        List<NotificationResponse> facultyNotifs = notificationService.getMyNotifications(facultyUser.getEmail());
        assertThat(facultyNotifs).hasSize(1);
        assertThat(facultyNotifs.get(0).getTitle()).isEqualTo("New event registration");
        assertThat(facultyNotifs.get(0).getMessage()).contains("CSE Student registered for your event: AI Workshop 2026");

        // Duplicate registration attempt
        assertThatThrownBy(() -> eventService.registerForEvent(event.getId(), cseStudentUser.getEmail()))
                .isInstanceOf(EventAlreadyRegisteredException.class);

        assertThat(notificationService.getMyNotifications(facultyUser.getEmail())).hasSize(1);
    }

    // --- CLUB TRIGGERS ---

    @Test
    @DisplayName("Published club notifies eligible students; student join notifies faculty owner")
    void testClubPublicationAndJoinNotifications() {
        FacultyClubRequest clubReq = FacultyClubRequest.builder()
                .name("Robotics Club 2026")
                .description("Robotics description")
                .category("Technical")
                .department("CSE")
                .published(true)
                .build();

        FacultyClubResponse club = facultyClubService.createClub(clubReq, facultyUser.getEmail());

        List<NotificationResponse> cseNotifs = notificationService.getMyNotifications(cseStudentUser.getEmail());
        assertThat(cseNotifs).hasSize(1);
        assertThat(cseNotifs.get(0).getTitle()).isEqualTo("New club available");
        assertThat(cseNotifs.get(0).getMessage()).contains("Robotics Club 2026");

        // Student joins club
        clubService.joinClub(club.getId(), cseStudentUser.getEmail());

        List<NotificationResponse> facultyNotifs = notificationService.getMyNotifications(facultyUser.getEmail());
        assertThat(facultyNotifs).hasSize(1);
        assertThat(facultyNotifs.get(0).getTitle()).isEqualTo("New club application");
        assertThat(facultyNotifs.get(0).getMessage()).contains("CSE Student has applied to join Robotics Club 2026");
    }

    // --- RESOURCE TRIGGERS ---

    @Test
    @DisplayName("Published resource notifies eligible students; draft creates none")
    void testResourcePublicationNotification() {
        FacultyResourceRequest draftReq = FacultyResourceRequest.builder()
                .title("Draft Notes")
                .description("Draft notes content")
                .subject("Computer Science")
                .published(false)
                .build();
        facultyResourceService.createResource(draftReq, facultyUser.getEmail());
        assertThat(notificationService.getMyNotifications(cseStudentUser.getEmail())).isEmpty();

        FacultyResourceRequest pubReq = FacultyResourceRequest.builder()
                .title("DBMS Unit 3 Notes")
                .description("Database systems unit 3")
                .subject("Database Systems")
                .targetDepartment("CSE")
                .published(true)
                .build();
        facultyResourceService.createResource(pubReq, facultyUser.getEmail());

        List<NotificationResponse> cseNotifs = notificationService.getMyNotifications(cseStudentUser.getEmail());
        assertThat(cseNotifs).hasSize(1);
        assertThat(cseNotifs.get(0).getTitle()).isEqualTo("New academic resource");
        assertThat(cseNotifs.get(0).getMessage()).contains("DBMS Unit 3 Notes");

        assertThat(notificationService.getMyNotifications(eceStudentUser.getEmail())).isEmpty();
    }

    // --- OPPORTUNITY TRIGGERS ---

    @Test
    @DisplayName("Published opportunity notifies eligible students; internal application notifies faculty owner")
    void testInternalOpportunityNotification() {
        FacultyOpportunityRequest pubReq = FacultyOpportunityRequest.builder()
                .title("Research Assistantship")
                .description("Lab assistant position")
                .organization("Campus Labs")
                .targetDepartment("CSE")
                .published(true)
                .build();
        FacultyOpportunityResponse opp = facultyOpportunityService.createOpportunity(pubReq, facultyUser.getEmail());

        List<NotificationResponse> cseNotifs = notificationService.getMyNotifications(cseStudentUser.getEmail());
        assertThat(cseNotifs).hasSize(1);
        assertThat(cseNotifs.get(0).getTitle()).isEqualTo("New opportunity available");

        // Student applies internally
        opportunityService.applyForOpportunity(opp.getId(), cseStudentUser.getEmail());

        List<NotificationResponse> facultyNotifs = notificationService.getMyNotifications(facultyUser.getEmail());
        assertThat(facultyNotifs).hasSize(1);
        assertThat(facultyNotifs.get(0).getTitle()).isEqualTo("New opportunity application");
        assertThat(facultyNotifs.get(0).getMessage()).contains("CSE Student applied for Research Assistantship");

        // Duplicate application attempt
        assertThatThrownBy(() -> opportunityService.applyForOpportunity(opp.getId(), cseStudentUser.getEmail()))
                .isInstanceOf(OpportunityAlreadyAppliedException.class);
        assertThat(notificationService.getMyNotifications(facultyUser.getEmail())).hasSize(1);
    }

    @Test
    @DisplayName("External opportunity link should NOT generate application notification to faculty")
    void testExternalOpportunityNoApplicationNotification() {
        FacultyOpportunityRequest extReq = FacultyOpportunityRequest.builder()
                .title("External Software Internship")
                .description("Apply on company portal")
                .organization("Tech Corp")
                .applicationUrl("https://techcorp.com/careers/123")
                .published(true)
                .build();
        FacultyOpportunityResponse opp = facultyOpportunityService.createOpportunity(extReq, facultyUser.getEmail());

        opportunityService.applyForOpportunity(opp.getId(), cseStudentUser.getEmail());

        // Faculty should receive NO application notification for external links
        assertThat(notificationService.getMyNotifications(facultyUser.getEmail())).isEmpty();
    }
}
