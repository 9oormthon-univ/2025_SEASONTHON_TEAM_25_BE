package com.freedom.quiz.domain.service;

import com.freedom.common.exception.custom.UserQuizNotFoundException;
import com.freedom.quiz.domain.entity.UserQuiz;
import com.freedom.quiz.infra.UserQuizRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserQuizServiceTest {

    @Mock
    private UserQuizRepository userQuizRepository;

    @InjectMocks
    private UpdateUserQuizService updateUserQuizService;

    @Test
    @DisplayName("정답 업데이트 - 성공 (정답)")
    void updateAnswer_Success_Correct() {
        // given
        Long userQuizId = 1L;
        String userAnswer = "1";
        boolean isCorrect = true;
        
        UserQuiz existingUserQuiz = UserQuiz.builder()
                .id(userQuizId)
                .userId(1L)
                .quizDate(LocalDate.of(2024, 1, 1))
                .build();
        
        when(userQuizRepository.findById(userQuizId))
                .thenReturn(Optional.of(existingUserQuiz));
        
        when(userQuizRepository.save(any(UserQuiz.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        updateUserQuizService.updateAnswer(userQuizId, userAnswer, isCorrect);

        // then
        ArgumentCaptor<UserQuiz> userQuizCaptor = ArgumentCaptor.forClass(UserQuiz.class);
        verify(userQuizRepository).save(userQuizCaptor.capture());
        
        UserQuiz savedUserQuiz = userQuizCaptor.getValue();
        assertThat(savedUserQuiz.getUserAnswer()).isEqualTo(userAnswer);
        assertThat(savedUserQuiz.getIsCorrect()).isEqualTo(isCorrect);
        assertThat(savedUserQuiz.getId()).isEqualTo(userQuizId);
        assertThat(savedUserQuiz.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("정답 업데이트 - 성공 (오답)")
    void updateAnswer_Success_Incorrect() {
        // given
        Long userQuizId = 2L;
        String userAnswer = "3";
        boolean isCorrect = false;
        
        UserQuiz existingUserQuiz = UserQuiz.builder()
                .id(userQuizId)
                .userId(2L)
                .quizDate(LocalDate.of(2024, 1, 2))
                .build();
        
        when(userQuizRepository.findById(userQuizId))
                .thenReturn(Optional.of(existingUserQuiz));
        
        when(userQuizRepository.save(any(UserQuiz.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        updateUserQuizService.updateAnswer(userQuizId, userAnswer, isCorrect);

        // then
        ArgumentCaptor<UserQuiz> userQuizCaptor = ArgumentCaptor.forClass(UserQuiz.class);
        verify(userQuizRepository).save(userQuizCaptor.capture());
        
        UserQuiz savedUserQuiz = userQuizCaptor.getValue();
        assertThat(savedUserQuiz.getUserAnswer()).isEqualTo(userAnswer);
        assertThat(savedUserQuiz.getIsCorrect()).isEqualTo(isCorrect);
        assertThat(savedUserQuiz.getId()).isEqualTo(userQuizId);
        assertThat(savedUserQuiz.getUserId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("정답 업데이트 - UserQuiz 존재하지 않음")
    void updateAnswer_UserQuizNotFound() {
        // given
        Long userQuizId = 999L;
        String userAnswer = "1";
        boolean isCorrect = true;
        
        when(userQuizRepository.findById(userQuizId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> updateUserQuizService.updateAnswer(userQuizId, userAnswer, isCorrect))
                .isInstanceOf(UserQuizNotFoundException.class);
        
        verify(userQuizRepository, never()).save(any(UserQuiz.class));
    }

    @Test
    @DisplayName("정답 업데이트 - 기존 답안을 새로운 답안으로 덮어쓰기")
    void updateAnswer_OverwriteExistingAnswer() {
        // given
        Long userQuizId = 3L;
        String oldAnswer = "2";
        String newAnswer = "4";
        boolean oldIsCorrect = false;
        boolean newIsCorrect = true;
        
        UserQuiz existingUserQuiz = UserQuiz.builder()
                .id(userQuizId)
                .userId(3L)
                .userAnswer(oldAnswer)
                .isCorrect(oldIsCorrect)
                .quizDate(LocalDate.of(2024, 1, 3))
                .build();
        
        when(userQuizRepository.findById(userQuizId))
                .thenReturn(Optional.of(existingUserQuiz));
        
        when(userQuizRepository.save(any(UserQuiz.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        updateUserQuizService.updateAnswer(userQuizId, newAnswer, newIsCorrect);

        // then
        ArgumentCaptor<UserQuiz> userQuizCaptor = ArgumentCaptor.forClass(UserQuiz.class);
        verify(userQuizRepository).save(userQuizCaptor.capture());
        
        UserQuiz savedUserQuiz = userQuizCaptor.getValue();
        assertThat(savedUserQuiz.getUserAnswer()).isEqualTo(newAnswer);
        assertThat(savedUserQuiz.getIsCorrect()).isEqualTo(newIsCorrect);
        // 기존 답안이 새로운 답안으로 완전히 교체되었는지 확인
        assertThat(savedUserQuiz.getUserAnswer()).isNotEqualTo(oldAnswer);
        assertThat(savedUserQuiz.getIsCorrect()).isNotEqualTo(oldIsCorrect);
    }

    @Test
    @DisplayName("정답 업데이트 - null 답안 처리")
    void updateAnswer_NullAnswer() {
        // given
        Long userQuizId = 4L;
        String userAnswer = null;
        boolean isCorrect = false;
        
        UserQuiz existingUserQuiz = UserQuiz.builder()
                .id(userQuizId)
                .userId(4L)
                .quizDate(LocalDate.of(2024, 1, 4))
                .build();
        
        when(userQuizRepository.findById(userQuizId))
                .thenReturn(Optional.of(existingUserQuiz));
        
        when(userQuizRepository.save(any(UserQuiz.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        updateUserQuizService.updateAnswer(userQuizId, userAnswer, isCorrect);

        // then
        ArgumentCaptor<UserQuiz> userQuizCaptor = ArgumentCaptor.forClass(UserQuiz.class);
        verify(userQuizRepository).save(userQuizCaptor.capture());
        
        UserQuiz savedUserQuiz = userQuizCaptor.getValue();
        assertThat(savedUserQuiz.getUserAnswer()).isNull();
        assertThat(savedUserQuiz.getIsCorrect()).isEqualTo(isCorrect);
    }
}
