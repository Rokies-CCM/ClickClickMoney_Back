package com.click.click.controller;

import com.click.click.dto.ConsumptionDTO;
import com.click.click.util.ApiResponse;
import com.click.click.service.ConsumptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consumptions")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    @PostMapping
    public ApiResponse<String> create(@Valid @RequestBody ConsumptionDTO request) {
        consumptionService.record(request);
        return ApiResponse.ok("저장됨");
    }
}
