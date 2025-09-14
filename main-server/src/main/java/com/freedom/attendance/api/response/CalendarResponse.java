package com.freedom.attendance.api.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CalendarResponse {
    private int year;
    private int month;
    private int totalDays;
    private boolean[] attendanceMap;
    private int attendanceCount;
    private int startDayOfWeek;

    public static CalendarResponse of(int year, int month, boolean[] attendanceMap) {
        int attendanceCount = 0;
        for (boolean attended : attendanceMap) {
            if (attended) {
                attendanceCount++;
            }
        }
        
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue();
        
        return CalendarResponse.builder()
                .year(year)
                .month(month)
                .totalDays(attendanceMap.length)
                .attendanceMap(attendanceMap)
                .attendanceCount(attendanceCount)
                .startDayOfWeek(startDayOfWeek)
                .build();
    }
}
