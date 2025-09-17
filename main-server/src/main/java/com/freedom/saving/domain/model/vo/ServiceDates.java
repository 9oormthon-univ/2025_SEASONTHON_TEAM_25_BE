package com.freedom.saving.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static com.freedom.common.exception.custom.SavingExceptions.*;

/**
 * 서비스 달력 기준의 시작/만기 일자.
 * 계산은 Application 레이어(SavingsDateService)에서 수행하고 도메인은 결과값만 보관
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceDates {

    @Column(name = "start_service_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "maturity_service_date", nullable = false)
    private LocalDate maturityDate;

    public ServiceDates(LocalDate startDate, LocalDate maturityDate) {
        if (startDate == null || maturityDate == null || maturityDate.isBefore(startDate)) {
            throw new SavingInvalidDatesException();
        }
        this.startDate = startDate;
        this.maturityDate = maturityDate;
    }
}
