# 4. archunit 의존성 검사
Date: 2025-08-26

# status
 Applied

# Context
헥사고날 아키텍처의 의존성 규칙을 준수하는지 검증하는 기능 추가
- 헥사고날 아키텍처에서 의존성 방향을 지키도록 강제하려면 부가적인 설계와 코드가 많이 들어가야 한다.
- 의존성 규칙을 지키는 설계는 보기에는 멋있어 보일 수 있지만, 많은 오버헤드(간접비용)가 발생한다.
- 아키텍처를 준수하는 코드를 유지하는데 들어가는 비용이 과다한 것보다는 아키텍처를 준수하는 문화를 만드는 것이 더 좋다.
- 그럼에도 불구하고 아키텍처 규칙을 위배해서 사용하면 안되는 곳에서 의존성 규칙을 무시하고 클래스를 가져다 사용하면<br>
  도메인 관리가 힘들어지고
- 깨진 유리창의 법칙처럼 의존성을 무시하는 코드가 하나 둘 늘어나 어느 순간 무정부 상태(무법지대, 스파게티 코드)에 놓일 수 있다.

이를 보완하기 위해 의존성 규칙을 준수하는지 여부를 테스트 케이스로 지원하는 archunit을 활용했다.

# Decision
### build.gradle 설정 추가
- build.gradle 
```
    testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'
```

### test/java/archunit/
- build.gradle
```java
    /**
     * 1) 도메인은 어떤 레이어에도 의존하지 않는다 (순수성 보장)
     */
    @ArchTest
    static final ArchRule domain_should_not_depend_on_others =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat().resideInAnyPackage(
                            APPLICATION, ADAPTER, CONFIG
                    );
    
    
    
     * 2) Application은 도메인에는 의존 가능하지만, 어댑터/설정에는 의존하지 않는다
     *    (유스케이스는 포트 인터페이스를 통해서만 바깥세상을 본다)
        */
@ArchTest
static final ArchRule application_should_depend_only_on_domain_or_itself =
        classes().that().resideInAPackage(APPLICATION)
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        APPLICATION, DOMAIN, "java..", "jakarta..", "org.slf4j..", "org.springframework.."
                );
... 생략 ... 
```

# Consequences
의존성 규칙을 무시하는 코드를 자동으로 검출함
개발자들은 불필요한 의존성 규칙 준수 코드를 개발할 필요없이 최소한의 헥사고날 아키텍처만 준수하고 
도메인 로직과 유즈케이스를 구현하는 서비스 개발에 집중 할 수 있다.

archunit도 유지보수가 필요하지만, 코드 베이스 전체가 의존성 규칙을 준수하는 디자인 설계를 넣고, <br>
개발자들이 이 설계에 따라 기능을 구현하는 것에 비하면 유지보수 비용도 훨씬 적게 들고, 
도메인이 많아져도 동일한 규칙으로 관리가 가능하다는 장점이 있다.

