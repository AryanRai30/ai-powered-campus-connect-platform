package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.dto.DocumentIngestionRequest;
import com.campusconnect.ai.rag.mapper.CampusDocumentConverter;
import com.campusconnect.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CampusDocumentConverterTest {

    private CampusDocumentConverter converter;
    private User testUser;

    @BeforeEach
    void setUp() {
        converter = new CampusDocumentConverter();
        testUser = User.builder()
                .id(100L)
                .email("admin@campus.edu")
                .firstName("Campus")
                .lastName("Admin")
                .build();
    }

    @Test
    @DisplayName("Should convert published active announcement into RAG document request with metadata")
    void testConvertAnnouncementSuccess() {
        Announcement announcement = Announcement.builder()
                .id(1L)
                .title("Midterm Exam Schedule Announced")
                .content("Midterm examinations start next Monday at 9:00 AM.")
                .category("EXAM")
                .published(true)
                .active(true)
                .publishedAt(LocalDateTime.now())
                .targetDepartment("Computer Science")
                .targetCourse("B.Tech")
                .targetYear(3)
                .targetSemester(6)
                .createdBy(testUser)
                .build();

        DocumentIngestionRequest request = converter.convertAnnouncement(announcement);

        assertNotNull(request);
        assertEquals("ANNOUNCEMENT-1", request.getDocumentId());
        assertEquals("Midterm Exam Schedule Announced", request.getTitle());
        assertEquals("ANNOUNCEMENT", request.getCategory());
        assertTrue(request.getContent().contains("Midterm Exam Schedule Announced"));
        assertEquals("ANNOUNCEMENT", request.getMetadata().get("sourceType"));
        assertEquals("1", request.getMetadata().get("sourceId"));
        assertEquals("Computer Science", request.getMetadata().get("targetDepartment"));
        assertEquals("admin@campus.edu", request.getMetadata().get("createdBy"));
    }

    @Test
    @DisplayName("Should return null for draft or inactive announcement")
    void testConvertDraftAnnouncement() {
        Announcement draft = Announcement.builder()
                .id(2L)
                .title("Draft Notice")
                .content("Draft content")
                .published(false)
                .active(true)
                .build();
        assertNull(converter.convertAnnouncement(draft));

        Announcement inactive = Announcement.builder()
                .id(3L)
                .title("Inactive Notice")
                .content("Old content")
                .published(true)
                .active(false)
                .build();
        assertNull(converter.convertAnnouncement(inactive));
    }

    @Test
    @DisplayName("Should convert published event into RAG document with metadata")
    void testConvertEventSuccess() {
        Event event = Event.builder()
                .id(10L)
                .title("Annual Hackathon 2026")
                .description("24-hour coding challenge for all students.")
                .category("COMPETITION")
                .organizer("Coding Club")
                .venue("Main Auditorium")
                .eventDate(LocalDate.of(2026, 11, 15))
                .registrationRequired(true)
                .published(true)
                .active(true)
                .createdBy(testUser)
                .build();

        DocumentIngestionRequest request = converter.convertEvent(event);

        assertNotNull(request);
        assertEquals("EVENT-10", request.getDocumentId());
        assertEquals("EVENT", request.getMetadata().get("sourceType"));
        assertEquals("Main Auditorium", request.getMetadata().get("venue"));
        assertEquals("Coding Club", request.getMetadata().get("organizer"));
    }

    @Test
    @DisplayName("Should return null for draft event")
    void testConvertDraftEvent() {
        Event draft = Event.builder().id(11L).title("Draft Event").published(false).active(true).build();
        assertNull(converter.convertEvent(draft));
    }

    @Test
    @DisplayName("Should convert published academic resource into RAG document")
    void testConvertAcademicResourceSuccess() {
        AcademicResource resource = AcademicResource.builder()
                .id(5L)
                .title("Data Structures Reference Notes")
                .description("Comprehensive study notes on Trees and Graphs.")
                .subject("Data Structures")
                .category("NOTES")
                .resourceType("PDF")
                .resourceUrl("https://campus.edu/files/ds-notes.pdf")
                .targetDepartment("Computer Science")
                .published(true)
                .active(true)
                .createdBy(testUser)
                .build();

        DocumentIngestionRequest request = converter.convertAcademicResource(resource);

        assertNotNull(request);
        assertEquals("ACADEMIC_RESOURCE-5", request.getDocumentId());
        assertEquals("Data Structures", request.getMetadata().get("subject"));
        assertEquals("PDF", request.getMetadata().get("resourceType"));
    }

    @Test
    @DisplayName("Should convert published club into RAG document")
    void testConvertClubSuccess() {
        Club club = Club.builder()
                .id(3L)
                .name("AI & Robotics Society")
                .description("Student society dedicated to machine learning and robotics projects.")
                .category("TECH")
                .presidentName("Alex Rivera")
                .meetingVenue("Lab 402")
                .published(true)
                .active(true)
                .createdBy(testUser)
                .build();

        DocumentIngestionRequest request = converter.convertClub(club);

        assertNotNull(request);
        assertEquals("CLUB-3", request.getDocumentId());
        assertEquals("Alex Rivera", request.getMetadata().get("presidentName"));
    }

    @Test
    @DisplayName("Should convert published opportunity into RAG document")
    void testConvertOpportunitySuccess() {
        Opportunity opportunity = Opportunity.builder()
                .id(7L)
                .title("AI Research Internship")
                .organization("Google DeepMind")
                .opportunityType("INTERNSHIP")
                .location("Remote")
                .skills("Python, PyTorch, LangChain")
                .eligibility("3rd and 4th year CS students")
                .deadline(LocalDate.of(2026, 12, 31))
                .description("Exciting opportunity to research agentic LLM systems.")
                .published(true)
                .active(true)
                .createdBy(testUser)
                .build();

        DocumentIngestionRequest request = converter.convertOpportunity(opportunity);

        assertNotNull(request);
        assertEquals("OPPORTUNITY-7", request.getDocumentId());
        assertEquals("Google DeepMind", request.getMetadata().get("organization"));
        assertEquals("INTERNSHIP", request.getMetadata().get("opportunityType"));
    }
}
