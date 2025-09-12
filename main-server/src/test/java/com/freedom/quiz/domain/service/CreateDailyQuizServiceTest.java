package com.freedom.quiz.domain.service;

import com.freedom.common.exception.custom.InsufficientQuizException;
import com.freedom.quiz.application.dto.UserQuizDto;
import com.freedom.quiz.domain.entity.Quiz;
import com.freedom.quiz.domain.entity.QuizType;
import com.freedom.quiz.domain.entity.UserQuiz;
import com.freedom.quiz.infra.QuizRepository;
import com.freedom.quiz.infra.UserQuizRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDailyQuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private UserQuizRepository userQuizRepository;

    @InjectMocks
    private CreateDailyQuizService createDailyQuizService;

    @Test
    @DisplayName("일일 퀴즈 생성 - News 4개 + Quiz 1개")
    void createDailyQuizzes_Success_News4_Quiz1() {
        // given
        Long userId = 1L;
        LocalDate quizDate = LocalDate.of(2024, 1, 3); // 수요일
        
        // 이번 주 출제된 퀴즈 없음
        when(userQuizRepository.findQuizIdsByUserIdAndDateRange(eq(userId), any(), any()))
                .thenReturn(Collections.emptyList());
        
        // News 퀴즈 4개 있음
        List<Quiz> newsQuizzes = List.of(
                createQuiz(1L, "news", QuizType.MCQ),
                createQuiz(2L, "news", QuizType.OX),
                createQuiz(3L, "news", QuizType.MCQ),
                createQuiz(4L, "news", QuizType.OX)
        );
        when(quizRepository.findNewsQuizzesInWeek(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(newsQuizzes);
        
        // Quiz 퀴즈 1개만 있음 (News 4개 + Quiz 1개 = 5개)
        List<Quiz> generalQuizzes = List.of(
                createQuiz(5L, "quiz", QuizType.MCQ)
        );
        when(quizRepository.findGeneralQuizzes(anyList(), eq(1)))
                .thenReturn(generalQuizzes);
        
        // UserQuiz 저장 성공
        when(userQuizRepository.saveAll(anyList()))
                .thenAnswer(invocation -> {
                    List<UserQuiz> userQuizzes = invocation.getArgument(0);
                    return userQuizzes.stream()
                            .map(uq -> UserQuiz.builder()
                                    .id((long) (userQuizzes.indexOf(uq) + 1))
                                    .userId(uq.getUserId())
                                    .quiz(uq.getQuiz())  // 원래 Quiz 객체를 그대로 사용
                                    .quizDate(uq.getQuizDate())
                                    .assignedDate(uq.getAssignedDate())
                                    .build())
                            .toList();
                });

        // when
        List<UserQuizDto> result = createDailyQuizService.createDailyQuizzes(userId, quizDate);

        // then
        assertThat(result).hasSize(5);
        assertThat(result.stream().allMatch(uq -> uq.getQuizDate().equals(quizDate))).isTrue();
        
        // 카테고리별 개수 검증
        long newsCount = result.stream().filter(dto -> "news".equals(dto.getCategory())).count();
        long quizCount = result.stream().filter(dto -> "quiz".equals(dto.getCategory())).count();
        
        assertThat(newsCount).as("뉴스 퀴즈 개수는 4개여야 함").isEqualTo(4);
        assertThat(quizCount).as("일반 퀴즈 개수는 1개여야 함").isEqualTo(1);
        assertThat(newsCount + quizCount).as("전체 퀴즈 개수는 5개여야 함").isEqualTo(5);
    }

    @Test
    @DisplayName("일일 퀴즈 생성 - News 2개 + Quiz 3개 (News 부족시 Quiz로 보충)")
    void createDailyQuizzes_Success_News2_Quiz3() {
        // given
        Long userId = 1L;
        LocalDate quizDate = LocalDate.of(2024, 1, 3);
        
        when(userQuizRepository.findQuizIdsByUserIdAndDateRange(eq(userId), any(), any()))
                .thenReturn(Collections.emptyList());
        
        // News 퀴즈 2개만 있음
        List<Quiz> newsQuizzes = List.of(
                createQuiz(1L, "news", QuizType.MCQ),
                createQuiz(2L, "news", QuizType.OX)
        );
        when(quizRepository.findNewsQuizzesInWeek(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(newsQuizzes);
        
        // Quiz 퀴즈 3개 있음 (News 2개 + Quiz 3개 = 5개)
        List<Quiz> generalQuizzes = List.of(
                createQuiz(3L, "quiz", QuizType.MCQ),
                createQuiz(4L, "quiz", QuizType.OX),
                createQuiz(5L, "quiz", QuizType.MCQ)
        );
        when(quizRepository.findGeneralQuizzes(anyList(), eq(3)))
                .thenReturn(generalQuizzes);
        
        when(userQuizRepository.saveAll(anyList()))
                .thenAnswer(invocation -> {
                    List<UserQuiz> userQuizzes = invocation.getArgument(0);
                    return userQuizzes.stream()
                            .map(uq -> UserQuiz.builder()
                                    .id((long) (userQuizzes.indexOf(uq) + 1))
                                    .userId(uq.getUserId())
                                    .quiz(uq.getQuiz())  // 원래 Quiz 객체를 그대로 사용
                                    .quizDate(uq.getQuizDate())
                                    .assignedDate(uq.getAssignedDate())
                                    .build())
                            .toList();
                });

        // when
        List<UserQuizDto> result = createDailyQuizService.createDailyQuizzes(userId, quizDate);

        // then
        assertThat(result).hasSize(5);
        
        // 카테고리별 개수 검증
        long newsCount = result.stream().filter(dto -> "news".equals(dto.getCategory())).count();
        long quizCount = result.stream().filter(dto -> "quiz".equals(dto.getCategory())).count();
        
        assertThat(newsCount).as("뉴스 퀴즈 개수는 2개여야 함").isEqualTo(2);
        assertThat(quizCount).as("일반 퀴즈 개수는 3개여야 함").isEqualTo(3);
        assertThat(newsCount + quizCount).as("전체 퀴즈 개수는 5개여야 함").isEqualTo(5);
    }

    @Test
    @DisplayName("일일 퀴즈 생성 - 퀴즈 없음으로 예외 발생")
    void createDailyQuizzes_InsufficientQuizException() {
        // given
        Long userId = 1L;
        LocalDate quizDate = LocalDate.of(2024, 1, 3);
        
        when(userQuizRepository.findQuizIdsByUserIdAndDateRange(eq(userId), any(), any()))
                .thenReturn(Collections.emptyList());
        
        // News 퀴즈 없음
        when(quizRepository.findNewsQuizzesInWeek(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(Collections.emptyList());
        
        // Quiz 퀴즈도 없음
        when(quizRepository.findGeneralQuizzes(anyList(), anyInt()))
                .thenReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> createDailyQuizService.createDailyQuizzes(userId, quizDate))
                .isInstanceOf(InsufficientQuizException.class)
                .hasMessage("출제 가능한 퀴즈가 없습니다.");
    }

    @Test
    @DisplayName("일일 퀴즈 생성 - 이미 출제된 퀴즈 제외")
    void createDailyQuizzes_ExcludePreviousQuizzes() {
        // given
        Long userId = 1L;
        LocalDate quizDate = LocalDate.of(2024, 1, 3);
        
        // 이미 출제된 퀴즈 ID들
        List<Long> excludeQuizIds = List.of(1L, 2L);
        when(userQuizRepository.findQuizIdsByUserIdAndDateRange(eq(userId), any(), any()))
                .thenReturn(excludeQuizIds);
        
        List<Quiz> newsQuizzes = List.of(
                createQuiz(3L, "news", QuizType.MCQ),
                createQuiz(4L, "news", QuizType.OX)
        );
        when(quizRepository.findNewsQuizzesInWeek(any(LocalDateTime.class), any(LocalDateTime.class), eq(excludeQuizIds)))
                .thenReturn(newsQuizzes);
        
        List<Quiz> generalQuizzes = List.of(
                createQuiz(5L, "quiz", QuizType.MCQ),
                createQuiz(6L, "quiz", QuizType.OX),
                createQuiz(7L, "quiz", QuizType.MCQ)
        );
        when(quizRepository.findGeneralQuizzes(eq(excludeQuizIds), eq(3)))
                .thenReturn(generalQuizzes);
        
        when(userQuizRepository.saveAll(anyList()))
                .thenAnswer(invocation -> {
                    List<UserQuiz> userQuizzes = invocation.getArgument(0);
                    return userQuizzes.stream()
                            .map(uq -> UserQuiz.builder()
                                    .id((long) (userQuizzes.indexOf(uq) + 1))
                                    .userId(uq.getUserId())
                                    .quiz(uq.getQuiz())  // 원래 Quiz 객체를 그대로 사용
                                    .quizDate(uq.getQuizDate())
                                    .assignedDate(uq.getAssignedDate())
                                    .build())
                            .toList();
                });

        // when
        List<UserQuizDto> result = createDailyQuizService.createDailyQuizzes(userId, quizDate);

        // then
        assertThat(result).hasSize(5);
        
        // 제외된 퀴즈 ID (1L, 2L)가 결과에 포함되지 않았는지 확인
        assertThat(result.stream().map(UserQuizDto::getQuizId))
                .doesNotContain(1L, 2L);
        
        // 카테고리별 개수 검증
        long newsCount = result.stream().filter(dto -> "news".equals(dto.getCategory())).count();
        long quizCount = result.stream().filter(dto -> "quiz".equals(dto.getCategory())).count();
        
        assertThat(newsCount).as("뉴스 퀴즈 개수는 2개여야 함").isEqualTo(2);
        assertThat(quizCount).as("일반 퀴즈 개수는 3개여야 함").isEqualTo(3);
        assertThat(newsCount + quizCount).as("전체 퀴즈 개수는 5개여야 함").isEqualTo(5);
    }

    @Test
    @DisplayName("일일 퀴즈 생성 - News 퀴즈 6개 있을 때 4개로 제한")
    void createDailyQuizzes_Success_News6_LimitTo4() {
        // given
        Long userId = 1L;
        LocalDate quizDate = LocalDate.of(2024, 1, 3);
        
        when(userQuizRepository.findQuizIdsByUserIdAndDateRange(eq(userId), any(), any()))
                .thenReturn(Collections.emptyList());
        
        // News 퀴즈 6개 있음 (4개 초과)
        List<Quiz> newsQuizzes = new ArrayList<>(List.of(
                createQuiz(1L, "news", QuizType.MCQ),
                createQuiz(2L, "news", QuizType.OX),
                createQuiz(3L, "news", QuizType.MCQ),
                createQuiz(4L, "news", QuizType.OX),
                createQuiz(5L, "news", QuizType.MCQ),
                createQuiz(6L, "news", QuizType.OX)
        ));
        when(quizRepository.findNewsQuizzesInWeek(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(newsQuizzes);
        
        // Quiz 퀴즈 1개만 있음 (최종적으로 News 4개 + Quiz 1개 = 5개)
        List<Quiz> generalQuizzes = List.of(
                createQuiz(7L, "quiz", QuizType.MCQ)
        );
        when(quizRepository.findGeneralQuizzes(anyList(), eq(1)))
                .thenReturn(generalQuizzes);
        
        when(userQuizRepository.saveAll(anyList()))
                .thenAnswer(invocation -> {
                    List<UserQuiz> userQuizzes = invocation.getArgument(0);
                    return userQuizzes.stream()
                            .map(uq -> UserQuiz.builder()
                                    .id((long) (userQuizzes.indexOf(uq) + 1))
                                    .userId(uq.getUserId())
                                    .quiz(uq.getQuiz())  // 원래 Quiz 객체를 그대로 사용
                                    .quizDate(uq.getQuizDate())
                                    .assignedDate(uq.getAssignedDate())
                                    .build())
                            .toList();
                });

        // when
        List<UserQuizDto> result = createDailyQuizService.createDailyQuizzes(userId, quizDate);

        // then
        assertThat(result).hasSize(5);
        
        // 카테고리별 개수 검증 - 뉴스 퀴즈가 6개 있었지만 4개로 제한되어야 함
        long newsCount = result.stream().filter(dto -> "news".equals(dto.getCategory())).count();
        long quizCount = result.stream().filter(dto -> "quiz".equals(dto.getCategory())).count();
        
        assertThat(newsCount).as("뉴스 퀴즈는 6개가 있어도 4개로 제한되어야 함").isEqualTo(4);
        assertThat(quizCount).as("일반 퀴즈 개수는 1개여야 함").isEqualTo(1);
        assertThat(newsCount + quizCount).as("전체 퀴즈 개수는 5개여야 함").isEqualTo(5);
        
        // 선택된 뉴스 퀴즈 ID가 1~6 중에서 정확히 4개인지 확인
        List<Long> selectedNewsIds = result.stream()
                .filter(dto -> "news".equals(dto.getCategory()))
                .map(UserQuizDto::getQuizId)
                .toList();
        assertThat(selectedNewsIds)
                .hasSize(4)
                .allSatisfy(id -> assertThat(id).isBetween(1L, 6L));
    }

    private Quiz createQuiz(Long id, String category, QuizType type) {
        return Quiz.builder()
                .id(id)
                .type(type)
                .category(category)
                .question("테스트 문제 " + id)
                .explanation("테스트 해설 " + id)
                .hint("테스트 힌트 " + id)
                .mcqOption1("선택지1")
                .mcqOption2("선택지2")
                .mcqOption3("선택지3")
                .mcqOption4("선택지4")
                .mcqCorrectIndex(1)
                .oxAnswer(true)
                .build();
    }
}
