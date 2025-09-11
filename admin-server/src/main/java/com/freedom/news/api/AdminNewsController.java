package com.freedom.news.api;

import com.freedom.news.api.response.AdminNewsDetailResponse;
import com.freedom.news.api.response.AdminNewsResponse;
import com.freedom.news.application.facade.AdminNewsAppService;
import com.freedom.common.dto.PageResponse;
import com.freedom.common.logging.Loggable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class AdminNewsController {

    private final AdminNewsAppService adminNewsService;

    @GetMapping
    @Loggable("관리자 뉴스 목록 조회")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AdminNewsResponse>> getNewsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("approveDate").descending());
        PageResponse<AdminNewsResponse> response = adminNewsService.getNewsList(pageable);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{newsId}")
    @Loggable("관리자 뉴스 상세 조회")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminNewsDetailResponse> getNewsDetail(@PathVariable Long newsId) {
        AdminNewsDetailResponse response = adminNewsService.getNewsDetail(newsId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{newsId}")
    @Loggable("관리자 뉴스 삭제")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNews(@PathVariable Long newsId) {
        adminNewsService.deleteNews(newsId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync")
    @Loggable("관리자 뉴스 수동 동기화")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> syncLatestNews() {
        adminNewsService.syncLatestNews();
        return ResponseEntity.ok().build();
    }
}
