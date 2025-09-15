package com.freedom.attendance.api.response;

import com.freedom.achievement.application.dto.AchievementDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceResponse {
    
    private boolean success;
    private String message;
    private String achievementType;
    private boolean achievementCreated;

    public static AttendanceResponse success(AchievementDto achievementDto) {
        return achievementDto != null
                ? AttendanceResponse.builder()
                    .success(true)
                    .message("출석체크가 완료되었습니다.")
                    .achievementType(achievementDto.getType())
                    .achievementCreated(true)
                    .build()
                : AttendanceResponse.builder()
                    .success(true)
                    .message("출석체크가 완료되었습니다.")
                    .achievementType(null)
                    .achievementCreated(false)
                    .build();
    }
    
    public static AttendanceResponse alreadyAttended() {
        return AttendanceResponse.builder()
                .success(false)
                .message("오늘 이미 출석하셨습니다.")
                .build();
    }
}
