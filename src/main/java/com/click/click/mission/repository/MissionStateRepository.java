package com.click.click.mission.repository;

import com.click.click.mission.entity.MissionStateEntity;
import com.click.click.mission.model.MissionCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MissionStateRepository extends JpaRepository<MissionStateEntity, Long> {

    List<MissionStateEntity> findByUserIdAndDate(Long userId, LocalDate date);

    Optional<MissionStateEntity> findByUserIdAndDateAndMissionCode(Long userId, LocalDate date, MissionCode missionCode);

    boolean existsByUserIdAndDateAndMissionCodeAndCompletedTrue(Long userId, LocalDate date, MissionCode missionCode);

    boolean existsByUserIdAndDateAndMissionCodeAndRewardClaimedTrue(Long userId, LocalDate date, MissionCode missionCode);

    /**
     * MySQL 전용 안전 upsert: 중복 키면 아무 것도 하지 않음.
     * completed/reward_claimed 기본값은 false, 타임스탬프는 null로 삽입.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query(
            value = """
                INSERT INTO mission_state
                  (user_id, date, mission_code, completed, reward_claimed, completed_at, claimed_at)
                VALUES
                  (:userId, :date, :code, false, false, NULL, NULL)
                ON DUPLICATE KEY UPDATE user_id = user_id
                """,
            nativeQuery = true
    )
    int insertIgnore(@Param("userId") Long userId,
                     @Param("date") LocalDate date,
                     @Param("code") String code);
}
