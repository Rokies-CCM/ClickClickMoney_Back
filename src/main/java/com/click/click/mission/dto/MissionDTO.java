package com.click.click.mission.dto;

import com.click.click.mission.model.MissionCode;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class MissionDTO {

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MissionItem {
        private MissionCode code;
        private boolean completed;
        private boolean rewardClaimed;
        private Instant completedAt;
        private Instant claimedAt;
        private Integer reward;      // 포인트 보상 (null 가능)
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TodayResponse {
        private LocalDate date;
        private List<MissionItem> missions;
    }

    // --- Quiz Submit ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SubmitQuizRequest {
        // 둘 중 아무거나 사용 가능. passed가 명시되면 그대로 사용, 아니면 score/비율로 판단
        private Integer score;          // 0~100
        private Integer correctCount;   // 정답 수
        private Integer totalCount;     // 문제 수
        private Boolean passed;         // 명시적 통과 여부(우선순위 높음)
        private String  metaJson;       // 추가 정보(문항, 답안 등)
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SubmitQuizResponse {
        private boolean passed;
        private boolean newlyCompleted; // 이번 제출로 처음 완료 상태가 되었는지
        private Integer score;
        private Integer correctCount;
        private Integer totalCount;
    }

    // --- Claim ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ClaimRequest {
        private MissionCode code;       // 기본: QUIZ
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ClaimResponse {
        private MissionCode code;
        private boolean claimed;        // 이번 호출에서 지급되었는지
        private boolean alreadyClaimed; // 과거에 이미 지급된 상태였는지
        private Integer reward;         // 지급된 포인트(이미 지급된 상태면 null 가능)
    }

    // --- Lottery (하루 1회 무료 / 티켓 복권 공용 응답) ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlayLotteryResponse {
        /** 이번 호출에서 실제 포인트 지급 여부 */
        private boolean granted;
        /** 지급된 포인트 (granted=false면 null 혹은 0) */
        private Integer reward;
        /**
         * 일일 무료 복권 전용 플래그.
         * - true  : 오늘 이미 플레이함(지급 불가)
         * - false : 오늘 최초 플레이(지급 가능)
         * - null  : 티켓 복권처럼 일일 제한이 없는 경우
         */
        private Boolean alreadyPlayedToday;
    }
}
