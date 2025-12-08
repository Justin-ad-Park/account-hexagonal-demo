# ADR-10: Application Port Command 도입 및 Request/Response DTO 분리 (Response만 Domain 의존 허용)

- Status: Accepted
- Date: 2025-12-08
- Version: ver10 (from ver09)

---

## Context

기존 구조(ver07~ver09)는 다음과 같은 특징을 갖고 있었다.

- `application.port.in` 패키지에 유스케이스 인터페이스(Port)가 정의되어 있었으나  
  메서드 시그니처는 대부분 primitive 타입을 직접 받는 방식이었다.
- `adapter.in.web.dto` 패키지에 Request/Response DTO가 혼재되어  
  **입력/출력 책임이 구분되지 않았고**, 일부 DTO는 domain model에 직접 의존하고 있었다.
- Controller는 Request DTO 또는 primitive 값을 받아 유스케이스를 호출했으나,  
  **Command 객체가 없어 유스케이스 입력을 의미 있게 표현하기 어려웠다.**
- ArchUnit 규칙은 DTO와 Port 구조가 단순한 전제를 기반으로 설계되어 있었으나,  
  DTO 분리 및 Command 추가 후에는 공정한 검증이 어려웠다.

헥사고날 아키텍처 목표인 **명확한 계층 경계**,  
**domain model에 대한 엄격한 접근 통제**,  
**유스케이스 중심의 API 정의**를 달성하기 위해 다음의 개선 요구가 있었다.

1. 유스케이스 입력을 primitive 묶음이 아닌 Command 객체로 캡슐화할 것
2. Request/Response DTO를 분리하여 domain 의존 범위를 명확히 할 것
3. Response DTO만 domain 의존을 허용하고 Request DTO는 금지할 것
4. ArchUnit 규칙으로 설계를 자동 검증할 것

---

## Decision

### 1) `application.port.in.command` 패키지 도입 및 Command 기반 Port 시그니처 적용

유스케이스 입력을 명확한 구조로 표현하기 위해 Command 타입을 추가한다.

``` java
// application/port/in/command/DepositCommand.java
public record DepositCommand(String accountNumber, long amount) {}
```

``` java
// application/port/in/DepositUseCase.java
public interface DepositUseCase {
    Account deposit(DepositCommand command);
}
```

---

### 2) DTO를 Request / Response 로 명확히 분리

`adapter.in.web.dto` 를 다음처럼 분리한다.

```
adapter.in.web.dto.request
adapter.in.web.dto.response
```

- Request DTO → domain model 의존 금지
- Response DTO → domain model 의존 허용

예:

``` java
// adapter.in.web.dto.request.CreateAccountRequest
public record CreateAccountRequest(String accountNumber, String name, long balance) {}
```

``` java
// adapter.in.web.dto.response.AccountResponse
public record AccountResponse(String accountNumber, String name, long balance) {
    public static AccountResponse of(Account account) {
        return new AccountResponse(
            account.getAccountNumber(),
            account.getName(),
            account.getBalance()
        );
    }
}
```

---

### 3) Controller 구조 개선

Controller는 다음 역할만 수행한다:

1. Request DTO를 JSON Body로 받음
2. Request → Command 변환
3. 유스케이스 호출
4. Domain → Response DTO 변환

Controller는 domain model을 직접 참조하지 않는다.

---

### 4) ArchUnit 규칙 수정

#### 4.1 Inbound Adapter가 의존 가능한 패키지 확장

Command 패키지 허용:

``` java
APP_PORT_IN_COMMAND = "..application.port.in.command..";
private static final String ADAPTER_IN = "..adapter..in..";
private static final String APP_PORT_IN = "..application..port..in..";
@ArchTest
static final ArchRule inbound_adapters_should_depend_on_app_in_ports_or_service =
        classes().that().resideInAPackage(ADAPTER_IN)
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        ADAPTER_IN, APPLICATION, APP_PORT_IN, APP_PORT_IN_COMMAND, APP_SERVICE, DOMAIN,
                        "java..", "javax..", "jakarta..", "org.slf4j..", "org.springframework..", "com.fasterxml.jackson.."
                );
```

#### 4.2 Port 인터페이스 규칙 보완 (Command는 제외)

``` java
classes().that().resideInAPackage(APP_PORT_IN)
    .and(not(resideInAnyPackage(APP_PORT_IN_COMMAND)))
    .or().resideInAPackage(APP_PORT_OUT)
    .should().beInterfaces();
```

#### 4.3 Request / Response DTO 도메인 의존 규칙

- Request DTO → domain 의존 금지
- Response DTO → domain 의존 허용

``` java
// Request DTO는 domain 금지
noClasses()
    .that().resideInAPackage(ADAPTER_IN_REQUEST)
    .should().dependOnClassesThat().resideInAPackage(DOMAIN_MODEL);
```

``` java
// Inbound adapter는 domain 금지, 단 response DTO는 예외
noClasses()
    .that().resideInAPackage(ADAPTER_IN)
    .and(not(resideInAnyPackage(ADAPTER_IN_RESPONSE)))
    .should().dependOnClassesThat().resideInAPackage(DOMAIN_MODEL);
```

---

## Consequences

### Positive

- 유스케이스 입력(Command)으로 의미가 명확해지고 확장성이 좋아짐
- Controller가 domain을 직접 다루지 않아 계층 경계가 강화됨
- Request/Response DTO 분리로 domain 의존 범위 명확
- ArchUnit 검증을 통해 구조적 안정성 향상
- Response DTO에 domain 의존 허용으로 표현계층 매핑이 단순해짐

### Negative

- Command/DTO 타입 증가
- ArchUnit 규칙 복잡도 증가
- 소규모 프로젝트에서는 다소 과한 설계처럼 보일 수 있음

---

## Alternatives Considered

1. **Port 시그니처에 primitive 유지**
    - 단순하지만 확장성 부족
2. **DTO 단일 패키지 유지**
    - 규칙 단순하지만 domain 의존 통제 어려움
3. **Response DTO도 domain 비의존으로 만들고 Service에서 매핑**
    - 가장 깔끔하지만 복잡성 증가로 인해 이번 단계에서는 보류

---

## Status / Migration Plan

1. `application.port.in.command` 패키지 생성
2. 유스케이스 포트를 Command 기반으로 수정
3. DTO Request/Response 패키지 분리
4. Controller에 Request→Command, Domain→Response 변환 적용
5. ArchUnit 규칙을 새로운 구조에 맞게 갱신
6. 전체 테스트 통과 확인  

