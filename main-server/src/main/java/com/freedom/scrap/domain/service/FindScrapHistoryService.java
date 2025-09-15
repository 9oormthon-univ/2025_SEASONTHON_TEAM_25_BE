package com.freedom.scrap.domain.service;

import com.freedom.scrap.domain.entity.ScrapHistory;
import com.freedom.scrap.infra.ScrapHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FindScrapHistoryService {

    private final ScrapHistoryRepository scrapHistoryRepository;

    public int findScrapHistoryCountByUserId(Long userId, String category) {
        LocalDate today = LocalDate.now();
        LocalDate thisWeekMonday = today.with(java.time.DayOfWeek.MONDAY);
        LocalDateTime start = thisWeekMonday.atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        if(category.equals("NEWS")) {
            return scrapHistoryRepository.countByUserIdAndTypeAndScrapAtBetween(userId, ScrapHistory.ScrapType.NEWS, start, end);
        }else{
            return scrapHistoryRepository.countByUserIdAndTypeAndScrapAtBetween(userId, ScrapHistory.ScrapType.QUIZ, start, end);
        }
    }

    public int getTotalScrapCountByType(Long userId, ScrapHistory.ScrapType scrapType) {
        return scrapHistoryRepository.countByUserIdAndType(userId, scrapType);
    }
}
