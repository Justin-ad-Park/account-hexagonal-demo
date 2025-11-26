package com.example.account.adapter.in.web;

import com.example.account.adapter.in.web.dto.ApiError;
import com.example.account.adapter.in.web.dto.ApiResponse;
import com.example.account.domain.exception.AccountNotFoundException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

// 전역 예외 처리기 (ApiResponse 패턴 적용 - 분리된 핸들러)
@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. AccountNotFoundException (404 Not Found 관련) 처리
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(AccountNotFoundException ex) {
        return returnApiResponseResponseEntity(ex.getMessage(), "NOT_FOUND");
    }

    // 2. IllegalArgumentException (400 Bad Request 관련) 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(IllegalArgumentException ex) {
        return returnApiResponseResponseEntity(ex.getMessage(), "BAD_REQUEST");
    }

    // 3. IllegalStateException (409 Conflict 관련) 처리
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflict(IllegalStateException ex) {
        return returnApiResponseResponseEntity(ex.getMessage(), "CONFLICT");
    }


     /* 최종 fallback: 잡히지 않은 모든 Exception (500 Internal Server Error) 처리 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleServerError(Exception ex) {
        return returnApiResponseResponseEntity("Internal Server Error occurred.", "SERVER_ERROR");
    }

    private static ResponseEntity<ApiResponse<Object>> returnApiResponseResponseEntity(String message, String code) {

        // HTTP 상태 코드는 200 OK를 반환하고, 응답 본문에 에러 정보 포함
        return ResponseEntity.ok(ApiResponse.failure(ApiError.of(code, message)));
    }
}
