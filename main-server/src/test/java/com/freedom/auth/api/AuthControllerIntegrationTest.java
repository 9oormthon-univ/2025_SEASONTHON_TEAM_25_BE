package com.freedom.auth.api;

import com.freedom.auth.api.request.CharacterNameRequest;
import com.freedom.auth.api.request.LoginRequest;
import com.freedom.auth.api.request.SignUpRequest;
import com.freedom.auth.api.response.CharacterNameResponse;
import com.freedom.auth.api.response.LoginResponse;
import com.freedom.auth.api.response.SignUpResponse;
import com.freedom.auth.domain.User;
import com.freedom.auth.domain.UserRole;
import com.freedom.auth.domain.UserStatus;
import com.freedom.auth.infra.RefreshTokenJpaRepository;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.common.security.JwtProvider;
import com.freedom.common.test.TestContainerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthController API 통합 테스트")
class AuthControllerIntegrationTest extends TestContainerConfig {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private RefreshTokenJpaRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private final String testPassword = "testpass123!";

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createTestUser(String email) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(testPassword))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }
    
    private String createAccessToken(Long userId) {
        return jwtProvider.createAccessToken(userId);
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() {
        SignUpRequest request = new SignUpRequest("newuser@example.com", "newpass123!");

        webTestClient.post()
                .uri("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SignUpResponse.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getEmail()).isEqualTo("newuser@example.com");
                    assertThat(response.getRole()).isEqualTo("USER");
                    assertThat(response.getStatus()).isEqualTo("ACTIVE");
                    assertThat(response.getCharacterName()).isNull();
                    assertThat(response.getCharacterCreated()).isFalse();
                    assertThat(response.getCreatedAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 이메일")
    void signUp_Fail_DuplicateEmail() {
        createTestUser("existing@example.com");
        SignUpRequest request = new SignUpRequest("existing@example.com", "newpass123!");

        webTestClient.post()
                .uri("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.code").isEqualTo("USER002")
                .jsonPath("$.message").isEqualTo("이미 사용 중인 이메일입니다.");
    }

    @Test
    @DisplayName("회원가입 실패 - 잘못된 이메일 형식")
    void signUp_Fail_InvalidEmail() {
        SignUpRequest request = new SignUpRequest("invalid-email", "newpass123!");

        webTestClient.post()
                .uri("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION001")
                .jsonPath("$.message").isEqualTo("입력값 검증에 실패했습니다.")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0].field").isEqualTo("email")
                .jsonPath("$.errors[0].code").isEqualTo("Email")
                .jsonPath("$.errors[0].message").isEqualTo("올바른 이메일 형식이어야 합니다.");
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        User testUser = createTestUser("existing@example.com");
        LoginRequest request = new LoginRequest("existing@example.com", testPassword);

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .value(response -> {
                    assertThat(response.getAccessToken()).isNotBlank();
                    assertThat(response.getRefreshToken()).isNotBlank();
                    assertThat(response.getTokenType()).isEqualTo("Bearer");
                    assertThat(response.getExpiresIn()).isPositive();
                    assertThat(response.getUser().getUserId()).isEqualTo(testUser.getId());
                    assertThat(response.getUser().getEmail()).isEqualTo("existing@example.com");
                    assertThat(response.getUser().getRole()).isEqualTo("USER");
                    assertThat(response.getUser().getStatus()).isEqualTo("ACTIVE");
                    assertThat(response.getUser().getCharacterName()).isNull();
                    assertThat(response.getUser().getCharacterCreated()).isFalse();
                });
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_Fail_UserNotFound() {
        LoginRequest request = new LoginRequest("notfound@example.com", testPassword);

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("USER001")
                .jsonPath("$.message").isEqualTo("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_Fail_InvalidPassword() {
        createTestUser("existing@example.com");
        LoginRequest request = new LoginRequest("existing@example.com", "wrongpassword");

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("USER003")
                .jsonPath("$.message").isEqualTo("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 탈퇴한 사용자")
    void login_Fail_WithdrawnUser() {
        User user = createTestUser("withdrawn@example.com");
        user.changeStatus(UserStatus.WITHDRAWN);
        userRepository.save(user);
        
        LoginRequest request = new LoginRequest("withdrawn@example.com", testPassword);

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("USER004")
                .jsonPath("$.message").isEqualTo("탈퇴한 사용자입니다.");
    }

    // ===== 캐릭터 이름 생성 API 테스트 =====
    
    @Test
    @DisplayName("캐릭터 이름 생성 성공")
    void createCharacterName_Success() {
        // given
        User testUser = createTestUser("user@example.com");
        String accessToken = createAccessToken(testUser.getId());
        CharacterNameRequest request = new CharacterNameRequest();
        // CharacterNameRequest가 setter가 없다면 리플렉션으로 설정하거나, 생성자를 만들어야 할 수도 있습니다.
        
        // when & then
        webTestClient.post()
                .uri("/api/auth/character/create-name")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"characterName\": \"멋진캐릭터\"}")  // JSON 직접 사용
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CharacterNameResponse.class)
                .value(response -> {
                    assertThat(response.getCharacterName()).isEqualTo("멋진캐릭터");
                    assertThat(response.isCharacterCreated()).isTrue();
                    assertThat(response.getMessage()).isEqualTo("캐릭터 이름이 성공적으로 생성되었습니다.");
                });

        // 실제 DB에서 확인
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getCharacterName()).isEqualTo("멋진캐릭터");
        assertThat(updatedUser.hasCharacterCreated()).isTrue();
    }

    @Test
    @DisplayName("캐릭터 이름 생성 실패 - 인증되지 않은 사용자")
    void createCharacterName_Fail_Unauthorized() {
        webTestClient.post()
                .uri("/api/auth/character/create-name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"characterName\": \"멋진캐릭터\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("캐릭터 이름 생성 실패 - 빈 캐릭터 이름")
    void createCharacterName_Fail_BlankCharacterName() {
        // given
        User testUser = createTestUser("user@example.com");
        String accessToken = createAccessToken(testUser.getId());

        // when & then
        webTestClient.post()
                .uri("/api/auth/character/create-name")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"characterName\": \"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION001")
                .jsonPath("$.message").isEqualTo("입력값 검증에 실패했습니다.")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0].field").isEqualTo("characterName")
                .jsonPath("$.errors[0].code").isEqualTo("NotBlank")
                .jsonPath("$.errors[0].message").isEqualTo("캐릭터 이름을 입력해주세요.");
    }

    @Test
    @DisplayName("캐릭터 이름 생성 실패 - 너무 긴 캐릭터 이름")
    void createCharacterName_Fail_TooLongCharacterName() {
        // given
        User testUser = createTestUser("user@example.com");
        String accessToken = createAccessToken(testUser.getId());
        String longName = "a".repeat(21); // 21자 (제한: 20자)

        // when & then
        webTestClient.post()
                .uri("/api/auth/character/create-name")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"characterName\": \"" + longName + "\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION001")
                .jsonPath("$.message").isEqualTo("입력값 검증에 실패했습니다.")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0].field").isEqualTo("characterName")
                .jsonPath("$.errors[0].code").isEqualTo("Size")
                .jsonPath("$.errors[0].message").isEqualTo("캐릭터 이름은 최대 20자까지 가능합니다.");
    }

    @Test
    @DisplayName("캐릭터 이름 생성 실패 - 중복된 캐릭터 이름")
    void createCharacterName_Fail_DuplicateCharacterName() {
        // given
        User existingUser = createTestUser("existing@example.com");
        existingUser.setCharacterNameAndMarkCreated("중복캐릭터");
        userRepository.save(existingUser);

        User testUser = createTestUser("user@example.com");
        String accessToken = createAccessToken(testUser.getId());

        // when & then
        webTestClient.post()
                .uri("/api/auth/character/create-name")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"characterName\": \"중복캐릭터\"}")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("이미 사용 중인 캐릭터 이름입니다.");
    }
}
