package com.click.click.points.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "points_tx")
public class PointsTxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long delta; // +적립 / -차감

    @Column(nullable = false, length = 64)
    private String reason; // MISSION_REWARD, REDEEM 등

    @Column(length = 64)
    private String source; // quiz, web 등

    // MariaDB TEXT와 정확히 매핑
    @Column(name = "meta_json", columnDefinition = "TEXT")
    private String metaJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
