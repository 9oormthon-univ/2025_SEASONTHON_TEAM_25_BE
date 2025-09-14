package com.freedom.home.api;

import com.freedom.common.security.CustomUserPrincipal;
import com.freedom.home.api.response.AttendanceResponse;
import com.freedom.home.api.response.HomeResponse;
import com.freedom.home.application.HomeFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeFacadeService homeService;

    @GetMapping
    public ResponseEntity<HomeResponse> getHome(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(homeService.getMainHomeData(principal.getId()));
    }

    @PostMapping("/attendance")
    public ResponseEntity<AttendanceResponse> markAttendance(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(homeService.markAttendance(principal.getId()));
    }
}
