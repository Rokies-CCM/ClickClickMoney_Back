package com.click.click.points.repository;

import com.click.click.points.entity.PointsWalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointsWalletRepository extends JpaRepository<PointsWalletEntity, Long> {
    Optional<PointsWalletEntity> findByUserId(Long userId);
}
