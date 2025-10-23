package com.click.click.mission.entity;

import com.click.click.mission.model.MissionCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "mission_state",
        uniqueConstraints = @UniqueConstraint(name = "uk_state", columnNames = {"user_id", "date", "mission_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionStateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_code", nullable = false, length = 32)
    private MissionCode missionCode;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "reward_claimed", nullable = false)
    private boolean rewardClaimed;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "claimed_at")
    private Instant claimedAt;
}
