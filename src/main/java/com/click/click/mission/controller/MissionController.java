package com.click.click.mission.controller;

import com.click.click.mission.dto.MissionDTO;
import com.click.click.mission.model.MissionCode;
import com.click.click.mission.service.MissionService;
import com.click.click.points.service.PointsService;
import com.click.click.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.ZoneId;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;
    private final PointsService pointsService;

    private Long userId(Principal principal) {
        if (principal == null) throw new IllegalStateException("인증 정보가 없습니다.");
        return pointsService.userIdFromUsername(principal.getName());
    }

    private ZoneId zone() {
        return ZoneId.systemDefault();
    }

    /** 오늘 미션 현황 — 캐시 금지(304 방지) */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<MissionDTO.TodayResponse>> today(Principal principal) {
        Long uid = userId(principal);
        MissionDTO.TodayResponse body = missionService.getToday(uid, zone());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(ApiResponse.ok(body));
    }

    /** 금융 퀴즈 제출 */
    @PostMapping("/quiz/submit")
    public ResponseEntity<ApiResponse<MissionDTO.SubmitQuizResponse>> submitQuiz(
            Principal principal, @RequestBody MissionDTO.SubmitQuizRequest req) {
        Long uid = userId(principal);
        MissionDTO.SubmitQuizResponse body = missionService.submitQuiz(uid, req, zone());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(ApiResponse.ok(body));
    }

    /** 보상 수령 (신규 플로우: 미션 완료 후 명시적으로 수령) */
    @PostMapping("/claim")
    public ResponseEntity<ApiResponse<MissionDTO.ClaimResponse>> claim(
            Principal principal, @RequestBody(required = false) MissionDTO.ClaimRequest req) {
        Long uid = userId(principal);
        MissionCode code = (req != null && req.getCode() != null) ? req.getCode() : MissionCode.QUIZ;
        MissionDTO.ClaimResponse body = missionService.claim(uid, code, zone());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(ApiResponse.ok(body));
    }

    /**
     * ✅ 레거시 호환: /missions/complete
     * - 프론트가 source/amount 쿼리로 호출하는 기존 방식 지원
     * - DB(mission_state)에 "완료/수령"을 기록하고, 포인트는 mission_state로 멱등 제어
     */
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<MissionDTO.ClaimResponse>> completeLegacy(
            Principal principal,
            @RequestParam(name = "source", required = false, defaultValue = "quiz") String source,
            @RequestParam(name = "amount", required = false) Integer amount
    ) {
        Long uid = userId(principal);
        MissionDTO.ClaimResponse body = missionService.completeLegacy(uid, source, amount, zone());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(ApiResponse.ok(body));
    }

    /**
     * 하루 1회 무료 복권 플레이
     * - MissionService.playDailyLottery는 PlayLotteryResponse를 반환
     */
    @PostMapping("/lottery/daily/play")
    public ResponseEntity<ApiResponse<MissionDTO.PlayLotteryResponse>> playDailyLottery(Principal principal) {
        Long uid = userId(principal);
        MissionDTO.PlayLotteryResponse body = missionService.playDailyLottery(uid, zone());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(ApiResponse.ok(body));
    }
}
