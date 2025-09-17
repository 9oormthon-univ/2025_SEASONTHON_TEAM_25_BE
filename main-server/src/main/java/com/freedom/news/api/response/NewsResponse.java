package com.freedom.news.api.response;

import com.freedom.news.application.dto.NewsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NewsResponse {
    
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
    
    public static NewsResponse from(NewsDto newsDto) {
        return NewsResponse.builder()
                .id(newsDto.getId())
                .newsItemId(newsDto.getNewsItemId())
                .title(newsDto.getTitle())
                .subTitle1(newsDto.getSubTitle1())
                .subTitle2(newsDto.getSubTitle2())
                .subTitle3(newsDto.getSubTitle3())
                .approveDate(newsDto.getApproveDate())
                .modifyDate(newsDto.getModifyDate())
                .originalImgUrl(newsDto.getOriginalImgUrl())
                .thumbnailUrl(newsDto.getThumbnailUrl())
                .aiSummary(newsDto.getAiSummary())
                .plainTextContent(newsDto.getPlainTextContent())
                .ministerCode(newsDto.getMinisterCode())
                .build();
    }
}
