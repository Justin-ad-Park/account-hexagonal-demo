
# Spring Boot 테스트 범위 어노테이션 정리

## 용도별 구분
- **통합 테스트**: `@SpringBootTest` (+ 필요 시 `@AutoConfigureMockMvc`, `@AutoConfigureWebTestClient`)
- **웹 슬라이스**: `@WebMvcTest`, `@WebFluxTest`, `@GraphQlTest`, `@RestClientTest`, `@JsonTest`
- **데이터 슬라이스**:  
  `@DataJpaTest`, `@JdbcTest`, `@DataJdbcTest`, `@JooqTest`, `@MybatisTest`,  
  `@DataMongoTest`, `@DataRedisTest`, `@DataNeo4jTest`, `@DataElasticsearchTest`, `@DataR2dbcTest`

> 공통 원칙
> - 슬라이스 어노테이션은 **해당 계층만 최소 로딩** → 빠른 테스트.
> - 서비스/외부 연동 등은 **`@MockBean`** 으로 주입하는 경우가 많음.
> - DB 계열은 기본적으로 **임베디드/테스트용 DB 대체**가 기본값.  
    >   → 필요 시 `@AutoConfigureTestDatabase(replace = NONE)` 로 실제 DB 사용.

---

## 한눈에 보는 요약 표

| 어노테이션 | 용도/시나리오 | 특장점 | 빈 로딩 범위 | 알아두면 좋은 점 |
|---|---|---|---|---|
| `@SpringBootTest` | 애플리케이션 **통합 테스트** | 전체 컨텍스트, 실제 구동과 유사 | 대부분의 Auto-Config + 전체 빈 | 느릴 수 있음. `webEnvironment` 옵션, `@AutoConfigureMockMvc` 함께 사용 |
| `@WebMvcTest` | **MVC 컨트롤러** 슬라이스 | 빠름, `MockMvc` 자동 구성 | `@Controller`, `@ControllerAdvice`, Jackson, MVC infra | 서비스/리포는 미로딩 → `@MockBean` 필요. 보안 필터 주의 |
| `@WebFluxTest` | **WebFlux 컨트롤러** | `WebTestClient` 지원 | WebFlux 관련 빈만 | 비동기 체인만 로딩. 서비스/리포 `@MockBean` 필요 |
| `@GraphQlTest` | **GraphQL 컨트롤러/스키마** | `GraphQlTester` | GraphQL 관련 컴포넌트 | Data/Service 미포함 |
| `@RestClientTest` | **외부 REST 호출** 코드 | `MockRestServiceServer` 자동 | Jackson, `RestTemplateBuilder` | 서버 없이 클라이언트 레이어만 검증 |
| `@JsonTest` | **직렬화/역직렬화** | `ObjectMapper`, `JacksonTester` | JSON 관련만 | 도메인 객체 JSON 컨트랙트 검증 |
| `@DataJpaTest` | **JPA/Repository** | 빠름, 기본 `@Transactional(rollback)` | JPA Repo, EntityManagerFactory, TestEntityManager | 기본 임베디드 DB 대체. 실제 DB는 `replace=NONE` |
| `@JdbcTest` | **순수 JDBC** | SQL 단위 검증 | `DataSource`, `JdbcTemplate` | 트랜잭션 롤백 기본 |
| `@DataJdbcTest` | **Spring Data JDBC** | JPA보다 단순/빠름 | Spring Data JDBC Repo | DB 대체 정책 동일 |
| `@JooqTest` | **jOOQ 기반** | 타입 안전 SQL DSL | jOOQ 컨텍스트 | 테스트 DB 필요 |
| `@MybatisTest` | **MyBatis 매퍼** | 매퍼 검증 | SqlSessionFactory, 매퍼 | XML/어노테이션 매퍼 집중 |
| `@DataMongoTest` | **MongoDB Repo** | 빠름(임베디드 가능) | MongoTemplate, Repo | flapdoodle-embed 사용 가능 |
| `@DataRedisTest` | **Redis 연동** | RedisTemplate 검증 | RedisTemplate 등 | 임베디드 Redis 없음 → Testcontainers 권장 |
| `@DataNeo4jTest` | **Neo4j Repo** | 그래프 질의 검증 | Neo4jTemplate/Repo | 서버 필요 (TC 권장) |
| `@DataElasticsearchTest` | **Elasticsearch Repo** | ES 연동 최소화 | ES Client/Repo | 최신버전은 내장 ES 없음 |
| `@DataR2dbcTest` | **R2DBC(리액티브)** Repo | 리액티브 DB 검증 | R2dbcEntityTemplate/Repo | 블로킹 혼용 주의 |

---

## 보조/연계 어노테이션
- `@AutoConfigureMockMvc` : `@SpringBootTest`와 함께 `MockMvc` 자동 구성
- `@AutoConfigureWebTestClient` : WebFlux 환경에서 `WebTestClient` 자동 구성
- `@AutoConfigureTestDatabase` : 테스트 DB 대체 정책 제어 (`replace=NONE` 등)
- `@TestConfiguration` : 테스트 전용 설정 클래스 정의
- `@MockBean` : 슬라이스에서 빠진 빈을 목킹
- `@DirtiesContext` : 컨텍스트 캐시 정리
- `@ActiveProfiles` : 테스트 프로파일 지정
- `@Sql`, `@SqlGroup` : 테스트 전후 SQL 실행
- `@Transactional` : DB 테스트 기본 롤백 지원

---

## 선택 가이드
- **컨트롤러만** 검증 → `@WebMvcTest` (+ `@MockBean`)
- **리포지토리(JPA)** 검증 → `@DataJpaTest`
- **애플리케이션 전체** → `@SpringBootTest`
- **외부 API 연동 코드** → `@RestClientTest`
- **JSON 직렬화/역직렬화** → `@JsonTest`
- **리액티브 스택** → `@WebFluxTest`, `@DataR2dbcTest`

---
## @SpringBootTest + @AutoConfigureMockMvc 동작 정리

### 1. 기본 동작
- **`@SpringBootTest`**  
  → 애플리케이션 전체 컨텍스트를 통합적으로 로딩  
  (서비스, 리포지토리, 설정 빈까지 전부 실제 빈으로 올라옴)

- **`@AutoConfigureMockMvc`**  
  → 이 컨텍스트 위에 **테스트용 `MockMvc` 빈을 추가 구성**  
  → **DispatcherServlet, 필터, 컨트롤러, 예외처리 등 MVC 인프라**가 실제로 로딩되지만  
  HTTP 서버를 띄우는 대신 **MockMvc 객체로 요청/응답을 시뮬레이션**

---

### 2. 중요한 구분
- **Service, Repository, Component 등** → **실제 빈**으로 로딩됨
- **Web Layer (Controller, HandlerMapping, Filter, Jackson 등)** → **실제 빈**으로 로딩됨
- 단, **네트워크 소켓은 열리지 않음** → `MockMvc`가 내부적으로 `DispatcherServlet`을 직접 호출

---

### 3. 구조 그림

```text
@SpringBootTest
 ├─ Service, Repository, Component … (실제 빈)
 ├─ Controller, Filter, ExceptionHandler … (실제 빈)
 └─ @AutoConfigureMockMvc → MockMvc 빈 주입
         └─ DispatcherServlet 직접 실행
```
