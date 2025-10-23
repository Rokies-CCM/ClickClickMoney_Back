package com.click.click.consumption.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumptionImportResultDTO {
    private int insertedCount;
    private int skippedCount;
    private int errorCount;
    private List<String> errors; // 상위 20개까지만
}