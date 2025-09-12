package com.freedom.quiz.infra.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HintResponse {

    @JsonProperty("hint")
    private String hint;

    @JsonProperty("is_valid")
    private boolean valid;
}
