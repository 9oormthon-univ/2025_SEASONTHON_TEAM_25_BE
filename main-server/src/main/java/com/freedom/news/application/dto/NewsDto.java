package com.freedom.news.application.dto;

import com.freedom.news.domain.entity.NewsArticle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NewsDto {
    
    private Long id;
    private String newsItemId;
    private String title;
    private String subTitle1;
    private String subTitle2;
    private String subTitle3;
    private LocalDateTime approveDate;
    private LocalDateTime modifyDate;
    private String originalImgUrl;
    private String thumbnailUrl;
    private String aiSummary;
    private String plainTextContent;
    private String ministerCode;
    
    public static NewsDto from(NewsArticle newsArticle) {
        return NewsDto.builder()
                .id(newsArticle.getId())
                .newsItemId(newsArticle.getNewsItemId())
                .title(newsArticle.getTitle())
                .subTitle1(newsArticle.getSubTitle1())
                .subTitle2(newsArticle.getSubTitle2())
                .subTitle3(newsArticle.getSubTitle3())
                .approveDate(newsArticle.getApproveDate())
                .modifyDate(newsArticle.getModifyDate())
                .originalImgUrl(newsArticle.getOriginalImgUrl())
                .thumbnailUrl(newsArticle.getThumbnailUrl())
                .aiSummary(newsArticle.getAiSummary())
                .plainTextContent(newsArticle.getPlainTextContent())
                .ministerCode(newsArticle.getMinisterCode())
                .build();
    }
}
