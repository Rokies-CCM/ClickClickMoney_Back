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
@RequestMapping("/memo")
@RequiredArgsConstructor
@Validated
public class MemoController {

    private final MemoService memoService;

    @PostMapping("/{id}")
    public ApiResponse<MemoDTO.Response> createMemo(
            @PathVariable("id") Long consumptionId,
            @RequestBody @Valid MemoDTO.CreateRequest req
    ) {
        return ApiResponse.ok(memoService.addMemo(consumptionId, req.value()));
    }

    @GetMapping("/{id}")
    public ApiResponse<List<MemoDTO.Response>> listMemos(@PathVariable("id") Long consumptionId) {
        return ApiResponse.ok(memoService.listMemos(consumptionId));
    }

    @PutMapping("/{memoId}")
    public ApiResponse<MemoDTO.Response> updateMemo(
            @PathVariable Long memoId,
            @RequestBody @Valid MemoDTO.UpdateRequest req
    ) {
        return ApiResponse.ok(memoService.updateMemo(memoId, req.value()));
    }


    @DeleteMapping("/{memoId}")
    public ApiResponse<Void> deleteMemo(@PathVariable Long memoId) {
        memoService.deleteMemo(memoId);
        return ApiResponse.ok(null);
    }
}
