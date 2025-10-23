package com.click.click.points.repository;

import com.click.click.points.entity.PointsTxEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface PointsTxRepository extends JpaRepository<PointsTxEntity, Long> {
    Page<PointsTxEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 같은 source의 미션 보상 중복 지급 방지(하루 1회)
    boolean existsByUserIdAndReasonAndSourceAndCreatedAtAfter(
            Long userId, String reason, String source, Instant after
    );
}
