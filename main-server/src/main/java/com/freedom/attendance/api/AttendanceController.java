package com.freedom.attendance.api;

import com.freedom.attendance.application.AttendanceAppService;
import com.freedom.common.security.CustomUserPrincipal;
import com.freedom.attendance.api.response.AttendanceResponse;
import com.freedom.attendance.api.response.CalendarResponse;
import com.freedom.common.exception.custom.InvalidAttendanceParameterException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceAppService attendanceAppService;

    @PostMapping
    public ResponseEntity<AttendanceResponse> markAttendance(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(attendanceAppService.markAttendance(principal.getId()));
    }

    @GetMapping("/calendar")
    public ResponseEntity<CalendarResponse> getAttendanceCalendar(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                                  @RequestParam(value = "year", required = true) int year,
                                                                  @RequestParam(value = "month", required = true) int month) {
        validateCalendarParameters(year, month);
        
        CalendarResponse response = attendanceAppService.getCalendar(principal.getId(), year, month);
        return ResponseEntity.ok(response);
    }
    
    private void validateCalendarParameters(int year, int month) {
        if (month < 1 || month > 12) {
            throw new InvalidAttendanceParameterException("월은 1~12 사이의 값이어야 합니다.");
        }
        if (year < 2000 || year > 2100) {
            throw new InvalidAttendanceParameterException("년도는 2000 ~ 2100 사이의 값이어야 합니다.");
        }
    }
}
