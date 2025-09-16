package com.freedom.saving.application.policy;

import com.freedom.saving.domain.policy.TickPolicy;

import com.freedom.common.time.TimeProvider;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static com.freedom.common.exception.custom.SavingExceptions.*;

public class RealDayEqualsServiceMonthPolicy implements TickPolicy {

    // TimeProvider 주입 이유:
    // - 오늘 날짜(today) 계산의 일관성과 테스트 용이성 확보
    public RealDayEqualsServiceMonthPolicy(TimeProvider timeProvider) {
        // 현재는 사용하지 않지만 향후 확장성을 위해 유지
    }

    @Override
    public int toTotalTicks(int termMonths) {
        if (termMonths <= 0) {
            throw new SavingPolicyInvalidException("termMonths는 1 이상이어야 합니다.");
        }
        return termMonths;
    }

    @Override
    public LocalDate calcFirstTransferDate(LocalDate joinDate) {
        requireNonNull(joinDate);
        // 가입 당일부터 납입 (1일 = 1개월 정책)
        return joinDate;
    }

    @Override
    public LocalDate calcMaturityDate(LocalDate joinDate, int termMonths) {
        requireNonNull(joinDate);
        int total = toTotalTicks(termMonths);
        return joinDate.plusDays(total);
    }

    @Override
    public LocalDate calcNextTransferDate(LocalDate joinDate, int currentTick) {
        requireNonNull(joinDate);
        if (currentTick < 0) {
            throw new SavingPolicyInvalidException("currentTick은 0 이상이어야 합니다.");
        }
        // next = 가입일 + 지금까지 처리된 회차 수 (가입일부터 시작)
        return joinDate.plusDays(currentTick);
    }

    @Override
    public int estimateCurrentTick(LocalDate joinDate, LocalDate today, int termMonths) {
        requireNonNull(joinDate);
        requireNonNull(today);

        int total = toTotalTicks(termMonths);
        LocalDate first = calcFirstTransferDate(joinDate);
        LocalDate maturity = calcMaturityDate(joinDate, termMonths);

        if (today.isBefore(first)) {
            return 0;                   // 첫 납입 전
        }
        if (!today.isBefore(maturity)) {
            return total;               // 만기일 이상이면 상한
        }

        long days = ChronoUnit.DAYS.between(first, today);
        if (days <= 0) return 0;
        if (days >= total) return total;
        return (int) days;
    }

    private void requireNonNull(LocalDate date) {
        if (Objects.isNull(date)) {
            throw new SavingPolicyInvalidException("날짜는 null일 수 없습니다.");
        }
    }
}
