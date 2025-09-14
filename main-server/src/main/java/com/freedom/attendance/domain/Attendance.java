package com.freedom.attendance.domain;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "attendance",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attendance_user_date", columnNames = {"user_id", "check_date"})
        }
)
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;

    @Builder
    public Attendance(Long userId, LocalDate checkDate) {
        this.userId = userId;
        this.checkDate = checkDate;
    }
}
