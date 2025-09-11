package com.freedom.quiz.application;

import com.freedom.common.dto.PageResponse;
import com.freedom.quiz.api.request.CreateQuizRequest;
import com.freedom.quiz.api.response.AdminQuizDetailResponse;
import com.freedom.quiz.api.response.AdminQuizResponse;
import com.freedom.quiz.application.dto.CreateQuizCommand;
import com.freedom.quiz.application.dto.QuizDomainDto;
import com.freedom.quiz.domain.service.FindQuizService;
import com.freedom.quiz.domain.service.QuizCommandService;
import com.freedom.quiz.domain.service.QuizImportService;
import com.freedom.quiz.domain.service.HintGenerationService;
import com.freedom.quiz.infra.client.response.ExternalQuizItem;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminQuizService {

    private final FindQuizService quizService;
    private final QuizCommandService quizCommandService;
    private final QuizImportService quizImportService;
    private final HintGenerationService hintGenerationService;

    public PageResponse<AdminQuizResponse> getQuizList(Pageable pageable) {
        Page<QuizDomainDto> domainPage = quizService.findQuizList(pageable);
        Page<AdminQuizResponse> responsePage = domainPage.map(AdminQuizResponse::from);
        
        return PageResponse.of(responsePage);
    }

    public AdminQuizDetailResponse getQuizDetail(Long quizId) {
        return AdminQuizDetailResponse.from(quizService.findQuizById(quizId));
    }

    public AdminQuizResponse createQuiz(CreateQuizRequest request) {

        CreateQuizCommand command = CreateQuizCommand.builder()
                .type(request.getType())
                .difficulty(request.getDifficulty())
                .category("quiz")
                .newsArticleId(request.getNewsArticleId())
                .question(request.getQuestion())
                .explanation(request.getExplanation())
                .hint(request.getHint())
                .oxAnswer(request.getOxAnswer())
                .mcqOption1(request.getMcqOption1())
                .mcqOption2(request.getMcqOption2())
                .mcqOption3(request.getMcqOption3())
                .mcqOption4(request.getMcqOption4())
                .mcqCorrectIndex(request.getMcqCorrectIndex())
                .build();

        return AdminQuizResponse.from(quizCommandService.createQuiz(command));
    }

    public void deleteQuiz(Long quizId) {
        quizCommandService.deleteQuiz(quizId);
    }

    public int importExternalQuizzes() {
        List<ExternalQuizItem> externalQuizzes = quizImportService.fetchExternalQuizzes();
        if (externalQuizzes.isEmpty()) {
            return 0;
        }
        
        List<CreateQuizCommand> savedCommands = new ArrayList<>();
        
        for (ExternalQuizItem externalQuiz : externalQuizzes) {
            String aiHint = generateAiHint(externalQuiz);
                
            CreateQuizCommand command = CreateQuizCommand.buildCreateQuizCommand(externalQuiz, aiHint);

            quizCommandService.createQuiz(command);
            savedCommands.add(command);
        }
        return savedCommands.size();
    }
    
    private String generateAiHint(ExternalQuizItem externalQuiz) {
        try {
            String quizData = buildQuizDataForAi(externalQuiz);
            return hintGenerationService.generateHint(quizData);
        } catch (Exception e) {
            return "문제의 핵심 키워드를 중심으로 생각해보세요.";
        }
    }
    
    private String buildQuizDataForAi(ExternalQuizItem externalQuiz) {
        StringBuilder quizData = new StringBuilder();
        
        quizData.append("문제: ").append(externalQuiz.getQuestionContent()).append("\n");
        quizData.append("문제 유형: ").append(externalQuiz.getCategory().contains("객관식퀴즈") ? "객관식" : "OX").append("\n");
        
        if (externalQuiz.getCategory().contains("객관식퀴즈")) {
            quizData.append("선택지:\n");
            quizData.append("1. ").append(externalQuiz.getOption1()).append("\n");
            quizData.append("2. ").append(externalQuiz.getOption2()).append("\n");
            quizData.append("3. ").append(externalQuiz.getOption3()).append("\n");
            quizData.append("4. ").append(externalQuiz.getOption4()).append("\n");
            quizData.append("정답: ").append(externalQuiz.getMcqCorrectIndex()).append("번\n");
        } else {
            quizData.append("정답: ").append(externalQuiz.getOxAnswer() ? "O (참)" : "X (거짓)").append("\n");
        }
        quizData.append("해설: ").append(externalQuiz.getExplanation());
        
        return quizData.toString();
    }
}
