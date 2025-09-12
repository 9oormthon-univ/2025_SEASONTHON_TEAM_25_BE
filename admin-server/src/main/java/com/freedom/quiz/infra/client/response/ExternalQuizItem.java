package com.freedom.quiz.infra.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExternalQuizItem {
    
    @JsonProperty("구분")
    private String category;
    
    @JsonProperty("문제내용")
    private String questionContent;
    
    @JsonProperty("번호")
    private Integer number;
    
    @JsonProperty("보기1")
    private String option1;
    
    @JsonProperty("보기2")
    private String option2;
    
    @JsonProperty("보기3")
    private String option3;
    
    @JsonProperty("보기4")
    private String option4;
    
    @JsonProperty("정답")
    private String correctAnswer;
    
    @JsonProperty("해설")
    private String explanation;

    public Integer getMcqCorrectIndex() {
        if (correctAnswer == null) return null;

        if (correctAnswer.contains("1번")) return 1;
        if (correctAnswer.contains("2번")) return 2;
        if (correctAnswer.contains("3번")) return 3;
        if (correctAnswer.contains("4번")) return 4;

        return null;
    }

    public Boolean getOxAnswer() {
        return correctAnswer.contains("1번");
    }
}
