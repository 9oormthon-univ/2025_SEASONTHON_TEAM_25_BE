package com.freedom.news.api;

import com.freedom.common.security.CustomUserPrincipal;
import com.freedom.news.api.response.NewsDetailResponse;
import com.freedom.news.api.response.NewsResponse;
import com.freedom.news.application.NewsQueryAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsQueryAppService newsQueryFacade;

    @GetMapping
    public ResponseEntity<Page<NewsResponse>> getNewsList(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Page<NewsResponse> newsList = newsQueryFacade.getRecentNewsList(page, size);
        return ResponseEntity.ok(newsList);
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<NewsDetailResponse> getNewsDetail(@PathVariable Long newsId,
                                                            @AuthenticationPrincipal CustomUserPrincipal principal) {
        NewsDetailResponse newsDetail = newsQueryFacade.getNewsDetail(newsId, principal.getId());
        return ResponseEntity.ok(newsDetail);
    }
}
