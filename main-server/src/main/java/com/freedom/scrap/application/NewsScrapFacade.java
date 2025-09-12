package com.freedom.scrap.application;

import com.freedom.auth.domain.User;
import com.freedom.auth.domain.service.FindUserService;
import com.freedom.common.dto.PageResponse;
import com.freedom.common.logging.Loggable;
import com.freedom.news.application.dto.NewsDetailDto;
import com.freedom.news.domain.service.FindNewsService;
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
public class NewsScrapFacade {
    
    private final NewsScrapToggleService newsScrapToggleService;
    private final FindNewsScrapService findNewsScrapService;
    private final FindNewsService findNewsService;
    private final FindUserService findUserService;

    @Loggable("뉴스 스크랩 토글")
    @Transactional
    public NewsScrapToggleResult toggleNewsScrap(Long userId, Long newsArticleId) {
        User user = findUserService.findById(userId);
        NewsDetailDto newsArticle = findNewsService.findNewsById(newsArticleId);
        boolean isScraped = newsScrapToggleService.toggleNewsScrap(user, newsArticle.getId());
        return NewsScrapToggleResult.of(newsArticleId, isScraped);
    }
    
    @Loggable("사용자 뉴스 스크랩 목록 조회")
    public PageResponse<NewsScrapDto> getNewsScrapList(Long userId, Pageable pageable) {
        Page<NewsScrap> newsScrapPage = findNewsScrapService.findNewsScrapsByUserId(userId, pageable);
        Page<NewsScrapDto> newsScrapDtoPage = newsScrapPage.map(NewsScrapDto::from);
        return PageResponse.of(newsScrapDtoPage);
    }
}
