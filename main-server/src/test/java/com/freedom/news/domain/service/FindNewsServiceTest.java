package com.freedom.news.domain.service;

import com.freedom.common.exception.custom.NewsNotFoundException;
import com.freedom.news.application.dto.NewsDetailDto;
import com.freedom.news.application.dto.NewsDto;
import com.freedom.news.domain.entity.NewsArticle;
import com.freedom.news.infra.repository.NewsArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindNewsService 단위 테스트")
class FindNewsServiceTest {

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @InjectMocks
    private FindNewsService findNewsService;

    @Test
    @DisplayName("이번 주 뉴스 조회 성공 - 월요일~일요일 범위")
    void findRecentNews_ThisWeek_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        NewsArticle mockNews = createMockNewsArticle("news001", "테스트 뉴스");
        Page<NewsArticle> mockPage = new PageImpl<>(List.of(mockNews), pageable, 1);

        given(newsArticleRepository.findRecentNewsByApproveDateBetween(
            any(LocalDateTime.class), 
            any(LocalDateTime.class), 
            eq(pageable)
        )).willReturn(mockPage);

        // when
        Page<NewsDto> result = findNewsService.findRecentNews(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 뉴스");
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(newsArticleRepository).findRecentNewsByApproveDateBetween(
            any(LocalDateTime.class), 
            any(LocalDateTime.class), 
            eq(pageable)
        );
    }

    @Test
    @DisplayName("이번 주 뉴스가 없을 때 전주 뉴스로 fallback")
    void findRecentNews_ThisWeekEmpty_FallbackToLastWeek() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        NewsArticle mockNews = createMockNewsArticle("news002", "전주 뉴스");
        Page<NewsArticle> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        Page<NewsArticle> lastWeekPage = new PageImpl<>(List.of(mockNews), pageable, 1);

        // 이번 주 뉴스 조회 시 빈 결과 반환
        given(newsArticleRepository.findRecentNewsByApproveDateBetween(
            any(LocalDateTime.class), 
            any(LocalDateTime.class), 
            eq(pageable)
        )).willReturn(emptyPage)
          .willReturn(lastWeekPage);

        // when
        Page<NewsDto> result = findNewsService.findRecentNews(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("전주 뉴스");
        assertThat(result.getTotalElements()).isEqualTo(1);

        // 두 번 호출되었는지 확인 (이번 주 + 전주)
        verify(newsArticleRepository, times(2)).findRecentNewsByApproveDateBetween(
            any(LocalDateTime.class), 
            any(LocalDateTime.class), 
            eq(pageable)
        );
    }

    @Test
    @DisplayName("뉴스 상세 조회 성공")
    void findNewsById_Success() {
        // given
        Long newsId = 1L;
        NewsArticle mockNews = createMockNewsArticle("news001", "상세 뉴스");
        
        given(newsArticleRepository.findById(newsId))
            .willReturn(Optional.of(mockNews));

        // when
        NewsDetailDto result = findNewsService.findNewsById(newsId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("상세 뉴스");
        
        verify(newsArticleRepository).findById(newsId);
    }

    @Test
    @DisplayName("뉴스 상세 조회 실패 - 존재하지 않는 ID")
    void findNewsById_NotFound() {
        // given
        Long newsId = 999L;
        
        given(newsArticleRepository.findById(newsId))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findNewsService.findNewsById(newsId))
            .isInstanceOf(NewsNotFoundException.class)
            .hasMessageContaining("존재하지 않는 뉴스 입니다.");
        
        verify(newsArticleRepository).findById(newsId);
    }

    private NewsArticle createMockNewsArticle(String newsItemId, String title) {
        return NewsArticle.builder()
                .newsItemId(newsItemId)
                .title(title)
                .contentsStatus("U")
                .modifyId(1)
                .approveDate(LocalDateTime.now().minusHours(1))
                .groupingCode("policy")
                .contentsType("H")
                .dataContents("<p>테스트 내용</p>")
                .plainTextContent("테스트 내용")
                .contentHash("testhash")
                .build();
    }
}
