package com.campusconnect.resource;

import com.campusconnect.dto.AcademicResourceResponse;
import com.campusconnect.entity.AcademicResource;
import com.campusconnect.repository.AcademicResourceRepository;
import com.campusconnect.service.AcademicResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class AcademicResourceIntegrationTest {

    @Autowired
    private AcademicResourceService resourceService;

    @Autowired
    private AcademicResourceRepository resourceRepository;

    private AcademicResource testResource;

    @BeforeEach
    void setUp() {
        testResource = resourceRepository.save(AcademicResource.builder()
                .title("Compiler Design Reference Notes")
                .description("Lectures on Lexical Analysis, Parsing, and Code Generation")
                .subject("Compiler Design")
                .category("Computer Science")
                .resourceType("NOTES")
                .resourceUrl("https://docs.oracle.com/en/java/")
                .build());
    }

    @Test
    @DisplayName("Should fetch all academic resources and filter by subject and type")
    void shouldFetchAllResourcesAndFilterBySubjectAndType() {
        List<AcademicResourceResponse> resources = resourceService.getAllResources(
                "Computer Science",
                "Compiler Design",
                "NOTES",
                null
        );

        assertThat(resources).isNotEmpty();
        assertThat(resources.get(0).getSubject()).isEqualTo("Compiler Design");
        assertThat(resources.get(0).getResourceType()).isEqualTo("NOTES");
    }

    @Test
    @DisplayName("Should search academic resources by keyword")
    void shouldSearchResourcesByKeyword() {
        List<AcademicResourceResponse> resources = resourceService.getAllResources(null, null, null, "Parsing");

        assertThat(resources).isNotEmpty();
        assertThat(resources.get(0).getTitle()).contains("Compiler Design");
    }

    @Test
    @DisplayName("Should fetch single academic resource by ID")
    void shouldFetchResourceById() {
        AcademicResourceResponse single = resourceService.getResourceById(testResource.getId());

        assertThat(single).isNotNull();
        assertThat(single.getId()).isEqualTo(testResource.getId());
        assertThat(single.getResourceUrl()).isEqualTo("https://docs.oracle.com/en/java/");
    }
}
