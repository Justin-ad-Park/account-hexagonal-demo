package com.example.account.adapter.in.web;

import com.example.account.adapter.in.web.dto.ApiError;
import com.example.account.adapter.in.web.dto.ApiResponse;
import com.example.account.domain.exception.AccountNotFoundException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

// 전역 예외 처리기 (ApiResponse 패턴 적용)
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외(AccountNotFoundException, IllegalArgumentException, IllegalStateException) 처리
     * 모든 예외를 잡아 ApiResponse.failure()로 래핑하여 200 OK 상태로 반환합니다.
     */
    @ExceptionHandler({AccountNotFoundException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Object>> handleBusinessExceptions(Exception ex) {

        final String code;
        final String message = ex.getMessage();

        // 발생한 예외 타입에 따라 응답 본문에 포함될 에러 코드(code)를 결정합니다.
        if (ex instanceof AccountNotFoundException) {
            // 404 관련 예외 처리
            code = "NOT_FOUND";
        } else if (ex instanceof IllegalArgumentException) {
            // 400 Bad Request 관련 예외 처리
            code = "BAD_REQUEST";
        } else if (ex instanceof IllegalStateException) {
            // 409 Conflict 관련 예외 처리
            code = "CONFLICT";
        } else {
            // 예상치 못한 기타 예외 처리 (만약을 대비한 기본값)
            code = "UNEXPECTED_ERROR";
        }

        // ApiError DTO를 생성하여 에러 정보 구성
        ApiError apiError = new ApiError(code, message);

        // ApiResponse.failure()를 사용하여 에러 응답을 래핑하고
        // HTTP 상태 코드는 200 OK를 반환합니다.
        return ResponseEntity.ok(ApiResponse.failure(apiError));
    }

    /**
     * (선택 사항) 서버 내부 에러 (500 Internal Server Error) 처리
     * 이 예외는 catch하지 못하고 넘어온 RuntimeException이나 Error를 처리할 수 있습니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleServerError(Exception ex) {
        // 서버 내부 오류는 500 상태 코드 대신 200 OK를 반환하되, 에러 코드를 'SERVER_ERROR'로 설정
        ApiError apiError = new ApiError("SERVER_ERROR", "Internal Server Error occurred.");
        return ResponseEntity.ok(ApiResponse.failure(apiError));
    }
}

/*
// 전역 예외 처리기
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiError("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(AccountNotFoundException.class) // 필요 시 커스텀
    public ResponseEntity<ApiError> handleNotFound(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("NOT_FOUND", ex.getMessage()));
    }
}*/
