package com.freedom.auth.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CharacterNameRequest {
    @NotBlank(message = "캐릭터 이름을 입력해주세요.")
    @Size(max = 20, message = "캐릭터 이름은 최대 20자까지 가능합니다.")
    private String characterName;
}
