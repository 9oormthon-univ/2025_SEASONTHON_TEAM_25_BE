package com.freedom.saving.domain.shapshot;

import com.freedom.common.exception.custom.SavingSnapshotIdentifiersInvalidException;
import lombok.Getter;

import java.util.Objects;


/**
 * FSS 상품 기본정보 스냅샷 "드래프트"
 * JPA 엔티티 도입 전, 매퍼의 출력용 불변 VO
 * 엔티티 생성 시 본 드래프트를 그대로 전달해 생성자/팩토리에서 매핑하도록 함
 */
@Getter
public class SavingProductSnapshotDraft {

    private final String dclsMonth;
    private final String finCoNo;
    private final String finPrdtCd;

    private final String korCoNm;
    private final String finPrdtNm;
    private final String joinWay;
    private final String mtrtInt;
    private final String spclCnd;
    private final String joinDeny;
    private final String joinMember;
    private final String etcNote;

    private final Integer maxLimit;
    private final String dclsStrtDay;
    private final String dclsEndDay;
    private final String finCoSubmDay;

    public SavingProductSnapshotDraft(
            String dclsMonth,
            String finCoNo,
            String finPrdtCd,
            String korCoNm,
            String finPrdtNm,
            String joinWay,
            String mtrtInt,
            String spclCnd,
            String joinDeny,
            String joinMember,
            String etcNote,
            Integer maxLimit,
            String dclsStrtDay,
            String dclsEndDay,
            String finCoSubmDay
    ) {
        // 최소 식별자 유효성(엔티티 전 단계에서도 기본 검증)
        if (isBlank(dclsMonth) || isBlank(finCoNo) || isBlank(finPrdtCd)) {
            throw new SavingSnapshotIdentifiersInvalidException();
        }
        this.dclsMonth = dclsMonth;
        this.finCoNo = finCoNo;
        this.finPrdtCd = finPrdtCd;
        this.korCoNm = nullToEmpty(korCoNm);
        this.finPrdtNm = nullToEmpty(finPrdtNm);
        this.joinWay = nullToEmpty(joinWay);
        this.mtrtInt = nullToEmpty(mtrtInt);
        this.spclCnd = nullToEmpty(spclCnd);
        this.joinDeny = nullToEmpty(joinDeny);
        this.joinMember = nullToEmpty(joinMember);
        this.etcNote = nullToEmpty(etcNote);
        this.maxLimit = maxLimit; // null 허용
        this.dclsStrtDay = nullToEmpty(dclsStrtDay);
        this.dclsEndDay = nullToEmpty(dclsEndDay);
        this.finCoSubmDay = nullToEmpty(finCoSubmDay);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nullToEmpty(String s) {
        return Objects.nonNull(s) ? s : "";
    }
}
