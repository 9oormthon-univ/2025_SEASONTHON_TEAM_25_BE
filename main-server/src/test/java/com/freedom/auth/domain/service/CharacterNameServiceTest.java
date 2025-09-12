package com.freedom.auth.domain.service;

import com.freedom.auth.domain.User;
import com.freedom.auth.domain.UserRole;
import com.freedom.auth.domain.UserStatus;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.common.exception.custom.CharacterAlreadyCreatedException;
import com.freedom.common.exception.custom.DuplicateCharacterNameException;
import com.freedom.common.exception.custom.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("CharacterNameService 단위 테스트")
class CharacterNameServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private CharacterNameService characterNameService;

    private User testUser;
    private final Long TEST_USER_ID = 1L;
    private final String TEST_CHARACTER_NAME = "멋진캐릭터";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("캐릭터 이름 생성 성공")
    void createCharacterName_Success() {
        // given
        given(userJpaRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
        given(userJpaRepository.existsByCharacterName(TEST_CHARACTER_NAME)).willReturn(false);
        given(userJpaRepository.save(any(User.class))).willReturn(testUser);

        // when
        String result = characterNameService.createCharacterName(TEST_USER_ID, TEST_CHARACTER_NAME);

        // then
        assertThat(result).isEqualTo(TEST_CHARACTER_NAME);
        assertThat(testUser.getCharacterName()).isEqualTo(TEST_CHARACTER_NAME);
        assertThat(testUser.hasCharacterCreated()).isTrue();

        then(userJpaRepository).should().findById(TEST_USER_ID);
        then(userJpaRepository).should().existsByCharacterName(TEST_CHARACTER_NAME);
        then(userJpaRepository).should().save(testUser);
    }

    @Test
    @DisplayName("캐릭터 이름 생성 실패 - 사용자를 찾을 수 없음")
    void createCharacterName_Fail_UserNotFound() {
        // given
        given(userJpaRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> characterNameService.createCharacterName(TEST_USER_ID, TEST_CHARACTER_NAME))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다. userId: " + TEST_USER_ID);

        then(userJpaRepository).should().findById(TEST_USER_ID);
        then(userJpaRepository).should(never()).existsByCharacterName(anyString());
        then(userJpaRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("캐릭터 이름 생성 실패 - 이미 캐릭터가 생성되어 있음")
    void createCharacterName_Fail_CharacterAlreadyCreated() {
        // given
        testUser.setCharacterNameAndMarkCreated("기존캐릭터"); // 이미 캐릭터 생성됨
        given(userJpaRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> characterNameService.createCharacterName(TEST_USER_ID, TEST_CHARACTER_NAME))
                .isInstanceOf(CharacterAlreadyCreatedException.class)
                .hasMessage("이미 캐릭터가 생성되어 있습니다.");
    }

    @Test
    @DisplayName("캐릭터 이름 생성 실패 - 중복된 캐릭터 이름")
    void createCharacterName_Fail_DuplicateCharacterName() {
        // given
        given(userJpaRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
        given(userJpaRepository.existsByCharacterName(TEST_CHARACTER_NAME)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> characterNameService.createCharacterName(TEST_USER_ID, TEST_CHARACTER_NAME))
                .isInstanceOf(DuplicateCharacterNameException.class)
                .hasMessage("이미 사용 중인 캐릭터 이름입니다.");
    }
}
