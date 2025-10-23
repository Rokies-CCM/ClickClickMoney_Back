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
@Table(name = "points_wallet")
public class PointsWalletEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Long balance;

    @Column(name = "total_earned", nullable = false)
    private Long totalEarned;

    @Column(name = "total_spent", nullable = false)
    private Long totalSpent;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
