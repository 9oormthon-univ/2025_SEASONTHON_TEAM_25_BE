package com.freedom.news.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProcessedNews {

    private List<ProcessedBlock> processedBlocks;
    private String entirePlainText;

    public static ProcessedNews of(List<ProcessedBlock> processedBlocks, String entirePlainText) {
        return ProcessedNews.builder()
                .processedBlocks(processedBlocks)
                .entirePlainText(entirePlainText)
                .build();
    }
}
