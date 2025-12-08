# ADR-09: DomainCommands 패턴 제거 및 Rich Aggregate 기반 구조로 전환

- Status: Accepted
- Date: 2025-12-08
- Version: ver09 (from ver06)

---

## Context

ver07에서는 다음과 같은 목표로 `application` 과 `domain` 계층을 분리하였다.

- `domain` 계층은 **순수 POJO**로 도메인 규칙만을 포함한다.
- `application` 계층은 포트(Port)를 통해 외부 인터페이스(웹, DB, 파일 등)에 접근하며 유스케이스 오케스트레이션을 담당한다.
- 헥사고날 아키텍처 원칙에 따라, 도메인 계층은 프레임워크나 I/O에 의존하지 않는다.

이를 구현하기 위해 ver07에서는 다음 패턴이 도입되었다.

- `AccountCommands`
- `@DomainCommand` 어노테이션
- ArchUnit 규칙을 통한 사용 위치 제약

이 설계는 도메인 변경이 Application 계층을 통해서만 일어나도록 강제하는 데 효과적이었다.  
하지만 패턴이 스케일링될수록 아래의 한계가 드러났다.

1. **도메인 로직 분산**
    - 변경 로직이 `Account` 와 `AccountCommands` 로 나뉘어 가독성과 응집도가 낮아진다.

2. **정적 메서드 기반의 확장성 부족**
    - 도메인 정책 객체를 DI로 주입할 수 없어 복잡한 규칙 확장 제약이 발생한다.

3. **아키텍처 테스트와 패턴 간 결합도 증가**
    - `@DomainCommand` 를 기준으로 한 ArchUnit 규칙이 설계 변경을 어렵게 만든다.

4. **Adapter의 도메인 타입 직접 생성 문제**
    - 컨트롤러에서 `new Amount(amount)` 형태로 도메인 값 객체를 생성하고 있었으며,  
      이는 헥사고날 경계를 약화시킬 위험이 있다.

이러한 이유로 DomainCommands 패턴을 제거하고,  
Rich Aggregate + Application Service 중심 구조로 재정비하기로 결정한다.

---

## Decision

### 1. DomainCommands 및 관련 요소 제거

- `AccountCommands` 클래스 삭제
- `@DomainCommand` 어노테이션 삭제
- ArchUnit의 DomainCommand 관련 규칙 삭제

DomainCommands 패턴과 아키텍쳐 규칙 간 결합도를 제거하여 설계 유연성을 확보한다.

---

### 2. Rich Aggregate 기반으로 전환

- `Account` 애그리거트의 도메인 메서드를 `public` 으로 전환한다.
- 상태 변경(`deposit`, `withdraw`)은 `Account` 내부에서 직접 수행한다.
- 생성 시 불변식은 기존과 동일하게 유지한다.

---

### 3. Application Port는 primitive 기반으로 단순화

UseCase 인터페이스 변경:

- Before
    - `deposit(String accountNumber, Amount amount)`
    - `withdraw(String accountNumber, Amount amount)`

- After
    - `deposit(String accountNumber, long amount)`
    - `withdraw(String accountNumber, long amount)`

도메인 값 객체(`Amount`) 생성 책임은 Application Service로 이동한다.

---

### 4. Inbound Adapter는 도메인 모델에 직접 의존하지 않도록 ArchUnit 규칙 강화

다음 규칙을 추가한다.
```java
    /**
     * (선택 규칙)
     * 어댑터(in/web)는 도메인 모델에 직접 의존하지 않고,
     * 도메인 모델이 필요하면 dto 계층을 거쳐서 사용하도록 강제
     */
    private static final String DOMAIN_MODEL = "..domain..model..";
    private static final String ADAPTER_IN_DTO = "..adapter.in..web..dto..";

    @ArchTest
    static final ArchRule inbound_adapters_should_not_depend_on_domain_model =
            noClasses()
                    .that().resideInAPackage(ADAPTER_IN)
                    .and(DescribedPredicate.not(resideInAnyPackage(ADAPTER_IN_DTO)))
                    .should().dependOnClassesThat().resideInAPackage(DOMAIN_MODEL);

```

- Adapter는 primitive + DTO만 다루도록 강제한다.
- DTO 계층(`adapter.in.web.dto`)은 domain → DTO 변환을 위해 예외로 허용한다.

---

### 5. Controller에서 도메인 타입 생성 제거

- `new Amount(amount)` 생성 코드는 제거한다.
- UseCase 인터페이스가 primitive 타입을 받도록 변경함에 따라,  
  Adapter는 도메인 모델에 의존하지 않는다.

---

## Consequences

### Positive

- **도메인 응집도 증가:** Rich Aggregate로 비즈니스 규칙이 한 곳에 모인다.
- **Application 계층 책임 명확화:** 도메인 값 객체 생성 및 도메인 변경 흐름이 Application에서 일관되게 관리된다.
- **Adapter와 Domain의 경계 강화:** 헥사고날 아키텍처의 의도가 더 명확하게 드러난다.
- **설계 단순화:** DomainCommands 제거로 패턴 자체의 복잡도가 줄어든다.

### Negative / Trade-offs

- 기존 DomainCommands 패턴이 제공하던 “도메인 변경 진입점 강제” 기능은 제거된다.
- UseCase 및 Adapter 시그니처 변경으로 인한 마이그레이션 비용 발생.
- 패키지 구조 변경 시 ArchUnit 규칙 유지 관리 필요.

---

## Alternatives Considered

### Alternative 1 — DomainCommands 유지
- 명확한 진입점 제공
- 그러나 확장성 부족 및 도메인 규칙 분산 문제 지속

### Alternative 2 — DomainCommands를 인스턴스 Domain Service로 승격
- DI 가능
- 그러나 구조가 더 복잡해지고, 이번 결정(단순화)과 방향성이 다름

### Alternative 3 — Adapter에서 도메인 객체 생성 허용
- 규칙 완화
- 하지만 헥사고날 경계를 약화시키므로 배제함

---

## Status / Migration Plan

1. DomainCommands 및 관련 코드/테스트 제거
2. `Account` 의 도메인 메서드를 `public` 으로 변경
3. UseCase 인터페이스를 primitive 기반으로 수정
4. Application Service에서 도메인 값 객체 생성
5. Controller에서 도메인 타입 제거
6. ArchUnit 규칙 추가 후 전체 테스트 수행
7. 신규 도메인에도 동일 패턴 적용

---
