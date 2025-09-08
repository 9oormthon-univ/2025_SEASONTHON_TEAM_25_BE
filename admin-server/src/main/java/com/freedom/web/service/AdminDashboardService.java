package com.freedom.web.service;

import com.freedom.news.infra.repository.NewsArticleRepository;
import com.freedom.quiz.infra.QuizRepository;
import com.freedom.auth.api.dto.DashboardStatsResponse;
import com.freedom.auth.domain.UserRole;
import com.freedom.auth.infra.AdminJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final NewsArticleRepository newsArticleRepository;
    private final QuizRepository quizRepository;
    private final AdminJpaRepository userRepository;

    public DashboardStatsResponse getDashboardStats() {
        long totalNewsCount = newsArticleRepository.count();
        long totalQuizCount = quizRepository.count();
        long totalUserCount = userRepository.countByRole(UserRole.USER);
        
        return DashboardStatsResponse.of(totalNewsCount, totalQuizCount, totalUserCount);
    }
}
