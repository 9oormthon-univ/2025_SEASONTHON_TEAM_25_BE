package com.freedom.auth.api.response;

import com.freedom.achievement.application.dto.AchievementDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CharacterNameResponse {
    
    private final String characterName;
    private final boolean characterCreated;
    private final String achievementType;
    private final boolean achievementCreated;
    private final String message;
    public static CharacterNameResponse success(String characterName, AchievementDto achievementDto) {
        return CharacterNameResponse.builder()
                .characterName(characterName)
                .characterCreated(true)
                .achievementType(achievementDto.getType())
                .achievementCreated(true)
                .message("캐릭터 이름이 성공적으로 생성되었습니다.")
                .build();
    }
}
