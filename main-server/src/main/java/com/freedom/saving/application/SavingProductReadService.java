package com.freedom.saving.application;

import com.freedom.common.exception.custom.SavingProductNotFoundException;
import com.freedom.saving.application.read.SavingProductDetail;
import com.freedom.saving.application.read.SavingProductListItem;
import com.freedom.saving.application.read.SavingProductOptionItem;
import com.freedom.saving.domain.SavingProductOptionSnapshot;
import com.freedom.saving.domain.SavingProductSnapshot;
import com.freedom.saving.infra.snapshot.SavingProductOptionSnapshotJpaRepository;
import com.freedom.saving.infra.snapshot.SavingProductSnapshotJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 읽기 전용 유스케이스: 상품 목록/상세 조회
 * 도메인 엔티티를 외부로 직접 노출하지 않고, application DTO(SavingProductListItem/Detail)로 매핑해 반환
 */
@Service
@Transactional(readOnly = true)
public class SavingProductReadService {

    private final SavingProductSnapshotJpaRepository productRepo;
    private final SavingProductOptionSnapshotJpaRepository optionRepo;

    public SavingProductReadService(SavingProductSnapshotJpaRepository productRepo,
                                    SavingProductOptionSnapshotJpaRepository optionRepo) {
        this.productRepo = productRepo;
        this.optionRepo = optionRepo;
    }

    /**
     * [목록] 인기순 적금 상품 조회
     *
     * 정렬 기준: 최신 스냅샷 중 subscriberCount 내림차순
     * 프론트 요구에 따라 전체 리스트를 반환한다.
     */
    public Page<SavingProductListItem> getPopularSavingProducts(int page, int size) {
        // 1) 최신 스냅샷 전체 조회(가입자수 내림차순)
        List<SavingProductSnapshot> contents = productRepo.findAllLatestOrderBySubscriberCountDesc();

        // 2) 엔티티 -> 목록 DTO
        List<SavingProductListItem> items = new ArrayList<SavingProductListItem>();
        for (SavingProductSnapshot s : contents) {
            SavingProductListItem item = new SavingProductListItem();
            item.setProductSnapshotId(s.getId());
            item.setProductName(s.getFinPrdtNm());
            item.setBankName(s.getKorCoNm());

            // DTO에 해당 필드가 아직 존재한다면 null/빈값으로 둔다.
            item.setAiSummary(s.getAiSummary() != null ? s.getAiSummary() : "");

            items.add(item);
        }

        // 3) Page 래핑 반환 (전체)
        return new PageImpl<SavingProductListItem>(items, PageRequest.of(0, items.isEmpty() ? 1 : items.size()), items.size());
    }

    /**
     * [상세] 상품 1건 상세 조회(헤더 + 옵션 목록)
     */
    public SavingProductDetail getDetail(Long productSnapshotId) {
        // 1) 스냅샷 단건 조회
        Optional<SavingProductSnapshot> optional = productRepo.findById(productSnapshotId);
        if (!optional.isPresent()) {
            throw new SavingProductNotFoundException(productSnapshotId);
        }
        SavingProductSnapshot s = optional.get();

        // 2) 옵션 목록 조회
        List<SavingProductOptionSnapshot> options =
                optionRepo.findByProductSnapshotIdOrderBySaveTrmMonthsAsc(productSnapshotId);

        // 3) 엔티티 -> 상세 DTO 매핑
        SavingProductDetail detail = new SavingProductDetail();
        detail.setProductSnapshotId(s.getId());
        detail.setProductName(s.getFinPrdtNm());
        detail.setBankName(s.getKorCoNm());
        detail.setJoinWay(s.getJoinWay());
        detail.setMaturityInterest(s.getMtrtInt()); // 만기 후 이자율
        detail.setSpecialCondition(s.getSpclCnd());
        detail.setJoinDeny(s.getJoinDeny());
        detail.setJoinMember(s.getJoinMember());
        detail.setMaxLimit(s.getMaxLimit()); // 최고한도
        detail.setEtcNote(s.getEtcNote());
        detail.setFetchedAt(s.getFetchedAt());
        detail.setAiSummary(s.getAiSummary() != null ? s.getAiSummary() : "");

        // 상품 정보 자세히 보기 섹션용 정보 설정
        detail.setFinancialCompanyName(s.getKorCoNm());
        detail.setFinancialProductName(s.getFinPrdtNm());
        detail.setMaturityInterestRate(s.getMtrtInt());
        detail.setPreferentialConditions(s.getSpclCnd());
        detail.setJoinRestrictions(s.getJoinDeny());
        detail.setJoinTarget(s.getJoinMember());
        detail.setMaxLimitFormatted(formatAmount(s.getMaxLimit()));

        List<SavingProductOptionItem> optionItems = new ArrayList<SavingProductOptionItem>();
        List<Integer> availableTerms = new ArrayList<>();
        List<BigDecimal> savingsRates = new ArrayList<>();
        List<BigDecimal> maxPreferentialRates = new ArrayList<>();
        
        BigDecimal minRate = null;
        BigDecimal maxRate = null;
        
        for (SavingProductOptionSnapshot o : options) {
            SavingProductOptionItem oi = new SavingProductOptionItem();
            oi.setTermMonths(o.getSaveTrmMonths());
            oi.setRate(o.getIntrRate());
            oi.setRatePreferential(o.getIntrRate2());
            oi.setRateType(o.getIntrRateType());
            oi.setRateTypeName(o.getIntrRateTypeNm());

            optionItems.add(oi);
            
            // 히어로 섹션용 정보 수집
            if (o.getSaveTrmMonths() != null) {
                availableTerms.add(o.getSaveTrmMonths());
            }
            if (o.getIntrRate() != null) {
                savingsRates.add(o.getIntrRate().setScale(2, java.math.RoundingMode.HALF_UP));
                if (minRate == null || o.getIntrRate().compareTo(minRate) < 0) {
                    minRate = o.getIntrRate();
                }
                if (maxRate == null || o.getIntrRate().compareTo(maxRate) > 0) {
                    maxRate = o.getIntrRate();
                }
            }
            if (o.getIntrRate2() != null) {
                maxPreferentialRates.add(o.getIntrRate2().setScale(2, java.math.RoundingMode.HALF_UP));
            }
        }
        
        detail.setOptions(optionItems);
        detail.setAvailableTermMonths(availableTerms);
        detail.setSavingsRates(savingsRates);
        detail.setMaxPreferentialRates(maxPreferentialRates);
        
        // 히어로 섹션용 정보 설정
        detail.setInterestRateRange(formatInterestRateRange(minRate, maxRate));
        detail.setAvailableTerms(formatAvailableTerms(availableTerms));
        detail.setMaxMonthlyAmount(formatAmount(s.getMaxLimit()));
        
        // 옵션에서 첫 번째 항목의 정보를 상세 정보로 설정
        if (!options.isEmpty()) {
            SavingProductOptionSnapshot firstOption = options.get(0);
            detail.setSavingsRateType(firstOption.getIntrRateType());
            detail.setSavingsRateTypeName(firstOption.getIntrRateTypeNm());
            // 적립 유형은 기본적으로 "정액적립"으로 설정 (실제 데이터에 따라 수정 필요)
            detail.setDepositType("정액적립");
            detail.setDepositTypeName("정액적립");
        }

        return detail;
    }
    
    /**
     * 금액 포맷팅 (천 단위 콤마)
     */
    private String formatAmount(Integer amount) {
        if (amount == null) return "제한없음";
        return String.format("%,d원", amount);
    }
    
    /**
     * 금리 범위 포맷팅
     */
    private String formatInterestRateRange(BigDecimal minRate, BigDecimal maxRate) {
        if (minRate == null || maxRate == null) return "금리 정보 없음";
        if (minRate.equals(maxRate)) {
            return String.format("연 %.2f%%", minRate);
        }
        return String.format("연 %.2f%%~%.2f%%", minRate, maxRate);
    }
    
    /**
     * 가능한 기간 포맷팅
     */
    private String formatAvailableTerms(List<Integer> terms) {
        if (terms.isEmpty()) return "기간 정보 없음";
        return terms.stream()
                .sorted()
                .map(term -> term + "개월")
                .reduce((a, b) -> a + "/" + b)
                .orElse("기간 정보 없음") + " 중 선택";
    }
}
