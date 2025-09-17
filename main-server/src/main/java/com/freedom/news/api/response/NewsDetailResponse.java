package com.freedom.news.api.response;

import com.freedom.achievement.application.dto.AchievementDto;
import com.freedom.news.application.dto.NewsDetailDto;
import com.freedom.scrap.application.dto.NewsScrapDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class NewsDetailResponse {
    
    private Long id;
    private String newsItemId;
    private String title;
    private LocalDateTime approveDate;
    private LocalDateTime modifyDate;
    private boolean isScraped;
    private String originalImgUrl;
    private String thumbnailUrl;
    private String aiSummary;
    private String plainTextContent;
    private String ministerCode;
    private List<NewsContentBlockResponse> contentBlocks;
    private String achievementType;
    private boolean achievementCreated;
    
    public static NewsDetailResponse of(NewsDetailDto newsDetailDto, NewsScrapDto newsScrapDto, AchievementDto achievementDto) {
        return NewsDetailResponse.builder()
                .id(newsDetailDto.getId())
                .newsItemId(newsDetailDto.getNewsItemId())
                .title(newsDetailDto.getTitle())
                .approveDate(newsDetailDto.getApproveDate())
                .modifyDate(newsDetailDto.getModifyDate())
                .isScraped(newsScrapDto != null)
                .originalImgUrl(newsDetailDto.getOriginalImgUrl())
                .thumbnailUrl(newsDetailDto.getThumbnailUrl())
                .aiSummary(newsDetailDto.getAiSummary())
                .plainTextContent(newsDetailDto.getPlainTextContent())
                .ministerCode(newsDetailDto.getMinisterCode())
                .contentBlocks(newsDetailDto.getContentBlocks()
                        .stream()
                        .map(NewsContentBlockResponse::from)
                        .toList())
                .achievementType(achievementDto != null ? achievementDto.getType() : null)
                .achievementCreated(achievementDto != null)
                .build();
    }
}
