# 1. Hexagonal Architecture
Date: 2025-08-08

# status
 Applied

# Context
Spring MVC 모델로 프로젝트를 수행하는 경우 도메인 경계가 불분명하고, Service에 너무 많은 역할이 몰려 있어<br>
이해하기 어렵고 유지보수가 힘든 구조가 된다. 
 
MVC 모델에 도메인 설계없이 Service에 도메인 로직을 구현하면, <br>
도메인 정책(비즈니스 로직), 영속성 레이어 호출(Persistent Layer, MyBatis, JPA..)
, 외부에 public method 제공 등 Service의 역할이 많아져 SRP(단일 책임 원칙)를 위배하게 되며,  
이를 시작으로 SOLID 원칙을 대부분 위배하는 스파게티 코드가 되기 쉽다. 

# Decision
DDD(Domain Driven Development)를 적용해 Service에서 도메인 정책(비즈니스 핵심 로직)을 분리
DDD의 아키텍처는 헥사고날 아키텍처를 적용해 보기로 한다.

# Consequences
- 기본적인 헥사고날 구조 적용

### 전체 구조
- domain : Spring 등 프레임워크 의존없이 POJO(Plain Old Java Object)로 개발
  - model : Entity(Rich Entity), Value Object로 구성
- application
  - port
    - in : usecase interface
    - out : persistent interface
  - service : usecase(in) - domain - persistent(out) 연결 및 service 조합  
- adapter
  - in : 외부 입력 및 입력 validation 처리, usecase 연결, usecase의 response를 외부 response로 변환
    - web : restful API controller 구성
  - out : 영속성 레이어 처리, 외부 이벤트 생성, 서비스 결과를 출력(다른 시스템의 입력)으로 연결

