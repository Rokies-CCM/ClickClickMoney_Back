package com.click.click.consumption.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyDashboardDTO {

    private String yearMonth;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate from;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate to;

    private Long totalAmount;
    private Long totalCount;

    // 해당 월 목표 예산(합산 결과)
    private Long targetBudget;

    // 카테고리 분포 (금액 기준)
    private List<ConsumptionSummaryDTO> byCategory;
}