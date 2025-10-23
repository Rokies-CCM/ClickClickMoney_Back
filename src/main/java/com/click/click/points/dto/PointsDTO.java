package com.click.click.points.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

public class PointsDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TxResponse {
        private Long id;
        private Long delta;
        private String reason;
        private String source;
        private String metaJson;
        private Instant createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SummaryResponse {
        private Long balance;
        private Long totalEarned;
        private Long totalSpent;
        private List<TxResponse> recent; // 최근 N건
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RedeemRequest {
        private Integer amount; // 양수
    }

    // 내부 적립 API 바디
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AwardRequest {
        private Long userId;
        private Integer amount; // 양수
        private String reason;  // e.g. MISSION_REWARD
        private String source;  // e.g. quiz
        private String metaJson;
    }
}
