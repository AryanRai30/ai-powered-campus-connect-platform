package com.campusconnect.club;

import com.campusconnect.dto.ClubMembershipStatusResponse;
import com.campusconnect.dto.ClubResponse;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.entity.Club;
import com.campusconnect.exception.ClubAlreadyJoinedException;
import com.campusconnect.repository.ClubRepository;
import com.campusconnect.service.AuthService;
import com.campusconnect.service.ClubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class ClubIntegrationTest {

    @Autowired
    private ClubService clubService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private AuthService authService;

    private Club testClub;
    private final String studentEmail = "student.club.test@example.com";

    @BeforeEach
    void setUp() {
        authService.register(RegisterRequest.builder()
                .firstName("Club")
                .lastName("Student")
                .email(studentEmail)
                .password("Password@123")
                .build());

        testClub = clubRepository.save(Club.builder()
                .name("AI & ML Society")
                .description("Exploring Machine Learning algorithms and Neural Networks")
                .category("Technology")
                .presidentName("David Lin")
                .meetingDay("Monday")
                .meetingTime(LocalTime.of(16, 0))
                .meetingVenue("Lab 204")
                .build());
    }

    @Test
    @DisplayName("Should fetch all clubs and filter by category")
    void shouldFetchAllClubsAndFilterByCategory() {
        List<ClubResponse> clubs = clubService.getAllClubs("Technology", null, studentEmail);
        assertThat(clubs).isNotEmpty();
        assertThat(clubs.get(0).getCategory()).isEqualTo("Technology");
    }

    @Test
    @DisplayName("Should join club and prevent duplicate membership")
    void shouldJoinClubAndPreventDuplicateMembership() {
        ClubMembershipStatusResponse response = clubService.joinClub(testClub.getId(), studentEmail);
        assertThat(response.isJoined()).isTrue();

        ClubMembershipStatusResponse status = clubService.getMembershipStatus(testClub.getId(), studentEmail);
        assertThat(status.isJoined()).isTrue();

        assertThatThrownBy(() -> clubService.joinClub(testClub.getId(), studentEmail))
                .isInstanceOf(ClubAlreadyJoinedException.class)
                .hasMessageContaining("Student has already joined this club.");
    }

    @Test
    @DisplayName("Should retrieve clubs joined by current student")
    void shouldFetchStudentMyClubs() {
        clubService.joinClub(testClub.getId(), studentEmail);

        List<ClubResponse> myClubs = clubService.getMyClubs(studentEmail);
        assertThat(myClubs).hasSize(1);
        assertThat(myClubs.get(0).getName()).isEqualTo("AI & ML Society");
    }
}
