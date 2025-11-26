package com.example.account.adapter.in.web.dto;

// 공통 에러 DTO (web 계층)
public class ApiError {
    private final String code;
    private final String message;
    private ApiError(String code, String message) { this.code = code; this.message = message; }
    public String getCode() { return code; }
    public String getMessage() { return message; }

    public static ApiError of(String code, String message) {
        return new ApiError(code, message);
    }
}