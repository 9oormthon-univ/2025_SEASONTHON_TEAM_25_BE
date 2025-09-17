package com.freedom.saving.domain.model.vo;

import com.freedom.common.exception.custom.SavingExceptions;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * FSS 옵션(기간/금리) 스냅샷 "드래프트"
 * 금리는 BigDecimal로 보정(소수 오차 방지)
 */
@Getter
public class SavingProductOptionSnapshotDraft {

    private final String dclsMonth;
    private final String finCoNo;
    private final String finPrdtCd;

    private final String intrRateType;
    private final String intrRateTypeNm;

    private final Integer saveTrmMonths;
    private final BigDecimal intrRate;
    private final BigDecimal intrRate2;

    public SavingProductOptionSnapshotDraft(
            String dclsMonth,
            String finCoNo,
            String finPrdtCd,
            String intrRateType,
            String intrRateTypeNm,
            Integer saveTrmMonths,
            BigDecimal intrRate,
            BigDecimal intrRate2
    ) {
        if (isBlank(dclsMonth) || isBlank(finCoNo) || isBlank(finPrdtCd)) {
            throw new SavingExceptions.SavingSnapshotIdentifiersInvalidException();
        }
        this.dclsMonth = dclsMonth;
        this.finCoNo = finCoNo;
        this.finPrdtCd = finPrdtCd;
        this.intrRateType = nullToEmpty(intrRateType);
        this.intrRateTypeNm = nullToEmpty(intrRateTypeNm);
        this.saveTrmMonths = saveTrmMonths; // null 허용(파싱 실패 시), 엔티티에서 추가 검증 가능
        this.intrRate = intrRate;           // null 허용
        this.intrRate2 = intrRate2;         // null 허용
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
    private String nullToEmpty(String s) {
        return Objects.nonNull(s) ? s : "";
    }
}
