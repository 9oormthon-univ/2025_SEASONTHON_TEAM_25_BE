package com.freedom.attendance.domain.service;

import com.freedom.attendance.domain.Attendance;
import com.freedom.attendance.infra.AttendanceRepository;
import com.freedom.auth.domain.User;
import com.freedom.auth.domain.UserRole;
import com.freedom.auth.domain.UserStatus;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.common.exception.custom.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceCommandService 단위 테스트")
class AttendanceCommandServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceCommandService attendanceCommandService;

    @Nested
    @DisplayName("markAttendance 메서드")
    class MarkAttendanceTest {

        @Test
        @DisplayName("사용자가 존재하고 출석하지 않은 상태인 경우 출석 처리 후 true를 반환한다")
        void shouldMarkAttendanceAndReturnTrueWhenUserExistsAndNotAttended() {
            // given
            Long userId = 1L;
            User user = User.builder()
                    .email("test@example.com")
                    .password("password")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();

            User attendedUser = User.builder()
                    .email("test@example.com")
                    .password("password")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();
            attendedUser.completeAttendance();

            given(userJpaRepository.findById(userId)).willReturn(Optional.of(user));
            given(userJpaRepository.save(any(User.class))).willReturn(attendedUser);
            given(attendanceRepository.save(any(Attendance.class))).willReturn(any());

            // when
            boolean result = attendanceCommandService.markAttendance(userId);

            // then
            assertThat(result).isTrue();

            // 출석 기록이 올바르게 저장되는지 검증
            ArgumentCaptor<Attendance> attendanceCaptor = ArgumentCaptor.forClass(Attendance.class);
            verify(attendanceRepository).save(attendanceCaptor.capture());
            
            Attendance savedAttendance = attendanceCaptor.getValue();
            assertThat(savedAttendance.getUserId()).isEqualTo(userId);
            assertThat(savedAttendance.getCheckDate()).isEqualTo(LocalDate.now());

            // 사용자가 저장되는지 검증
            verify(userJpaRepository).save(user);
            verify(userJpaRepository).findById(userId);
        }

        @Test
        @DisplayName("사용자가 이미 출석한 상태인 경우 false를 반환하고 추가 처리하지 않는다")
        void shouldReturnFalseWhenUserAlreadyAttended() {
            // given
            Long userId = 1L;
            User user = User.builder()
                    .email("test@example.com")
                    .password("password")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();
            user.completeAttendance(); // 이미 출석 완료 상태

            given(userJpaRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            boolean result = attendanceCommandService.markAttendance(userId);

            // then
            assertThat(result).isFalse();

            // 추가 처리가 되지 않는지 검증
            verify(attendanceRepository, never()).save(any());
            verify(userJpaRepository, never()).save(any());
            verify(userJpaRepository).findById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID인 경우 UserNotFoundException을 발생시킨다")
        void shouldThrowUserNotFoundExceptionWhenUserNotExists() {
            // given
            Long userId = 999L;
            given(userJpaRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> attendanceCommandService.markAttendance(userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");

            // 추가 처리가 되지 않는지 검증
            verify(attendanceRepository, never()).save(any());
            verify(userJpaRepository, never()).save(any());
            verify(userJpaRepository).findById(userId);
        }

        @Test
        @DisplayName("출석 처리 시 현재 날짜로 Attendance 엔티티가 생성된다")
        void shouldCreateAttendanceWithCurrentDateWhenMarkingAttendance() {
            // given
            Long userId = 1L;
            User user = User.builder()
                    .email("test@example.com")
                    .password("password")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();

            User attendedUser = User.builder()
                    .email("test@example.com")
                    .password("password")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();
            attendedUser.completeAttendance();

            given(userJpaRepository.findById(userId)).willReturn(Optional.of(user));
            given(userJpaRepository.save(any(User.class))).willReturn(attendedUser);

            // when
            attendanceCommandService.markAttendance(userId);

            // then
            ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
            verify(attendanceRepository).save(captor.capture());

            Attendance capturedAttendance = captor.getValue();
            assertThat(capturedAttendance.getUserId()).isEqualTo(userId);
            assertThat(capturedAttendance.getCheckDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("출석 처리 시 User 엔티티의 출석 상태가 변경된다")
        void shouldChangeUserAttendanceStatusWhenMarkingAttendance() {
            // given
            Long userId = 1L;
            User user = User.builder()
                    .email("test@example.com")
                    .password("password")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();

            User attendedUser = User.builder()
                    .email("test@example.com")
                    .password("password")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();
            attendedUser.completeAttendance();

            given(userJpaRepository.findById(userId)).willReturn(Optional.of(user));
            given(userJpaRepository.save(user)).willReturn(attendedUser);

            // when
            boolean result = attendanceCommandService.markAttendance(userId);

            // then
            assertThat(result).isTrue();

            // User의 completeAttendance() 메서드가 호출되어 출석 상태가 변경되었는지 확인
            verify(userJpaRepository).save(user);
        }
    }
}
