package com.click.click.points.service;

import com.click.click.points.dto.PointsDTO;
import com.click.click.points.entity.PointsTxEntity;
import com.click.click.points.entity.PointsWalletEntity;
import com.click.click.points.repository.PointsTxRepository;
import com.click.click.points.repository.PointsWalletRepository;
import com.click.click.user.entity.UserEntity;
import com.click.click.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsWalletRepository walletRepo;
    private final PointsTxRepository txRepo;
    private final UserRepository userRepo;

    private PointsWalletEntity getOrCreate(Long userId) {
        return walletRepo.findByUserId(userId).orElseGet(() -> {
            PointsWalletEntity w = PointsWalletEntity.builder()
                    .userId(userId)
                    .balance(0L)
                    .totalEarned(0L)
                    .totalSpent(0L)
                    .updatedAt(Instant.now())
                    .build();
            return walletRepo.save(w);
        });
    }

    private Long getUserIdByUsername(String username) {
        UserEntity u = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return u.getId();
    }

    /** 포인트 적립(일반). 트랜잭션 내에서 지갑/거래 모두 기록 */
    @Transactional
    public void award(Long userId, long amount, String reason, String source, String metaJson) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        PointsWalletEntity w = getOrCreate(userId);
        w.setBalance(w.getBalance() + amount);
        w.setTotalEarned(w.getTotalEarned() + amount);
        w.setUpdatedAt(Instant.now());
        walletRepo.save(w);

        PointsTxEntity tx = PointsTxEntity.builder()
                .userId(userId)
                .delta(amount)
                .reason(reason == null ? "UNKNOWN" : reason)
                .source(source)
                .metaJson(metaJson)
                .createdAt(Instant.now())
                .build();
        txRepo.save(tx);
    }

    /** 포인트 사용(차감) */
    @Transactional
    public void redeem(Long userId, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        PointsWalletEntity w = getOrCreate(userId);
        if (w.getBalance() < amount) {
            throw new IllegalStateException("보유 포인트가 부족합니다.");
        }
        w.setBalance(w.getBalance() - amount);
        w.setTotalSpent(w.getTotalSpent() + amount);
        w.setUpdatedAt(Instant.now());
        walletRepo.save(w);

        PointsTxEntity tx = PointsTxEntity.builder()
                .userId(userId)
                .delta(-amount)
                .reason("REDEEM")
                .source("web")
                .metaJson(null)
                .createdAt(Instant.now())
                .build();
        txRepo.save(tx);
    }

    /**
     * ✅ (v2 정석) 미션 보상 전용 헬퍼.
     * - 미션의 보상 지급은 MissionService에서 idempotent(중복 방지) 체크 후 이 메서드를 호출하세요.
     * - reason은 "MISSION_REWARD"로 고정, source에는 미션 코드(예: "quiz", "visit", "budget"...)를 넘깁니다.
     */
    @Transactional
    public void awardMissionReward(Long userId, long amount, String missionCode) {
        if (missionCode == null || missionCode.isBlank()) {
            throw new IllegalArgumentException("missionCode must not be blank");
        }
        award(userId, amount, "MISSION_REWARD", missionCode, null);
    }

    /**
     * (구 방식) points_tx만으로 '하루 1회' 보상 제어.
     * v2에서는 mission_state가 단일 진실원본이므로 사용 지양. 필요 시 하위 호환만 유지.
     */
    @Deprecated
    @Transactional
    public boolean awardDailyMissionIfNotYet(Long userId, long amount, String source, ZoneId zoneId) {
        ZoneId zone = (zoneId != null) ? zoneId : ZoneId.systemDefault();
        Instant startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        boolean already = txRepo.existsByUserIdAndReasonAndSourceAndCreatedAtAfter(
                userId, "MISSION_REWARD", source, startOfToday
        );
        if (already) return false;
        award(userId, amount, "MISSION_REWARD", source, null);
        return true;
    }

    /* ===================== 추가: 복권 전용 헬퍼 ===================== */

    /**
     * ✅ 무료 복권(하루 1회) 지급 헬퍼
     * - source를 "lottery_daily"로 고정하여 포인트 탭의 무제한 복권("lottery_unlimited")과 충돌 방지
     * - amount가 0이면 아무것도 하지 않고 false를 반환(지급 없음)
     */
    @Transactional
    public boolean awardLotteryDailyIfNotYet(Long userId, long amount, ZoneId zoneId) {
        if (amount <= 0) return false; // 0p면 지급하지 않음
        return awardDailyMissionIfNotYet(userId, amount, "lottery_daily", zoneId);
    }

    /**
     * ✅ 복권 이용권(무제한) 지급 헬퍼
     * - 일일 제한 없음. reason을 "LOTTERY", source를 "lottery_unlimited"로 기록
     * - amount가 0이면 아무것도 하지 않음(예: 꽝 처리)
     */
    @Transactional
    public void awardLotteryUnlimited(Long userId, long amount) {
        if (amount <= 0) return; // 0p면 지급하지 않음
        award(userId, amount, "LOTTERY", "lottery_unlimited", null);
    }

    /* ============================================================ */

    @Transactional(readOnly = true)
    public PointsDTO.SummaryResponse getSummary(Long userId, int recentSize) {
        PointsWalletEntity w = getOrCreate(userId);
        Page<PointsTxEntity> recent = txRepo.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(0, Math.max(1, recentSize))
        );
        List<PointsDTO.TxResponse> recentMapped = recent.getContent().stream().map(t ->
                PointsDTO.TxResponse.builder()
                        .id(t.getId())
                        .delta(t.getDelta())
                        .reason(t.getReason())
                        .source(t.getSource())
                        .metaJson(t.getMetaJson())
                        .createdAt(t.getCreatedAt())
                        .build()
        ).toList();

        return PointsDTO.SummaryResponse.builder()
                .balance(w.getBalance())
                .totalEarned(w.getTotalEarned())
                .totalSpent(w.getTotalSpent())
                .recent(recentMapped)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<PointsDTO.TxResponse> getTxPage(Long userId, int page, int size) {
        Page<PointsTxEntity> p = txRepo.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(Math.max(0, page), Math.max(1, size))
        );
        return p.map(t -> PointsDTO.TxResponse.builder()
                .id(t.getId())
                .delta(t.getDelta())
                .reason(t.getReason())
                .source(t.getSource())
                .metaJson(t.getMetaJson())
                .createdAt(t.getCreatedAt())
                .build());
    }

    // 헬퍼: username -> userId
    @Transactional(readOnly = true)
    public Long userIdFromUsername(String username) {
        return getUserIdByUsername(username);
    }
}
