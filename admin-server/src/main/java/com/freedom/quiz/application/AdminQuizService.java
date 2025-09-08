package com.freedom.quiz.application;

import com.freedom.common.dto.PageResponse;
import com.freedom.quiz.api.request.CreateQuizRequest;
import com.freedom.quiz.api.response.AdminQuizDetailResponse;
import com.freedom.quiz.api.response.AdminQuizResponse;
import com.freedom.quiz.application.dto.AdminQuizDto;
import com.freedom.quiz.domain.dto.CreateQuizCommand;
import com.freedom.quiz.domain.dto.QuizDomainDto;
import com.freedom.quiz.domain.entity.QuizType;
import com.freedom.quiz.domain.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 관리자 퀴즈 Application 서비스
 * - API 계층과 도메인 계층 사이의 중간 계층 역할
 * - 비즈니스 플로우 조율 및 트랜잭션 경계 관리
 * - Request/Response DTO와 Domain DTO 간 변환
 */
@Service
@RequiredArgsConstructor
public class AdminQuizService {

    private final QuizService quizService;

    /**
     * 관리자 퀴즈 목록 조회
     */
    public PageResponse<AdminQuizResponse> getQuizList(Pageable pageable) {
        Page<QuizDomainDto> domainPage = quizService.findQuizList(pageable);
        Page<AdminQuizDto> dtoPage = domainPage.map(AdminQuizDto::from);
        Page<AdminQuizResponse> responsePage = dtoPage.map(AdminQuizResponse::from);
        
        return PageResponse.of(responsePage);
    }

    /**
     * 관리자 퀴즈 상세 조회
     */
    public AdminQuizDetailResponse getQuizDetail(Long quizId) {
        QuizDomainDto domainDto = quizService.findQuizById(quizId);
        AdminQuizDto dto = AdminQuizDto.from(domainDto);
        
        return AdminQuizDetailResponse.from(dto);
    }

    /**
     * 퀴즈 생성
     */
    public AdminQuizResponse createQuiz(CreateQuizRequest request) {
        // Request 유효성 검증
        validateCreateQuizRequest(request);
        
        // Request → Command 변환
        CreateQuizCommand command = CreateQuizCommand.builder()
                .type(request.getType())
                .difficulty(request.getDifficulty())
                .category(request.getCategoryDisplayName())
                .newsArticleId(request.getNewsArticleId())
                .question(request.getQuestion())
                .explanation(request.getExplanation())
                .oxAnswer(request.getOxAnswer())
                .mcqOption1(request.getMcqOption1())
                .mcqOption2(request.getMcqOption2())
                .mcqOption3(request.getMcqOption3())
                .mcqOption4(request.getMcqOption4())
                .mcqCorrectIndex(request.getMcqCorrectIndex())
                .build();

        // 도메인 서비스 호출
        QuizDomainDto domainDto = quizService.createQuiz(command);
        AdminQuizDto dto = AdminQuizDto.from(domainDto);
        
        return AdminQuizResponse.from(dto);
    }

    /**
     * 퀴즈 삭제
     */
    public void deleteQuiz(Long quizId) {
        quizService.deleteQuiz(quizId);
    }

    /**
     * 퀴즈 생성 요청 유효성 검증
     */
    private void validateCreateQuizRequest(CreateQuizRequest request) {
        if (request.getType() == QuizType.OX) {
            request.validateOxQuiz();
        } else if (request.getType() == QuizType.MCQ) {
            request.validateMcqQuiz();
        }
    }
}
