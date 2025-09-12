package com.freedom.quiz.domain.service;

import com.freedom.common.exception.custom.UserQuizNotFoundException;
import com.freedom.quiz.application.dto.UserQuizDto;
import com.freedom.quiz.domain.entity.Quiz;
import com.freedom.quiz.domain.entity.QuizType;
import com.freedom.quiz.domain.entity.UserQuiz;
import com.freedom.quiz.infra.UserQuizRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindUserQuizServiceTest {

    @Mock
    private UserQuizRepository userQuizRepository;

    @InjectMocks
    private FindUserQuizService findUserQuizService;

    @Test
    @DisplayName("일일 퀴즈 조회 - 성공")
    void findDailyQuizzes_Success() {
        // given
        Long userId = 1L;
        LocalDate quizDate = LocalDate.of(2024, 1, 1);
        
        List<UserQuiz> userQuizzes = List.of(
                createUserQuiz(1L, createQuiz(1L, QuizType.MCQ)),
                createUserQuiz(2L, createQuiz(2L, QuizType.OX))
        );
        
        when(userQuizRepository.findByUserIdAndQuizDate(userId, quizDate))
                .thenReturn(userQuizzes);

        // when
        List<UserQuizDto> result = findUserQuizService.findDailyQuizzes(userId, quizDate);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserQuizId()).isEqualTo(1L);
        assertThat(result.get(0).getType()).isEqualTo(QuizType.MCQ);
        assertThat(result.get(1).getUserQuizId()).isEqualTo(2L);
        assertThat(result.get(1).getType()).isEqualTo(QuizType.OX);
    }

    @Test
    @DisplayName("일일 퀴즈 조회 - 빈 결과")
    void findDailyQuizzes_EmptyResult() {
        // given
        Long userId = 1L;
        LocalDate quizDate = LocalDate.of(2024, 1, 1);
        
        when(userQuizRepository.findByUserIdAndQuizDate(userId, quizDate))
                .thenReturn(List.of());

        // when
        List<UserQuizDto> result = findUserQuizService.findDailyQuizzes(userId, quizDate);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("UserQuiz ID로 조회 - 성공")
    void findUserQuizById_Success() {
        // given
        Long userQuizId = 1L;
        UserQuiz userQuiz = createUserQuiz(userQuizId, createQuiz(1L, QuizType.MCQ));
        
        when(userQuizRepository.findById(userQuizId))
                .thenReturn(Optional.of(userQuiz));

        // when
        UserQuizDto result = findUserQuizService.findUserQuizById(userQuizId);

        // then
        assertThat(result.getUserQuizId()).isEqualTo(userQuizId);
        assertThat(result.getType()).isEqualTo(QuizType.MCQ);
        assertThat(result.getQuestion()).isEqualTo("테스트 문제");
    }

    @Test
    @DisplayName("UserQuiz ID로 조회 - 존재하지 않음")
    void findUserQuizById_NotFound() {
        // given
        Long userQuizId = 999L;
        
        when(userQuizRepository.findById(userQuizId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findUserQuizService.findUserQuizById(userQuizId))
                .isInstanceOf(UserQuizNotFoundException.class);
    }

    @Test
    @DisplayName("퀴즈 ID 조회 - 성공")
    void findQuizIdByUserQuizId_Success() {
        // given
        Long userQuizId = 1L;
        Long expectedQuizId = 100L;
        
        when(userQuizRepository.findQuizIdByUserQuizId(userQuizId))
                .thenReturn(expectedQuizId);

        // when
        Long result = findUserQuizService.findQuizIdByUserQuizId(userQuizId);

        // then
        assertThat(result).isEqualTo(expectedQuizId);
    }

    @Test
    @DisplayName("퀴즈 ID 조회 - 존재하지 않음")
    void findQuizIdByUserQuizId_NotFound() {
        // given
        Long userQuizId = 999L;
        
        when(userQuizRepository.findQuizIdByUserQuizId(userQuizId))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> findUserQuizService.findQuizIdByUserQuizId(userQuizId))
                .isInstanceOf(UserQuizNotFoundException.class);
    }

    private UserQuiz createUserQuiz(Long userQuizId, Quiz quiz) {
        return UserQuiz.builder()
                .id(userQuizId)
                .userId(1L)
                .quiz(quiz)
                .quizDate(LocalDate.of(2024, 1, 1))
                .build();
    }

    private Quiz createQuiz(Long quizId, QuizType type) {
        return Quiz.builder()
                .id(quizId)
                .type(type)
                .question("테스트 문제")
                .explanation("테스트 해설")
                .hint("테스트 힌트")
                .mcqOption1("선택지1")
                .mcqOption2("선택지2")
                .mcqOption3("선택지3")
                .mcqOption4("선택지4")
                .mcqCorrectIndex(1)
                .oxAnswer(true)
                .build();
    }
}
