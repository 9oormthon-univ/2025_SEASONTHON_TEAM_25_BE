package com.freedom.auth.application.dto;

import com.freedom.auth.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginDto {
    
    private final Long userId;
    private final String email;
    private final String role;
    private final String status;
    private final Boolean characterCreated;
    
    public static LoginDto from(User user) {
        return LoginDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .characterCreated(user.hasCharacterCreated())
                .build();
    }
}
