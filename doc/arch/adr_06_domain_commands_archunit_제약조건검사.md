# 5. adr_06_domain_commands_archunit_제약조건검사
Date: 2025-08-28

# status
 Applied

# Context
RichEntity의 메소드를 private으로 변경하고, DomainCommands로 위임해서 RichEntity의 비즈니스 로직을<br>
다른 도메인에서 사용할 수 없고 private으로 숨겼지만, DomainCommands를 이용하면 여전히 RichEntity의 비즈니스 로직이 
노출될 수 있는 문제점을 개선하고자 한다.
도메인(서비스 포함)의 비즈니스 로직을 개발할 때 최소한의 노력으로 이를 외부에 숨길 수 있는 방식으로 
Annotation + archunit으로 제약 조건 검출 로직을 추가했다.
 

# Decision
- 어노테이션 @DomainCommand 생성
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainCommand {}
```

- 도메인 RichEntity의 비즈니스 로직(private 메서드)를 위임하는 Commands 클래스에 어노테이션 추가 
```java
@DomainCommand
public final class AccountCommands {
  ... 생략 ...
}
```

- ArchUnit 테스트에 @DomainCommand 어노테이션 검출 테스트 추가
```java
public class ArchitectureTest {
  ... 생략 ...
  /** 프로덕션 클래스만 로드(테스트/외부 라이브러리 제외) */
  private JavaClasses loadProductionClasses() {
    return new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE);
  }

  /**
   * 9) @DomainCommand이 붙은 클래스는 서비스 패키지에서만 의존해야 한다.
   * (즉, AccountCommands 등은 application.service 안에서만 사용할 수 있다)
   *
   * 테스트 방법 : AbuseControllerForArchunit의 주석을 풀어 AccountCommands를 직접 사용하도록 만든 후 테스트
   */
  @Test
  void forbid_using_DomainCommand_outside_service_package() {
    JavaClasses classes = loadProductionClasses();

    ArchRule rule = noClasses()
            .that().resideOutsideOfPackage(APP_SERVICE)
            .should().dependOnClassesThat().areAnnotatedWith(DomainCommand.class);

    rule.check(classes);
  }

  /**
   * 10)@DomainCommand 어노테이션 사용 위반 검출
   * @DomainCommand는 오직 application.service 패키지만 사용해야 한다.
   *
   *      * 테스트 방법
   *          1) AbuseControllerForArchunit의 주석을 풀어 AccountCommands를 직접 사용하도록 만든 후 테스트
   *          2) 다른 패키지에 어노테이션을 붙여 테스트
   */
  @Test
  void only_services_may_depend_on_DomainCommand_types() {
    JavaClasses classes = loadProductionClasses();

    ArchRule rule = classes()
            .that().areAnnotatedWith(DomainCommand.class)
            .should().onlyHaveDependentClassesThat().resideInAnyPackage(APP_SERVICE);

    rule.check(classes);
  }
}
```

- DomainCommands 상용을 위배하는 사례 코드 생성
  - //@DomainCommand 주석을 풀면 10)어노테이션 사용 위반
  - depositAbuse() 메서드 주석을 풀면 9)
```java
package com.example.account.adapter.in.web;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

//@DomainCommand
@RestController
@RequestMapping("/accounts")
@Validated
public class AbuseControllerForArchunit {

    /**
     * DomainCommands를 강제 사용했을 때 ArchUnit에서 검출이 되는지 테스트용
     */
//    @PostMapping("/{accountNumber}/depositAbuse")
//    public ResponseEntity<AccountResponse>  depositAbuse(@PathVariable @NotBlank String accountNumber, @RequestParam @PositiveOrZero long amount) {
//        var acc = Account.of("test-001", "Abuser", 999);
//
//        // 이 부분에서 AccountCommands를 강제로 사용
//        AccountCommands.deposit(acc, new Amount(100));
//
//        var body = AccountResponse.of(acc);
//        return ResponseEntity.ok(body); // 200 OK
//    }
}
```

# Consequences
이제 Commands 클래스에 @DomainCommands 어노테이션만 붙이면 헥사고날 아키텍처의 구조에 맞춰   
외부로부터 도메인 비즈니스 로직을 직접 호출하지 못하도록 차단하면서도, 
헥사고날 아키텍처의 도메인 경계 밖에서도 도메인 Entity를 사용할 수 있게 되었다. 

다시 한번 사용 방법을 정리하면, 
- Entity의 비즈니스 로직은 package-private으로 숨기고, 
- 이를 Commands 클래스의 public static 메서드로 위임한다.
- Commands 클래스에는 @DomainCommands 어노테이션을 붙이기만 하면 된다.
- 이렇게 하면 Service 패키지에서만 Commands 클래스를 이용할 수 있게 제약 조건이 체크된다. 

현재는 샘플로 example 내에 account 밖에 없지만, 도메인이 늘어나면 <br>
### ArchitectureTest를 확장성 있게 만들어 도메인이 추가되어도 쉽게 추가된 도메인 내의 제약조건을 검증할 수 있도록 리팩토링을 해야 한다.

