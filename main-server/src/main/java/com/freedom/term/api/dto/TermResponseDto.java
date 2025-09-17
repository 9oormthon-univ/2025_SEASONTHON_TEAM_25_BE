package com.freedom.term.api.dto;

/**
 * 금융 용어 조회 API의 단건 응답 DTO
 * @param term          용어 이름
 * @param description   용어 설명
 */
public record TermResponseDto(
        String term,
        String description
) {
}
