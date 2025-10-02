# 2. 영속성 adapter에 H2 DB 추가
Date: 2025-08-12

# status
 Applied

# Context
file로 영속성 구조를 만드는 기본 로직은 실무 수준의 예제로 부족한 것으로 판단하고 영속성 layer로 RDBMS를 추가했다.
DB는 가벼운 H2로 구성했다.

# Decision
- application.yaml에 embedded DB인 경우 초기 스크립트(schema.sql)를 실행하도록 처리 
```yaml
spring:
  sql:
    init:
      mode: embedded
```

- 테이블 생성 스크립트 추가 
  - resources/schema.sql
```sql
CREATE TABLE IF NOT EXISTS account (
    account_number VARCHAR(64) PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    balance        BIGINT       NOT NULL
    );
```

- 프로파일 추가 : 영속성 레이어를 선택적으로 사용할 수 있도록 h2 프로파일 추가 
  - ${user.home} 을 사용해서 개개인의 환경에 대응되도록 h2 DB 파일 경로 지정
- mybatis 적용 : mapper 위치 등 지정

```yaml
---
spring:
  config:
    activate:
      on-profile: h2
  datasource:
    url: jdbc:h2:mem:accounts;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=LEGACY
    #url: "jdbc:h2:file:${user.home}/test/account-db;MODE=LEGACY"
    driver-class-name: org.h2.Driver
    username: sa
    password:
  sql:
    init:
      mode: embedded  #always | never | embedded
  h2:
    console:
      enabled: true  # http://localhost:8080/h2-console
      path: /h2-console
persistence:
  type: h2
mybatis:
  mapper-locations: classpath*:mappers/**/*.xml     # ★ XML 위치
  type-aliases-package: com.example.account.domain.model
  configuration:
    map-underscore-to-camel-case: true
```

- build.grable dependencies 추가
```yaml
    // H2 + JDBC
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    runtimeOnly 'com.h2database:h2'

    // mybatis
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'

```

# Consequences
- Application 실행(AccountApplication) 시 yaml 프로파일 설정에 따라 영속성 레이어 적용

```yaml
spring:
  profiles:
    active: file  # file | h2
```

- h2 사용 시 다음 링크로 h2 데이터 확인 가능
  - http://localhost:8080/h2-console/
  
```yaml
# application.yaml
  h2:
    console:
      enabled: true  # http://localhost:8080/h2-console
      path: /h2-console
```


