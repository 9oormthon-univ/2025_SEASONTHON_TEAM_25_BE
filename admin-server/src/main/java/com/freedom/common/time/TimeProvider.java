package com.freedom.common.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface TimeProvider {

    // 현재 순간(Instant)
    Instant instant();

    // 현재 시간(타임존 반영)
    ZonedDateTime now();

    // 오늘 날짜(로컬 날짜, 타임존 반영)
    LocalDate today();

    // 애플리케이션 타임존
    ZoneId zoneId();
}
