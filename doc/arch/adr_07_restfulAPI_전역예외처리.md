# 7. restful API 전역 예외 처리
Date: 2025-08-29

# status
 Applied

# Context
- Web Contoller 에서 예외 발생에 대한 처리를 개별적으로 하는 것은 귀찮은 일이며, 
- 응답 값 변경이 발생하는 경우에도 API Spec을 통일시키기 위해 모든 API를 수정해야 하는 커플링이 발생한다. 

# Decision
- 예외(exception)가 throw되면 정해진 포맷에 맞게 API 예외 응답을 하는 ExceptionHandler 추가
- 기존 Controller의 response를 Spring에서 제공하는 ResponseEntity<T> 로 API의 응답을 변경 


```java
@RestController
@RequestMapping("/accounts")
@Validated
public class AccountController {
    // ... 생략 ... 
    @PostMapping
    public ResponseEntity<AccountResponse> create(/* 생략 */) {
        var acc = createAccountUseCase.createAccount(accountNumber, name, balance);
        var body = AccountResponse.of(acc);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);    //201 Created
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountResponse> deposit(/*...*/) {
        var acc = depositUseCase.deposit(accountNumber, new Amount(amount));
        var body = AccountResponse.of(acc);
        return ResponseEntity.ok(body); // 200 OK
    }
    // ... 생략 ... 
}
```


### ApiError.java 
 
```java
package com.example.account.adapter.in.web.dto;

// 공통 에러 DTO (web 계층)
public class ApiError {
    private final String code;
    private final String message;
    public ApiError(String code, String message) { this.code = code; this.message = message; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
```
### GlobalExceptionHandler
```java
package com.example.account.adapter.in.web;

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
}
```

--- 

## 사용 예
- Account 도메인 엔티티의 메서드 출금(withdraw)에서 계좌 잔액보다 출금 요청액이 많은 경우에 IllegalStateException 이 터진다.
#### [Account] 
```java
// ... 생략 ...
    void withdraw(Amount amount) {
        if (amount.getValue() <= 0) throw new IllegalArgumentException("Withdraw must be positive");
        if (balance < amount.getValue()) throw new IllegalStateException("Insufficient balance");
        balance -= amount.getValue();
    }
```

#### [GlobalExceptionHandler]
- 예외를 캐치해서 리턴한다. 
```java
// 전역 예외 처리기
@ControllerAdvice
public class GlobalExceptionHandler {
    // ... 
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("CONFLICT", ex.getMessage()));
    }

    // ... 
}
```

### [출금 API 호출 예]
```
POST http://localhost:8080/accounts/it-001/withdraw?amount=300

[response Header]
HTTP/1.1 409
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 28 Aug 2025 23:43:07 GMT

[response body]
{
"code": "CONFLICT",
"message": "Insufficient balance"
}
```


# Consequences


- 

