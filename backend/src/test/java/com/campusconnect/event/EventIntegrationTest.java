package com.campusconnect.event;

import com.campusconnect.dto.EventResponse;
import com.campusconnect.dto.RegistrationStatusResponse;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.entity.Event;
import com.campusconnect.exception.EventAlreadyRegisteredException;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.service.AuthService;
import com.campusconnect.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class EventIntegrationTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AuthService authService;

    private Event testEvent;
    private final String studentEmail = "student.event.test@example.com";

    @BeforeEach
    void setUp() {
        authService.register(RegisterRequest.builder()
                .firstName("Event")
                .lastName("Student")
                .email(studentEmail)
                .password("Password@123")
                .build());

        testEvent = eventRepository.save(Event.builder()
                .title("Tech Workshop 2026")
                .description("Hands-on workshop on Spring Boot and React")
                .eventDate(LocalDate.now().plusDays(3))
                .eventTime(LocalTime.of(14, 0))
                .venue("Lab 101")
                .category("Workshop")
                .organizer("Tech Society")
                .registrationRequired(true)
                .build());
    }

    @Test
    @DisplayName("Should fetch all events and filter by category")
    void shouldFetchAllEventsAndFilterByCategory() {
        List<EventResponse> events = eventService.getAllEvents("Workshop", null, studentEmail);
        assertThat(events).isNotEmpty();
        assertThat(events.get(0).getCategory()).isEqualTo("Workshop");
    }

    @Test
    @DisplayName("Should register student for event and prevent duplicate registration")
    void shouldRegisterStudentForEventAndPreventDuplicateRegistration() {
        RegistrationStatusResponse response = eventService.registerForEvent(testEvent.getId(), studentEmail);
        assertThat(response.isRegistered()).isTrue();

        RegistrationStatusResponse status = eventService.getRegistrationStatus(testEvent.getId(), studentEmail);
        assertThat(status.isRegistered()).isTrue();

        assertThatThrownBy(() -> eventService.registerForEvent(testEvent.getId(), studentEmail))
                .isInstanceOf(EventAlreadyRegisteredException.class)
                .hasMessageContaining("Student is already registered for this event.");
    }
}
