package com.freedom.saving.infra;

import com.freedom.saving.domain.SavingProductOptionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 목적
 * - 특정 상품 스냅샷에 귀속된 옵션(기간/금리) 조회
 * - 필요 시 정렬(기간 오름차순)로 노출
 *
 * 설계 메모
 * - @OneToMany를 피했기 때문에 FK(Long) 기반 쿼리로만 조회
 */
@Repository
public interface SavingProductOptionSnapshotJpaRepository extends JpaRepository<SavingProductOptionSnapshot, Long> {

    List<SavingProductOptionSnapshot> findByProductSnapshotIdOrderBySaveTrmMonthsAsc(Long productSnapshotId);

    // 옵션 전량 삭제 쿼리 — 스냅샷 재적재 시 사용 가능
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SavingProductOptionSnapshot o where o.productSnapshotId = :productSnapshotId")
    int deleteAllByProductSnapshotId(@Param("productSnapshotId") Long productSnapshotId);

    @Query("""
           select distinct o.saveTrmMonths
             from SavingProductOptionSnapshot o
            where o.productSnapshotId = :productSnapshotId
              and o.saveTrmMonths is not null
            order by o.saveTrmMonths asc
           """)
    List<Integer> findDistinctTerms(@Param("productSnapshotId") Long productSnapshotId);

    // 추가: 기간 존재 여부
    boolean existsByProductSnapshotIdAndSaveTrmMonths(Long productSnapshotId,
                                                      Integer saveTrmMonths);

    // 기간 일치하는 옵션 중 금리가 가장 높은 하나 선택
    SavingProductOptionSnapshot findFirstByProductSnapshotIdAndSaveTrmMonthsOrderByIntrRateDesc(Long productSnapshotId,
                                                                                                Integer saveTrmMonths);
}
