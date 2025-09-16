package com.freedom.auth.domain.service;

import com.freedom.auth.domain.User;
import com.freedom.auth.domain.UserRole;
import com.freedom.auth.domain.UserStatus;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.common.exception.custom.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindUserService 단위 테스트")
class FindUserServiceTest {

    @Mock
    private UserJpaRepository userRepository;

    @InjectMocks
    private FindUserService findUserService;

    @Test
    @DisplayName("ID로 사용자 조회 성공")
    void findById_Success() {
        // given
        Long userId = 1L;
        User expectedUser = createTestUser(userId, "test@example.com");
        given(userRepository.findById(userId)).willReturn(Optional.of(expectedUser));

        // when
        User result = findUserService.findById(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.hasCharacterCreated()).isFalse();
    }

    @Test
    @DisplayName("ID로 사용자 조회 실패 - 존재하지 않는 사용자")
    void findById_Fail_UserNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findUserService.findById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("id: 999");
    }

    @Test
    @DisplayName("이메일로 사용자 조회 성공")
    void findByEmail_Success() {
        // given
        String email = "test@example.com";
        User expectedUser = createTestUser(1L, email);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(expectedUser));

        // when
        User result = findUserService.findByEmail(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.hasCharacterCreated()).isFalse();
    }

    @Test
    @DisplayName("이메일로 사용자 조회 실패 - 존재하지 않는 사용자")
    void findByEmail_Fail_UserNotFound() {
        // given
        String email = "notfound@example.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findUserService.findByEmail(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("email: notfound@example.com");
    }

    @Test
    @DisplayName("다양한 상태의 사용자 조회")
    void findUser_WithDifferentStatuses() {
        // given - 탈퇴한 사용자
        Long withdrawnUserId = 2L;
        User withdrawnUser = createTestUserWithStatus(withdrawnUserId, "withdrawn@example.com", UserStatus.WITHDRAWN);
        given(userRepository.findById(withdrawnUserId)).willReturn(Optional.of(withdrawnUser));

        // when
        User result = findUserService.findById(withdrawnUserId);

        // then
        assertThat(result.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(result.isWithdrawn()).isTrue();
        assertThat(result.isActive()).isFalse();
    }

    @Test
    @DisplayName("정지된 사용자 조회")
    void findUser_SuspendedUser() {
        // given
        Long suspendedUserId = 3L;
        User suspendedUser = createTestUserWithStatus(suspendedUserId, "suspended@example.com", UserStatus.SUSPENDED);
        given(userRepository.findById(suspendedUserId)).willReturn(Optional.of(suspendedUser));

        // when
        User result = findUserService.findById(suspendedUserId);

        // then
        assertThat(result.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(result.isSuspended()).isTrue();
        assertThat(result.isActive()).isFalse();
    }

    private User createTestUser(Long id, String email) {
        return createTestUserWithStatus(id, email, UserStatus.ACTIVE);
    }

    private User createTestUserWithStatus(Long id, String email, UserStatus status) {
        User user = User.builder()
                .email(email)
                .password("encodedPassword")
                .role(UserRole.USER)
                .status(status)
                .build();
        
        // ID는 리플렉션으로 설정 (테스트용)
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return user;
    }
}
