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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            // 정렬 정책 키. popular(인기순), name(상품명 가나다순) 지원
            @RequestParam(name = "sort", defaultValue = "name") String sort) {

        // 아직 SAVING만 지원하므로 방어적으로 검증
        if (!"SAVING".equalsIgnoreCase(type)) {
            throw new SavingExceptions.SavingPolicyInvalidException("지원하지 않는 type 값입니다. (허용: SAVING)");
        }
        
        // 지원하는 정렬 옵션 검증
        if (!isValidSortOption(sort)) {
            throw new SavingExceptions.SavingPolicyInvalidException(
                "지원하지 않는 sort 값입니다. (허용: popular, name)");
        }
        
        // 정렬 옵션에 따라 서비스 호출
        return Map.of(
                "content",
                readService.getSavingProducts(sort, 0, Integer.MAX_VALUE).getContent()
        );
    }
    
    /**
     * 지원하는 정렬 옵션인지 검증
     */
    private boolean isValidSortOption(String sort) {
        return "popular".equalsIgnoreCase(sort) || 
               "name".equalsIgnoreCase(sort);
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
