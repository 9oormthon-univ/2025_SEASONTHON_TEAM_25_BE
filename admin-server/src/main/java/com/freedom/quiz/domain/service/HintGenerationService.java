package com.freedom.quiz.domain.service;

import com.freedom.quiz.infra.client.QuizAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HintGenerationService {

    private final QuizAiClient quizAiClient;

    public String generateHint(String questionContext) {
        if (questionContext == null || questionContext.trim().isEmpty()) {
            return "문제를 다시 읽어보고 핵심 키워드를 찾아보세요.";
        }
        return quizAiClient.generateHint(questionContext);
    }
}
