package com.freedom.auth.infra;

import com.freedom.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByCharacterName(String characterName);
    
    @Modifying
    @Query("UPDATE User u SET u.attendance = false WHERE u.attendance = true")
    void resetAllAttendanceStatus();
}
