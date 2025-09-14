package com.freedom.scrap.infra;

import com.freedom.scrap.domain.entity.ScrapHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ScrapHistoryRepository extends JpaRepository<ScrapHistory, Long> {

    ScrapHistory findByUserIdAndScrapIdAndType(Long userId, Long scrapId, ScrapHistory.ScrapType scrapType);

    int countByUserIdAndTypeAndScrapAtBetween(Long userId, ScrapHistory.ScrapType scrapType, LocalDateTime start, LocalDateTime end);
}
