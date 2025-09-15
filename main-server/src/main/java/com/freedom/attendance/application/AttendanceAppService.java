package com.freedom.attendance.application;

import com.freedom.achievement.application.dto.AchievementDto;
import com.freedom.achievement.domain.entity.Achievement;
import com.freedom.achievement.domain.service.AchievementCommandService;
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
    private final AchievementCommandService achievementCommandService;

    @Transactional
    public AttendanceResponse markAttendance(Long userId) {
        boolean isSuccess = attendanceCommandService.markAttendance(userId);
        AchievementDto achievementDto = null;
        if (isSuccess) {
            achievementDto = checkMorningSunshineAchievement(userId);
        }
        return isSuccess
                ? AttendanceResponse.success(achievementDto)
                : AttendanceResponse.alreadyAttended();
    }

    private AchievementDto checkMorningSunshineAchievement(Long userId) {
        int consecutiveDays = attendanceReadService.getCurrentMonthConsecutiveAttendanceDays(userId);
        if (consecutiveDays == 30) {
            return achievementCommandService.grantAchievement(userId, Achievement.AchievementType.MORNING_SUNSHINE);
        }
        return null;
    }

    public CalendarResponse getCalendar(Long userId, int year, int month) {
        boolean[] attendanceMap = attendanceReadService.getAttendanceMapOfMonth(userId, year, month);
        return CalendarResponse.of(year, month, attendanceMap);
    }
}
