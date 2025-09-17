package com.freedom.saving.application.service;

import com.freedom.saving.api.dto.MaturityPreviewRequest;
import com.freedom.saving.api.dto.MaturityPreviewResponse;
import com.freedom.saving.domain.model.SavingProductOptionSnapshot;
import com.freedom.saving.infra.persistence.SavingProductOptionSnapshotJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 적금 만기 금액 미리보기 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavingMaturityPreviewService {

    private final SavingMaturityCalculationService calculationService;
    private final SavingProductOptionSnapshotJpaRepository optionRepository;

    /**
     * 적금 만기 금액 미리보기
     * 
     * @param productId 상품 ID
     * @param request 만기 금액 계산 요청
     * @return 만기 금액 미리보기 결과
     */
    public MaturityPreviewResponse previewMaturity(Long productId, MaturityPreviewRequest request) {

        // 1. 해당 기간의 옵션 조회
        List<SavingProductOptionSnapshot> options = optionRepository
                .findByProductSnapshotIdOrderBySaveTrmMonthsAsc(productId);
        
        SavingProductOptionSnapshot targetOption = options.stream()
                .filter(option -> option.getSaveTrmMonths() != null && 
                        option.getSaveTrmMonths().equals(request.getTermMonths()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 기간(" + request.getTermMonths() + "개월)의 옵션을 찾을 수 없습니다."));

        // 2. 우리 서비스 금리로 계산 (기본 금리)
        SavingMaturityCalculationService.MaturityCalculationResult ourServiceResult = 
                calculationService.calculateMaturityFromOption(request.getMonthlyAmount(), targetOption);

        // 3. 우대 금리 적용 시 계산 (intr_rate2 사용)
        if (targetOption.getIntrRate2() == null) {
            throw new IllegalArgumentException("우대 금리 정보가 없습니다.");
        }
        
        SavingMaturityCalculationService.MaturityCalculationResult preferentialResult = 
                calculationService.calculateMaturityWithPreferentialRate(
                        request.getMonthlyAmount(), targetOption);

        // 4. 응답 생성
        MaturityPreviewResponse response = new MaturityPreviewResponse(
                MaturityPreviewResponse.MaturityInfo.from(ourServiceResult),
                MaturityPreviewResponse.MaturityInfo.from(preferentialResult)
        );


        return response;
    }
}
