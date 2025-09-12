package com.freedom.auth.api.response;

import com.freedom.auth.application.dto.LoginDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfo {
    
    private final Long userId;
    private final String email;
    private final String role;
    private final String status;
    private final String characterName;
    private final Boolean characterCreated;
    
    public static UserInfo from(LoginDto loginDto) {
        return UserInfo.builder()
                .userId(loginDto.getUserId())
                .email(loginDto.getEmail())
                .role(loginDto.getRole())
                .characterName(loginDto.getCharacterName())
                .status(loginDto.getStatus())
                .characterCreated(loginDto.getCharacterCreated())
                .build();
    }
}
