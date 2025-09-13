//package com.freedom.news.application.facade;
//
//import com.freedom.common.exception.custom.NewsNotFoundException;
//import com.freedom.news.api.response.NewsDetailResponse;
//import com.freedom.news.api.response.NewsResponse;
//import com.freedom.news.application.NewsQueryAppService;
//import com.freedom.news.application.dto.NewsContentBlockDto;
//import com.freedom.news.application.dto.NewsDetailDto;
//import com.freedom.news.application.dto.NewsDto;
//import com.freedom.news.domain.service.FindNewsService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("NewsQueryAppService 단위 테스트")
//class NewsQueryAppServiceTest {
//
//    @Mock
//    private FindNewsService findNewsService;
//
//    @InjectMocks
//    private NewsQueryAppService newsQueryAppService;
//
//    @Test
//    @DisplayName("뉴스 리스트 조회 성공")
//    void getRecentNewsList_Success() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        NewsDto mockNews = createMockNewsDto();
//        Page<NewsDto> mockPage = new PageImpl<>(List.of(mockNews), pageable, 1);
//
//        given(findNewsService.findRecentNews(any(Pageable.class)))
//            .willReturn(mockPage);
//
//        // when
//        Page<NewsResponse> result = newsQueryAppService.getRecentNewsList(0, 10);
//
//        // then
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 제목");
//        assertThat(result.getContent().get(0).getNewsItemId()).isEqualTo("news001");
//        assertThat(result.getContent().get(0).getAiSummary()).isEqualTo("AI 요약");
//        assertThat(result.getTotalElements()).isEqualTo(1);
//
//        verify(findNewsService).findRecentNews(any(Pageable.class));
//    }
//
//    @Test
//    @DisplayName("뉴스 상세 조회 성공")
//    void getNewsDetail_Success() {
//        // given
//        Long newsId = 1L;
//        NewsDetailDto mockNews = createMockNewsDetailDto();
//
//        given(findNewsService.findNewsById(newsId))
//            .willReturn(mockNews);
//
//        // when
//        NewsDetailResponse result = newsQueryAppService.getNewsDetail(newsId);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getTitle()).isEqualTo("테스트 제목");
//        assertThat(result.getContentBlocks()).hasSize(2);
//        assertThat(result.getContentBlocks().get(0).getBlockType()).isEqualTo("TEXT");
//        assertThat(result.getContentBlocks().get(1).getBlockType()).isEqualTo("IMAGE");
//
//        verify(findNewsService).findNewsById(newsId);
//    }
//
//    @Test
//    @DisplayName("뉴스 상세 조회 실패 - 존재하지 않는 뉴스")
//    void getNewsDetail_Fail_NotFound() {
//        // given
//        Long newsId = 999L;
//
//        given(findNewsService.findNewsById(newsId))
//            .willThrow(new NewsNotFoundException("존재하지 않는 뉴스 입니다." + newsId));
//
//        // when & then
//        assertThatThrownBy(() -> newsQueryAppService.getNewsDetail(newsId))
//            .isInstanceOf(NewsNotFoundException.class)
//            .hasMessageContaining("존재하지 않는 뉴스 입니다.");
//
//        verify(findNewsService).findNewsById(newsId);
//    }
//
//    private NewsDto createMockNewsDto() {
//        return NewsDto.builder()
//                .id(1L)
//                .newsItemId("news001")
//                .title("테스트 제목")
//                .subTitle1("부제목1")
//                .subTitle2("부제목2")
//                .subTitle3("부제목3")
//                .approveDate(LocalDateTime.now())
//                .modifyDate(LocalDateTime.now())
//                .thumbnailUrl("https://example.com/thumb.jpg")
//                .aiSummary("AI 요약")
//                .plainTextContent("본문 내용")
//                .build();
//    }
//
//    private NewsDetailDto createMockNewsDetailDto() {
//        List<NewsContentBlockDto> contentBlocks = List.of(
//            NewsContentBlockDto.builder()
//                .blockType("TEXT")
//                .originalContent("<p>텍스트 블록</p>")
//                .plainContent("텍스트 블록")
//                .blockOrder(1)
//                .build(),
//            NewsContentBlockDto.builder()
//                .blockType("IMAGE")
//                .originalContent("<img src='test.jpg'>")
//                .url("https://example.com/test.jpg")
//                .altText("테스트 이미지")
//                .blockOrder(2)
//                .build()
//        );
//
//        return NewsDetailDto.builder()
//                .id(1L)
//                .newsItemId("news001")
//                .title("테스트 제목")
//                .approveDate(LocalDateTime.now())
//                .modifyDate(LocalDateTime.now())
//                .thumbnailUrl("https://example.com/thumb.jpg")
//                .aiSummary("AI 요약")
//                .plainTextContent("본문 내용")
//                .contentBlocks(contentBlocks)
//                .build();
//    }
//}
