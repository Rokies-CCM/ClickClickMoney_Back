package com.click.click.consumption.controller;

import com.click.click.consumption.dto.ConsumptionDTO;
import com.click.click.consumption.dto.ConsumptionSearchDTO;
import com.click.click.consumption.dto.ConsumptionSummaryDTO;
import com.click.click.consumption.dto.MonthlyDashboardDTO;
import com.click.click.consumption.dto.ConsumptionImportResultDTO;
import com.click.click.consumption.service.ConsumptionService;
import com.click.click.util.ApiResponse;
import com.click.click.util.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/consumptions")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    /** 단건/배열 저장 (기존) */
    @PostMapping("/save")
    public ApiResponse<String> create(@Valid @RequestBody ConsumptionDTO request) {
        consumptionService.record(request);
        return ApiResponse.ok("저장됨");
    }

    /** 기간 + (선택)카테고리 페이지 조회 (기존) */
    @GetMapping("/load")
    public ApiResponse<PageResponse<ConsumptionSearchDTO>> load(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ConsumptionSearchDTO> data =
                consumptionService.findPage(startDate, endDate, category, page, size);
        return ApiResponse.ok(PageResponse.from(data));
    }

    /** 월(YYYY-MM) 기준 목록 페이지 조회 (프론트가 월을 넘길 때 편리) */
    @GetMapping("/list-by-month")
    public ApiResponse<PageResponse<ConsumptionSearchDTO>> listByMonth(
            @RequestParam String yearMonth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        Page<ConsumptionSearchDTO> data =
                consumptionService.findPage(from, to, null, page, size);
        return ApiResponse.ok(PageResponse.from(data));
    }

    /** 월(YYYY-MM) 대시보드 요약: 총지출/건수/카테고리 분포 (기존) */
    @GetMapping("/monthly")
    public ApiResponse<MonthlyDashboardDTO> monthly(
            @RequestParam String yearMonth
    ) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        return ApiResponse.ok(consumptionService.getMonthlyDashboard(from, to));
    }

    /** 요약 API (yearMonth 또는 year/month 둘 다 지원) */
    @GetMapping("/summary")
    public ApiResponse<MonthlyDashboardDTO> summary(
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        YearMonth ym = resolveYearMonth(yearMonth, year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        return ApiResponse.ok(consumptionService.getMonthlyDashboard(from, to));
    }

    /** CSV 업로드 (multipart/form-data, file 필드) */
    @PostMapping(value = "/upload-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ConsumptionImportResultDTO> uploadCsv(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(consumptionService.importCsv(file));
    }

    /** (선택) 카테고리 분포만 별도 조회가 필요할 때 */
    @GetMapping("/by-category")
    public ApiResponse<List<ConsumptionSummaryDTO>> byCategory(
            @RequestParam String yearMonth
    ) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        return ApiResponse.ok(consumptionService.summarize(from, to, null));
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @Min(0) Long amount
    ) {
        consumptionService.update(id, date, category, amount);
        return ApiResponse.ok("수정됨");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        consumptionService.delete(id);
        return ApiResponse.ok(null);
    }

    /* ---------- 내부 유틸 ---------- */
    private static YearMonth resolveYearMonth(String yearMonth, Integer year, Integer month) {
        if (yearMonth != null && !yearMonth.isBlank()) {
            return YearMonth.parse(yearMonth.trim());
        }
        if (Objects.nonNull(year) && Objects.nonNull(month)) {
            return YearMonth.of(year, month);
        }
        // 파라미터 없으면 현재 월
        return YearMonth.now();
    }
}