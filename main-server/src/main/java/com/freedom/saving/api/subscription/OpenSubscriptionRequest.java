package com.freedom.saving.api.subscription;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OpenSubscriptionRequest(
        @NotNull
        Long productSnapshotId,

        // 필수: 가입할 기간(개월). 상품 옵션에서 제공하는 기간 중 선택
        @NotNull
        @JsonAlias({"termMoonths"})
        @Positive
        Integer termMonths,

        // 정액적립식이므로 필수: 양수여야 함
        @NotNull
        @Positive
        BigDecimal autoDebitAmount
) { }
