package com.freedom.news.application.facade;

import com.freedom.news.api.response.AdminNewsDetailResponse;
import com.freedom.news.api.response.AdminNewsResponse;
import com.freedom.news.application.dto.AdminNewsDto;
import com.freedom.news.application.dto.AdminNewsDetailDto;
import com.freedom.news.domain.service.AdminNewsQueryService;
import com.freedom.news.domain.service.AdminNewsCommandService;
import com.freedom.news.domain.service.NewsSyncService;
import com.freedom.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNewsAppService {

    private final AdminNewsQueryService adminNewsQueryService;
    private final AdminNewsCommandService adminNewsCommandService;
    private final NewsSyncService newsSyncService;

    public PageResponse<AdminNewsResponse> getNewsList(Pageable pageable) {
        Page<AdminNewsDto> newsPage = adminNewsQueryService.getNewsList(pageable);
        
        Page<AdminNewsResponse> responsePage = newsPage.map(AdminNewsResponse::from);
        
        return PageResponse.of(responsePage);
    }

    public AdminNewsDetailResponse getNewsDetail(Long newsId) {
        AdminNewsDetailDto dto = adminNewsQueryService.getNewsDetail(newsId);
        return AdminNewsDetailResponse.from(dto);
    }

    @Transactional
    public void deleteNews(Long newsId) {
        adminNewsCommandService.deleteNews(newsId);
    }

    @Transactional
    public void syncLatestNews() {
        newsSyncService.syncLatestNews();
    }
}
