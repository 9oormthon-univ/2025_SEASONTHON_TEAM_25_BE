package com.freedom.auth.domain.service;

import com.freedom.auth.domain.User;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.common.exception.custom.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final UserJpaRepository userJpaRepository;

    @Transactional
    public boolean markAttendance(Long userId) {
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        if (user.getAttendance()) return false;

        user.completeAttendance();
        return userJpaRepository.save(user).getAttendance();
    }

    @Transactional(readOnly = true)
    public boolean isAttendanceCompleted(Long userId) {
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        return user.getAttendance();
    }
}
