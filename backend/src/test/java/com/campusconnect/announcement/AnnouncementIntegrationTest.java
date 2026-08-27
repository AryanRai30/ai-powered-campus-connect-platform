package com.campusconnect.announcement;

import com.campusconnect.dto.AnnouncementResponse;
import com.campusconnect.entity.Announcement;
import com.campusconnect.repository.AnnouncementRepository;
import com.campusconnect.service.AnnouncementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class AnnouncementIntegrationTest {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @BeforeEach
    void setUp() {
        announcementRepository.save(Announcement.builder()
                .title("Older Notice")
                .content("Older announcement content")
                .category("General")
                .publishedAt(LocalDateTime.now().minusDays(5))
                .build());

        announcementRepository.save(Announcement.builder()
                .title("Newer Notice")
                .content("Newer announcement content")
                .category("Academic")
                .publishedAt(LocalDateTime.now().minusHours(1))
                .build());
    }

    @Test
    @DisplayName("Should fetch announcements ordered newest first")
    void shouldFetchAnnouncementsOrderedNewestFirst() {
        List<AnnouncementResponse> announcements = announcementService.getAllAnnouncements(null, null);

        assertThat(announcements).hasSizeGreaterThanOrEqualTo(2);
        assertThat(announcements.get(0).getTitle()).isEqualTo("Newer Notice");
    }

    @Test
    @DisplayName("Should fetch single announcement by ID")
    void shouldFetchAnnouncementById() {
        List<AnnouncementResponse> announcements = announcementService.getAllAnnouncements(null, null);
        Long id = announcements.get(0).getId();

        AnnouncementResponse single = announcementService.getAnnouncementById(id);
        assertThat(single).isNotNull();
        assertThat(single.getId()).isEqualTo(id);
    }
}
