# 2. http client plug-in(intelliJ) 추가
Date: 2025-08-12

# status
 Applied

# Context
restful API의 반복 테스트를 쉽게 하기 위해 http client plug-in 추가

# Decision

### http client 설치
- IntelliJ -> Settings -> Plug-in 

### http 테스트 파일 작성 
- test\httprequest 폴더 생성
- 테스트 파일 추가 
- 1create_account.http 
```http request
### 1. 계좌 생성
POST http://localhost:8080/accounts?accountNumber=it-001&name=Bob&balance=1000
Content-Type: application/json
```
  - 2deposit.http
```http request
### 2. 입금
POST http://localhost:8080/accounts/it-001/deposit?amount=500
Content-Type: application/json
```

- 3withdraw.http
```http request
### 3. 출금
POST http://localhost:8080/accounts/it-001/withdraw?amount=300
Content-Type: application/json
```


# Consequences
restful API의 실제 호출 서비스 및 DB 변경 확인을 쉽게 할 수 있게 됨


