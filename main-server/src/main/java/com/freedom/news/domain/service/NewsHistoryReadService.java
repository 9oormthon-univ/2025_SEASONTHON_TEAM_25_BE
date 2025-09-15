package com.freedom.news.domain.service;

import com.freedom.news.infra.repository.NewsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsHistoryReadService {

    private final NewsHistoryRepository newsHistoryRepository;

    @Transactional(readOnly = true)
    public int countNewsRead(Long userId) {
        // 전체 기간 뉴스 읽기 개수 조회
        return newsHistoryRepository.countByUserId(userId);
    }
}
