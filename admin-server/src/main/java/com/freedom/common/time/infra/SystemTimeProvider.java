package com.freedom.common.time.infra;

import com.freedom.common.time.TimeProvider;

import java.time.*;

public class SystemTimeProvider implements TimeProvider {

    private final Clock clock;

    // Clock을 주입 받는 이유:
    // 테스트에서 Clock.fixed(...)를 넣어 재현성 확보
    public SystemTimeProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant instant() {
        return clock.instant();
    }

    @Override
    public ZonedDateTime now() {
        return ZonedDateTime.now(clock);
    }

    @Override
    public LocalDate today() {
        return LocalDate.now(clock);
    }

    @Override
    public ZoneId zoneId() {
        return clock.getZone();
    }
}
