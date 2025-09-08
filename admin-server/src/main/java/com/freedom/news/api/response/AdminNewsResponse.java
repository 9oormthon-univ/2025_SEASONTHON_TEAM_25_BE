package com.freedom.news.api.response;

import com.freedom.news.application.dto.AdminNewsDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminNewsResponse {
    private Long id;
    private String newsTitle;
    private String aiSummary;
    private String organizationName;
    private LocalDateTime approvalDate;

    public static AdminNewsResponse from(AdminNewsDto dto) {
        return AdminNewsResponse.builder()
            .id(dto.getId())
            .newsTitle(dto.getNewsTitle())
            .aiSummary(dto.getAiSummary())
            .organizationName(dto.getOrganizationName())
            .approvalDate(dto.getApprovalDate())
            .build();
    }
}
