package com.click.click.budget.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BudgetDTO {

    @Getter
    @Setter
    public static class Request {

        @NotNull
        @JsonFormat(pattern = "yyyy-MM")
        private java.time.YearMonth month;

        @NotBlank
        private String category;

        @NotNull
        @PositiveOrZero
        private Long amount;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String month;
        private String category;
        private Long amount;
    }
}