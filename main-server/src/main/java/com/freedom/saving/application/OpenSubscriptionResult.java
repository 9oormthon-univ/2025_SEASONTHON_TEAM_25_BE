package com.freedom.saving.application;


import java.time.LocalDate;

/**
 * 가입 결과(애플리케이션 계층)
 */
public record OpenSubscriptionResult(
        Long subscriptionId,
        LocalDate startDate,
        LocalDate maturityDate
) {}
