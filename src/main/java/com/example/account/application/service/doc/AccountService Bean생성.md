# account-hexagonal-demo

### @Service 어노테이션으로 빈 주입을 변경함. 인터페이스의 구현체가 하나(AccountService)이기 때문에 @Service 어노테이션으로 빈 주입 가능
 만약 둘 이상의 구현체가 있다면 @Service 어노테이션을 하나에만 붙이거나(사용할 빈 선택), 아니면 @Configuration 클래스로 빈을 주입해야 함
 (AccountServiceConfig.java, AccountService2.java 참고)

### AccountService를 package-private 으로 변경한 이유가 헥사고날 아키텍처에서 <br>in, out 포트를 제외한 내부 도메인 서비스를 외부에서 직접 호출하지 못하도록 막기 위해서 였는데, <br> ApplicationConfig에서 AccountService를 @Bean으로 등록하면 결국 package-private이 무용지물이 되는 것인가? 


- 무용지물 아니다. 
- AccountService를 package-private로 숨긴 상태에서, @Bean으로 등록했다고 해서 외부 패키지가 그 구현체를 “타입으로” 사용할 수 있게 되진 않아요. 
- 컨테이너엔 빈 인스턴스가 있지만, **코드 레벨(컴파일 시점)**에서 다른 패키지는 AccountService 타입을 참조할 수 없기 때문에 직접 의존을 만들 수 없습니다.
- 컨트롤러는 오직 포트 인터페이스로만 주입받죠.

### 왜 괜찮은지 핵심만 정리할게요.

---

## 왜 package-private + @Bean 조합이 유효한가?
### 1. 컴파일 타임 가시성 차단

- AccountService가 package-private이면 application.service 패키지 밖의 클래스에서

```java
@Autowired AccountService s; // ❌ 컴파일 에러: 접근 불가
```
이렇게 타입 참조 자체가 안 됩니다.

- 결국 외부는 CreateAccountUseCase, DepositUseCase, WithdrawUseCase 포트 인터페이스로만 의존을 만들 수 있어요.

---
### 2. 스프링 컨테이너에 인스턴스가 존재하는 것과, 코드가 참조 가능한 건 별개
- 컨테이너에 AccountService 인스턴스가 있어도, 외부 코드가 그 구현 타입 이름을 모르면(접근 불가면) 주입 선언을 못 합니다.
- 런타임에 ApplicationContext.getBean("accountService") 같은 문자열 룩업으로 끌어오는 건 가능하지만, 
- 그건 DI 원칙과 헥사고날의 의도를 거스르는 안티패턴이고 코드 리뷰/규칙으로 막아야 할 영역입니다. (일반적인 @Autowired/생성자 주입 경로에선 포트만 보입니다.)

---
### 3. 컨트롤러에 객체가 주입되는 경로

ApplicationConfig가 같은 패키지에서 @Bean으로

```java
@Bean CreateAccountUseCase createAccountUseCase(AccountService s) { return s; }
```

처럼 포트 인터페이스 타입 빈을 공개합니다.

- AccountController는 생성자에서 포트들만 받으므로 (구현체 모름) DIP(Dependency Inversion Principle)가 유지됩니다.
- DIP : 고수준 모듈은 저수준 모듈에 의존해서는 안된다. 둘 다 추상화(인터페이스)에 의존해야 한다.
  - 고수준 모듈 : 비즈니스 규직, 정책, 핵심로직(AccountService)
  - 저수준 모듈 : 외부 시스템 연동, DB 접근, 파일 처리 등 기술적인 세부사항(예-FileAccountPersistenceAdapter)

```
[고수준] AccountService  --->  [포트] LoadAccountPort / SaveAccountPort  <---  [저수준] FileAccountPersistenceAdapter
```

---
## [요약 정리] Spring 환경에서 DIP 구현
- Spring에서는 DIP를 **DI(Dependency Injection)**로 구현합니다.
- AccountService 생성자에 LoadAccountPort와 SaveAccountPort를 주입
- 어떤 Adapter를 쓸지는 **Spring 설정(yaml, @Configuration 등)**에서 결정
- 이렇게 하면 AccountService는 파일이든 H2 DB든 전혀 신경 쓰지 않고, 포트 인터페이스만 호출

- 장점
  - 유연성 – 저장소를 파일 → H2 DB → MySQL로 변경해도 AccountService 변경 불필요
  - 테스트 용이성 – Port를 Mock으로 대체하면 DB 없이도 단위 테스트 가능
  - 결합도 감소 – 도메인 로직이 기술 스택에 종속되지 않음
  - 유지보수성 향상 – 기능 확장 시 영향 범위 최소화



---
## 더 “강하게” 막고 싶다면
아래는 선택지예요. 지금도 충분히 좋지만, 팀 규칙/배포 형태에 따라 강화할 수 있어요.

- 멀티 모듈(Gradle/Subproject)로 나눠서 application.service 모듈을 export 하지 않기 <br>
→ 다른 모듈이 AccountService 심볼을 아예 참조 못 함.
- JPMS(자바 9 모듈) 사용: module-info.java에서 exports에 application.service를 제외.
- 빈 타입 노출 최소화: 지금처럼 포트 빈만 외부에서 사용하고, 구현체는 패키지 내부에 유지.
(지금 구조가 이걸 충실히 지킴)

--- 
### 혹시 “아예 AccountService 빈을 등록하지 않기”가 가능한가?
가능은 한데 깔끔하게 하나의 인스턴스를 세 포트 모두로 공유하려면 결국 내부에서 그 인스턴스를 한 번 만들어 재사용해야 합니다.
현실적으로는 지금처럼 AccountService를 패키지 내부 @Bean으로 만들고, 포트 빈을 그 인스턴스로 노출하는 방식이 가장 단순하고 안전합니다. (외부에서 구현 타입 주입은 여전히 불가)

### 결론
- ***@Bean*** 등록이 곧 “외부에서 구현체 직접 사용 가능”을 의미하지 않습니다.
- 패키지 가시성 + 포트만 노출로 헥사고날의 경계가 유지되고, 외부는 인터페이스만 의존하게 됩니다.
- 더 강한 격리를 원하면 모듈(빌드/JPMS) 단위로 내보내기(export)를 막는 전략을 추가하세요.