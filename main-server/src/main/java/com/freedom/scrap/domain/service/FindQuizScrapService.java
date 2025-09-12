package com.freedom.scrap.domain.service;

import com.freedom.common.logging.Loggable;
import com.freedom.scrap.domain.entity.QuizScrap;
import com.freedom.scrap.infra.QuizScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindQuizScrapService {
    
    private final QuizScrapRepository quizScrapRepository;
    
    @Loggable("사용자 퀴즈 스크랩 목록 조회")
    public Page<QuizScrap> findQuizScrapsByUserId(Long userId, Pageable pageable) {
        return quizScrapRepository.findByUserIdOrderByScrappedDateDesc(userId, pageable);
    }
}
