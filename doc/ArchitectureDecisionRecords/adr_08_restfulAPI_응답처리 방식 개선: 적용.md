# 8. adr_08_restfulAPI_응답처리 방식 개선
Date: 2025-11-34

# status
 Applied

# Context
- Web Contoller의 정상 응답과 예외 응답의 Response 타입이 달라 클라이언트의 로직이 복잡해 지는 것을 개선

# Decision
- 예외(exception)가 throw되면 정해진 포맷에 맞게 API 예외 응답을 하는 ExceptionHandler 추가
- Controller의 response를 ResponseEntity<AccountResponse>에서 ResponseEntity<ApiResponse<AccountResponse>>로
- 한번 더 감싸서 정상 응답과 실패 응답 모두 

# Consequences

### 공통 응답 DTO 추가

```java
package com.example.account.adapter.in.web.dto;

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
```


### Controller 응답 방식 변경
 
```java
    @GetMapping("/{accountNumber}")
public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable @NotBlank String accountNumber) {
    var acc = getAccountQuery.getAccount(accountNumber);
    var body = AccountResponse.of(acc);
    // 항상 200 OK와 함께 ApiResponse.success()를 반환
    return ResponseEntity.ok(ApiResponse.success(body));
}
}
```
### GlobalExceptionHandler
```java
package com.example.account.adapter.in.web;
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
```

--- 

## 사용 예
- Account 도메인 엔티티의 메서드 출금(withdraw)에서 계좌 잔액보다 출금 요청액이 많은 경우에 IllegalStateException 이 터진다.
#### [Account] 
```java
    @Test @Order(4)
void 계좌조회() {
    var response = restTemplate.exchange(
            url("/accounts/" + ACC_NO),
            HttpMethod.GET,
            null,
            ACCOUNT_API_RESPONSE_TYPE
    );

    var account = AccountTestHelper.extractData(response);

    assertThat(account.getBalance()).isEqualTo(1200L);
}
```

#### API 예외가 발생한 경우 - [GlobalExceptionHandler]
```java
   @Test @Order(5)
void 없는계좌조회_shouldHandleNotFound() {
    // 실패 응답은 데이터 타입이 null이므로 <Object>로 받거나,
    // 테스트의 일관성을 위해 <AccountResponse>를 유지하되 데이터 필드는 무시합니다.
    var response = restTemplate.exchange(
            url("/accounts/noAccount"),
            HttpMethod.GET,
            null,
            ACCOUNT_API_RESPONSE_TYPE
    );

    // 1. 상태 코드는 여전히 200 OK여야 함
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    var apiResponse = response.getBody();
    assertThat(apiResponse).isNotNull();

    // 2. success 필드가 false인지 확인 (비정상 로직 처리)
    assertThat(apiResponse.isSuccess()).isFalse();

    // 3. 에러 정보 확인
    ApiError error = apiResponse.getError();
    assertThat(error).isNotNull();
    // AccountNotFoundException을 GlobalExceptionHandler에서 "NOT_FOUND"로 처리했는지 확인
    assertThat(error.getCode()).isEqualTo("NOT_FOUND");
}
}
```

