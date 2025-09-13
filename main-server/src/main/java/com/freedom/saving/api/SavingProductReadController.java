package com.freedom.saving.api;

import com.freedom.common.exception.custom.SavingExceptions;
import com.freedom.saving.api.dto.MaturityPreviewRequest;
import com.freedom.saving.api.dto.MaturityPreviewResponse;
import com.freedom.saving.application.SavingMaturityPreviewService;
import com.freedom.saving.application.SavingProductReadService;
import com.freedom.saving.application.read.SavingProductDetail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saving")
@RequiredArgsConstructor
public class SavingProductReadController {

    private final SavingProductReadService readService;
    private final SavingMaturityPreviewService maturityPreviewService;

    @GetMapping
    public Object getProducts(
            // 타입은 현재 SAVING만 지원. 추후 예/적금 분리 시 확장 포인트.
            @RequestParam(name = "type", defaultValue = "SAVING") @NotBlank String type,
            // 정렬 정책 키. 현재 popular(임시)만 허용.
            @RequestParam(name = "sort", defaultValue = "popular") String sort) {

        // 아직 SAVING만 지원하므로 방어적으로 검증
        if (!"SAVING".equalsIgnoreCase(type)) {
            throw new SavingExceptions.SavingPolicyInvalidException("지원하지 않는 type 값입니다. (허용: SAVING)");
        }
        // popular 외 값이 들어오면 현재 동작과 다른 결과가 나올 수 있어 방어적으로 검증
        if (!"popular".equalsIgnoreCase(sort)) {
            throw new SavingExceptions.SavingPolicyInvalidException("지원하지 않는 sort 값입니다. (허용: popular)");
        }
        // 서비스는 인기순 = subscriberCount DESC 전체 반환
        return java.util.Map.of(
                "content",
                readService.getPopularSavingProducts(0, Integer.MAX_VALUE).getContent()
        );
    }

    @GetMapping("/{id}")
    public SavingProductDetail getProductDetail(
            @PathVariable("id") @Positive Long productSnapshotId) {
        return readService.getDetail(productSnapshotId);
    }

    /**
     * 적금 만기 금액 미리보기
     * 
     * @param productId 상품 ID
     * @param request 만기 금액 계산 요청
     * @return 만기 금액 미리보기 결과
     */
    @PostMapping("/{id}/maturity-preview")
    public ResponseEntity<MaturityPreviewResponse> previewMaturity(
            @PathVariable("id") @Positive Long productId,
            @Valid @RequestBody MaturityPreviewRequest request) {
        
        MaturityPreviewResponse response = maturityPreviewService.previewMaturity(productId, request);
        return ResponseEntity.ok(response);
    }
}
