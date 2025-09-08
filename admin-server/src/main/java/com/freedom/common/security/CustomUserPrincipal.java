package com.freedom.common.security;

import com.freedom.auth.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 커스텀 UserDetails 구현체
 * Spring Security의 기본 User 객체 대신 사용자 ID를 직접 접근할 수 있도록 함
 */
@Getter
public class CustomUserPrincipal implements UserDetails {
    
    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;
    
    public CustomUserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getId().toString();
        this.password = user.getPassword();
        this.authorities = Collections.singletonList(
            new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().getAuthority())
        );
        this.accountNonExpired = true;
        this.accountNonLocked = !user.isSuspended();
        this.credentialsNonExpired = true;
        this.enabled = !user.isWithdrawn();
    }
    
    /**
     * 사용자 ID 반환 (컨트롤러에서 @AuthenticationPrincipal(expression = "id")로 접근 가능)
     */
    public Long getId() {
        return id;
    }
}
