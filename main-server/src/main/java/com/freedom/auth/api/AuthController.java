package com.freedom.auth.api;

import com.freedom.auth.api.request.*;
import com.freedom.auth.api.response.CharacterNameResponse;
import com.freedom.auth.api.response.LoginResponse;
import com.freedom.auth.api.response.SignUpResponse;
import com.freedom.auth.api.response.TokenResponse;
import com.freedom.auth.application.AuthFacade;
import com.freedom.auth.application.dto.SignUpDto;
import com.freedom.auth.application.dto.TokenDto;
import com.freedom.common.logging.Loggable;
import com.freedom.common.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthFacade authFacade;
    
    @Loggable("회원가입 API")
    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpDto signUpDto = authFacade.signUp(
                request.getEmail(), 
                request.getPassword()
        );
        
        SignUpResponse response = SignUpResponse.from(signUpDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Loggable("로그인 API")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenDto tokenDto = authFacade.login(request.getEmail(), request.getPassword());
        LoginResponse response = LoginResponse.from(tokenDto);
        
        return ResponseEntity.ok(response);
    }
    
    @Loggable("토큰 갱신 API")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenDto tokenDto = authFacade.refreshTokens(request.getRefreshToken());
        TokenResponse response = TokenResponse.from(tokenDto);
        
        return ResponseEntity.ok(response);
    }
    
    @Loggable("로그아웃 API")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authFacade.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @Loggable("캐릭터 이름 생성")
    @PostMapping("/character/create-name")
    public ResponseEntity<CharacterNameResponse> createCharacterName(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CharacterNameRequest request) {
        
        String characterName = authFacade.createCharacterName(principal.getId(), request.getCharacterName());
        CharacterNameResponse response = CharacterNameResponse.success(characterName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
