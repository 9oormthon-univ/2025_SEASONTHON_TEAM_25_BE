package com.freedom.news.api.response;

import com.freedom.news.application.dto.AdminNewsDetailDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminNewsDetailResponse {
    private Long id;
    private String newsTitle;
    private String aiSummary;
    private String organizationName;
    private LocalDateTime approvalDate;
    private String newsUrl;
    private List<NewsContentBlockResponse> contentBlocks;

    @Getter
    @Builder
    public static class NewsContentBlockResponse {
        private Long id;
        private String blockType;
        private String content;
        private Integer orderIndex;
        private String url;
        private String altText;
    }

    public static AdminNewsDetailResponse from(AdminNewsDetailDto dto) {
        return AdminNewsDetailResponse.builder()
            .id(dto.getId())
            .newsTitle(dto.getNewsTitle())
            .aiSummary(dto.getAiSummary())
            .organizationName(dto.getOrganizationName())
            .approvalDate(dto.getApprovalDate())
            .newsUrl(dto.getNewsUrl())
            .contentBlocks(dto.getContentBlocks().stream()
                .map(block -> NewsContentBlockResponse.builder()
                    .id(block.getId())
                    .blockType(block.getBlockType())
                    .content(block.getContent())
                    .orderIndex(block.getOrderIndex())
                    .url(block.getUrl())
                    .altText(block.getAltText())
                    .build())
                .toList())
            .build();
    }
}
