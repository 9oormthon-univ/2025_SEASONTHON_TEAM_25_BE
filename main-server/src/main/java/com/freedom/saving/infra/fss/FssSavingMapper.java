package com.freedom.saving.infra.fss;

import com.freedom.saving.domain.shapshot.SavingProductOptionSnapshotDraft;
import com.freedom.saving.domain.shapshot.SavingProductSnapshotDraft;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * FSS 응답 DTO -> 스냅샷 드래프트 변환기
 * 외부 스키마를 내부 도메인 모델로 안전하게 변환
 */
@Component
public class FssSavingMapper {

    public List<SavingProductSnapshotDraft> toProductDrafts(FssSavingResponseDto dto) {
        List<SavingProductSnapshotDraft> result = new ArrayList<SavingProductSnapshotDraft>();
        if (dto == null || dto.result == null || dto.result.baseList == null) {
            return result;
        }

        List<FssSavingResponseDto.Base> baseList = dto.result.baseList;
        for (FssSavingResponseDto.Base b : baseList) {
            // prdt_div가 "S"(적금)인지 확인
            if (dto.result.prdtDiv != null && !"S".equalsIgnoreCase(dto.result.prdtDiv)) {
                continue;
            }

            SavingProductSnapshotDraft draft = new SavingProductSnapshotDraft(
                    safeTrim(b.dclsMonth),
                    safeTrim(b.finCoNo),
                    safeTrim(b.finPrdtCd),
                    safeTrim(b.korCoNm),
                    safeTrim(b.finPrdtNm),
                    safeTrim(b.joinWay),
                    safeTrim(b.mtrtInt),
                    safeTrim(b.spclCnd),
                    safeTrim(b.joinDeny),
                    safeTrim(b.joinMember),
                    safeTrim(b.etcNote),
                    b.maxLimit,                        // Integer 그대로
                    safeTrim(b.dclsStrtDay),
                    safeTrim(b.dclsEndDay),
                    safeTrim(b.finCoSubmDay)
            );
            result.add(draft);
        }
        return result;
    }

    public List<SavingProductOptionSnapshotDraft> toOptionDrafts(FssSavingResponseDto dto) {
        List<SavingProductOptionSnapshotDraft> result = new ArrayList<SavingProductOptionSnapshotDraft>();
        if (dto == null || dto.result == null || dto.result.optionList == null) {
            return result;
        }

        List<FssSavingResponseDto.Option> optionList = dto.result.optionList;
        for (FssSavingResponseDto.Option o : optionList) {
            if (dto.result.prdtDiv != null && !"S".equalsIgnoreCase(dto.result.prdtDiv)) {
                continue;
            }

            Integer saveTrmMonths = parseIntSafe(o.saveTrm);
            BigDecimal intrRate = toBigDecimal(o.intrRate);
            BigDecimal intrRate2 = toBigDecimal(o.intrRate2);

            SavingProductOptionSnapshotDraft draft = new SavingProductOptionSnapshotDraft(
                    safeTrim(o.dclsMonth),
                    safeTrim(o.finCoNo),
                    safeTrim(o.finPrdtCd),
                    safeTrim(o.intrRateType),
                    safeTrim(o.intrRateTypeNm),
                    safeTrim(o.rsrvType),
                    safeTrim(o.rsrvTypeNm),
                    saveTrmMonths,
                    intrRate,
                    intrRate2
            );
            result.add(draft);
        }
        return result;
    }

    private String safeTrim(String s) {
        if (s == null) return "";
        return s.trim();
    }

    private Integer parseIntSafe(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return null; // 잘못된 값은 null로 넘기고 엔티티 생성부에서 추가 검증
        }
    }

    private BigDecimal toBigDecimal(Double d) {
        if (d == null) return null;
        // Double 오차를 피하기 위해 BigDecimal.valueOf 사용
        return BigDecimal.valueOf(d);
    }
}
