package com.freedom.saving.application.policy;

import com.freedom.common.time.TimeProvider;
import com.freedom.saving.domain.SavingProductOptionSnapshot;
import com.freedom.saving.domain.SavingProductSnapshot;
import com.freedom.saving.domain.shapshot.SavingProductOptionSnapshotDraft;
import com.freedom.saving.domain.shapshot.SavingProductSnapshotDraft;
import com.freedom.saving.infra.fss.FssSavingApiClient;
import com.freedom.saving.infra.fss.FssSavingMapper;
import com.freedom.saving.infra.fss.FssSavingResponseDto;
import com.freedom.saving.infra.snapshot.SavingProductOptionSnapshotJpaRepository;
import com.freedom.saving.infra.snapshot.SavingProductSnapshotJpaRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *  금감원(FSS) 적금 상품 스냅샷 동기화 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductSnapshotSyncService {

    private static final String GROUP_BANK = "020000"; // 은행만 수집

    private final FssSavingApiClient fssClient;
    private final FssSavingMapper mapper;
    private final SavingProductSnapshotJpaRepository productRepository;
    private final SavingProductOptionSnapshotJpaRepository optionRepository;
    private final TimeProvider timeProvider;

    /**
     * 은행(020000) 전체 페이지 수집.
     */
    public SyncResult syncAll() {
        SyncResult bank = syncGroup(GROUP_BANK);

        log.info("[FSS SYNC] banks only done. pages={}, products={}, options={}, skipped={}",
                bank.pages, bank.products, bank.options, bank.skipped);
        return bank;
    }

    public SyncResult syncGroup(String topFinGrpNo) {
        SyncResult acc = new SyncResult();
        int pageNo = 1;

        while (true) {
            FssSavingResponseDto dto = fssClient.fetchSavings(topFinGrpNo, pageNo).block();
            if (dto == null || dto.result == null) {
                log.warn("[FSS SYNC] empty result. group={}, page={}", topFinGrpNo, pageNo);
                break;
            }

            // 에러 코드 처리
            if (dto.result.errCd != null && !"000".equals(dto.result.errCd)) {
                log.warn("[FSS SYNC] error from FSS: group={}, page={}, err_cd={}, err_msg={}",
                        topFinGrpNo, pageNo, dto.result.errCd, dto.result.errMsg);
                break;
            }

            PageResult pr = processOnePage(dto);
            acc.pages += 1;
            acc.products += pr.productsSaved;
            acc.options += pr.optionsSaved;
            acc.skipped += pr.skipped;

            // 페이지 종료 판단
            if (dto.result.maxPageNo == null || dto.result.nowPageNo == null) {
                if (isEmptyPage(dto)) break;
                pageNo += 1;
                continue;
            }
            if (dto.result.nowPageNo >= dto.result.maxPageNo) {
                break;
            }
            pageNo += 1;
        }

        log.info("[FSS SYNC] group {} done. pages={}, products={}, options={}, skipped={}",
                topFinGrpNo, acc.pages, acc.products, acc.options, acc.skipped);
        return acc;
    }

    private boolean isEmptyPage(FssSavingResponseDto dto) {
        boolean baseEmpty = dto.result.baseList == null || dto.result.baseList.isEmpty();
        boolean optEmpty  = dto.result.optionList == null || dto.result.optionList.isEmpty();
        return baseEmpty && optEmpty;
    }

    /**
     * 한 페이지 처리: baseList 로 제품을 저장하고, 매칭되는 optionList 도 함께 저장
     */
    @Transactional
    protected PageResult processOnePage(FssSavingResponseDto dto) {
        PageResult result = new PageResult();
        LocalDateTime fetchedAt = timeProvider.now().toLocalDateTime();

        // 미리 option 드래프트를 전부 변환해두고, 제품별로 매칭
        List<SavingProductOptionSnapshotDraft> allOptionDrafts = mapper.toOptionDrafts(dto);

        List<SavingProductSnapshotDraft> productDrafts = mapper.toProductDrafts(dto);
        for (int i = 0; i < productDrafts.size(); i++) {
            SavingProductSnapshotDraft pd = productDrafts.get(i);

            // 중복 방지: 동일 월/회사/상품코드가 이미 있으면 skip
            boolean exists = productRepository.existsByDclsMonthAndFinCoNoAndFinPrdtCd(
                    pd.getDclsMonth(), pd.getFinCoNo(), pd.getFinPrdtCd());
            if (exists) {
                result.skipped += 1;
                continue;
            }

            // 최신 플래그 해제 -> 신규 저장(최신)
            productRepository.clearLatest(pd.getFinCoNo(), pd.getFinPrdtCd());

            SavingProductSnapshot saved = productRepository.save(
                    SavingProductSnapshot.from(pd, true, fetchedAt));
            result.productsSaved += 1;

            // 해당 제품에 속하는 옵션만 매칭해서 저장
            List<SavingProductOptionSnapshotDraft> matched = matchOptions(allOptionDrafts, pd);
            for (SavingProductOptionSnapshotDraft od : matched) {
                SavingProductOptionSnapshot option = SavingProductOptionSnapshot.from(od, saved.getId());
                optionRepository.save(option);
                result.optionsSaved += 1;
            }
        }
        return result;
    }

    /**
     * base(회사/상품코드/공시월)가 일치하는 옵션만 필터
     */
    private List<SavingProductOptionSnapshotDraft> matchOptions(
            List<SavingProductOptionSnapshotDraft> all,
            SavingProductSnapshotDraft pd) {

        List<SavingProductOptionSnapshotDraft> list = new ArrayList<SavingProductOptionSnapshotDraft>();
        for (SavingProductOptionSnapshotDraft od : all) {
            boolean sameMonth = pd.getDclsMonth().equals(od.getDclsMonth());
            boolean sameCo = pd.getFinCoNo().equals(od.getFinCoNo());
            boolean samePrdt = pd.getFinPrdtCd().equals(od.getFinPrdtCd());
            if (sameMonth && sameCo && samePrdt) {
                list.add(od);
            }
        }
        return list;
    }


    // 전체/그룹 동기화 결과 요약용 dto
    @Getter
    public static class SyncResult {
        private int pages;
        private int products;
        private int options;
        private int skipped;
    }

    // 내부 페이지 처리 결과
    private static class PageResult {
        int productsSaved;
        int optionsSaved;
        int skipped;
    }
}
