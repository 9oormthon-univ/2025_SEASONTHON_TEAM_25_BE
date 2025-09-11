package com.freedom.quiz.domain.service;

import com.freedom.common.exception.custom.QuizNotFoundException;
import com.freedom.news.infra.client.NewsQuizGenerationClient;
import com.freedom.quiz.application.dto.CreateQuizCommand;
import com.freedom.quiz.application.dto.QuizDomainDto;
import com.freedom.quiz.domain.entity.Quiz;
import com.freedom.quiz.infra.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizCommandService {

    private final QuizRepository quizRepository;
    private final NewsQuizGenerationClient quizClient;

    @Transactional
    public QuizDomainDto createQuiz(CreateQuizCommand command) {
        Quiz quiz = Quiz.builder()
                .type(command.type())
                .difficulty(command.difficulty())
                .category(command.category())
                .newsArticleId(command.newsArticleId())
                .question(command.question())
                .explanation(command.explanation())
                .hint(command.hint())
                .oxAnswer(command.oxAnswer())
                .mcqOption1(command.mcqOption1())
                .mcqOption2(command.mcqOption2())
                .mcqOption3(command.mcqOption3())
                .mcqOption4(command.mcqOption4())
                .mcqCorrectIndex(command.mcqCorrectIndex())
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);
        return QuizDomainDto.from(savedQuiz);
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizNotFoundException(quizId));

        quizRepository.delete(quiz);
    }

    @Transactional
    public void generateAndSaveFromNews(Long newsArticleId, String title, String summary, String plainText, String category) {
        NewsQuizGenerationClient.QuizPack pack = quizClient.generateQuizzes(title, summary, plainText);
        if (pack == null) return;

        List<Quiz> toSave = new ArrayList<>();

        // OX 퀴즈 생성
        if (pack.ox() != null) {
            var ox = pack.ox();
            Quiz entity = Quiz.createOxQuizEntity(newsArticleId, category, ox);
            toSave.add(entity);
        }

        // 4지선다 퀴즈 생성
        if (pack.mcq() != null && isValidMcqQuiz(pack.mcq())) {
            var mcq = pack.mcq();
            Quiz entity = Quiz.createMcqQuizEntity(newsArticleId, category, mcq);
            if (entity != null) {
                toSave.add(entity);
            }
        }

        if (!toSave.isEmpty()) {
            quizRepository.saveAll(toSave);
        }
    }

    private boolean isValidMcqQuiz(NewsQuizGenerationClient.McqQuiz mcq) {
        return mcq.options() != null && mcq.options().size() == 4;
    }
}
