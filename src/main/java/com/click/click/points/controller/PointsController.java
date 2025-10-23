// path : PointsController.java
package com.click.click.points.controller;

import com.click.click.points.dto.PointsDTO;
import com.click.click.points.service.PointsService;
import com.click.click.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    // ✅ 기본값 부여(환경변수 없을 때 부트 실패 방지)
    @Value("${app.ai.internal-key:dev-ai-key-change-me}")
    private String internalKey;

    // === 사용자용 ===
    @GetMapping("/points/me")
    public ApiResponse<PointsDTO.SummaryResponse> mySummary(Authentication auth,
                                                            @RequestParam(name = "recent", defaultValue = "5") int recent) {
        Long userId = pointsService.userIdFromUsername(auth.getName());
        return ApiResponse.ok(pointsService.getSummary(userId, recent));
    }

    // ✅ PageImpl 경고 제거: 안정적인 DTO로 래핑하여 반환
    @GetMapping("/points/tx")
    public ApiResponse<PageResult<PointsDTO.TxResponse>> myTx(Authentication auth,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "50") int size) {
        Long userId = pointsService.userIdFromUsername(auth.getName());
        Page<PointsDTO.TxResponse> p = pointsService.getTxPage(userId, page, size);
        PageResult<PointsDTO.TxResponse> dto = PageResult.of(p);
        return ApiResponse.ok(dto);
    }

    @PostMapping("/points/redeem")
    public ApiResponse<String> redeem(Authentication auth, @RequestBody PointsDTO.RedeemRequest req) {
        Long userId = pointsService.userIdFromUsername(auth.getName());
        pointsService.redeem(userId, req.getAmount());
        return ApiResponse.ok("OK");
    }

    /** ✅ 카탈로그 복권 플레이(무제한) — 0~50p 랜덤 보상, reason=LOTTERY_REWARD, source=catalog */
    @PostMapping("/points/lottery/catalog/play")
    public ApiResponse<LotteryPlayResponse> playCatalogLottery(Authentication auth) {
        Long userId = pointsService.userIdFromUsername(auth.getName());
        int reward = ThreadLocalRandom.current().nextInt(0, 51);
        if (reward > 0) {
            pointsService.award(userId, reward, "LOTTERY_REWARD", "catalog", null);
        }
        return ApiResponse.ok(new LotteryPlayResponse(reward));
    }

    // === 내부 서버-투-서버(예: AI가 미션 보상 적립) ===
    @PostMapping("/internal/points/award")
    public ApiResponse<String> internalAward(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                             @RequestHeader(value = "X-AI-KEY", required = false) String xKey,
                                             @RequestBody PointsDTO.AwardRequest req) {
        if (xKey == null || !xKey.equals(internalKey)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "invalid internal key");
        }
        pointsService.award(req.getUserId(), req.getAmount(), req.getReason(), req.getSource(), req.getMetaJson());
        return ApiResponse.ok("OK");
    }

    /* ---------- 안정적인 페이지 DTO ---------- */
    public record PageResult<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            int numberOfElements
    ) {
        public static <T> PageResult<T> of(Page<T> p) {
            return new PageResult<>(
                    p.getContent(),
                    p.getNumber(),
                    p.getSize(),
                    p.getTotalElements(),
                    p.getTotalPages(),
                    p.isFirst(),
                    p.isLast(),
                    p.getNumberOfElements()
            );
        }
    }

    /* ---------- 카탈로그 복권 응답 DTO ---------- */
    public record LotteryPlayResponse(int reward) {}
}
