package com.freedom.saving.infra.snapshot;

import com.freedom.saving.domain.SavingProductSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 목적
 * - 최신 스냅샷(isLatest=true) 조회
 * - 중복 방지 확인(공시월/회사/상품코드)
 * - 최신 플래그 해제 토글 쿼리
 *
 * 설계 메모
 * - 최신 토글은 "회사+상품코드" 단위로 1건만 유지하도록 함
 * - 실제 토글 순서는 서비스에서: (1) clearLatest → (2) 신규 저장(isLatest=true)
 */
@Repository
public interface SavingProductSnapshotJpaRepository extends JpaRepository<SavingProductSnapshot, Long> {

    // 최신 스냅샷 1건 조회
    Optional<SavingProductSnapshot> findTop1ByFinCoNoAndFinPrdtCdAndIsLatestTrue(String finCoNo, String finPrdtCd);

    // 중복 여부(유니크 키와 동일 컬럼 조합) 확인
    boolean existsByDclsMonthAndFinCoNoAndFinPrdtCd(String dclsMonth, String finCoNo, String finPrdtCd);

    // 특정 공시월 전부(운영 점검/관리용)
    List<SavingProductSnapshot> findAllByDclsMonth(String dclsMonth);

    // 최신 플래그 해제(회사+상품코드 범위에서 isLatest=true -> false)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update SavingProductSnapshot s " +
            "set s.isLatest = false " +
            "where s.finCoNo = :finCoNo and s.finPrdtCd = :finPrdtCd and s.isLatest = true")
    int clearLatest(@Param("finCoNo") String finCoNo, @Param("finPrdtCd") String finPrdtCd);

    /**
     * 최신 스냅샷만 페이지 조회
     * 정렬은 Pageable의 Sort를 따른다
     */
    Page<SavingProductSnapshot> findByIsLatestTrue(Pageable pageable);

    /**
     * 최신 스냅샷 전체 조회 - 가입자수 내림차순(없으면 0)
     */
    @Query("select s from SavingProductSnapshot s where s.isLatest = true order by coalesce(s.subscriberCount, 0) desc")
    List<SavingProductSnapshot> findAllLatestOrderBySubscriberCountDesc();
}
