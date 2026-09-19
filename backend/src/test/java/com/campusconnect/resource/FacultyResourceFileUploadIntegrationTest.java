package com.campusconnect.resource;

import com.campusconnect.dto.FacultyResourceRequest;
import com.campusconnect.dto.FacultyResourceResponse;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.repository.AcademicResourceRepository;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.AcademicResourceService;
import com.campusconnect.service.FacultyResourceService;
import com.campusconnect.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FacultyResourceFileUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FacultyResourceService facultyResourceService;

    @Autowired
    private AcademicResourceService academicResourceService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AcademicResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String facultyAEmail = "faculty.upload.a@campusconnect.edu";
    private String facultyBEmail = "faculty.upload.b@campusconnect.edu";

    @BeforeEach
    void setUp() {
        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role")));

        User userA = User.builder()
                .firstName("Faculty")
                .lastName("Alpha")
                .email(facultyAEmail)
                .password(passwordEncoder.encode("Pass@12345"))
                .isActive(true)
                .build();
        userA.addRole(facultyRole);
        userRepository.save(userA);

        User userB = User.builder()
                .firstName("Faculty")
                .lastName("Beta")
                .email(facultyBEmail)
                .password(passwordEncoder.encode("Pass@12345"))
                .isActive(true)
                .build();
        userB.addRole(facultyRole);
        userRepository.save(userB);
    }

    @Test
    @DisplayName("1. Faculty can upload an allowed PDF file")
    void facultyCanUploadPdf() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "lecture-notes.pdf", "application/pdf", "%PDF-1.4 Dummy PDF Content".getBytes()
        );

        FacultyResourceRequest request = FacultyResourceRequest.builder()
                .title("Data Structures Notes")
                .description("Unit 1 Trees & Graphs")
                .subject("Data Structures")
                .category("Computer Science")
                .published(true)
                .build();

        FacultyResourceResponse response = facultyResourceService.createResource(request, pdfFile, facultyAEmail);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getHasFile()).isTrue();
        assertThat(response.getOriginalFileName()).isEqualTo("lecture-notes.pdf");
        assertThat(response.getResourceType()).isEqualTo("PDF");
        assertThat(response.getFileSize()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("2. Faculty can upload an allowed PPT/PPTX file")
    void facultyCanUploadPptx() {
        MockMultipartFile pptFile = new MockMultipartFile(
                "file", "presentation.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "Dummy PPTX Content".getBytes()
        );

        FacultyResourceRequest request = FacultyResourceRequest.builder()
                .title("Algorithms Slides")
                .description("Unit 2 Dynamic Programming")
                .subject("Algorithms")
                .published(true)
                .build();

        FacultyResourceResponse response = facultyResourceService.createResource(request, pptFile, facultyAEmail);

        assertThat(response.getHasFile()).isTrue();
        assertThat(response.getOriginalFileName()).isEqualTo("presentation.pptx");
        assertThat(response.getResourceType()).isEqualTo("PPT");
    }

    @Test
    @DisplayName("3. Faculty can upload an allowed DOCX document")
    void facultyCanUploadDocx() {
        MockMultipartFile docFile = new MockMultipartFile(
                "file", "syllabus.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Dummy DOCX Content".getBytes()
        );

        FacultyResourceRequest request = FacultyResourceRequest.builder()
                .title("Course Syllabus")
                .description("Syllabus details")
                .subject("Software Engineering")
                .published(true)
                .build();

        FacultyResourceResponse response = facultyResourceService.createResource(request, docFile, facultyAEmail);

        assertThat(response.getHasFile()).isTrue();
        assertThat(response.getOriginalFileName()).isEqualTo("syllabus.docx");
        assertThat(response.getResourceType()).isEqualTo("DOC");
    }

    @Test
    @DisplayName("4. Faculty can upload an allowed MP4 video file")
    void facultyCanUploadVideo() {
        MockMultipartFile videoFile = new MockMultipartFile(
                "file", "lecture1.mp4", "video/mp4", "Dummy MP4 Content".getBytes()
        );

        FacultyResourceRequest request = FacultyResourceRequest.builder()
                .title("React Basics Video Lecture")
                .description("Intro video")
                .subject("Web Development")
                .published(true)
                .build();

        FacultyResourceResponse response = facultyResourceService.createResource(request, videoFile, facultyAEmail);

        assertThat(response.getHasFile()).isTrue();
        assertThat(response.getOriginalFileName()).isEqualTo("lecture1.mp4");
        assertThat(response.getResourceType()).isEqualTo("VIDEO");
    }

    @Test
    @DisplayName("5. Executable files (.exe) are strictly rejected")
    void executableFileIsRejected() {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file", "malware.exe", "application/x-msdownload", "Executable binary bytes".getBytes()
        );

        FacultyResourceRequest request = FacultyResourceRequest.builder()
                .title("Dangerous File")
                .description("Test malware upload")
                .subject("Security")
                .build();

        assertThatThrownBy(() -> facultyResourceService.createResource(request, exeFile, facultyAEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Executable or dangerous file types are strictly prohibited");
    }

    @Test
    @DisplayName("6. Faculty A cannot modify or delete Faculty B's resource")
    void facultyACannotModifyFacultyBResource() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "facultyB-notes.pdf", "application/pdf", "%PDF Dummy Content".getBytes()
        );

        FacultyResourceResponse resourceB = facultyResourceService.createResource(
                FacultyResourceRequest.builder()
                        .title("Faculty B Private Material")
                        .description("Faculty B notes")
                        .subject("Math")
                        .published(false)
                        .build(),
                pdfFile,
                facultyBEmail
        );

        assertThatThrownBy(() -> facultyResourceService.updateResource(
                resourceB.getId(),
                FacultyResourceRequest.builder().title("Hacked Title").description("Hacked").subject("Math").build(),
                null,
                facultyAEmail
        )).isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> facultyResourceService.deleteResource(resourceB.getId(), facultyAEmail))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("7. Replacing a resource file deletes the old file and attaches the new file")
    void replacingFileDeletesOldFile() {
        MockMultipartFile oldFile = new MockMultipartFile(
                "file", "v1.pdf", "application/pdf", "v1 content".getBytes()
        );
        FacultyResourceResponse created = facultyResourceService.createResource(
                FacultyResourceRequest.builder()
                        .title("Versioned Document")
                        .description("v1 description")
                        .subject("Physics")
                        .published(true)
                        .build(),
                oldFile,
                facultyAEmail
        );

        String oldStoredFileName = created.getStoredFileName();
        assertThat(oldStoredFileName).isNotNull();

        MockMultipartFile newFile = new MockMultipartFile(
                "file", "v2.pdf", "application/pdf", "v2 new content".getBytes()
        );

        FacultyResourceResponse updated = facultyResourceService.updateResource(
                created.getId(),
                FacultyResourceRequest.builder()
                        .title("Versioned Document V2")
                        .description("v2 updated description")
                        .subject("Physics")
                        .build(),
                newFile,
                facultyAEmail
        );

        assertThat(updated.getOriginalFileName()).isEqualTo("v2.pdf");
        assertThat(updated.getStoredFileName()).isNotEqualTo(oldStoredFileName);
    }

    @Test
    @DisplayName("8. Legacy URL resources continue to work without file attachment")
    void legacyUrlResourceWorks() {
        FacultyResourceResponse legacy = facultyResourceService.createResource(
                FacultyResourceRequest.builder()
                        .title("Legacy Documentation Link")
                        .description("External java docs")
                        .subject("Java")
                        .resourceUrl("https://docs.oracle.com/en/java/")
                        .published(true)
                        .build(),
                facultyAEmail
        );

        assertThat(legacy.getId()).isNotNull();
        assertThat(legacy.getResourceUrl()).isEqualTo("https://docs.oracle.com/en/java/");
        assertThat(legacy.getHasFile()).isFalse();
    }
}
