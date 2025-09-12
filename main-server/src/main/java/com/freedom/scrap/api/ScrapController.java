package com.freedom.scrap.api;

import com.freedom.common.dto.PageResponse;
import com.freedom.common.logging.Loggable;
import com.freedom.common.security.CustomUserPrincipal;
import com.freedom.scrap.api.request.QuizScrapRequest;
import com.freedom.scrap.application.QuizScrapFacade;
import com.freedom.scrap.application.NewsScrapFacade;
import com.freedom.scrap.application.dto.NewsScrapDto;
import com.freedom.scrap.application.dto.NewsScrapToggleResult;
import com.freedom.scrap.application.dto.QuizScrapDto;
import com.freedom.scrap.application.dto.QuizScrapToggleResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scrap")
@RequiredArgsConstructor
public class ScrapController {
    
    private final NewsScrapFacade scrapFacade;
    private final QuizScrapFacade quizScrapFacade;

    @Loggable("뉴스 스크랩 API")
    @PostMapping("/news/{newsArticleId}")
    public ResponseEntity<NewsScrapToggleResult> toggleNewsScrap(
            @PathVariable Long newsArticleId,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        
        NewsScrapToggleResult result = scrapFacade.toggleNewsScrap(
                userPrincipal.getId(), newsArticleId);
        
        return ResponseEntity.ok(result);
    }

    @Loggable("뉴스 스크랩 목록 조회 API")
    @GetMapping("/news")
    public ResponseEntity<PageResponse<NewsScrapDto>> getNewsScrapList(
            @PageableDefault(size = 20, sort = "scrappedDate", direction = Sort.Direction.DESC) 
            Pageable pageable,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        
        PageResponse<NewsScrapDto> response = scrapFacade.getNewsScrapList(
                userPrincipal.getId(), pageable);
        
        return ResponseEntity.ok(response);
    }
    
    @Loggable("퀴즈 스크랩 API")
    @PostMapping("/quiz")
    public ResponseEntity<QuizScrapToggleResult> toggleQuizScrap(
            @Valid @RequestBody QuizScrapRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        
        QuizScrapToggleResult result = quizScrapFacade.toggleQuizScrap(
                userPrincipal.getId(), 
                request.getUserQuizId(), 
                request.getIsCorrect());
        
        return ResponseEntity.ok(result);
    }
    
    @Loggable("퀴즈 스크랩 목록 조회 API")
    @GetMapping("/quiz")
    public ResponseEntity<PageResponse<QuizScrapDto>> getQuizScrapList(
            @PageableDefault(size = 20, sort = "scrappedDate", direction = Sort.Direction.DESC) 
            Pageable pageable,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        
        PageResponse<QuizScrapDto> response = quizScrapFacade.getQuizScrapList(
                userPrincipal.getId(), pageable);
        
        return ResponseEntity.ok(response);
    }
}
