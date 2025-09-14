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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceReadService 단위 테스트")
class AttendanceReadServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceReadService attendanceReadService;

    @Nested
    @DisplayName("isAttendanceCompleted 메서드")
    class IsAttendanceCompletedTest {

        @Test
        @DisplayName("사용자가 존재하고 출석 완료 상태인 경우 true를 반환한다")
        void shouldReturnTrueWhenUserExistsAndAttendanceCompleted() {
            // given
            Long userId = 1L;
            User user = User.builder()
                    .email("test@example.com")
                    .password("password")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();
            user.completeAttendance();

            given(userJpaRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            boolean result = attendanceReadService.isAttendanceCompleted(userId);

            // then
            assertThat(result).isTrue();
            verify(userJpaRepository).findById(userId);
        }

        @Test
        @DisplayName("사용자가 존재하지만 출석 미완료 상태인 경우 false를 반환한다")
        void shouldReturnFalseWhenUserExistsButAttendanceNotCompleted() {
            // given
            Long userId = 1L;
            User user = User.builder()
                    .email("test@example.com")
                    .password("password")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();

            given(userJpaRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            boolean result = attendanceReadService.isAttendanceCompleted(userId);

            // then
            assertThat(result).isFalse();
            verify(userJpaRepository).findById(userId);
        }

        @Test
        @DisplayName("사용자가 존재하지 않는 경우 UserNotFoundException을 발생시킨다")
        void shouldThrowUserNotFoundExceptionWhenUserNotExists() {
            // given
            Long userId = 999L;
            given(userJpaRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> attendanceReadService.isAttendanceCompleted(userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");

            verify(userJpaRepository).findById(userId);
        }
    }

    @Nested
    @DisplayName("getAttendanceMapOfMonth 메서드")
    class GetAttendanceMapOfMonthTest {

        @Test
        @DisplayName("특정 월의 출석 기록이 있는 경우 정확한 boolean 배열을 반환한다")
        void shouldReturnCorrectBooleanArrayWhenAttendanceRecordsExist() {
            // given
            Long userId = 1L;
            int year = 2024;
            int month = 12;

            List<Attendance> attendanceList = List.of(
                    createAttendance(1L, LocalDate.of(2024, 12, 1)),
                    createAttendance(2L, LocalDate.of(2024, 12, 5)),
                    createAttendance(3L, LocalDate.of(2024, 12, 15)),
                    createAttendance(4L, LocalDate.of(2024, 12, 31))
            );

            LocalDate start = LocalDate.of(2024, 12, 1);
            LocalDate end = LocalDate.of(2024, 12, 31);

            given(attendanceRepository.findAllByUserIdAndCheckDateBetween(userId, start, end))
                    .willReturn(attendanceList);

            // when
            boolean[] result = attendanceReadService.getAttendanceMapOfMonth(userId, year, month);

            // then
            assertThat(result).hasSize(31); // 12월은 31일
            assertThat(result[0]).isTrue();   // 1일
            assertThat(result[1]).isFalse();  // 2일
            assertThat(result[4]).isTrue();   // 5일
            assertThat(result[14]).isTrue();  // 15일
            assertThat(result[30]).isTrue();  // 31일

            verify(attendanceRepository).findAllByUserIdAndCheckDateBetween(userId, start, end);
        }

        @Test
        @DisplayName("출석 기록이 없는 경우 모든 값이 false인 배열을 반환한다")
        void shouldReturnAllFalseBooleanArrayWhenNoAttendanceRecords() {
            // given
            Long userId = 1L;
            int year = 2024;
            int month = 2; // 2월 (윤년이므로 29일)

            LocalDate start = LocalDate.of(2024, 2, 1);
            LocalDate end = LocalDate.of(2024, 2, 29);

            given(attendanceRepository.findAllByUserIdAndCheckDateBetween(userId, start, end))
                    .willReturn(List.of());

            // when
            boolean[] result = attendanceReadService.getAttendanceMapOfMonth(userId, year, month);

            // then
            assertThat(result).hasSize(29); // 2024년 2월은 윤년이므로 29일
            assertThat(result).containsOnly(false);

            verify(attendanceRepository).findAllByUserIdAndCheckDateBetween(userId, start, end);
        }

        @Test
        @DisplayName("2월 평년의 경우 28일 배열을 반환한다")
        void shouldReturn28DaysArrayForFebruaryInNonLeapYear() {
            // given
            Long userId = 1L;
            int year = 2023; // 평년
            int month = 2;

            LocalDate start = LocalDate.of(2023, 2, 1);
            LocalDate end = LocalDate.of(2023, 2, 28);

            given(attendanceRepository.findAllByUserIdAndCheckDateBetween(userId, start, end))
                    .willReturn(List.of());

            // when
            boolean[] result = attendanceReadService.getAttendanceMapOfMonth(userId, year, month);

            // then
            assertThat(result).hasSize(28); // 2023년 2월은 평년이므로 28일
            assertThat(result).containsOnly(false);

            verify(attendanceRepository).findAllByUserIdAndCheckDateBetween(userId, start, end);
        }

        @Test
        @DisplayName("30일인 월의 경우 30일 배열을 반환한다")
        void shouldReturn30DaysArrayForThirtyDaysMonth() {
            // given
            Long userId = 1L;
            int year = 2024;
            int month = 4; // 4월 (30일)

            LocalDate start = LocalDate.of(2024, 4, 1);
            LocalDate end = LocalDate.of(2024, 4, 30);

            Attendance attendance = createAttendance(1L, LocalDate.of(2024, 4, 30));
            given(attendanceRepository.findAllByUserIdAndCheckDateBetween(userId, start, end))
                    .willReturn(List.of(attendance));

            // when
            boolean[] result = attendanceReadService.getAttendanceMapOfMonth(userId, year, month);

            // then
            assertThat(result).hasSize(30); // 4월은 30일
            assertThat(result[29]).isTrue();  // 30일 (인덱스 29)

            verify(attendanceRepository).findAllByUserIdAndCheckDateBetween(userId, start, end);
        }
    }

    private Attendance createAttendance(Long id, LocalDate checkDate) {
        return Attendance.builder()
                .userId(1L)
                .checkDate(checkDate)
                .build();
    }
}
