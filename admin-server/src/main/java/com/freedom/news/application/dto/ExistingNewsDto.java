package com.freedom.news.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExistingNewsDto {

    private String newsItemId;
    private Integer modifyId;
    private String contentHash;
    
    public static ExistingNewsDto of(String newsItemId, Integer modifyId, String contentHash) {
        return new ExistingNewsDto(newsItemId, modifyId, contentHash);
    }
}
