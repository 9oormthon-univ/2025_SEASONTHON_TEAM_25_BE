package com.freedom.quiz.api;

import com.freedom.quiz.api.request.CreateQuizRequest;
import com.freedom.quiz.api.response.AdminQuizDetailResponse;
import com.freedom.quiz.api.response.AdminQuizResponse;
import com.freedom.quiz.api.response.ImportResultResponse;
import com.freedom.quiz.application.AdminQuizService;
import com.freedom.common.dto.PageResponse;
import com.freedom.common.logging.Loggable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class AdminQuizController {

    private final AdminQuizService adminQuizService;

    @GetMapping
    @Loggable("AdminQuizController : 관리자 퀴즈 목록 조회")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AdminQuizResponse>> getQuizList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        PageResponse<AdminQuizResponse> response = adminQuizService.getQuizList(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{quizId}")
    @Loggable("AdminQuizController : 관리자 퀴즈 상세 조회")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminQuizDetailResponse> getQuizDetail(@PathVariable Long quizId) {
        AdminQuizDetailResponse response = adminQuizService.getQuizDetail(quizId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Loggable("AdminQuizController : 퀴즈 생성")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminQuizResponse> createQuiz(@Valid @RequestBody CreateQuizRequest request) {
        AdminQuizResponse response = adminQuizService.createQuiz(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{quizId}")
    @Loggable("AdminQuizController : 퀴즈 삭제")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        adminQuizService.deleteQuiz(quizId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/import")
    @Loggable("AdminQuizController : 외부 퀴즈 가져오기")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImportResultResponse> importExternalQuizzes() {
        int importedCount = adminQuizService.importExternalQuizzes();
        return ResponseEntity.ok(ImportResultResponse.from(importedCount));
    }
}
