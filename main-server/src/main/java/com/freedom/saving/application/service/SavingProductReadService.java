package com.freedom.saving.application.service;

import com.freedom.common.exception.custom.SavingProductNotFoundException;
import com.freedom.saving.util.ProductSortUtil;
import com.freedom.saving.util.SavingProductQueryUtil;
import com.freedom.saving.application.query.SavingProductDetail;
import com.freedom.saving.application.query.SavingProductListItem;
import com.freedom.saving.domain.SavingProductOptionSnapshot;
import com.freedom.saving.domain.SavingProductSnapshot;
import com.freedom.saving.infra.SavingProductOptionSnapshotJpaRepository;
import com.freedom.saving.infra.SavingProductSnapshotJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.freedom.common.exception.custom.SavingExceptions.*;
import static com.freedom.saving.util.JoinDenyConverter.*;

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
     * [목록] 정렬 옵션과 은행사 필터에 따른 적금 상품 조회
     * 정렬 옵션: popular(인기순), name(상품명 가나다순)
     * 은행사 필터: 여러 은행사 선택 가능
     * 프론트 요구에 따라 전체 리스트를 반환한다.
     */
    public Page<SavingProductListItem> getSavingProducts(String sort, List<String> bankNames, int page, int size) {
        // 1) 최신 스냅샷 전체 조회
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

        // 3) 은행사 필터링 적용
        items = applyBankFilter(items, bankNames);

        // 4) 정렬 옵션에 따른 정렬 적용
        applySorting(items, sort);

        // 5) Page 래핑 반환 (전체)
        return new PageImpl<SavingProductListItem>(items, PageRequest.of(0, items.isEmpty() ? 1 : items.size()), items.size());
    }

    /**
     * [목록] 정렬 옵션에 따른 적금 상품 조회
     * 정렬 옵션: popular(인기순), name(상품명 가나다순)
     * 프론트 요구에 따라 전체 리스트를 반환한다.
     */
    public Page<SavingProductListItem> getSavingProducts(String sort, int page, int size) {
        return getSavingProducts(sort, Collections.emptyList(), page, size);
    }

    /**
     * [목록] 인기순 적금 상품 조회
     * 정렬 기준: 최신 스냅샷 중 subscriberCount 내림차순
     * 프론트 요구에 따라 전체 리스트를 반환한다.
     */
    public Page<SavingProductListItem> getPopularSavingProducts(int page, int size) {
        return getSavingProducts("popular", page, size);
    }

    /**
     * [목록] 적금 상품 조회 결과와 은행사명 리스트를 함께 반환
     * @param type 상품 타입 (현재 SAVING만 지원)
     * @param sort 정렬 옵션
     * @param bankNames 은행사 필터
     * @return 상품 목록과 은행사명 리스트가 포함된 Map
     */
    public Map<String, Object> getSavingProductsWithBankNames(String type, String sort, List<String> bankNames) {

        if (!"SAVING".equalsIgnoreCase(type)) {
            throw new SavingPolicyInvalidException("지원하지 않는 type 값입니다. (허용: SAVING)");
        }
        
        // 지원하는 정렬 옵션 검증
        if (!SavingProductQueryUtil.isValidSortOption(sort)) {
            throw new SavingPolicyInvalidException(
                "지원하지 않는 sort 값입니다. (허용: popular, name)");
        }
        
        var products = getSavingProducts(sort, bankNames, 0, Integer.MAX_VALUE);
        var bankNameList = products.getContent().stream()
                .map(SavingProductListItem::getBankName)
                .distinct()
                .sorted()
                .toList();
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", products.getContent());
        result.put("bankNames", bankNameList);
        return result;
    }

    /**
     * 은행사 필터링을 적용
     * @param items 상품 목록
     * @param bankNames 필터링할 은행사명 리스트 (빈 리스트인 경우 필터링하지 않음)
     * @return 필터링된 상품 목록
     */
    private List<SavingProductListItem> applyBankFilter(List<SavingProductListItem> items, List<String> bankNames) {
        if (bankNames == null || bankNames.isEmpty()) {
            return items;
        }
        
        return items.stream()
                .filter(item -> bankNames.contains(item.getBankName()))
                .collect(Collectors.toList());
    }

    /**
     * 정렬 옵션에 따라 리스트를 정렬
     */
    private void applySorting(List<SavingProductListItem> items, String sort) {
        switch (sort.toLowerCase()) {
            case "name":
                ProductSortUtil.sortByProductName(items, SavingProductListItem::getProductName);
                break;
            case "popular":
            default:
                // 기본값은 인기순 (이미 DB에서 정렬되어 있음)
                break;
        }
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
        detail.setProductName(s.getFinPrdtNm());
        detail.setBankName(s.getKorCoNm());
        detail.setMaturityInterest(s.getMtrtInt()); // 만기 후 이자율
        detail.setSpecialCondition(s.getSpclCnd()); // 우대조건
        detail.setJoinDeny(convertJoinDeny(s.getJoinDeny())); // 가입제한
        detail.setJoinMember(s.getJoinMember()); // 가입대상
        detail.setMaxLimit(s.getMaxLimit()); // 최고한도

        // 모든 옵션의 정보를 수집
        if (!options.isEmpty()) {
            SavingProductOptionSnapshot firstOption = options.get(0);
            detail.setIntrRateType(firstOption.getIntrRateType()); // 저축 금리 유형
            detail.setIntrRateTypeNm(firstOption.getIntrRateTypeNm()); // 저축 금리 유형명
            detail.setRsrvType("정액적립"); // 적립 유형 (기본값)
            detail.setRsrvTypeNm("정액적립"); // 적립 유형명 (기본값)
            
            // 모든 옵션의 기간, 금리 정보 수집
            List<Integer> saveTrmList = new ArrayList<>();
            List<BigDecimal> intrRateList = new ArrayList<>();
            List<BigDecimal> intrRate2List = new ArrayList<>();
            
            for (SavingProductOptionSnapshot option : options) {
                if (option.getSaveTrmMonths() != null) {
                    saveTrmList.add(option.getSaveTrmMonths());
                }
                if (option.getIntrRate() != null) {
                    intrRateList.add(option.getIntrRate());
                }
                if (option.getIntrRate2() != null) {
                    intrRate2List.add(option.getIntrRate2());
                }
            }
            
            detail.setSaveTrm(saveTrmList); // 저축 기간 목록
            detail.setIntrRate(intrRateList); // 저축 금리 목록
            detail.setIntrRate2(intrRate2List); // 최고 우대금리 목록
        }

        return detail;
    }
}
