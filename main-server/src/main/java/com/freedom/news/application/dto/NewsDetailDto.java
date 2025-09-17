package com.freedom.news.application.dto;

import com.freedom.news.domain.entity.NewsArticle;
import com.freedom.news.domain.entity.NewsContentBlock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class NewsDetailDto {

    private Long id;
    private String newsItemId;
    private String title;
    private LocalDateTime approveDate;
    private LocalDateTime modifyDate;
    private String originalImgUrl;;
    private String thumbnailUrl;
    private String aiSummary;
    private String plainTextContent;
    private String ministerCode;
    private String originalUrl;
    private List<NewsContentBlockDto> contentBlocks;

    public static NewsDetailDto from(NewsArticle newsArticle, List<NewsContentBlock> contentBlocks) {
        List<NewsContentBlockDto> contentBlockDtos = contentBlocks
                .stream()
                .map(NewsContentBlockDto::from)
                .toList();

        return NewsDetailDto.builder()
                .id(newsArticle.getId())
                .newsItemId(newsArticle.getNewsItemId())
                .title(newsArticle.getTitle())
                .approveDate(newsArticle.getApproveDate())
                .modifyDate(newsArticle.getModifyDate())
                .originalImgUrl(newsArticle.getOriginalImgUrl())
                .thumbnailUrl(newsArticle.getThumbnailUrl())
                .aiSummary(newsArticle.getAiSummary())
                .plainTextContent(newsArticle.getPlainTextContent())
                .ministerCode(newsArticle.getMinisterCode())
                .originalUrl(newsArticle.getOriginalUrl())
                .contentBlocks(contentBlockDtos)
                .build();
    }
}
