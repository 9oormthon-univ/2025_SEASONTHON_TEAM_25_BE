package com.freedom.attendance.application;

import com.freedom.attendance.api.response.CalendarResponse;
import com.freedom.attendance.domain.service.AttendanceCommandService;
import com.freedom.attendance.api.response.AttendanceResponse;
import com.freedom.attendance.domain.service.AttendanceReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceAppService {

    private final AttendanceCommandService attendanceCommandService;
    private final AttendanceReadService attendanceReadService;

    @Transactional
    public AttendanceResponse markAttendance(Long userId) {
        boolean isSuccess = attendanceCommandService.markAttendance(userId);

        return isSuccess
                ? AttendanceResponse.success()
                : AttendanceResponse.alreadyAttended();
    }

    public CalendarResponse getCalendar(Long userId, int year, int month) {
        boolean[] attendanceMap = attendanceReadService.getAttendanceMapOfMonth(userId, year, month);
        return CalendarResponse.of(year, month, attendanceMap);
    }
}
