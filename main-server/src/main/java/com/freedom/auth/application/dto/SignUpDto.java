package com.freedom.auth.application.dto;

import com.freedom.auth.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SignUpDto {
    
    private final Long id;
    private final String email;
    private final String role;
    private final String status;
    private final String characterName;
    private final Boolean characterCreated;
    private final LocalDateTime createdAt;
    
    public static SignUpDto from(User user) {
        return SignUpDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .characterName(user.getCharacterName())
                .status(user.getStatus().name())
                .characterCreated(user.hasCharacterCreated())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
