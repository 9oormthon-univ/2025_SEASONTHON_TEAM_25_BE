package com.freedom.quiz.domain.service;

import com.freedom.common.exception.custom.QuizNotFoundException;
import com.freedom.quiz.application.dto.QuizDomainDto;
import com.freedom.quiz.domain.entity.Quiz;
import com.freedom.quiz.infra.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindQuizService {

    private final QuizRepository quizRepository;

    @Transactional(readOnly = true)
    public Page<QuizDomainDto> findQuizList(Pageable pageable) {
        Page<Quiz> quizPage = quizRepository.findAll(pageable);
        return quizPage.map(QuizDomainDto::from);
    }

    @Transactional(readOnly = true)
    public QuizDomainDto findQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizNotFoundException(quizId));
        return QuizDomainDto.from(quiz);
    }
}
