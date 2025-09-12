package com.freedom.auth.domain.service;

import com.freedom.auth.domain.User;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.common.exception.custom.CharacterAlreadyCreatedException;
import com.freedom.common.exception.custom.DuplicateCharacterNameException;
import com.freedom.common.exception.custom.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CharacterNameService {
    
    private final UserJpaRepository userJpaRepository;
    
    @Transactional
    public String createCharacterName(Long userId, String characterName) {
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));
        if (user.hasCharacterCreated()) {
            throw new CharacterAlreadyCreatedException("이미 캐릭터가 생성되어 있습니다.");
        }
        if (userJpaRepository.existsByCharacterName(characterName)) {
            throw new DuplicateCharacterNameException("이미 사용 중인 캐릭터 이름입니다.");
        }
        user.setCharacterNameAndMarkCreated(characterName);
        return userJpaRepository.save(user).getCharacterName();
    }
}
