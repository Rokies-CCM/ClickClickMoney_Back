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

    // 카테고리 분포
    private List<ConsumptionSummaryDTO> byCategory;
}
