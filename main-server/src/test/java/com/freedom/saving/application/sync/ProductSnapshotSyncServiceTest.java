package com.freedom.saving.application.sync;

import com.freedom.common.time.TimeProvider;
import com.freedom.saving.application.policy.ProductSnapshotSyncService;
import com.freedom.saving.domain.SavingProductOptionSnapshot;
import com.freedom.saving.domain.SavingProductSnapshot;
import com.freedom.saving.domain.shapshot.SavingProductSnapshotDraft;
import com.freedom.saving.infra.fss.FssSavingApiClient;
import com.freedom.saving.infra.fss.FssSavingMapper;
import com.freedom.saving.infra.fss.FssSavingResponseDto;
import com.freedom.saving.infra.snapshot.SavingProductOptionSnapshotJpaRepository;
import com.freedom.saving.infra.snapshot.SavingProductSnapshotJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.*;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSnapshotSyncServiceTest {

    @Mock private FssSavingApiClient fssClient;
    // 매퍼는 실제 객체 사용(스키마 변환 검증)
    private FssSavingMapper mapper;

    @Mock private SavingProductSnapshotJpaRepository productRepo;
    @Mock private SavingProductOptionSnapshotJpaRepository optionRepo;

    private TimeProvider timeProvider;

    private ProductSnapshotSyncService service;

    @BeforeEach
    void setUp() {
        mapper = new FssSavingMapper();

        ZoneId zone = ZoneId.of("Asia/Seoul");
        Clock fixed = Clock.fixed(LocalDate.of(2025, 9, 1).atStartOfDay(zone).toInstant(), zone);
        timeProvider = new FixedTimeProvider(fixed);

        service = new ProductSnapshotSyncService(
                fssClient, mapper, productRepo, optionRepo, timeProvider
        );
    }

    // --- helper TimeProvider (명시적 구현) ---
    private static class FixedTimeProvider implements TimeProvider {
        private final Clock clock;
        FixedTimeProvider(Clock clock) { this.clock = clock; }
        @Override public Instant instant() { return clock.instant(); }
        @Override public ZonedDateTime now() { return ZonedDateTime.now(clock); }
        @Override public LocalDate today() { return LocalDate.now(clock); }
        @Override public ZoneId zoneId() { return clock.getZone(); }
    }

    // --- 공통 DTO 생성 유틸 (가독성 위해 명시적 작성) ---
    private FssSavingResponseDto pageDto(String dclsMonth, String finCoNo, String finPrdtCd,
                                         int nowPage, int maxPage, int optionCount) {
        FssSavingResponseDto dto = new FssSavingResponseDto();
        FssSavingResponseDto.Result r = new FssSavingResponseDto.Result();
        r.prdtDiv = "S";
        r.errCd = "000";
        r.nowPageNo = nowPage;
        r.maxPageNo = maxPage;

        FssSavingResponseDto.Base base = new FssSavingResponseDto.Base();
        base.dclsMonth = dclsMonth;
        base.finCoNo = finCoNo;
        base.finPrdtCd = finPrdtCd;
        base.korCoNm = "은행A";
        base.finPrdtNm = "적금A";
        base.joinWay = "인터넷";
        base.mtrtInt = "만기 후 연 1%";
        base.spclCnd = "우대 없음";
        base.joinDeny = "1";
        base.joinMember = "제한 없음";
        base.etcNote = "";
        base.maxLimit = 50000000;
        base.dclsStrtDay = "20250101";
        base.dclsEndDay = "20251231";
        base.finCoSubmDay = "202501021030";

        r.baseList = new java.util.ArrayList<FssSavingResponseDto.Base>();
        r.baseList.add(base);

        r.optionList = new java.util.ArrayList<FssSavingResponseDto.Option>();
        for (int i = 0; i < optionCount; i++) {
            FssSavingResponseDto.Option opt = new FssSavingResponseDto.Option();
            opt.dclsMonth = dclsMonth;
            opt.finCoNo = finCoNo;
            opt.finPrdtCd = finPrdtCd;
            opt.intrRateType = "S";
            opt.intrRateTypeNm = "단리";
            opt.saveTrm = String.valueOf(12 + i);
            opt.intrRate = 3.2 + (i * 0.1);
            opt.intrRate2 = 3.5 + (i * 0.1);
            r.optionList.add(opt);
        }

        dto.result = r;
        return dto;
    }

    // --- 엔티티 id 리플렉션 세팅(저장 결과를 흉내내기 위해 필요) ---
    private void setId(Object entity, Long id) {
        try {
            Field f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("given 유효한 1페이지 when syncGroup then 제품/옵션 저장 및 최신 플래그 토글")
    void given_valid_page_when_syncGroup_then_save_and_toggle() {
        // given
        FssSavingResponseDto page1 = pageDto("202509", "001", "PRD001", 1, 1, 2);
        when(fssClient.fetchSavings("020000", 1)).thenReturn(Mono.just(page1));

        // 중복 없음
        when(productRepo.existsByDclsMonthAndFinCoNoAndFinPrdtCd("202509", "001", "PRD001"))
                .thenReturn(false);

        // clearLatest는 0 or 1 반환(검증은 호출 여부로 충분)
        when(productRepo.clearLatest("001", "PRD001")).thenReturn(1);

        // save 반환 엔티티(id 필요)
        SavingProductSnapshot saved = SavingProductSnapshot.from(
                new SavingProductSnapshotDraft("202509", "001", "PRD001",
                        "은행A", "적금A", "인터넷", "만기후1%", "우대없음",
                        "1", "제한없음", "", 50000000, "20250101", "20251231", "202501021030"),
                true,
                LocalDateTime.of(2025, 9, 1, 0, 0));
        setId(saved, 100L);
        when(productRepo.save(any(SavingProductSnapshot.class))).thenReturn(saved);

        // when
        ProductSnapshotSyncService.SyncResult result = service.syncGroup("020000");

        // then
        assertEquals(1, result.getPages());
        assertEquals(1, result.getProducts());
        assertEquals(2, result.getOptions());
        assertEquals(0, result.getSkipped());

        verify(productRepo, times(1)).clearLatest("001", "PRD001");
        verify(productRepo, times(1)).save(any(SavingProductSnapshot.class));
        verify(optionRepo, times(2)).save(any(SavingProductOptionSnapshot.class));
    }

    @Test
    @DisplayName("given 중복 제품 when syncGroup then 스킵되고 저장/토글되지 않는다")
    void given_duplicate_when_syncGroup_then_skip() {
        // given
        FssSavingResponseDto page1 = pageDto("202509", "001", "PRD001", 1, 1, 2);
        when(fssClient.fetchSavings("020000", 1)).thenReturn(Mono.just(page1));

        when(productRepo.existsByDclsMonthAndFinCoNoAndFinPrdtCd("202509", "001", "PRD001"))
                .thenReturn(true); // 이미 존재 → 스킵

        // when
        ProductSnapshotSyncService.SyncResult result = service.syncGroup("020000");

        // then
        assertEquals(1, result.getPages());
        assertEquals(0, result.getProducts()); // 저장 안 됨
        assertEquals(0, result.getOptions());
        assertEquals(1, result.getSkipped());

        verify(productRepo, never()).clearLatest(anyString(), anyString());
        verify(productRepo, never()).save(any(SavingProductSnapshot.class));
        verify(optionRepo, never()).save(any(SavingProductOptionSnapshot.class));
    }

    @Test
    @DisplayName("given 서비스 전체 수집 when syncAll then 은행(020000)만 호출된다")
    void given_syncAll_then_banks_only() {
        // given: 1페이지로 종료
        FssSavingResponseDto page1 = pageDto("202509", "001", "PRD001", 1, 1, 1);
        when(fssClient.fetchSavings("020000", 1)).thenReturn(Mono.just(page1));

        // 저장 반환 엔티티 id 준비
        SavingProductSnapshot saved = SavingProductSnapshot.from(
                new SavingProductSnapshotDraft("202509", "001", "PRD001",
                        "은행A", "적금A", "인터넷", "만기후1%", "우대없음",
                        "1", "제한없음", "", 50000000, "20250101", "20251231", "202501021030"),
                true,
                LocalDateTime.of(2025, 9, 1, 0, 0));
        setId(saved, 200L);
        when(productRepo.existsByDclsMonthAndFinCoNoAndFinPrdtCd("202509", "001", "PRD001"))
                .thenReturn(false);
        when(productRepo.save(any(SavingProductSnapshot.class))).thenReturn(saved);

        // when
        ProductSnapshotSyncService.SyncResult r = service.syncAll();

        // then
        assertNotNull(r);
        verify(fssClient, atLeastOnce()).fetchSavings(eq("020000"), anyInt());
    }
}
