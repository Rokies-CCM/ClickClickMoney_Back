package com.click.click.consumption.controller;

import com.click.click.consumption.dto.MemoDTO;
import com.click.click.consumption.service.MemoService;
import com.click.click.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/memo", "/memos"}) // 단수/복수 경로 모두 지원
@RequiredArgsConstructor
@Validated
public class MemoController {

    private final MemoService memoService;

    /**
     * 소비ID 기준 새 메모 추가 (업서트 용도로 프론트에서 사용)
     * POST /memo/{consumptionId}
     */
    @PostMapping("/{id}")
    public ApiResponse<MemoDTO.Response> createMemo(
            @PathVariable("id") Long consumptionId,
            @RequestBody @Valid MemoDTO.CreateRequest req
    ) {
        return ApiResponse.ok(memoService.addMemo(consumptionId, req.value()));
    }

    /**
     * 소비ID 기준 메모 목록 조회 (최신순을 가정)
     * GET /memo/{consumptionId}
     */
    @GetMapping("/{id}")
    public ApiResponse<List<MemoDTO.Response>> listMemos(@PathVariable("id") Long consumptionId) {
        return ApiResponse.ok(memoService.listMemos(consumptionId));
    }

    /**
     * 소비ID 기준 최신 메모 한 건만 조회 (디버깅/간편 조회용)
     * GET /memo/{consumptionId}/latest
     */
    @GetMapping("/{id}/latest")
    public ApiResponse<MemoDTO.Response> latestMemo(@PathVariable("id") Long consumptionId) {
        List<MemoDTO.Response> all = memoService.listMemos(consumptionId);
        MemoDTO.Response latest = (all != null && !all.isEmpty()) ? all.get(0) : null;
        return ApiResponse.ok(latest);
    }

    /**
     * 메모ID 기준 내용 수정
     * PUT /memo/{memoId}
     */
    @PutMapping("/{memoId}")
    public ApiResponse<MemoDTO.Response> updateMemo(
            @PathVariable Long memoId,
            @RequestBody @Valid MemoDTO.UpdateRequest req
    ) {
        return ApiResponse.ok(memoService.updateMemo(memoId, req.value()));
    }

    /**
     * 메모ID 기준 삭제
     * DELETE /memo/{memoId}
     */
    @DeleteMapping("/{memoId}")
    public ApiResponse<Void> deleteMemo(@PathVariable Long memoId) {
        memoService.deleteMemo(memoId);
        return ApiResponse.ok(null);
    }
}