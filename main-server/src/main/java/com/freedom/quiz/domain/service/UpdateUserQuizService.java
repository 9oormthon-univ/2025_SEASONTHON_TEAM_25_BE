package com.freedom.quiz.domain.service;

import com.freedom.quiz.domain.entity.UserQuiz;
import com.freedom.common.exception.custom.UserQuizNotFoundException;
import com.freedom.quiz.infra.UserQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateUserQuizService {

    private final UserQuizRepository userQuizRepository;

    public void updateAnswer(Long userQuizId, String userAnswer, boolean isCorrect) {
        UserQuiz userQuiz = userQuizRepository.findById(userQuizId)
                .orElseThrow(() -> new UserQuizNotFoundException(String.valueOf(userQuizId)));

        userQuiz.submitAnswer(userAnswer, isCorrect);
        userQuizRepository.save(userQuiz);
    }
}
