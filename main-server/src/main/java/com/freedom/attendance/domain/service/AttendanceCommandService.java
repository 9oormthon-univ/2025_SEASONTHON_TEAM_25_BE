package com.freedom.attendance.domain.service;

import com.freedom.attendance.domain.Attendance;
import com.freedom.attendance.infra.AttendanceRepository;
import com.freedom.auth.domain.User;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.common.exception.custom.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AttendanceCommandService {

    private final UserJpaRepository userJpaRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public boolean markAttendance(Long userId) {
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        if (user.getAttendance()) return false;

        user.completeAttendance();
        Attendance attendance = Attendance.builder().userId(userId).checkDate(LocalDate.now()).build();
        attendanceRepository.save(attendance);
        return userJpaRepository.save(user).getAttendance();
    }
}
