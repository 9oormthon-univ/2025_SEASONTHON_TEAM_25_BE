package com.freedom.auth.domain;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(name = "characterName", length = 50)
    private String characterName;

    @Column(name = "character_created", nullable = false)
    private Boolean characterCreated;

    @Column(name = "attendance", nullable = false)
    private Boolean attendance;

    @Column(name = "last_auto_payment_date")
    private LocalDate lastAutoPaymentDate;
    
    @Builder
    public User(String email, String password, UserRole role, UserStatus status, String characterName) {
        this.email = email;
        this.password = password;
        this.role = role != null ? role : UserRole.USER;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.characterName = characterName;
        this.attendance = false;
        this.characterCreated = false;
    }
    
    public void changeStatus(UserStatus status) {
        this.status = status;
    }
    
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }
    
    public boolean isWithdrawn() {
        return UserStatus.WITHDRAWN.equals(this.status);
    }
    
    public boolean isSuspended() {
        return UserStatus.SUSPENDED.equals(this.status);
    }

    public void completeAttendance() {
        this.attendance = true;
    }

    public boolean hasCharacterCreated() {
        return this.characterCreated;
    }

    public void updateLastAutoPaymentDate(LocalDate date) {
        this.lastAutoPaymentDate = date;
    }
    
    public void setCharacterNameAndMarkCreated(String characterName) {
        this.characterName = characterName;
        this.characterCreated = true;
    }
}
