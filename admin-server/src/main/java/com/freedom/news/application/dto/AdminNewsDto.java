package com.freedom.news.application.dto;

import com.freedom.news.domain.entity.NewsArticle;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminNewsDto {
    private Long id;
    private String newsTitle;
    private String aiSummary;
    private String organizationName;
    private LocalDateTime approvalDate;

    public static AdminNewsDto from(NewsArticle newsArticle) {
        return AdminNewsDto.builder()
            .id(newsArticle.getId())
            .newsTitle(newsArticle.getTitle())
            .aiSummary(newsArticle.getAiSummary())
            .organizationName(getOrganizationName(newsArticle.getMinisterCode()))
            .approvalDate(newsArticle.getApproveDate())
            .build();
    }

    private static String getOrganizationName(String ministerCode) {
        if (ministerCode == null) return "알 수 없음";
        
        return switch (ministerCode) {
            case "1741000" -> "기획재정부";
            case "1270000" -> "교육부";
            case "1370000" -> "과학기술정보통신부";
            case "1750000" -> "외교부";
            case "1310000" -> "법무부";
            case "1320000" -> "국방부";
            case "1330000" -> "행정안전부";
            case "1340000" -> "문화체육관광부";
            case "1360000" -> "농림축산식품부";
            case "1380000" -> "산업통상자원부";
            case "1390000" -> "보건복지부";
            case "1400000" -> "환경부";
            case "1420000" -> "고용노동부";
            case "1440000" -> "여성가족부";
            case "1480000" -> "국토교통부";
            case "1490000" -> "해양수산부";
            case "1760000" -> "중소벤처기업부";
            default -> ministerCode;
        };
    }
}
