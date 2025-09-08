package com.freedom.auth.domain.service;

import com.freedom.auth.domain.User;
import com.freedom.auth.infra.AdminJpaRepository;
import com.freedom.common.exception.custom.DuplicateEmailException;
import com.freedom.common.exception.custom.InvalidPasswordException;
import com.freedom.common.exception.custom.UserSuspendedException;
import com.freedom.common.exception.custom.UserWithdrawnException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateUserService {
    
    private final AdminJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public void validateEmailDuplication(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }
    
    public void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new InvalidPasswordException();
        }
    }

    public void validateUserStatus(User user) {
        if (user.isWithdrawn()) {
            throw new UserWithdrawnException(user.getEmail());
        }
        
        if (user.isSuspended()) {
            throw new UserSuspendedException(user.getEmail());
        }
    }
}
