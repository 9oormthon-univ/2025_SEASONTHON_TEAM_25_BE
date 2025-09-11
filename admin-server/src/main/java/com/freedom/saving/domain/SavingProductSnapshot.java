package com.freedom.saving.domain;

import com.freedom.saving.domain.shapshot.SavingProductSnapshotDraft;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * "가입 시점에 고정되는 상품 기본정보" 스냅샷
 * 외부 API 데이터가 시간이 지나 변경되어도, 가입 당시 상태를 보존하기 위해 스냅샷을 둔다.
 */
@Getter
@Entity
@Table(name = "saving_product_snapshot",
        uniqueConstraints = {
                // 동일 공시월/회사/상품코드 조합으로 중복 삽입 방지
                @UniqueConstraint(name = "uk_sps_month_co_prdt",
                        columnNames = {"dcls_month", "fin_co_no", "fin_prdt_cd"})
        },
        indexes = {
                @Index(name = "idx_sps_latest", columnList = "is_latest"),
                @Index(name = "idx_sps_fin_prdt", columnList = "fin_prdt_cd"),
                @Index(name = "idx_sps_dcls_month", columnList = "dcls_month")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavingProductSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 식별자 계열
    @Column(name = "dcls_month", nullable = false, length = 6)   // YYYYMM
    private String dclsMonth;

    @Column(name = "fin_co_no", nullable = false, length = 20)
    private String finCoNo;

    @Column(name = "fin_prdt_cd", nullable = false, length = 50)
    private String finPrdtCd;

    @Column(name = "kor_co_nm", length = 100)
    private String korCoNm;

    @Column(name = "fin_prdt_nm", length = 200)
    private String finPrdtNm;

    @Column(name = "join_way", length = 1000)
    private String joinWay;

    @Column(name = "mtrt_int", columnDefinition = "TEXT")
    private String mtrtInt;

    @Column(name = "spcl_cnd", columnDefinition = "TEXT")
    private String spclCnd;

    @Column(name = "join_deny", length = 10)
    private String joinDeny;

    @Column(name = "join_member", length = 1000)
    private String joinMember;

    @Column(name = "etc_note", columnDefinition = "TEXT")
    private String etcNote;

    @Column(name = "max_limit")
    private Integer maxLimit;

    @Column(name = "dcls_strt_day", length = 8)
    private String dclsStrtDay;

    @Column(name = "dcls_end_day", length = 8)
    private String dclsEndDay;

    @Column(name = "fin_co_subm_day", length = 12) // YYYYMMDDHHMI
    private String finCoSubmDay;

    // 관리 필드
    @Column(name = "is_latest", nullable = false)
    private boolean isLatest;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    // 가입자 수(인기순 정렬용 집계 컬럼)
    @Column(name = "subscriber_count", nullable = false)
    private Long subscriberCount = 0L;

    // 생성 전용 팩토리: 드래프트를 받아 엔티티를 생성
    public static SavingProductSnapshot from(SavingProductSnapshotDraft d, boolean isLatest, LocalDateTime fetchedAt) {
        SavingProductSnapshot e = new SavingProductSnapshot();
        e.dclsMonth = d.getDclsMonth();
        e.finCoNo = d.getFinCoNo();
        e.finPrdtCd = d.getFinPrdtCd();
        e.korCoNm = d.getKorCoNm();
        e.finPrdtNm = d.getFinPrdtNm();
        e.joinWay = d.getJoinWay();
        e.mtrtInt = d.getMtrtInt();
        e.spclCnd = d.getSpclCnd();
        e.joinDeny = d.getJoinDeny();
        e.joinMember = d.getJoinMember();
        e.etcNote = d.getEtcNote();
        e.maxLimit = d.getMaxLimit();
        e.dclsStrtDay = d.getDclsStrtDay();
        e.dclsEndDay = d.getDclsEndDay();
        e.finCoSubmDay = d.getFinCoSubmDay();
        e.isLatest = isLatest;
        e.fetchedAt = fetchedAt;
        return e;
    }
}
