package com.freedom.scrap.application;

import com.freedom.auth.domain.User;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.common.dto.PageResponse;
import com.freedom.common.exception.custom.NewsNotFoundException;
import com.freedom.common.logging.Loggable;
import com.freedom.news.domain.entity.NewsArticle;
import com.freedom.news.infra.repository.NewsArticleRepository;
import com.freedom.scrap.application.dto.NewsScrapDto;
import com.freedom.scrap.application.dto.NewsScrapToggleResult;
import com.freedom.scrap.domain.entity.NewsScrap;
import com.freedom.scrap.domain.service.FindNewsScrapService;
import com.freedom.scrap.domain.service.NewsScrapToggleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScrapFacade {
    
    private final NewsScrapToggleService newsScrapToggleService;
    private final FindNewsScrapService findNewsScrapService;
    private final UserJpaRepository userRepository;
    private final NewsArticleRepository newsArticleRepository;
    
    @Loggable("뉴스 스크랩 토글")
    @Transactional
    public NewsScrapToggleResult toggleNewsScrap(Long userId, Long newsArticleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId: " + userId));
        
        NewsArticle newsArticle = newsArticleRepository.findById(newsArticleId)
            .orElseThrow(() -> new NewsNotFoundException("뉴스를 찾을 수 없습니다. newsArticleId: " + newsArticleId));
        
        boolean isScraped = newsScrapToggleService.toggleNewsScrap(user, newsArticle);
        
        return NewsScrapToggleResult.of(newsArticleId, isScraped);
    }
    
    @Loggable("사용자 뉴스 스크랩 목록 조회")
    public PageResponse<NewsScrapDto> getNewsScrapList(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다. userId: " + userId);
        }
        Page<NewsScrap> newsScrapPage = findNewsScrapService.findNewsScrapsByUserId(userId, pageable);
        Page<NewsScrapDto> newsScrapDtoPage = newsScrapPage.map(NewsScrapDto::from);
        return PageResponse.of(newsScrapDtoPage);
    }
}
