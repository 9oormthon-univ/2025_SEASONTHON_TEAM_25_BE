package com.freedom.quiz.application.dto;

import com.freedom.quiz.domain.entity.QuizDifficulty;
import com.freedom.quiz.domain.entity.QuizType;
import com.freedom.quiz.infra.client.response.ExternalQuizItem;
import lombok.Builder;

@Builder
public record CreateQuizCommand(
        QuizType type,
        QuizDifficulty difficulty,
        String category,
        Long newsArticleId,
        String question,
        String explanation,
        String hint,
        Boolean oxAnswer,
        String mcqOption1,
        String mcqOption2,
        String mcqOption3,
        String mcqOption4,
        Integer mcqCorrectIndex
) {
    public static CreateQuizCommand buildCreateQuizCommand(ExternalQuizItem quizItem, String aiHint) {
        if(quizItem.getCategory().contains("객관식퀴즈")){
            return CreateQuizCommand.builder()
                    .type(QuizType.MCQ)
                    .difficulty(QuizDifficulty.MEDIUM)
                    .category("quiz")
                    .newsArticleId(null)
                    .question(quizItem.getQuestionContent())
                    .explanation(quizItem.getExplanation())
                    .hint(aiHint)
                    .oxAnswer(null)
                    .mcqOption1(quizItem.getOption1())
                    .mcqOption2(quizItem.getOption2())
                    .mcqOption3(quizItem.getOption3())
                    .mcqOption4(quizItem.getOption4())
                    .mcqCorrectIndex(quizItem.getMcqCorrectIndex())
                    .build();
        } else {
            return CreateQuizCommand.builder()
                    .type(QuizType.OX)
                    .difficulty(QuizDifficulty.MEDIUM)
                    .category("quiz")
                    .newsArticleId(null)
                    .question(quizItem.getQuestionContent())
                    .explanation(quizItem.getExplanation())
                    .hint(aiHint)
                    .oxAnswer(quizItem.getOxAnswer())
                    .mcqOption1(null)
                    .mcqOption2(null)
                    .mcqOption3(null)
                    .mcqOption4(null)
                    .mcqCorrectIndex(null)
                    .build();
        }
    }
}
