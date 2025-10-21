package com.click.click.budget.controller;


import com.click.click.budget.dto.BudgetDTO;
import com.click.click.budget.entity.BudgetEntity;
import com.click.click.budget.service.BudgetService;
import com.click.click.util.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ApiResponse<BudgetDTO.Response> upsert(@Valid @RequestBody BudgetDTO.Request body) {
        BudgetEntity saved = budgetService.upsert(
                body.getMonth(), body.getCategory(), body.getAmount()
        );
        return ApiResponse.ok(new BudgetDTO.Response(
                saved.getId().longValue(),
                saved.getYearMonth().toString().substring(0, 7),
                saved.getCategory().getName(),
                saved.getAmount()
        ));
    }

    @GetMapping
    public ApiResponse<List<BudgetEntity>> list(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth month
    ) {
        return ApiResponse.ok(budgetService.findByMonth(month));
    }

    @PutMapping("/{id}")
    public ApiResponse<BudgetEntity> updateAmount(
            @PathVariable
            Integer id,

            @RequestParam
            @Min(0)
            long amount
    ) {
        return ApiResponse.ok(budgetService.updateAmount(id, amount));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        budgetService.delete(id);
        return ApiResponse.ok(null);
    }
}