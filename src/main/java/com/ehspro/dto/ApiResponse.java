package com.ehspro.dto;

public record ApiResponse<T>(int statusCode, String message, T results) {
    public static <T> ApiResponse<T> success(T results) {
        return new ApiResponse<>(200, "Successful", results);
    }
}
