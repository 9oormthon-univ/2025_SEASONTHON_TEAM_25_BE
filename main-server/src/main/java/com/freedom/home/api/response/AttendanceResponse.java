package com.freedom.home.api.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceResponse {
    
    private boolean success;
    private String message;
    
    public static AttendanceResponse success() {
        return AttendanceResponse.builder()
                .success(true)
                .message("출석체크가 완료되었습니다.")
                .build();
    }
    
    public static AttendanceResponse alreadyAttended() {
        return AttendanceResponse.builder()
                .success(false)
                .message("오늘 이미 출석하셨습니다.")
                .build();
    }
}
