package com.freedom.common.time;

import com.freedom.common.time.infra.SystemTimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

    @Bean
    public Clock appClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }

    @Bean
    public TimeProvider timeProvider(Clock appClock) {
        return new SystemTimeProvider(appClock);
    }
}
