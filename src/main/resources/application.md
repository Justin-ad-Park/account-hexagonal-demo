
```yaml
spring:
  sql:
    init:
      mode: embedded  #always | never | embedded
```

실행 순서
mode: 
always인 경우 다음 순서로 동작합니다.
embedded인 경우 내장 DB(h2)인 경우 동작
naver는 동작하지 않음


1. 데이터소스(DataSource) 생성
2. schema.sql 실행 → 테이블 구조 등 스키마 생성
3. 여러 파일이면 이름 순서대로 실행
3. data.sql 실행 → 초기 데이터 삽입
5. JPA 등을 사용하면 이후 Hibernate가 추가적으로 스키마 변경을 적용할 수 있음
