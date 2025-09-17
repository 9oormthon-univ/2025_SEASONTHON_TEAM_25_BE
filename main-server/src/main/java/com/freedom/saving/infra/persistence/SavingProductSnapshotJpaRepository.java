package com.freedom.saving.infra.persistence;

import com.freedom.saving.domain.model.SavingProductSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingProductSnapshotJpaRepository extends JpaRepository<SavingProductSnapshot, Long> {

    /**
     * 최신 스냅샷 전체 조회 - 가입자수 내림차순(없으면 0)
     */
    @Query("select s from SavingProductSnapshot s where s.isLatest = true order by coalesce(s.subscriberCount, 0) desc")
    List<SavingProductSnapshot> findAllLatestOrderBySubscriberCountDesc();
}
