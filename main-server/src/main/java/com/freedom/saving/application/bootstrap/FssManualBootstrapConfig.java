package com.freedom.saving.application.bootstrap;

import com.freedom.common.time.TimeProvider;
import com.freedom.saving.domain.SavingProductOptionSnapshot;
import com.freedom.saving.domain.SavingProductSnapshot;
import com.freedom.saving.domain.shapshot.SavingProductOptionSnapshotDraft;
import com.freedom.saving.domain.shapshot.SavingProductSnapshotDraft;
import com.freedom.saving.infra.fss.FssSavingApiClient;
import com.freedom.saving.infra.fss.FssSavingResponseDto;
import com.freedom.saving.infra.snapshot.SavingProductOptionSnapshotJpaRepository;
import com.freedom.saving.infra.snapshot.SavingProductSnapshotJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * 로컬/개발에서 FSS(020000: 은행권) 적금 상품 스냅샷 1회 적재 Runner
 * - app.fss.bootstrap-enabled=true 일 때만 동작
 * - 중복 방지: (dcls_month, fin_co_no, fin_prdt_cd)
 * - 최신 토글: (fin_co_no, fin_prdt_cd) 단위로 is_latest=true 1건 유지
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FssManualBootstrapConfig {

    private final FssSavingApiClient fssClient;
    private final SavingProductSnapshotJpaRepository productRepo;
    private final SavingProductOptionSnapshotJpaRepository optionRepo;
    private final PlatformTransactionManager txManager;
    private final TimeProvider timeProvider;

    @Value("${app.fss.bootstrap-enabled:false}")
    private boolean enabled;

    @Bean
    ApplicationRunner fssBootstrapRunner() {
        return args -> {
            if (!enabled) {
                log.info("[FSS-BOOTSTRAP] disabled. skip.");
                return;
            }

            final String group = "020000"; // 은행권만 수집
            int page = 1;

            log.info("[FSS-BOOTSTRAP] start group={}", group);

            while (true) {
                FssSavingResponseDto dto;
                try {
                    // Runner에서만 block() 사용 (웹 요청 스레드 아님)
                    dto = fssClient.fetchSavings(group, page).block();
                } catch (Exception e) {
                    log.error("[FSS-BOOTSTRAP] fetch error. page={}", page, e);
                    break;
                }

                if (dto == null || dto.result == null) {
                    log.warn("[FSS-BOOTSTRAP] empty result at page={}", page);
                    break;
                }

                Integer nowPage = dto.result.nowPageNo;
                Integer maxPage = dto.result.maxPageNo;
                int baseCnt = dto.result.baseList == null ? 0 : dto.result.baseList.size();
                int optCnt  = dto.result.optionList == null ? 0 : dto.result.optionList.size();
                log.info("[FSS-BOOTSTRAP] page {}/{}: base={}, option={}", nowPage, maxPage, baseCnt, optCnt);

                // 이번 페이지에서 "신규로 저장된" 스냅샷 id를 (finCoNo|finPrdtCd) 키로 보관
                Map<String, Long> newSnapshotIdByKey = new HashMap<String, Long>();

                // 1) baseList → 스냅샷 저장 (중복 방지 + 최신 토글)
                if (dto.result.baseList != null) {
                    for (int i = 0; i < dto.result.baseList.size(); i++) {
                        FssSavingResponseDto.Base b = dto.result.baseList.get(i);
                        if (b == null) continue;
                        if (b.dclsMonth == null || b.finCoNo == null || b.finPrdtCd == null) continue;

                        boolean exists;
                        try {
                            exists = productRepo.existsByDclsMonthAndFinCoNoAndFinPrdtCd(
                                    b.dclsMonth, b.finCoNo, b.finPrdtCd
                            );
                        } catch (Exception ex) {
                            log.error("[FSS-BOOTSTRAP] exists check fail ({},{},{}) idx={}",
                                    b.dclsMonth, b.finCoNo, b.finPrdtCd, i, ex);
                            continue;
                        }
                        if (exists) {
                            // 동일 공시월/회사/상품 스냅샷 이미 존재 → skip
                            log.debug("[FSS-BOOTSTRAP] duplicate skip ({},{},{})", b.dclsMonth, b.finCoNo, b.finPrdtCd);
                            continue;
                        }

                        // Draft(불변 VO) 생성 → 엔티티 정적 팩토리로 변환
                        SavingProductSnapshotDraft draft = new SavingProductSnapshotDraft(
                                b.dclsMonth,
                                b.finCoNo,
                                b.finPrdtCd,
                                b.korCoNm,
                                b.finPrdtNm,
                                b.joinWay,
                                b.mtrtInt,
                                b.spclCnd,
                                b.joinDeny,
                                b.joinMember,
                                b.etcNote,
                                b.maxLimit,
                                b.dclsStrtDay,
                                b.dclsEndDay,
                                b.finCoSubmDay
                        );

                        final String key = buildKey(b.finCoNo, b.finPrdtCd);
                        // TimeProvider(ZonedDateTime) → LocalDateTime
                        final LocalDateTime fetchedAt =
                                (timeProvider == null
                                        ? java.time.ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                                        : timeProvider.now())
                                        .toLocalDateTime();

                        // 트랜잭션: 최신 토글 해제 → from(draft,true,fetchedAt) 저장
                        TransactionTemplate tt = new TransactionTemplate(txManager);
                        Long savedId = tt.execute(new TransactionCallback<Long>() {
                            @Override
                            public Long doInTransaction(TransactionStatus status) {
                                productRepo.clearLatest(b.finCoNo, b.finPrdtCd); // 기존 최신 해제
                                SavingProductSnapshot entity = SavingProductSnapshot.from(draft, true, fetchedAt);
                                SavingProductSnapshot saved = productRepo.save(entity);
                                return saved.getId();
                            }
                        });

                        if (savedId != null) {
                            newSnapshotIdByKey.put(key, savedId);
                        }
                    }
                }

                // 2) optionList → 방금 저장된 스냅샷에만 옵션 저장
                if (dto.result.optionList != null && !newSnapshotIdByKey.isEmpty()) {
                    for (int i = 0; i < dto.result.optionList.size(); i++) {
                        FssSavingResponseDto.Option o = dto.result.optionList.get(i);
                        if (o == null) continue;
                        if (o.finCoNo == null || o.finPrdtCd == null) continue;

                        String key = buildKey(o.finCoNo, o.finPrdtCd);
                        Long snapshotId = newSnapshotIdByKey.get(key);
                        if (snapshotId == null) {
                            // 이번 페이지에서 신규 저장되지 않은 상품의 옵션은 skip (중복 적재 방지)
                            continue;
                        }

                        Integer termMonths = parseIntSafe(o.saveTrm);
                        BigDecimal rate = toBigDecimal(o.intrRate);
                        BigDecimal ratePreferential = toBigDecimal(o.intrRate2);

                        // Draft 생성 → 엔티티 정적 팩토리로 변환
                        SavingProductOptionSnapshotDraft optDraft = new SavingProductOptionSnapshotDraft(
                                o.dclsMonth,
                                o.finCoNo,
                                o.finPrdtCd,
                                o.intrRateType,
                                o.intrRateTypeNm,
                                termMonths,
                                rate,
                                ratePreferential
                        );

                        // 트랜잭션: 옵션 1건 저장
                        TransactionTemplate tt = new TransactionTemplate(txManager);
                        tt.execute(new TransactionCallbackWithoutResult() {
                            @Override
                            protected void doInTransactionWithoutResult(TransactionStatus status) {
                                SavingProductOptionSnapshot opt =
                                        SavingProductOptionSnapshot.from(optDraft, snapshotId);
                                optionRepo.save(opt);
                            }
                        });
                    }
                }

                // 다음 페이지로
                if (maxPage == null || nowPage == null || nowPage.intValue() >= maxPage.intValue()) {
                    break;
                }
                page++;
            }

            log.info("[FSS-BOOTSTRAP] finished group={}", group);
        };
    }

    private String buildKey(String finCoNo, String finPrdtCd) {
        StringBuilder sb = new StringBuilder();
        sb.append(finCoNo == null ? "" : finCoNo);
        sb.append('|');
        sb.append(finPrdtCd == null ? "" : finPrdtCd);
        return sb.toString();
    }

    /**
     * 문자열 숫자 방어적 파싱(FSS saveTrm이 문자열이라 숫자 외 문자가 들어올 가능성 대비)
     */
    private Integer parseIntSafe(String s) {
        if (s == null) return null;
        int len = s.length();
        if (len == 0) return null;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return null;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(Double d) {
        if (d == null) return null;
        return BigDecimal.valueOf(d.doubleValue());
    }
}