package com.freedom.saving.api.subscription;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record OpenSubscriptionResponse(
        Long subscriptionId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate maturityDate,
        String message
) { }
