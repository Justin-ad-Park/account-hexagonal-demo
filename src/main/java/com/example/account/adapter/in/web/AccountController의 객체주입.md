# account-hexagonal-demo


## ApplicationConfig가 PackagePrivate으로 선언되어 있는데, <br> AccountController에 어떻게 객체가 주입이 되나?

### 1) @Configuration은 스캔 대상이고, public일 필요가 없음
- @Configuration은 내부적으로 @Component가 붙어 있어 컴포넌트 스캔 대상입니다.
- 클래스가 public이 아니어도(= package-private) 스프링은 리플렉션으로 읽어서 빈 정의를 등록할 수 있어요.
- 전제: 이 설정 클래스가 컴포넌트 스캔 범위 안에 있어야 함.
- 예) **@SpringBootApplication** 이 **com.example.account** 패키지에 있으니, 그 하위인
**com.example.account.application.service** 도 자동 스캔됩니다.

---
### 2) @Bean 메서드도 public일 필요 없음
- accountService(...), createAccountUseCase(...) 등 메서드가 package-private 이어도 OK.
- 스프링은 @Configuration 클래스를 CGLIB로 프록시(proxyBeanMethods=true 기본)해서
이 메서드들을 통해 리턴된 객체들을 싱글턴 빈으로 컨테이너에 등록합니다.

---
### 3) AccountController는 “타입(포트 인터페이스)”로 주입 받음
- AccountController 생성자 파라미터는 CreateAccountUseCase, DepositUseCase, WithdrawUseCase 인터페이스 타입이죠.
- ApplicationConfig의 @Bean들이 바로 그 인터페이스 타입 빈을 등록합니다:

```java
@Bean CreateAccountUseCase createAccountUseCase(AccountService s) { return s; }
```

- 여기서 **s**는 **AccountService**(package-private 구현체)지만, 반환 타입이 인터페이스라서
컨테이너엔 “포트 인터페이스 빈”이 등록됩니다.

- 결과적으로 컨트롤러는 인터페이스 타입 매칭으로 주입을 받습니다. <br>
구현체 AccountService가 package-private인지 여부는 컨트롤러 입장에선 상관없습니다.

---

### 4) “숨기기”가 실제로 이뤄짐
- AccountService 구현체는 같은 패키지 밖에서 타입으로 직접 주입/의존하기 어렵고(패키지 가시성), 외부에는 포트 인터페이스만 노출됩니다.
- 즉, 헥사고날에서 원하는 “도메인 서비스 구현 숨김 + 포트만 노출”이 제대로 작동합니다.

---
### 5) 참고: 스캔 범위 밖이면?
- 만약 ApplicationConfig가 컴포넌트 스캔 경로 밖에 있다면 @Import(ApplicationConfig.class)로 가져오면 됩니다.
- 이때도 public일 필요는 없어요.

--- 
### 정리:
#### ApplicationConfig가 package-private이라도 스프링이 스캔 → @Bean 등록 → 인터페이스 타입으로 주입 하는 흐름이 유지되므로, AccountController에는 문제 없이 포트 빈들이 주입됩니다.

