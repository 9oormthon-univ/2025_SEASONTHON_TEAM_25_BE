package com.freedom.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    
    private long totalNewsCount;
    private long totalQuizCount;
    private long totalUserCount;
    
    public static DashboardStatsResponse of(long newsCount, long quizCount, long userCount) {
        return new DashboardStatsResponse(newsCount, quizCount, userCount);
    }
}
