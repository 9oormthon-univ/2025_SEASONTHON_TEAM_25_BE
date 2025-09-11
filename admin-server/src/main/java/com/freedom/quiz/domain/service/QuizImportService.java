package com.freedom.quiz.domain.service;

import com.freedom.quiz.infra.client.ExternalQuizClient;
import com.freedom.quiz.infra.client.response.ExternalQuizApiResponse;
import com.freedom.quiz.infra.client.response.ExternalQuizItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizImportService {

    private final ExternalQuizClient externalQuizClient;

    public List<ExternalQuizItem> fetchExternalQuizzes() {
        try {
            ExternalQuizApiResponse apiResponse = externalQuizClient.getQuizAPI();
            return apiResponse.getData();
        } catch (Exception e) {
            throw new RuntimeException("외부 퀴즈 데이터 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
