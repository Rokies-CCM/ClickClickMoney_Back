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
                body.getMonth(), body.getAmount()
        );
        return ApiResponse.ok(toResponse(saved));
    }

    @GetMapping
    public ApiResponse<List<BudgetDTO.Response>> list(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        List<BudgetEntity> rows = budgetService.findByMonth(month);
        List<BudgetDTO.Response> dto = rows.stream().map(this::toResponse).toList();
        return ApiResponse.ok(dto);
    }

    @PutMapping("/{id}")
    public ApiResponse<BudgetDTO.Response> updateAmount(
            @PathVariable Integer id,
            @RequestParam @Min(0) long amount
    ) {
        BudgetEntity updated = budgetService.updateAmount(id, amount);
        return ApiResponse.ok(toResponse(updated));
    }

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