package com.freedom.auth.domain.service;

import com.freedom.auth.domain.User;
import com.freedom.auth.infra.AdminJpaRepository;
import com.freedom.common.exception.custom.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindUserService {
    
    private final AdminJpaRepository userRepository;
    
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("id: " + id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("email: " + email));
    }
}
