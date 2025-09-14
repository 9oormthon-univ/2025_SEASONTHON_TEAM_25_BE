package com.freedom.saving.application.signup;

import com.freedom.common.time.TimeProvider;
import com.freedom.saving.domain.payment.SavingPaymentHistory;
import com.freedom.saving.domain.payment.SavingPaymentHistoryRepository;
import com.freedom.saving.application.port.SavingProductSnapshotPort;
import com.freedom.saving.application.port.SavingSubscriptionPort;
import com.freedom.common.exception.custom.InvalidAutoDebitAmountForFixedException;
import com.freedom.common.exception.custom.ProductSnapshotNotFoundException;
import com.freedom.common.exception.custom.ProductTermNotSupportedException;
import com.freedom.saving.domain.policy.TickPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * 적금 가입 유스케이스
 *
 * 설계 메모
 * - SRP: 도메인 규칙(기간/유형 검증)만 담당. 저장/조회는 포트로 위임
 * - 수집 단계에서 fin_prdt_cd 기반으로 옵션-스냅샷이 이미 연결됨.
 *   가입 시점엔 productSnapshotId 하나만 알면 충분.
 */
@Service
@RequiredArgsConstructor
public class SavingSubscriptionService {

    private final SavingProductSnapshotPort snapshotPort;
    private final SavingSubscriptionPort subscriptionPort;
    private final TimeProvider timeProvider;                // 현재 시각
    private final TickPolicy tickPolicy;                    // 1일 = 1개월 정책
    private final SavingPaymentHistoryRepository paymentHistoryRepository; // 가입 시 스케줄 생성

    private static final String RESERVE_S = "S"; // 정액적립식만 사용

    private LocalDate serviceToday(ZonedDateTime now) {
        // "현실 하루 = 서비스 한 달", '오늘'을 서비스 기준일로 그대로 쓴다고 가정
        return now.toLocalDate();
    }

    @Transactional
    public OpenSubscriptionResult open(OpenSubscriptionCommand cmd) {
        // 1) 스냅샷 존재
        if (!snapshotPort.existsSnapshot(cmd.getProductSnapshotId())) {
            throw new ProductSnapshotNotFoundException(cmd.getProductSnapshotId());
        }

        // 2) 기간 검증 (사용자가 선택한 기간이 유효한지 확인)
        int chosenTerm = validateAndGetTerm(cmd.getProductSnapshotId(), cmd.getTermMonths());

        // 3) 적립유형은 정액적립식으로 고정
        String chosenReserve = RESERVE_S;

        // 4) 정액식이므로 금액 필수
        validateAutoDebitAmountForFixed(cmd.getAutoDebitAmount());

        // 5) 서비스 달력 날짜 계산
        ZonedDateTime now = timeProvider.now();
        LocalDate startServiceDate = serviceToday(now);
        LocalDate maturityServiceDate = tickPolicy.calcMaturityDate(startServiceDate, chosenTerm);

        // 6) 저장 (서비스 일자 전달)
        Long subscriptionId = subscriptionPort.open(
                cmd.getUserId(),
                cmd.getProductSnapshotId(),
                chosenTerm,
                chosenReserve,
                cmd.getAutoDebitAmount(),
                startServiceDate,
                maturityServiceDate
        );

        // 정액적립식(S): 가입과 동시에 전체 납입 스케줄(PLANNED) 생성
        if (RESERVE_S.equals(chosenReserve)) {
            // 첫 납입 예정일 = 가입일 + 1 서비스일
            java.time.LocalDate firstDue = tickPolicy.calcFirstTransferDate(startServiceDate);
            java.math.BigDecimal expected = cmd.getAutoDebitAmount();
            for (int i = 1; i <= chosenTerm; i++) {
                java.time.LocalDate due = firstDue.plusDays(i - 1L);
                SavingPaymentHistory planned = SavingPaymentHistory.planned(
                        subscriptionId,
                        i,
                        due,
                        expected
                );
                paymentHistoryRepository.save(planned);
            }
        }
        // 인기 집계 증가
        snapshotPort.incrementSubscriberCount(cmd.getProductSnapshotId());
        return new OpenSubscriptionResult(subscriptionId, startServiceDate, maturityServiceDate);
    }

    /**
     * 기간 검증:
     * - 사용자가 선택한 기간이 상품에서 지원하는 기간인지 확인
     */
    private int validateAndGetTerm(Long snapshotId, Integer requestedTerm) {
        List<Integer> supported = snapshotPort.getSupportedTermMonths(snapshotId);
        if (!supported.contains(requestedTerm)) {
            throw new ProductTermNotSupportedException(requestedTerm);
        }
        return requestedTerm;
    }


    private void validateAutoDebitAmountForFixed(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAutoDebitAmountForFixedException(amount);
        }
    }
}
