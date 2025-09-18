package com.freedom.scrap.domain.service;

import com.freedom.common.logging.Loggable;
import com.freedom.scrap.application.dto.NewsScrapDto;
import com.freedom.scrap.domain.entity.NewsScrap;
import com.freedom.scrap.infra.NewsScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindNewsScrapService {
    
    private final NewsScrapRepository newsScrapRepository;
    
    @Loggable("사용자 뉴스 스크랩 목록 조회")
    public Page<NewsScrap> findNewsScrapsByUserId(Long userId, Pageable pageable) {
        return newsScrapRepository.findByUserIdOrderByScrappedDateDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public NewsScrapDto getNewsScrapById(Long newsId, Long userId) {
        NewsScrap newsScrap = newsScrapRepository.findByUserIdAndNewsArticleId(userId, newsId).orElse(null);
        return newsScrap != null ? NewsScrapDto.from(newsScrap) : null;
    }
}
