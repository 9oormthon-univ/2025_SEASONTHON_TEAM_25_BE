package com.freedom.auth.api.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CharacterNameResponse {
    
    private final String characterName;
    private final boolean characterCreated;
    private final String message;
    
    public static CharacterNameResponse success(String characterName) {
        return CharacterNameResponse.builder()
                .characterName(characterName)
                .characterCreated(true)
                .message("캐릭터 이름이 성공적으로 생성되었습니다.")
                .build();
    }
}
