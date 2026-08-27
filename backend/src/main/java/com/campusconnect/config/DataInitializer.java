package com.campusconnect.config;

import com.campusconnect.entity.AcademicResource;
import com.campusconnect.entity.Announcement;
import com.campusconnect.entity.Club;
import com.campusconnect.entity.Event;
import com.campusconnect.entity.Opportunity;
import com.campusconnect.entity.Role;
import com.campusconnect.repository.AcademicResourceRepository;
import com.campusconnect.repository.AnnouncementRepository;
import com.campusconnect.repository.ClubRepository;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.OpportunityRepository;
import com.campusconnect.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Safely initializes essential seed roles, sample events, announcements, student clubs,
 * academic resources, and career opportunities if empty upon application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final RoleRepository roleRepository;
    private final EventRepository eventRepository;
    private final AnnouncementRepository announcementRepository;
    private final ClubRepository clubRepository;
    private final AcademicResourceRepository resourceRepository;
    private final OpportunityRepository opportunityRepository;

    public DataInitializer(
            RoleRepository roleRepository,
            EventRepository eventRepository,
            AnnouncementRepository announcementRepository,
            ClubRepository clubRepository,
            AcademicResourceRepository resourceRepository,
            OpportunityRepository opportunityRepository
    ) {
        this.roleRepository = roleRepository;
        this.eventRepository = eventRepository;
        this.announcementRepository = announcementRepository;
        this.clubRepository = clubRepository;
        this.resourceRepository = resourceRepository;
        this.opportunityRepository = opportunityRepository;
    }

    @Override
    public void run(String... args) {
        seedRoles();
        seedEvents();
        seedAnnouncements();
        seedClubs();
        seedAcademicResources();
        seedOpportunities();
    }

    private void seedRoles() {
        try {
            List<Role> defaultRoles = List.of(
                new Role("STUDENT", "Student role with access to academic and campus features"),
                new Role("FACULTY", "Faculty role for managing courses and academic resources"),
                new Role("CLUB_ADMIN", "Club administrator role for event and activity management"),
                new Role("SUPER_ADMIN", "Super administrator role with full system privileges")
            );

            for (Role role : defaultRoles) {
                if (!roleRepository.existsByName(role.getName())) {
                    roleRepository.save(role);
                    logger.info("Initialized default role: {}", role.getName());
                }
            }
        } catch (Exception e) {
            logger.warn("DataInitializer skipped role seeding: {}", e.getMessage());
        }
    }

    private void seedEvents() {
        try {
            if (eventRepository.count() == 0) {
                List<Event> sampleEvents = List.of(
                    Event.builder()
                        .title("AI & Cloud Computing Symposium 2026")
                        .description("Join industry experts and researchers for a full-day summit on emerging generative AI architectures, distributed systems, and cloud native technology.")
                        .eventDate(LocalDate.now().plusDays(5))
                        .eventTime(LocalTime.of(10, 0))
                        .venue("Main Auditorium, Innovation Block")
                        .category("Tech Summit")
                        .organizer("Computer Science Dept & Tech Club")
                        .registrationRequired(true)
                        .build(),

                    Event.builder()
                        .title("Annual Hackathon: CodeForGood 2026")
                        .description("36-hour non-stop hackathon building open source solutions for sustainability, healthcare, and education. Mentorship and prizes included!")
                        .eventDate(LocalDate.now().plusDays(12))
                        .eventTime(LocalTime.of(9, 30))
                        .venue("Student Activity Center (SAC) Lab 4")
                        .category("Hackathon")
                        .organizer("Coding Society & ACM Student Chapter")
                        .registrationRequired(true)
                        .build(),

                    Event.builder()
                        .title("Campus Cultural Fest & Music Night")
                        .description("Experience vibrant acoustic performances, dance showcases, food stalls, and interactive art galleries hosted by campus cultural societies.")
                        .eventDate(LocalDate.now().plusDays(18))
                        .eventTime(LocalTime.of(17, 0))
                        .venue("Open Air Theatre (OAT)")
                        .category("Cultural")
                        .organizer("Student Cultural Committee")
                        .registrationRequired(false)
                        .build(),

                    Event.builder()
                        .title("Career Guidance & Resume Workshop")
                        .description("Master technical resume crafting, LinkedIn optimization, and mock interview practice with senior alumni and placement officer advisors.")
                        .eventDate(LocalDate.now().plusDays(8))
                        .eventTime(LocalTime.of(14, 0))
                        .venue("Seminar Hall B, Block 3")
                        .category("Workshop")
                        .organizer("Training & Placement Cell")
                        .registrationRequired(true)
                        .build()
                );

                eventRepository.saveAll(sampleEvents);
                logger.info("Initialized {} sample campus events.", sampleEvents.size());
            }
        } catch (Exception e) {
            logger.warn("DataInitializer skipped event seeding: {}", e.getMessage());
        }
    }

    private void seedAnnouncements() {
        try {
            if (announcementRepository.count() == 0) {
                List<Announcement> sampleAnnouncements = List.of(
                    Announcement.builder()
                        .title("Mid-Semester Examination Schedule Released")
                        .content("The official timetable for Autumn 2026 Mid-Semester Examinations is now live. Please review your respective course slots and hall ticket guidelines on the academic portal.")
                        .category("Academic")
                        .publishedAt(LocalDateTime.now().minusHours(2))
                        .build(),

                    Announcement.builder()
                        .title("Library Operating Hours Extended for Finals Prep")
                        .content("Central Library will remain open 24/7 starting next Monday to support students during examination preparation. High-speed Wi-Fi and quiet study pods available.")
                        .category("General")
                        .publishedAt(LocalDateTime.now().minusDays(1))
                        .build(),

                    Announcement.builder()
                        .title("Urgent: Campus Wi-Fi Infrastructure Maintenance Notice")
                        .content("Scheduled network upgrades will take place this Sunday from 02:00 AM to 06:00 AM. Internet services in residence halls may experience brief interruptions.")
                        .category("Urgent")
                        .publishedAt(LocalDateTime.now().minusDays(2))
                        .build()
                );

                announcementRepository.saveAll(sampleAnnouncements);
                logger.info("Initialized {} sample campus announcements.", sampleAnnouncements.size());
            }
        } catch (Exception e) {
            logger.warn("DataInitializer skipped announcement seeding: {}", e.getMessage());
        }
    }

    private void seedClubs() {
        try {
            if (clubRepository.count() == 0) {
                List<Club> sampleClubs = List.of(
                    Club.builder()
                        .name("Coding Club")
                        .description("A community of passionate developers exploring competitive programming, web development, open source contributions, and algorithm workshops.")
                        .category("Technology")
                        .presidentName("Alex Turner")
                        .meetingDay("Wednesday")
                        .meetingTime(LocalTime.of(17, 0))
                        .meetingVenue("Innovation Lab 2")
                        .build(),

                    Club.builder()
                        .name("Robotics Club")
                        .description("Designing autonomous rovers, drones, IoT hardware, and participating in national robotics competitions.")
                        .category("Technology")
                        .presidentName("Samantha Vance")
                        .meetingDay("Friday")
                        .meetingTime(LocalTime.of(16, 30))
                        .meetingVenue("SAC Mechatronics Studio")
                        .build(),

                    Club.builder()
                        .name("Cultural & Arts Club")
                        .description("Promoting campus creativity through drama productions, live music jams, contemporary dance, and fine arts exhibitions.")
                        .category("Cultural")
                        .presidentName("Rohan Verma")
                        .meetingDay("Tuesday")
                        .meetingTime(LocalTime.of(17, 30))
                        .meetingVenue("Open Air Amphitheatre")
                        .build(),

                    Club.builder()
                        .name("Sports & Athletics Club")
                        .description("Fostering athletic excellence, organizing intra-mural tournaments in football, basketball, badminton, and track events.")
                        .category("Sports")
                        .presidentName("Marcus Johnson")
                        .meetingDay("Saturday")
                        .meetingTime(LocalTime.of(8, 0))
                        .meetingVenue("Central Sports Complex")
                        .build(),

                    Club.builder()
                        .name("Entrepreneurship & Innovation Cell")
                        .description("Empowering student founders with startup incubation, seed grant pitch sessions, venture capital panels, and founder talks.")
                        .category("Business")
                        .presidentName("Priya Sharma")
                        .meetingDay("Thursday")
                        .meetingTime(LocalTime.of(18, 0))
                        .meetingVenue("Incubator Hub, Hall 3")
                        .build()
                );

                clubRepository.saveAll(sampleClubs);
                logger.info("Initialized {} sample campus clubs.", sampleClubs.size());
            }
        } catch (Exception e) {
            logger.warn("DataInitializer skipped club seeding: {}", e.getMessage());
        }
    }

    private void seedAcademicResources() {
        try {
            if (resourceRepository.count() == 0) {
                List<AcademicResource> sampleResources = List.of(
                    AcademicResource.builder()
                        .title("Data Structures & Algorithms Lecture Notes")
                        .description("Detailed notes covering Arrays, Linked Lists, Binary Search Trees, Graph Algorithms, and Dynamic Programming with time complexity analysis.")
                        .subject("Data Structures")
                        .category("Computer Science")
                        .resourceType("NOTES")
                        .resourceUrl("https://example.com/resources/dsa-notes.pdf")
                        .build(),

                    AcademicResource.builder()
                        .title("Database Management Systems Reference Guide")
                        .description("Comprehensive guide on Relational Model, ER Diagrams, SQL Queries, Normalization (1NF to BCNF), ACID Properties, and Indexing.")
                        .subject("Database Systems")
                        .category("Computer Science")
                        .resourceType("PDF")
                        .resourceUrl("https://example.com/resources/dbms-guide.pdf")
                        .build(),

                    AcademicResource.builder()
                        .title("Operating Systems Concepts & Process Scheduling Video Series")
                        .description("Video lecture series covering Process Management, Concurrency & Deadlocks, Memory Management, Paging, and File System Architecture.")
                        .subject("Operating Systems")
                        .category("Computer Science")
                        .resourceType("VIDEO")
                        .resourceUrl("https://example.com/resources/os-lectures")
                        .build(),

                    AcademicResource.builder()
                        .title("Java Enterprise Edition & Spring Boot Handbook")
                        .description("Curated documentation and practical examples on Java 21 features, Spring MVC, Spring Data JPA, JWT Security, and REST API development.")
                        .subject("Programming Languages")
                        .category("Software Engineering")
                        .resourceType("WEBSITE")
                        .resourceUrl("https://example.com/resources/java-spring-guide")
                        .build(),

                    AcademicResource.builder()
                        .title("Web Development with React 18 & TypeScript")
                        .description("Complete cheatsheet on React Functional Components, Custom Hooks, State Management, Tailwind CSS styling, and Vite bundling.")
                        .subject("Web Technology")
                        .category("Software Engineering")
                        .resourceType("WEBSITE")
                        .resourceUrl("https://example.com/resources/react-ts-handbook")
                        .build(),

                    AcademicResource.builder()
                        .title("Artificial Intelligence & Machine Learning Fundamentals")
                        .description("Introductory PDF covering Supervised/Unsupervised Learning, Regression, Neural Networks, Decision Trees, and Scikit-Learn code snippets.")
                        .subject("Artificial Intelligence")
                        .category("Data Science")
                        .resourceType("PDF")
                        .resourceUrl("https://example.com/resources/ai-ml-intro.pdf")
                        .build()
                );

                resourceRepository.saveAll(sampleResources);
                logger.info("Initialized {} sample academic resources.", sampleResources.size());
            }
        } catch (Exception e) {
            logger.warn("DataInitializer skipped academic resource seeding: {}", e.getMessage());
        }
    }

    private void seedOpportunities() {
        try {
            if (opportunityRepository.count() == 0) {
                List<Opportunity> sampleOpportunities = List.of(
                    Opportunity.builder()
                        .title("Software Development Engineer Intern")
                        .description("Opportunity to build scalable microservices and RESTful APIs using Java 21 and Spring Boot. Mentorship from senior engineers included.")
                        .organization("TechCorp Systems")
                        .opportunityType("INTERNSHIP")
                        .location("Remote / Bangalore")
                        .skills("Java, Spring Boot, MySQL, REST APIs")
                        .deadline(LocalDate.now().plusDays(30))
                        .applicationUrl("https://example.com/careers/sde-intern")
                        .build(),

                    Opportunity.builder()
                        .title("Java Backend Developer Intern")
                        .description("Work with high-throughput database systems, security filters, and microservice architectures.")
                        .organization("Nexus Innovations")
                        .opportunityType("INTERNSHIP")
                        .location("Hybrid / Hyderabad")
                        .skills("Java, JPA/Hibernate, Microservices, Git")
                        .deadline(LocalDate.now().plusDays(20))
                        .applicationUrl("https://example.com/careers/java-intern")
                        .build(),

                    Opportunity.builder()
                        .title("Full-Stack Web Developer Trainee")
                        .description("Build modern user interfaces with React, TypeScript, and Tailwind CSS while connecting to Node/Java backends.")
                        .organization("CloudScale Solutions")
                        .opportunityType("JOB")
                        .location("On-site / Pune")
                        .skills("TypeScript, React, Node.js, Tailwind CSS")
                        .deadline(LocalDate.now().plusDays(45))
                        .applicationUrl("https://example.com/careers/fullstack-trainee")
                        .build(),

                    Opportunity.builder()
                        .title("National Student Coding Competition 2026")
                        .description("Participate in algorithmic problem solving and speed programming challenges. Cash prizes, certificates, and direct interview waivers.")
                        .organization("ACM Student Chapter")
                        .opportunityType("COMPETITION")
                        .location("Virtual / Online")
                        .skills("Data Structures, Algorithms, C++, Python")
                        .deadline(LocalDate.now().plusDays(14))
                        .applicationUrl("https://example.com/competitions/code-2026")
                        .build(),

                    Opportunity.builder()
                        .title("Campus Student Innovation Challenge")
                        .description("Grant scholarship and seed capital for promising tech, hardware, and social impact student startups.")
                        .organization("University Innovation Council")
                        .opportunityType("SCHOLARSHIP")
                        .location("Main Campus")
                        .skills("Prototyping, Business Pitching, Innovation")
                        .deadline(LocalDate.now().plusDays(25))
                        .applicationUrl("https://example.com/scholarships/innovation-grant")
                        .build(),

                    Opportunity.builder()
                        .title("Technical Interview & Portfolio Workshop")
                        .description("Interactive hands-on bootcamp covering live coding techniques, mock interviews, system design basics, and LinkedIn profile reviews.")
                        .organization("Career Guidance Cell")
                        .opportunityType("WORKSHOP")
                        .location("Seminar Hall A")
                        .skills("System Design, Mock Interviews, Resume Optimization")
                        .deadline(LocalDate.now().plusDays(7))
                        .applicationUrl("https://example.com/workshops/interview-prep")
                        .build()
                );

                opportunityRepository.saveAll(sampleOpportunities);
                logger.info("Initialized {} sample career opportunities.", sampleOpportunities.size());
            }
        } catch (Exception e) {
            logger.warn("DataInitializer skipped opportunity seeding: {}", e.getMessage());
        }
    }
}
