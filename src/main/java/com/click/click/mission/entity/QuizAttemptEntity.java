package com.click.click.mission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "quiz_attempts", indexes = {
        @Index(name = "ix_quiz_user_date_passed", columnList = "user_id,date,passed")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer score;      // 0~100

    @Column(nullable = false)
    private boolean passed;

    /**
     * MariaDB의 JSON 타입은 실제로 LONGTEXT로 생성됩니다.
     * Hibernate 검증 불일치를 피하기 위해 LONGVARCHAR로 명시 매핑합니다.
     * - @Lob 는 제거
     * - columnDefinition="LONGTEXT" 로 명확화
     * - @JdbcTypeCode(SqlTypes.LONGVARCHAR) 로 Hibernate 타입 지정
     */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "meta_json", columnDefinition = "LONGTEXT")
    private String metaJson;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;
}
