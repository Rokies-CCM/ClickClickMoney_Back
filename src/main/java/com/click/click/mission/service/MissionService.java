package com.click.click.mission.service;

import com.click.click.mission.dto.MissionDTO;
import com.click.click.mission.entity.MissionStateEntity;
import com.click.click.mission.entity.QuizAttemptEntity;
import com.click.click.mission.model.MissionCode;
import com.click.click.mission.repository.MissionStateRepository;
import com.click.click.mission.repository.QuizAttemptRepository;
import com.click.click.points.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionStateRepository stateRepo;
    private final QuizAttemptRepository quizRepo;
    private final PointsService pointsService;

    // 미션 기본 보상
    private static final int QUIZ_REWARD = 30;
    private static final int VISIT_REWARD = 20;
    private static final int BUDGET_REWARD = 20;
    private static final int EXPENSE_REWARD = 20;
    private static final int LOTTERY_REWARD = 0;   // 복권은 별도 흐름
    private static final int EXCHANGE_REWARD = 0;  // 교환도 별도 흐름

    private LocalDate today(ZoneId zone) {
        ZoneId z = (zone != null) ? zone : ZoneId.systemDefault();
        return LocalDate.now(z);
    }

    /**
     * 동시성 안전 upsert:
     * - 먼저 조회
     * - 없으면 INSERT IGNORE (중복 시 무시)
     * - 그리고 재조회하여 반환
     * 예외 기반(save → catch) 방식은 세션 오류를 남길 수 있어 피함.
     */
    private MissionStateEntity upsertState(Long userId, LocalDate date, MissionCode code) {
        return stateRepo.findByUserIdAndDateAndMissionCode(userId, date, code)
                .orElseGet(() -> {
                    stateRepo.insertIgnore(userId, date, code.name());
                    return stateRepo.findByUserIdAndDateAndMissionCode(userId, date, code)
                            .orElseThrow();
                });
    }

    private Integer rewardOf(MissionCode code) {
        return switch (code) {
            case QUIZ -> QUIZ_REWARD;
            case VISIT -> VISIT_REWARD;
            case BUDGET -> BUDGET_REWARD;
            case EXPENSE -> EXPENSE_REWARD;
            case LOTTERY -> LOTTERY_REWARD;
            case EXCHANGE -> EXCHANGE_REWARD;
        };
    }

    private MissionCode parseSource(String source) {
        if (source == null) return MissionCode.QUIZ;
        String s = source.trim().toLowerCase();
        return switch (s) {
            case "quiz" -> MissionCode.QUIZ;
            case "visit" -> MissionCode.VISIT;
            case "budget" -> MissionCode.BUDGET;
            case "expense" -> MissionCode.EXPENSE;
            case "lottery" -> MissionCode.LOTTERY;
            case "exchange" -> MissionCode.EXCHANGE;
            default -> MissionCode.QUIZ;
        };
    }

    /** 오늘의 미션 현황 조회 */
    @Transactional
    public MissionDTO.TodayResponse getToday(Long userId, ZoneId zone) {
        LocalDate date = today(zone);

        EnumSet<MissionCode> all = EnumSet.of(
                MissionCode.QUIZ, MissionCode.VISIT, MissionCode.BUDGET,
                MissionCode.EXPENSE, MissionCode.LOTTERY, MissionCode.EXCHANGE
        );

        List<MissionDTO.MissionItem> items = new ArrayList<>();

        for (MissionCode code : all) {
            MissionStateEntity st = upsertState(userId, date, code);

            // QUIZ: 오늘 최초 통과가 있는데 completed=false라면 보정
            if (code == MissionCode.QUIZ && !st.isCompleted()) {
                boolean hasPassed = quizRepo.existsByUserIdAndDateAndPassedTrue(userId, date);
                if (hasPassed) {
                    st.setCompleted(true);
                    st.setCompletedAt(Instant.now());
                    stateRepo.save(st);
                }
            }

            items.add(MissionDTO.MissionItem.builder()
                    .code(code)
                    .completed(st.isCompleted())
                    .rewardClaimed(st.isRewardClaimed())
                    .completedAt(st.getCompletedAt())
                    .claimedAt(st.getClaimedAt())
                    .reward(rewardOf(code))
                    .build());
        }

        return MissionDTO.TodayResponse.builder()
                .date(date)
                .missions(items)
                .build();
    }

    /** 퀴즈 제출/채점 기록(점수/통과 여부) + 상태 반영 */
    @Transactional
    public MissionDTO.SubmitQuizResponse submitQuiz(Long userId, MissionDTO.SubmitQuizRequest req, ZoneId zone) {
        LocalDate date = today(zone);

        boolean passed;
        Integer score = req.getScore();
        Integer correct = req.getCorrectCount();
        Integer total = req.getTotalCount();

        if (req.getPassed() != null) {
            passed = req.getPassed();
            if (score == null && correct != null && total != null && total > 0) {
                score = Math.round((correct * 100f) / total);
            }
            if (score == null) score = 0;
        } else if (score != null) {
            passed = score >= 70; // 기본 임계치
        } else if (correct != null && total != null && total > 0) {
            score = Math.round((correct * 100f) / total);
            passed = score >= 70;
        } else {
            throw new IllegalArgumentException("점수 또는 정답 수/문항 수, 혹은 passed 중 하나는 필요합니다.");
        }

        QuizAttemptEntity attempt = QuizAttemptEntity.builder()
                .userId(userId)
                .date(date)
                .score(score)
                .passed(passed)
                .metaJson(req.getMetaJson())
                .createdAt(Instant.now())
                .build();
        quizRepo.save(attempt);

        boolean newlyCompleted = false;
        MissionStateEntity st = upsertState(userId, date, MissionCode.QUIZ);
        if (passed && !st.isCompleted()) {
            st.setCompleted(true);
            st.setCompletedAt(Instant.now());
            stateRepo.save(st);
            newlyCompleted = true;
        }

        return MissionDTO.SubmitQuizResponse.builder()
                .passed(passed)
                .newlyCompleted(newlyCompleted)
                .score(score)
                .correctCount(correct)
                .totalCount(total)
                .build();
    }

    /** 보상 수령(신규 플로우) — mission_state만으로 멱등 제어 */
    @Transactional
    public MissionDTO.ClaimResponse claim(Long userId, MissionCode code, ZoneId zone) {
        LocalDate date = today(zone);

        MissionStateEntity st = stateRepo.findByUserIdAndDateAndMissionCode(userId, date, code)
                .orElseGet(() -> upsertState(userId, date, code));

        if (!st.isCompleted()) {
            throw new IllegalStateException("아직 미션을 완료하지 않았습니다.");
        }

        if (st.isRewardClaimed()) {
            return MissionDTO.ClaimResponse.builder()
                    .code(code)
                    .claimed(false)
                    .alreadyClaimed(true)
                    .reward(null)
                    .build();
        }

        Integer reward = rewardOf(code);
        if (reward != null && reward > 0) {
            // v2 정식 API로 지급 (중복 방지는 mission_state가 담당)
            pointsService.awardMissionReward(userId, reward, code.name().toLowerCase());
        }

        st.setRewardClaimed(true);
        st.setClaimedAt(Instant.now());
        stateRepo.save(st);

        return MissionDTO.ClaimResponse.builder()
                .code(code)
                .claimed(true)
                .alreadyClaimed(false)
                .reward(reward)
                .build();
    }

    /**
     * ✅ 레거시 호환: /missions/complete
     * - source/amount를 받아 즉시 완료 + 수령 처리
     * - 포인트 지급은 mission_state(rewardClaimed)로 멱등 제어
     */
    @Transactional
    public MissionDTO.ClaimResponse completeLegacy(Long userId, String source, Integer amount, ZoneId zone) {
        LocalDate date = today(zone);
        MissionCode code = parseSource(source);

        MissionStateEntity st = upsertState(userId, date, code);

        // 완료 처리(아직 미완료였다면 완료 시각 기록)
        if (!st.isCompleted()) {
            st.setCompleted(true);
            st.setCompletedAt(Instant.now());
        }

        // 보상 계산
        int reward = (amount != null && amount > 0) ? amount : (rewardOf(code) != null ? rewardOf(code) : 0);

        boolean grantedNow = false;
        if (!st.isRewardClaimed()) {
            if (reward > 0) {
                pointsService.awardMissionReward(userId, reward, code.name().toLowerCase());
                grantedNow = true;
            }
            st.setRewardClaimed(true);
            st.setClaimedAt(Instant.now());
            stateRepo.save(st);
        } else {
            // 이미 수령된 상태
            grantedNow = false;
        }

        return MissionDTO.ClaimResponse.builder()
                .code(code)
                .claimed(grantedNow)
                .alreadyClaimed(!grantedNow)
                .reward(reward)
                .build();
    }

    /**
     * ✅ 하루 1회 무료 복권 플레이
     * - 0~50p 랜덤 보상
     * - 오늘 이미 플레이했다면 재지급/재완료 불가
     * - 상태(mission_state) 완료/수령 기록
     * - 응답: PlayLotteryResponse (프론트에서 보상 안내에 사용)
     */
    @Transactional
    public MissionDTO.PlayLotteryResponse playDailyLottery(Long userId, ZoneId zone) {
        LocalDate date = today(zone);
        MissionStateEntity st = upsertState(userId, date, MissionCode.LOTTERY);

        // 이미 오늘 수령했으면 차단
        if (st.isRewardClaimed()) {
            return MissionDTO.PlayLotteryResponse.builder()
                    .granted(false)
                    .reward(null)
                    .alreadyPlayedToday(true)
                    .build();
        }

        // 0~50 랜덤 보상
        int reward = ThreadLocalRandom.current().nextInt(0, 51);

        // 완료 플래그 및 시각
        if (!st.isCompleted()) {
            st.setCompleted(true);
            st.setCompletedAt(Instant.now());
        }

        // 무료 복권 지급 (mission_state로 멱등 제어)
        if (reward > 0) {
            pointsService.awardMissionReward(userId, reward, "lottery_daily");
        }

        // 수령 처리(오늘 소진)
        st.setRewardClaimed(true);
        st.setClaimedAt(Instant.now());
        stateRepo.save(st);

        return MissionDTO.PlayLotteryResponse.builder()
                .granted(true)                 // 0p라도 플레이는 성공 처리
                .reward(reward)                // 0일 수도 있음
                .alreadyPlayedToday(false)
                .build();
    }
}
