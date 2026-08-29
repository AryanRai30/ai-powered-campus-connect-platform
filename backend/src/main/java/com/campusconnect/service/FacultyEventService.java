package com.campusconnect.service;

import com.campusconnect.dto.EventRegistrationResponse;
import com.campusconnect.dto.FacultyEventRequest;
import com.campusconnect.dto.FacultyEventResponse;
import com.campusconnect.entity.Event;
import com.campusconnect.entity.EventRegistration;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.EventRegistrationRepository;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.StudentProfileRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacultyEventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public FacultyEventService(
            EventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            StudentProfileRepository studentProfileRepository,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedFaculty(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        boolean isFaculty = user.getRoles().stream()
                .anyMatch(r -> "FACULTY".equalsIgnoreCase(r.getName()));

        if (!isFaculty) {
            throw new AccessDeniedException("User does not possess FACULTY role.");
        }
        return user;
    }

    private Event getEventAndVerifyOwnership(Long id, User faculty) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (event.getCreatedBy() == null || !event.getCreatedBy().getId().equals(faculty.getId())) {
            throw new AccessDeniedException("Access Denied: You do not have permission to modify this event.");
        }
        return event;
    }

    public FacultyEventResponse createEvent(FacultyEventRequest request, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .eventTime(request.getEventTime())
                .venue(request.getVenue())
                .category(request.getCategory())
                .organizer(request.getOrganizer())
                .registrationRequired(request.getRegistrationRequired() != null ? request.getRegistrationRequired() : false)
                .targetDepartment(request.getTargetDepartment())
                .targetCourse(request.getTargetCourse())
                .targetYear(request.getTargetYear())
                .targetSemester(request.getTargetSemester())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .active(true)
                .createdBy(faculty)
                .build();

        Event saved = eventRepository.save(event);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FacultyEventResponse> getFacultyEvents(String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        List<Event> list = eventRepository.findAllByCreatedByOrderByIdDesc(faculty);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacultyEventResponse getFacultyEventById(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Event event = getEventAndVerifyOwnership(id, faculty);
        return mapToResponse(event);
    }

    public FacultyEventResponse updateEvent(Long id, FacultyEventRequest request, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Event event = getEventAndVerifyOwnership(id, faculty);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setEventTime(request.getEventTime());
        event.setVenue(request.getVenue());
        event.setCategory(request.getCategory());
        event.setOrganizer(request.getOrganizer());
        if (request.getRegistrationRequired() != null) {
            event.setRegistrationRequired(request.getRegistrationRequired());
        }
        event.setTargetDepartment(request.getTargetDepartment());
        event.setTargetCourse(request.getTargetCourse());
        event.setTargetYear(request.getTargetYear());
        event.setTargetSemester(request.getTargetSemester());
        if (request.getPublished() != null) {
            event.setPublished(request.getPublished());
        }

        Event updated = eventRepository.save(event);
        return mapToResponse(updated);
    }

    public void deleteEvent(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Event event = getEventAndVerifyOwnership(id, faculty);
        eventRepository.delete(event);
    }

    public FacultyEventResponse publishEvent(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Event event = getEventAndVerifyOwnership(id, faculty);
        event.setPublished(true);
        Event updated = eventRepository.save(event);
        return mapToResponse(updated);
    }

    public FacultyEventResponse unpublishEvent(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Event event = getEventAndVerifyOwnership(id, faculty);
        event.setPublished(false);
        Event updated = eventRepository.save(event);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getEventRegistrations(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Event event = getEventAndVerifyOwnership(id, faculty);

        List<EventRegistration> registrations = registrationRepository.findByEventId(event.getId());
        return registrations.stream().map(reg -> {
            User studentUser = reg.getUser();
            StudentProfile profile = studentProfileRepository.findByUserId(studentUser.getId()).orElse(null);
            return EventRegistrationResponse.builder()
                    .registrationId(reg.getId())
                    .studentId(studentUser.getId())
                    .firstName(studentUser.getFirstName())
                    .lastName(studentUser.getLastName())
                    .email(studentUser.getEmail())
                    .department(profile != null ? profile.getDepartment() : null)
                    .year(profile != null ? profile.getYear() : null)
                    .registeredAt(reg.getRegisteredAt())
                    .build();
        }).collect(Collectors.toList());
    }

    private FacultyEventResponse mapToResponse(Event e) {
        long regCount = registrationRepository.countByEventId(e.getId());
        return FacultyEventResponse.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .eventDate(e.getEventDate())
                .eventTime(e.getEventTime())
                .venue(e.getVenue())
                .category(e.getCategory())
                .organizer(e.getOrganizer())
                .registrationRequired(e.isRegistrationRequired())
                .registrationCount(regCount)
                .published(e.getPublished())
                .active(e.getActive())
                .targetDepartment(e.getTargetDepartment())
                .targetCourse(e.getTargetCourse())
                .targetYear(e.getTargetYear())
                .targetSemester(e.getTargetSemester())
                .createdByEmail(e.getCreatedBy() != null ? e.getCreatedBy().getEmail() : null)
                .createdByName(e.getCreatedBy() != null ? e.getCreatedBy().getFirstName() + " " + e.getCreatedBy().getLastName() : null)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
