package com.freedom.quiz.domain.service;

import com.freedom.common.exception.custom.QuizNotFoundException;
import com.freedom.news.infra.client.NewsQuizGenerationClient;
import com.freedom.quiz.domain.dto.CreateQuizCommand;
import com.freedom.quiz.domain.dto.QuizDomainDto;
import com.freedom.quiz.domain.entity.Quiz;
import com.freedom.quiz.domain.entity.QuizDifficulty;
import com.freedom.quiz.domain.entity.QuizType;
import com.freedom.quiz.infra.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 퀴즈 도메인 서비스
 * - 퀴즈 관련 핵심 비즈니스 로직 처리
 * - Entity와 Domain DTO 간의 변환 담당
 * - Application 계층에는 Domain DTO만 노출
 */
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final NewsQuizGenerationClient quizClient;

    /**
     * 퀴즈 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<QuizDomainDto> findQuizList(Pageable pageable) {
        Page<Quiz> quizPage = quizRepository.findAll(pageable);
        return quizPage.map(QuizDomainDto::from);
    }

    /**
     * 퀴즈 상세 조회
     */
    @Transactional(readOnly = true)
    public QuizDomainDto findQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizNotFoundException(quizId));
        return QuizDomainDto.from(quiz);
    }

    /**
     * 퀴즈 생성
     */
    @Transactional
    public QuizDomainDto createQuiz(CreateQuizCommand command) {
        // 도메인 규칙 검증
        validateQuizCommand(command);

        Quiz quiz = Quiz.builder()
                .type(command.type())
                .difficulty(command.difficulty())
                .category(command.category())
                .newsArticleId(command.newsArticleId())
                .question(command.question())
                .explanation(command.explanation())
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

    /**
     * 퀴즈 삭제
     */
    @Transactional
    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizNotFoundException(quizId));
        
        quizRepository.delete(quiz);
    }

    /**
     * 뉴스 기반 퀴즈 자동 생성
     */
    @Transactional
    public void generateAndSaveFromNews(Long newsArticleId, String title, String summary, String plainText, String category) {
        NewsQuizGenerationClient.QuizPack pack = quizClient.generateQuizzes(title, summary, plainText);
        if (pack == null) return;

        List<Quiz> toSave = new ArrayList<>();

        // OX 퀴즈 생성
        if (pack.ox() != null) {
            var ox = pack.ox();
            Quiz entity = createOxQuizEntity(newsArticleId, category, ox);
            toSave.add(entity);
        }

        // 4지선다 퀴즈 생성
        if (pack.mcq() != null && isValidMcqQuiz(pack.mcq())) {
            var mcq = pack.mcq();
            Quiz entity = createMcqQuizEntity(newsArticleId, category, mcq);
            if (entity != null) {
                toSave.add(entity);
            }
        }

        if (!toSave.isEmpty()) {
            quizRepository.saveAll(toSave);
        }
    }

    /**
     * 퀴즈 커맨드 검증
     */
    private void validateQuizCommand(CreateQuizCommand command) {
        if (command.type() == QuizType.OX) {
            validateOxQuiz(command);
        } else if (command.type() == QuizType.MCQ) {
            validateMcqQuiz(command);
        }
    }

    private void validateOxQuiz(CreateQuizCommand command) {
        if (command.oxAnswer() == null) {
            throw new IllegalArgumentException("OX 퀴즈는 정답이 필요합니다.");
        }
    }

    private void validateMcqQuiz(CreateQuizCommand command) {
        if (command.mcqOption1() == null || command.mcqOption2() == null || 
            command.mcqOption3() == null || command.mcqOption4() == null) {
            throw new IllegalArgumentException("4지선다 퀴즈는 모든 선택지가 필요합니다.");
        }
        if (command.mcqCorrectIndex() == null || 
            command.mcqCorrectIndex() < 1 || command.mcqCorrectIndex() > 4) {
            throw new IllegalArgumentException("4지선다 퀴즈는 올바른 정답 인덱스(1-4)가 필요합니다.");
        }
    }

    private Quiz createOxQuizEntity(Long newsArticleId, String category, NewsQuizGenerationClient.OxQuiz ox) {
        return Quiz.builder()
                .type(QuizType.OX)
                .difficulty(QuizDifficulty.MEDIUM)
                .category(category)
                .newsArticleId(newsArticleId)
                .question(ox.question().trim())
                .explanation(ox.explanation().trim())
                .oxAnswer(ox.answer())
                .build();
    }

    private Quiz createMcqQuizEntity(Long newsArticleId, String category, NewsQuizGenerationClient.McqQuiz mcq) {
        List<NewsQuizGenerationClient.McqOption> options = new ArrayList<>(mcq.options());
        Collections.shuffle(options, ThreadLocalRandom.current());

        // 정답 위치 찾기
        int correctCount = 0;
        int correctIdx1Based = 0;
        for (int i = 0; i < 4; i++) {
            if (Boolean.TRUE.equals(options.get(i).correct())) {
                correctCount++;
                correctIdx1Based = i + 1;
            }
        }

        // 정답이 정확히 1개일 때만 퀴즈 생성
        if (correctCount != 1) {
            return null;
        }

        return Quiz.builder()
                .type(QuizType.MCQ)
                .difficulty(QuizDifficulty.MEDIUM)
                .category(category)
                .newsArticleId(newsArticleId)
                .question(mcq.question().trim())
                .explanation(mcq.explanation().trim())
                .mcqOption1(options.get(0).text())
                .mcqOption2(options.get(1).text())
                .mcqOption3(options.get(2).text())
                .mcqOption4(options.get(3).text())
                .mcqCorrectIndex(correctIdx1Based)
                .build();
    }

    private boolean isValidMcqQuiz(NewsQuizGenerationClient.McqQuiz mcq) {
        return mcq.options() != null && mcq.options().size() == 4;
    }
}
