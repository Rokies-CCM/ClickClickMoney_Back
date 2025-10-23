package com.click.click.mission.repository;

import com.click.click.mission.entity.QuizAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface QuizAttemptRepository extends JpaRepository<QuizAttemptEntity, Long> {

    boolean existsByUserIdAndDateAndPassedTrue(Long userId, LocalDate date);
}
