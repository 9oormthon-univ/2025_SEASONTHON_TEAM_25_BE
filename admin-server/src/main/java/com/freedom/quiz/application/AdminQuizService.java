package com.freedom.quiz.application;

import com.freedom.common.dto.PageResponse;
import com.freedom.quiz.api.request.CreateQuizRequest;
import com.freedom.quiz.api.response.AdminQuizDetailResponse;
import com.freedom.quiz.api.response.AdminQuizResponse;
import com.freedom.quiz.application.dto.CreateQuizCommand;
import com.freedom.quiz.application.dto.QuizDomainDto;
import com.freedom.quiz.domain.service.FindQuizService;
import com.freedom.quiz.domain.service.QuizCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminQuizService {

    private final FindQuizService quizService;
    private final QuizCommandService quizCommandService;

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
}
