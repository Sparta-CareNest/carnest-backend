CareNest:노인 돌봄 플랫폼
===

# 프로젝트 소개

## 핵심기술 목표

### 1. 각 기능별 서비스 분리(MSA 기반 시스템 설계)
- 기능에 따른 개별 애플리케이션을 생성하여 서비스가 독립적으로 존재할 수 있도록 구현.
- 한 서비스에서 이슈가 발생하더라도 다른서비스에는 영향을 미치지않아 시스템의 가용성 향상.
- 서비스 간 통신은 FeignClient 기반으로 구현하여 내부 결합도를 낮춤

### 2. 비동기 메시징 기반 실시간 데이터 처리
- Kafka를 활용한 이벤트 기반 통신 처리
- 시스템 장애 격리 및 데이터 동기화 이슈 최소화

### 3. 인증 및 보안
- JWT 기반 인증을 도입하여 사용자 요청에 대한 안전한 접근 제어 구현
- 각 서비스 별 접근가능한 권한을 설정하여 보안을 강화.

### 4. 모니터링 및 운영 자동화
- Prometheus + Grafana를 통해 각 서비스 상태 실시간 모니터링
- Docker 기반 개발/운영 환경 구성으로 일관된 실행 보장

### 5. 외부 API 연동을 통한 기능 확장
- 외부 기술 도입으로 서비스 확장성과 사용자 편의성 향상

## 구현 목표

### 1. MSA 기반 아키텍처 & 커뮤니케이션
- 멀티모듈 프로젝트 구조로 각 기능을 독립적인 마이크로서비스로 분리하여 개발 진행.
- 서비스 간 통신은 REST API를 통해 처리되며, 서브모듈 간의 통신은 FeignClient 활용.
- Spring Cloud Eureka를 이용한 서비스 디스커버리 및 Spring Cloud Gateway를 통한 API 라우팅.

### 2. 서비스 확장성 및 유연성
- 각 마이크로서비스는 수평 확장이 가능하며, 필요한 경우 독립적인 배포 및 유지보수 용이.
- Layered Architecture로 기반으로 클린 코드 및 DDD를 통한 도메인 중심 설계.

### 3. 권한관리 및 보안
- JWT 인증: 사용자 인증 및 권한 관리를 위해 JWT를 사용하며, 각 요청에서 JWT 토큰을 검증하여 인증된 사용자만 접근하도록 처리.
- GateWay의 WebFluxSecurity: API Gateway에서 WebFluxSecurity를 이용해 JWT 토큰을 검증하고, 권한에 따라 요청에 대한 접근을 인가 처리.
- 비밀번호 암호화: BCrypt 해시 알고리즘을 사용하여 비밀번호 입력 시 암호화 하여 저장.
- 데이터 유효성 검사: Spring Validator로 서버 측에서 유효성 검사 진행.

### 4. 사용자 경험 개선
- Slack API 연동: Slack API와 연동하여 메시지 작성 후 발송 시 실시간으로 전달되도록 구현.
- Toss payments  API 연동 : Toss Payments 연동을 고려한 결제 흐름 설계로 사용자의 편의성 강화
- Gemini API 연동 : Gemini API를 통한 자연어 기반 추천 기능으로 입력 방식의 다양성과 접근성 확보

### 5. API 문서화
- Swagger를 사용하여 API 문서 자동화 지원.
- 공통 응답 포맷과 예외 처리 정책을 통일하여 일관된 API 제공

### 6. 이벤트 기반 실시간 데이터 처리
- Kafka를 활용하여 각 서비스 별 이벤트 발행
- 비동기 메시징 구조로 서비스 간 결합도 감소 및 실시간 데이터 동기화

# 개발 환경 소개
### 개발환경

- spring boot 3.4.4
- Gradle
- java 17
- Docker


### 기술스택
| 분류            | 상세                                                                                                                                                     |
  |----------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------|
| Framework      | Spring Boot,JPA ,Spring Cloud (Eureka[MSA 간의 동적 서비스연결], Gateway[공통 진입점], Feign Client[서비스 통신], Config[중앙 설정 관리 및 서비스별 yml 통합 관리]), QueryDSL (동적 쿼리 생성) |
| Database       | PostgreSQL, Redis                                                                                                                                      |
| Security       | Spring Security(인증,인가), JWT (토큰 기반 인증)                                                                                                                 |
| Messaging  | Apache Kafka (이벤트 기반 비동기 통신)                                                                                                                           |
| External API  | Toss Payments API (결제 연동), Slack API (알림), Google Gemini API (Ai 서비스)                                                                                  |
| Documentation  | Swagger (API 문서화)                                                                                                                                      |
| Monitoring       | Prometheus, Grafana                                                                                                                                    |
|Test            | Postman                                                                                                                                                |
# 프로젝트 실행 방법


- Local 환경에서 필요한 .env 양식
- Docker Compose 등 실행에 필요한 환경