package com.freedom.saving.domain.policy;

import java.time.LocalDate;

/**
 * "현실 1일 = 서비스 1개월(=1 tick)" 규칙.
 * - termMonths == totalTicks
 */
public interface TickPolicy {

    // 총 필요 tick 수
    int toTotalTicks(int termMonths);

    // 첫 납입일 = 가입일 (가입 당일부터 납입)
    LocalDate calcFirstTransferDate(LocalDate joinDate);

    // 만기일 = 가입일 + termMonths 일
    LocalDate calcMaturityDate(LocalDate joinDate, int termMonths);

    /**
     * 다음 납입일 = 가입일 + currentTick
     *  - currentTick: 이미 처리 완료된 회차 수(0부터 시작)
     *  - 예) 가입 9/1, currentTick=0 -> next=9/1, currentTick=1 -> next=9/2
     */
    LocalDate calcNextTransferDate(LocalDate joinDate, int currentTick);

    /**
     * 진행 tick(경과 회차) 계산
     *  - today < firstTransferDate: 0
     *  - today >= maturityDate: totalTicks
     *  - 그 외: daysBetween(firstTransferDate, today) (음수면 0), 상한 totalTicks
     */
    int estimateCurrentTick(LocalDate joinDate, LocalDate today, int termMonths);
}
