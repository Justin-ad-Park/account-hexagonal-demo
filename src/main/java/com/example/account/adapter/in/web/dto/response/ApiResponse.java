package com.example.account.adapter.in.web.dto.response;

import com.example.account.adapter.in.web.dto.ApiError;

// 서버 측에서 사용할 공통 응답 DTO
public class ApiResponse<T> {
    private final boolean success; // 성공 여부
    private final T data;          // 성공 시 반환할 데이터
    private final ApiError error;  // 실패 시 반환할 에러 정보 (기존 ApiError 재사용)

    // 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    // 실패 응답 (ApiError를 인자로 받음)
    public static <T> ApiResponse<T> failure(ApiError error) {
        return new ApiResponse<>(false, null, error);
    }

    private ApiResponse(boolean success, T data, ApiError error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ApiError getError() { return error; }
}