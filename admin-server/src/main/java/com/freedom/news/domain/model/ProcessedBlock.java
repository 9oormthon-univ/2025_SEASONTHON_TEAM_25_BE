package com.freedom.news.domain.model;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class ProcessedBlock {

    private String type;
    private String originalContent;
    private String plainContent;
    private String url;
    private String alt;
    
    public static ProcessedBlock text(String originalContent, String plainContent) {
        return ProcessedBlock.builder()
                .type("text")
                .originalContent(originalContent)
                .plainContent(plainContent)
                .build();
    }

    public static ProcessedBlock heading(String originalContent, String plainContent, int level) {
        return ProcessedBlock.builder()
                .type("heading_" + level)
                .originalContent(originalContent)
                .plainContent(plainContent)
                .build();
    }
    
    public static ProcessedBlock image(String url, String alt) {
        return ProcessedBlock.builder()
                .type("image")
                .url(url)
                .alt(alt)
                .build();
    }

    public static ProcessedBlock paragraphBreak() {
        return ProcessedBlock.builder()
                .type("paragraph_break")
                .build();
    }
}
