package com.freedom.saving.api;

import com.freedom.saving.api.dto.MaturityPreviewRequest;
import com.freedom.saving.api.dto.MaturityPreviewResponse;
import com.freedom.saving.application.SavingMaturityCalculationService;
import com.freedom.saving.application.SavingProductReadService;
import com.freedom.saving.application.read.SavingProductDetail;
import com.freedom.saving.domain.SavingProductOptionSnapshot;
import com.freedom.saving.infra.snapshot.SavingProductOptionSnapshotJpaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 적금 만기 금액 미리보기 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/saving")
@RequiredArgsConstructor
public class SavingMaturityPreviewController {

    private final SavingMaturityCalculationService calculationService;
    private final SavingProductReadService productReadService;
    private final SavingProductOptionSnapshotJpaRepository optionRepository;

    /**
     * 적금 만기 금액 미리보기
     * 
     * @param request 만기 금액 계산 요청
     * @return 만기 금액 미리보기 결과
     */
    @PostMapping("/maturity-preview")
    public ResponseEntity<MaturityPreviewResponse> previewMaturity(@Valid @RequestBody MaturityPreviewRequest request) {
        log.info("적금 만기 금액 미리보기 요청: 월납입={}, 기간={}개월, 상품ID={}", 
                request.getMonthlyAmount(), request.getTermMonths(), request.getProductId());

        try {
            // 1. 상품 정보 조회
            SavingProductDetail productDetail = productReadService.getDetail(request.getProductId());
            
            // 2. 해당 기간의 옵션 조회
            List<SavingProductOptionSnapshot> options = optionRepository
                    .findByProductSnapshotIdOrderBySaveTrmMonthsAsc(request.getProductId());
            
            SavingProductOptionSnapshot targetOption = options.stream()
                    .filter(option -> option.getSaveTrmMonths() != null && 
                            option.getSaveTrmMonths().equals(request.getTermMonths()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "해당 기간(" + request.getTermMonths() + "개월)의 옵션을 찾을 수 없습니다."));

            // 3. 우리 서비스 금리로 계산 (기본 금리)
            SavingMaturityCalculationService.MaturityCalculationResult ourServiceResult = 
                    calculationService.calculateMaturityFromOption(request.getMonthlyAmount(), targetOption);

            // 4. 우대 금리 적용 시 계산 (intr_rate2 사용)
            if (targetOption.getIntrRate2() == null) {
                throw new IllegalArgumentException("우대 금리 정보가 없습니다.");
            }
            
            SavingMaturityCalculationService.MaturityCalculationResult preferentialResult = 
                    calculationService.calculateMaturityWithPreferentialRate(
                            request.getMonthlyAmount(), targetOption);


            // 5. 응답 생성
            MaturityPreviewResponse response = new MaturityPreviewResponse(
                    MaturityPreviewResponse.MaturityInfo.from(ourServiceResult),
                    MaturityPreviewResponse.MaturityInfo.from(preferentialResult),
                    productDetail.getProductName(),
                    productDetail.getBankName()
            );

            log.info("적금 만기 금액 미리보기 완료: 우리서비스={}, 우대금리={}",
                    ourServiceResult.totalAmount(), preferentialResult.totalAmount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("적금 만기 금액 미리보기 중 오류 발생", e);
            throw new RuntimeException("만기 금액 계산 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}
