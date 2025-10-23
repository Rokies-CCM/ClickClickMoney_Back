package com.click.click.budget.controller;

import com.click.click.budget.dto.BudgetDTO;
import com.click.click.budget.entity.BudgetEntity;
import com.click.click.budget.service.BudgetService;
import com.click.click.util.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping({"/budgets", "/budget"}) // 단수/복수 경로 모두 지원
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * 업서트(표준): POST /budgets
     * - 프론트는 body에 { "month":"yyyy-MM", "amount": 숫자 } 보냄
     */
    @PostMapping
    public ApiResponse<BudgetDTO.Response> upsert(@Valid @RequestBody BudgetDTO.Request body) {
        BudgetEntity saved = budgetService.upsert(body.getMonth(), body.getAmount());
        return ApiResponse.ok(toResponse(saved));
    }

    /**
     * 업서트(대체 경로/메서드):
     * - POST /budgets/upsert, PUT /budgets/upsert
     * - POST /budget/upsert,  PUT /budget/upsert
     * - POST /budgets/save,   PUT /budgets/save
     * - POST /budget/save,    PUT /budget/save
     * 모두 동일 로직으로 처리
     */
    @PostMapping({"/upsert", "/save"})
    public ApiResponse<BudgetDTO.Response> upsertAliasPost(@Valid @RequestBody BudgetDTO.Request body) {
        BudgetEntity saved = budgetService.upsert(body.getMonth(), body.getAmount());
        return ApiResponse.ok(toResponse(saved));
    }

    @PutMapping({"/upsert", "/save"})
    public ApiResponse<BudgetDTO.Response> upsertAliasPut(@Valid @RequestBody BudgetDTO.Request body) {
        BudgetEntity saved = budgetService.upsert(body.getMonth(), body.getAmount());
        return ApiResponse.ok(toResponse(saved));
    }

    /**
     * 월별 목록 조회:
     * GET /budgets?month=yyyy-MM  (또는 /budget?month=yyyy-MM)
     */
    @GetMapping
    public ApiResponse<List<BudgetDTO.Response>> list(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        List<BudgetEntity> rows = budgetService.findByMonth(month);
        List<BudgetDTO.Response> dto = rows.stream().map(this::toResponse).toList();
        return ApiResponse.ok(dto);
    }

    /**
     * 금액 수정(기존 ID 기준)
     * PUT /budgets/{id}?amount=12345
     */
    @PutMapping("/{id}")
    public ApiResponse<BudgetDTO.Response> updateAmount(
            @PathVariable Integer id,
            @RequestParam @Min(0) long amount
    ) {
        BudgetEntity updated = budgetService.updateAmount(id, amount);
        return ApiResponse.ok(toResponse(updated));
    }

    /**
     * 삭제
     * DELETE /budgets/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Integer id) {
        budgetService.delete(id);
        return ApiResponse.ok("deleted");
    }

    private BudgetDTO.Response toResponse(BudgetEntity e) {
        return new BudgetDTO.Response(
                e.getId() != null ? e.getId().longValue() : null,
                e.getBudgetMonth().toString().substring(0, 7),  // "yyyy-MM"
                e.getAmount()
        );
    }
}