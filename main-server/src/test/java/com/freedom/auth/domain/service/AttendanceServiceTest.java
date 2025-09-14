package com.freedom.auth.domain.service;

import com.freedom.auth.domain.User;
import com.freedom.auth.domain.UserRole;
import com.freedom.auth.domain.UserStatus;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.common.exception.custom.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService 단위 테스트")
class AttendanceServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private User testUser;
    private final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .characterName("테스트캐릭터")
                .build();
    }

    @Test
    @DisplayName("출석체크 성공")
    void markAttendance_Success() {
        // given
        given(userJpaRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
        given(userJpaRepository.save(any(User.class))).willReturn(testUser);

        // when
        boolean result = attendanceService.markAttendance(TEST_USER_ID);

        // then
        assertThat(result).isTrue();
        assertThat(testUser.getAttendance()).isTrue();

        then(userJpaRepository).should().findById(TEST_USER_ID);
        then(userJpaRepository).should().save(testUser);
    }

    @Test
    @DisplayName("출석체크 실패 - 이미 출석함")
    void markAttendance_Fail_AlreadyAttended() {
        // given
        testUser.completeAttendance(); // 이미 출석 완료
        given(userJpaRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));

        // when
        boolean result = attendanceService.markAttendance(TEST_USER_ID);

        // then
        assertThat(result).isFalse();
        assertThat(testUser.getAttendance()).isTrue();

        then(userJpaRepository).should().findById(TEST_USER_ID);
        then(userJpaRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("출석체크 실패 - 사용자를 찾을 수 없음")
    void markAttendance_Fail_UserNotFound() {
        // given
        given(userJpaRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> attendanceService.markAttendance(TEST_USER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        then(userJpaRepository).should().findById(TEST_USER_ID);
        then(userJpaRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("출석 상태 확인 - 출석 완료")
    void isAttendanceCompleted_True() {
        // given
        testUser.completeAttendance();
        given(userJpaRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));

        // when
        boolean result = attendanceService.isAttendanceCompleted(TEST_USER_ID);

        // then
        assertThat(result).isTrue();
        then(userJpaRepository).should().findById(TEST_USER_ID);
    }

    @Test
    @DisplayName("출석 상태 확인 - 출석 미완료")
    void isAttendanceCompleted_False() {
        // given
        given(userJpaRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));

        // when
        boolean result = attendanceService.isAttendanceCompleted(TEST_USER_ID);

        // then
        assertThat(result).isFalse();
        then(userJpaRepository).should().findById(TEST_USER_ID);
    }

    @Test
    @DisplayName("출석 상태 확인 실패 - 사용자를 찾을 수 없음")
    void isAttendanceCompleted_Fail_UserNotFound() {
        // given
        given(userJpaRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> attendanceService.isAttendanceCompleted(TEST_USER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        then(userJpaRepository).should().findById(TEST_USER_ID);
    }
}
