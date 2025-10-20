package com.click.click.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumptionDTO {

    private String question;

    @NotEmpty
    @Valid
    private List<Item> items;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        @NotNull
        private String category;
        @NotNull
        private BigInteger amount;
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConsumptionResponse {
        private String answer;
        private Object insights;
    }
}
