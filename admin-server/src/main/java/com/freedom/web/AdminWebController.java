package com.freedom.web;

import com.freedom.common.logging.Loggable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 웹 페이지 컨트롤러
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminWebController {

    @GetMapping("/login")
    @Loggable("관리자 로그인 페이지")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    @Loggable("관리자 대시보드 페이지")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/saving")
    @Loggable("적금 관리 페이지")
    public String savingManagement() {
        return "admin/saving";
    }

    @GetMapping("/news")
    @Loggable("뉴스 관리 페이지")
    public String newsManagement() {
        return "admin/news-management";
    }

    @GetMapping("/news/{newsId}")
    @Loggable("뉴스 상세 페이지")
    public String newsDetail(@PathVariable Long newsId, Model model) {
        model.addAttribute("newsId", newsId);
        return "admin/news-detail";
    }

    @GetMapping("/quiz")
    @Loggable("퀴즈 관리 페이지")
    public String quizManagement() {
        return "admin/quiz";
    }

    @GetMapping("/quiz/create")
    @Loggable("퀴즈 생성 페이지")
    public String quizCreatePage() {
        return "admin/quiz-create";
    }

    @GetMapping("/quiz/{quizId}")
    @Loggable("퀴즈 상세 페이지")
    public String quizDetail(@PathVariable Long quizId, Model model) {
        model.addAttribute("quizId", quizId);
        return "admin/quiz-detail";
    }

    @GetMapping("/users")
    @Loggable("사용자 관리 페이지")
    public String userManagement() {
        return "admin/users";
    }
}
