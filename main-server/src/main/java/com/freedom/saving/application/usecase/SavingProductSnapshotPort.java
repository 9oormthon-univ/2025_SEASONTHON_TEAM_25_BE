package com.freedom.saving.application.usecase;

import java.util.List;

/**
 * 스냅샷/옵션 조회 전용 포트(Out Port)
 * - infra에서 JPA를 이용해 구현
 * - 애플리케이션 서비스는 본 추상화를 통해서만 스냅샷/옵션을 조회한다
 */
public interface SavingProductSnapshotPort {

    /** 상품 스냅샷 존재 여부 */
    boolean existsSnapshot(Long productSnapshotId);

    /** 스냅샷 기준 지원 기간(개월) 목록 */
    List<Integer> getSupportedTermMonths(Long productSnapshotId);

    /** (방어용) 기간이 실제 존재하는지 */
    boolean existsOption(Long productSnapshotId, int termMonths);

    /** 가입 발생 시 인기 집계 증가 */
    void incrementSubscriberCount(Long productSnapshotId);

    /** 상품의 최고한도 조회 */
    Integer getMaxLimit(Long productSnapshotId);
}
