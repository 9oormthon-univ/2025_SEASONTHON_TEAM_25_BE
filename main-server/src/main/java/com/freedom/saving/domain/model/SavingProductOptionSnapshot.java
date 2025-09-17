package com.freedom.saving.domain.model;

import com.freedom.saving.domain.model.vo.SavingProductOptionSnapshotDraft;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * "기간/금리/적립유형" 옵션의 스냅샷
 * Product와의 연결은 FK로만 보유
 */
@Getter
@Entity
@Table(name = "saving_product_option_snapshot",
        uniqueConstraints = {
                // 하나의 상품스냅샷에 대해 동일(기간/금리유형) 중복 방지
                @UniqueConstraint(name = "uk_spos_product_term_rate",
                        columnNames = {"product_snapshot_id", "save_trm_months", "intr_rate_type"})
        },
        indexes = {
                @Index(name = "idx_spos_product", columnList = "product_snapshot_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavingProductOptionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_snapshot_id", nullable = false)
    private Long productSnapshotId;

    @Column(name = "dcls_month", nullable = false, length = 6)
    private String dclsMonth;

    @Column(name = "intr_rate_type", length = 10)
    private String intrRateType;

    @Column(name = "intr_rate_type_nm", length = 50)
    private String intrRateTypeNm;


    @Column(name = "save_trm_months")
    private Integer saveTrmMonths; // nullable 허용(FSS 값 이상치 대비)

    @Column(name = "intr_rate", precision = 10, scale = 4)
    private BigDecimal intrRate;

    @Column(name = "intr_rate2", precision = 10, scale = 4)
    private BigDecimal intrRate2;

    // 생성 팩토리: 드래프트 + FK 입력
    public static SavingProductOptionSnapshot from(SavingProductOptionSnapshotDraft d, Long productSnapshotId) {
        SavingProductOptionSnapshot e = new SavingProductOptionSnapshot();
        e.productSnapshotId = productSnapshotId;
        e.dclsMonth = d.getDclsMonth();
        e.intrRateType = d.getIntrRateType();
        e.intrRateTypeNm = d.getIntrRateTypeNm();
        e.saveTrmMonths = d.getSaveTrmMonths();
        e.intrRate = d.getIntrRate();
        e.intrRate2 = d.getIntrRate2();
        return e;
    }
}
