package com.campusconnect.service;

import com.campusconnect.dto.EventResponse;
import com.campusconnect.dto.RegistrationStatusResponse;
import com.campusconnect.entity.Event;
import com.campusconnect.entity.EventRegistration;
import com.campusconnect.entity.User;
import com.campusconnect.exception.EventAlreadyRegisteredException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.EventRegistrationRepository;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service providing business logic for Campus Events and Student Event Registrations.
 */
@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public EventService(
            EventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents(String category, String search, String currentUserEmail) {
        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        List<Event> events = eventRepository.filterEvents(category, search);

        return events.stream()
                .map(event -> mapToResponse(event, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId, String currentUserEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        return mapToResponse(event, currentUser);
    }

    public RegistrationStatusResponse registerForEvent(Long eventId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (registrationRepository.existsByEventIdAndUserId(eventId, user.getId())) {
            throw new EventAlreadyRegisteredException("Student is already registered for this event.");
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(user)
                .build();

        EventRegistration saved = registrationRepository.save(registration);

        return RegistrationStatusResponse.builder()
                .eventId(eventId)
                .registered(true)
                .registeredAt(saved.getRegisteredAt())
                .build();
    }

    @Transactional(readOnly = true)
    public RegistrationStatusResponse getRegistrationStatus(Long eventId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        Optional<EventRegistration> registrationOpt = registrationRepository.findByEventIdAndUserId(eventId, user.getId());

        return RegistrationStatusResponse.builder()
                .eventId(eventId)
                .registered(registrationOpt.isPresent())
                .registeredAt(registrationOpt.map(EventRegistration::getRegisteredAt).orElse(null))
                .build();
    }

    private EventResponse mapToResponse(Event event, User currentUser) {
        long count = registrationRepository.countByEventId(event.getId());
        boolean isRegistered = false;
        if (currentUser != null) {
            isRegistered = registrationRepository.existsByEventIdAndUserId(event.getId(), currentUser.getId());
        }

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .eventTime(event.getEventTime())
                .venue(event.getVenue())
                .category(event.getCategory())
                .organizer(event.getOrganizer())
                .registrationRequired(event.isRegistrationRequired())
                .registrationCount(count)
                .registered(isRegistered)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
