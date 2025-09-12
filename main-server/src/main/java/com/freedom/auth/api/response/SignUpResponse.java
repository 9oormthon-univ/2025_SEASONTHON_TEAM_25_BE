package com.freedom.auth.api.response;

import com.freedom.auth.application.dto.SignUpDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SignUpResponse {
    
    private final Long id;
    private final String email;
    private final String role;
    private final String status;
    private final String characterName;
    private final Boolean characterCreated;
    private final LocalDateTime createdAt;
    
    public static SignUpResponse from(SignUpDto dto) {
        return SignUpResponse.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .role(dto.getRole())
                .characterName(dto.getCharacterName())
                .status(dto.getStatus())
                .characterCreated(dto.getCharacterCreated())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
