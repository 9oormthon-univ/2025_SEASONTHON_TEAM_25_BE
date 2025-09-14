package com.freedom.attendance.infra;

import com.freedom.attendance.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findAllByUserIdAndCheckDateBetween(Long userId, LocalDate start, LocalDate end);
}
