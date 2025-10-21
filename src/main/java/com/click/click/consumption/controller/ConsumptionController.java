package com.click.click.consumption.controller;

import com.click.click.consumption.dto.ConsumptionDTO;
import com.click.click.consumption.dto.ConsumptionSearchDTO;
import com.click.click.util.ApiResponse;
import com.click.click.consumption.service.ConsumptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/consumptions")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    @PostMapping("/save")
    public ApiResponse<String> create(@Valid @RequestBody ConsumptionDTO request) {
        consumptionService.record(request);
        return ApiResponse.ok("저장됨");
    }

    @GetMapping("/load")
    public ApiResponse<Page<ConsumptionSearchDTO>> load(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    )

    {
        Page<ConsumptionSearchDTO> data =
                consumptionService.findPage(startDate, endDate, category, page, size);
        return ApiResponse.ok(data);
    }
    @PutMapping("/{id}")
    public ApiResponse<String> update(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(required = false)
            String category,

            @RequestParam(required = false)
            @Min(0)
            Long amount
    ) {
        consumptionService.update(id, date, category, amount);
        return ApiResponse.ok("수정됨");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        consumptionService.delete(id);
        return ApiResponse.ok(null);
    }
}