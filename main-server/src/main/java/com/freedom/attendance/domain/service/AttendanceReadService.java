package com.freedom.attendance.domain.service;

import com.freedom.attendance.infra.AttendanceRepository;
import com.freedom.auth.domain.User;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.common.exception.custom.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DayOfWeek;

@Service
@RequiredArgsConstructor
public class AttendanceReadService {

    private final UserJpaRepository userJpaRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    public boolean isAttendanceCompleted(Long userId) {
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return user.getAttendance();
    }

    @Transactional(readOnly = true)
    public boolean[] getAttendanceMapOfMonth(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        int dim = ym.lengthOfMonth();
        boolean[] map = new boolean[dim];
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();
        attendanceRepository.findAllByUserIdAndCheckDateBetween(userId, start, end)
                .forEach(a -> map[a.getCheckDate().getDayOfMonth() - 1] = true);
        return map;
    }

    @Transactional(readOnly = true)
    public int getWeeklyConsecutiveAttendanceDays(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        
        int consecutiveDays = 0;
        LocalDate checkDate = today;
        
        while (!checkDate.isBefore(startOfWeek)) {
            if (attendanceRepository.existsByUserIdAndCheckDate(userId, checkDate)) {
                consecutiveDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                if (checkDate.equals(today)) {
                    checkDate = checkDate.minusDays(1);
                    continue;
                }
                break;
            }
        }
        return consecutiveDays;
    }

    @Transactional(readOnly = true)
    public int getCurrentMonthConsecutiveAttendanceDays(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        int consecutiveDays = 0;
        LocalDate checkDate = today;

        while (!checkDate.isBefore(startOfMonth)) {
            if (attendanceRepository.existsByUserIdAndCheckDate(userId, checkDate)) {
                consecutiveDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        return consecutiveDays;
    }
}
