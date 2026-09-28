package com.campusconnect.ai.rag.mapper;

import com.campusconnect.ai.rag.dto.DocumentIngestionRequest;
import com.campusconnect.entity.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Converter component for transforming authorized campus entity records
 * (Announcements, Events, Academic Resources, Clubs, Opportunities)
 * into standardized RAG DocumentIngestionRequest payloads with rich metadata.
 */
@Component
public class CampusDocumentConverter {

    /**
     * Converts an Announcement into a RAG DocumentIngestionRequest.
     * Returns null if announcement is null, draft (published=false), or inactive (active=false).
     */
    public DocumentIngestionRequest convertAnnouncement(Announcement a) {
        if (a == null || !Boolean.TRUE.equals(a.getPublished()) || !Boolean.TRUE.equals(a.getActive())) {
            return null;
        }

        String docId = "ANNOUNCEMENT-" + a.getId();
        StringBuilder text = new StringBuilder();
        text.append("[Campus Announcement]\n");
        text.append("Title: ").append(a.getTitle()).append("\n");
        if (a.getCategory() != null) text.append("Category: ").append(a.getCategory()).append("\n");
        if (a.getPublishedAt() != null) text.append("Published Date: ").append(a.getPublishedAt()).append("\n");
        if (a.getTargetDepartment() != null) text.append("Target Department: ").append(a.getTargetDepartment()).append("\n");
        if (a.getTargetCourse() != null) text.append("Target Course: ").append(a.getTargetCourse()).append("\n");
        if (a.getTargetYear() != null) text.append("Target Year: ").append(a.getTargetYear()).append("\n");
        if (a.getTargetSemester() != null) text.append("Target Semester: ").append(a.getTargetSemester()).append("\n");
        if (a.getCreatedBy() != null) {
            text.append("Creator: ").append(a.getCreatedBy().getFirstName()).append(" ").append(a.getCreatedBy().getLastName());
            if (a.getCreatedBy().getEmail() != null) text.append(" (").append(a.getCreatedBy().getEmail()).append(")");
            text.append("\n");
        }
        text.append("\nContent:\n").append(a.getContent());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("sourceType", "ANNOUNCEMENT");
        metadata.put("sourceId", String.valueOf(a.getId()));
        metadata.put("title", a.getTitle());
        if (a.getCategory() != null) metadata.put("category", a.getCategory());
        if (a.getTargetDepartment() != null) metadata.put("targetDepartment", a.getTargetDepartment());
        if (a.getTargetCourse() != null) metadata.put("targetCourse", a.getTargetCourse());
        if (a.getTargetYear() != null) metadata.put("targetYear", String.valueOf(a.getTargetYear()));
        if (a.getTargetSemester() != null) metadata.put("targetSemester", String.valueOf(a.getTargetSemester()));
        if (a.getCreatedBy() != null && a.getCreatedBy().getEmail() != null) {
            metadata.put("createdBy", a.getCreatedBy().getEmail());
        }
        if (a.getPublished() != null) metadata.put("published", String.valueOf(a.getPublished()));
        if (a.getActive() != null) metadata.put("active", String.valueOf(a.getActive()));

        return DocumentIngestionRequest.builder()
                .documentId(docId)
                .title(a.getTitle())
                .category("ANNOUNCEMENT")
                .content(text.toString())
                .metadata(metadata)
                .build();
    }

    /**
     * Converts an Event into a RAG DocumentIngestionRequest.
     * Returns null if event is null, draft, or inactive.
     */
    public DocumentIngestionRequest convertEvent(Event e) {
        if (e == null || !Boolean.TRUE.equals(e.getPublished()) || !Boolean.TRUE.equals(e.getActive())) {
            return null;
        }

        String docId = "EVENT-" + e.getId();
        StringBuilder text = new StringBuilder();
        text.append("[Campus Event]\n");
        text.append("Title: ").append(e.getTitle()).append("\n");
        if (e.getCategory() != null) text.append("Category: ").append(e.getCategory()).append("\n");
        if (e.getOrganizer() != null) text.append("Organizer: ").append(e.getOrganizer()).append("\n");
        if (e.getEventDate() != null) text.append("Event Date: ").append(e.getEventDate());
        if (e.getEventTime() != null) text.append(" Time: ").append(e.getEventTime());
        text.append("\n");
        if (e.getVenue() != null) text.append("Venue: ").append(e.getVenue()).append("\n");
        text.append("Registration Required: ").append(e.isRegistrationRequired()).append("\n");
        if (e.getTargetDepartment() != null) text.append("Target Department: ").append(e.getTargetDepartment()).append("\n");
        if (e.getTargetCourse() != null) text.append("Target Course: ").append(e.getTargetCourse()).append("\n");
        if (e.getTargetYear() != null) text.append("Target Year: ").append(e.getTargetYear()).append("\n");
        if (e.getTargetSemester() != null) text.append("Target Semester: ").append(e.getTargetSemester()).append("\n");
        if (e.getCreatedBy() != null) {
            text.append("Created By: ").append(e.getCreatedBy().getFirstName()).append(" ").append(e.getCreatedBy().getLastName()).append("\n");
        }
        text.append("\nDescription:\n").append(e.getDescription());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("sourceType", "EVENT");
        metadata.put("sourceId", String.valueOf(e.getId()));
        metadata.put("title", e.getTitle());
        if (e.getCategory() != null) metadata.put("category", e.getCategory());
        if (e.getVenue() != null) metadata.put("venue", e.getVenue());
        if (e.getOrganizer() != null) metadata.put("organizer", e.getOrganizer());
        if (e.getEventDate() != null) metadata.put("eventDate", e.getEventDate().toString());
        if (e.getTargetDepartment() != null) metadata.put("targetDepartment", e.getTargetDepartment());
        if (e.getTargetCourse() != null) metadata.put("targetCourse", e.getTargetCourse());
        if (e.getTargetYear() != null) metadata.put("targetYear", String.valueOf(e.getTargetYear()));
        if (e.getTargetSemester() != null) metadata.put("targetSemester", String.valueOf(e.getTargetSemester()));
        if (e.getCreatedBy() != null && e.getCreatedBy().getEmail() != null) {
            metadata.put("createdBy", e.getCreatedBy().getEmail());
        }
        if (e.getPublished() != null) metadata.put("published", String.valueOf(e.getPublished()));
        if (e.getActive() != null) metadata.put("active", String.valueOf(e.getActive()));

        return DocumentIngestionRequest.builder()
                .documentId(docId)
                .title(e.getTitle())
                .category("EVENT")
                .content(text.toString())
                .metadata(metadata)
                .build();
    }

    /**
     * Converts an AcademicResource into a RAG DocumentIngestionRequest.
     * Returns null if resource is null, draft, or inactive.
     */
    public DocumentIngestionRequest convertAcademicResource(AcademicResource r) {
        if (r == null || !Boolean.TRUE.equals(r.getPublished()) || !Boolean.TRUE.equals(r.getActive())) {
            return null;
        }

        String docId = "ACADEMIC_RESOURCE-" + r.getId();
        StringBuilder text = new StringBuilder();
        text.append("[Academic Resource]\n");
        text.append("Title: ").append(r.getTitle()).append("\n");
        text.append("Subject: ").append(r.getSubject()).append("\n");
        if (r.getCategory() != null) text.append("Category: ").append(r.getCategory()).append("\n");
        if (r.getResourceType() != null) text.append("Resource Type: ").append(r.getResourceType()).append("\n");
        if (r.getResourceUrl() != null) text.append("Resource URL: ").append(r.getResourceUrl()).append("\n");
        if (r.getOriginalFileName() != null) text.append("File Name: ").append(r.getOriginalFileName()).append("\n");
        if (r.getTargetDepartment() != null) text.append("Target Department: ").append(r.getTargetDepartment()).append("\n");
        if (r.getTargetCourse() != null) text.append("Target Course: ").append(r.getTargetCourse()).append("\n");
        if (r.getTargetYear() != null) text.append("Target Year: ").append(r.getTargetYear()).append("\n");
        if (r.getTargetSemester() != null) text.append("Target Semester: ").append(r.getTargetSemester()).append("\n");
        if (r.getTargetSection() != null) text.append("Target Section: ").append(r.getTargetSection()).append("\n");
        if (r.getCreatedBy() != null) {
            text.append("Uploaded By: ").append(r.getCreatedBy().getFirstName()).append(" ").append(r.getCreatedBy().getLastName()).append("\n");
        }
        text.append("\nDescription:\n").append(r.getDescription());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("sourceType", "ACADEMIC_RESOURCE");
        metadata.put("sourceId", String.valueOf(r.getId()));
        metadata.put("title", r.getTitle());
        metadata.put("subject", r.getSubject());
        if (r.getCategory() != null) metadata.put("category", r.getCategory());
        if (r.getResourceType() != null) metadata.put("resourceType", r.getResourceType());
        if (r.getTargetDepartment() != null) metadata.put("targetDepartment", r.getTargetDepartment());
        if (r.getTargetCourse() != null) metadata.put("targetCourse", r.getTargetCourse());
        if (r.getTargetYear() != null) metadata.put("targetYear", String.valueOf(r.getTargetYear()));
        if (r.getTargetSemester() != null) metadata.put("targetSemester", String.valueOf(r.getTargetSemester()));
        if (r.getTargetSection() != null) metadata.put("targetSection", r.getTargetSection());
        if (r.getCreatedBy() != null && r.getCreatedBy().getEmail() != null) {
            metadata.put("createdBy", r.getCreatedBy().getEmail());
        }
        if (r.getPublished() != null) metadata.put("published", String.valueOf(r.getPublished()));
        if (r.getActive() != null) metadata.put("active", String.valueOf(r.getActive()));

        return DocumentIngestionRequest.builder()
                .documentId(docId)
                .title(r.getTitle())
                .category("ACADEMIC_RESOURCE")
                .content(text.toString())
                .sourceUrl(r.getResourceUrl())
                .metadata(metadata)
                .build();
    }

    /**
     * Converts a Club into a RAG DocumentIngestionRequest.
     * Returns null if club is null, draft, or inactive.
     */
    public DocumentIngestionRequest convertClub(Club c) {
        if (c == null || !Boolean.TRUE.equals(c.getPublished()) || !Boolean.TRUE.equals(c.getActive())) {
            return null;
        }

        String docId = "CLUB-" + c.getId();
        StringBuilder text = new StringBuilder();
        text.append("[Student Club]\n");
        text.append("Club Name: ").append(c.getName()).append("\n");
        if (c.getCategory() != null) text.append("Category: ").append(c.getCategory()).append("\n");
        if (c.getDepartment() != null) text.append("Department: ").append(c.getDepartment()).append("\n");
        if (c.getPresidentName() != null) text.append("President: ").append(c.getPresidentName()).append("\n");
        if (c.getMeetingDay() != null) text.append("Meeting Day: ").append(c.getMeetingDay());
        if (c.getMeetingTime() != null) text.append(" Time: ").append(c.getMeetingTime());
        text.append("\n");
        if (c.getMeetingVenue() != null) text.append("Meeting Venue: ").append(c.getMeetingVenue()).append("\n");
        if (c.getCreatedBy() != null) {
            text.append("Created By: ").append(c.getCreatedBy().getFirstName()).append(" ").append(c.getCreatedBy().getLastName()).append("\n");
        }
        text.append("\nDescription:\n").append(c.getDescription());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("sourceType", "CLUB");
        metadata.put("sourceId", String.valueOf(c.getId()));
        metadata.put("title", c.getName());
        if (c.getCategory() != null) metadata.put("category", c.getCategory());
        if (c.getDepartment() != null) metadata.put("department", c.getDepartment());
        if (c.getPresidentName() != null) metadata.put("presidentName", c.getPresidentName());
        if (c.getMeetingVenue() != null) metadata.put("meetingVenue", c.getMeetingVenue());
        if (c.getCreatedBy() != null && c.getCreatedBy().getEmail() != null) {
            metadata.put("createdBy", c.getCreatedBy().getEmail());
        }
        if (c.getPublished() != null) metadata.put("published", String.valueOf(c.getPublished()));
        if (c.getActive() != null) metadata.put("active", String.valueOf(c.getActive()));

        return DocumentIngestionRequest.builder()
                .documentId(docId)
                .title(c.getName())
                .category("CLUB")
                .content(text.toString())
                .metadata(metadata)
                .build();
    }

    /**
     * Converts an Opportunity into a RAG DocumentIngestionRequest.
     * Returns null if opportunity is null, draft, or inactive.
     */
    public DocumentIngestionRequest convertOpportunity(Opportunity o) {
        if (o == null || !Boolean.TRUE.equals(o.getPublished()) || !Boolean.TRUE.equals(o.getActive())) {
            return null;
        }

        String docId = "OPPORTUNITY-" + o.getId();
        StringBuilder text = new StringBuilder();
        text.append("[Campus Opportunity]\n");
        text.append("Title: ").append(o.getTitle()).append("\n");
        text.append("Organization: ").append(o.getOrganization()).append("\n");
        if (o.getOpportunityType() != null) text.append("Type: ").append(o.getOpportunityType()).append("\n");
        if (o.getLocation() != null) text.append("Location: ").append(o.getLocation()).append("\n");
        if (o.getSkills() != null) text.append("Skills Required: ").append(o.getSkills()).append("\n");
        if (o.getEligibility() != null) text.append("Eligibility: ").append(o.getEligibility()).append("\n");
        if (o.getDeadline() != null) text.append("Application Deadline: ").append(o.getDeadline()).append("\n");
        if (o.getApplicationUrl() != null) text.append("Application URL: ").append(o.getApplicationUrl()).append("\n");
        if (o.getTargetDepartment() != null) text.append("Target Department: ").append(o.getTargetDepartment()).append("\n");
        if (o.getTargetCourse() != null) text.append("Target Course: ").append(o.getTargetCourse()).append("\n");
        if (o.getTargetYear() != null) text.append("Target Year: ").append(o.getTargetYear()).append("\n");
        if (o.getTargetSemester() != null) text.append("Target Semester: ").append(o.getTargetSemester()).append("\n");
        if (o.getCreatedBy() != null) {
            text.append("Posted By: ").append(o.getCreatedBy().getFirstName()).append(" ").append(o.getCreatedBy().getLastName()).append("\n");
        }
        text.append("\nDescription:\n").append(o.getDescription());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("sourceType", "OPPORTUNITY");
        metadata.put("sourceId", String.valueOf(o.getId()));
        metadata.put("title", o.getTitle());
        metadata.put("organization", o.getOrganization());
        if (o.getOpportunityType() != null) metadata.put("opportunityType", o.getOpportunityType());
        if (o.getLocation() != null) metadata.put("location", o.getLocation());
        if (o.getDeadline() != null) metadata.put("deadline", o.getDeadline().toString());
        if (o.getTargetDepartment() != null) metadata.put("targetDepartment", o.getTargetDepartment());
        if (o.getTargetCourse() != null) metadata.put("targetCourse", o.getTargetCourse());
        if (o.getTargetYear() != null) metadata.put("targetYear", String.valueOf(o.getTargetYear()));
        if (o.getTargetSemester() != null) metadata.put("targetSemester", String.valueOf(o.getTargetSemester()));
        if (o.getCreatedBy() != null && o.getCreatedBy().getEmail() != null) {
            metadata.put("createdBy", o.getCreatedBy().getEmail());
        }
        if (o.getPublished() != null) metadata.put("published", String.valueOf(o.getPublished()));
        if (o.getActive() != null) metadata.put("active", String.valueOf(o.getActive()));

        return DocumentIngestionRequest.builder()
                .documentId(docId)
                .title(o.getTitle())
                .category("OPPORTUNITY")
                .content(text.toString())
                .sourceUrl(o.getApplicationUrl())
                .metadata(metadata)
                .build();
    }
}
