package com.click.click.util;


import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;


    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).data(data).build();
    }
    public static <T> ApiResponse<T> fail(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }
}