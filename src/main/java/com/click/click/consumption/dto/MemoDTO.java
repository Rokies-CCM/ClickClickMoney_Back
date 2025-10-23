package com.click.click.consumption.dto;

import jakarta.validation.constraints.NotBlank;

public class MemoDTO {

    public record CreateRequest(
            @NotBlank(message = "메모 내용은 필수입니다.") String value
    ) {}

    public record UpdateRequest(
            @NotBlank(message = "메모 내용은 필수입니다.") String value
    ) {}

    public record Response(
            Long id,
            Long consumptionId,
            String value
    ) {}
}