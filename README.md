+ # CareNest: 노인 돌봄 플랫폼
![CareNest](https://github.com/user-attachments/assets/4e274875-547e-4346-800a-c19af770c47e)


# 👋🏻 프로젝트 소개
### **🪺 CareNest**

대한민국은 고령화 사회로 진입 중이며, 전문적인 시니어 케어 서비스의 수요가 증가하고 있습니다. 이에 따라 신뢰할 수 있는 간병인 매칭 시스템에 대한 필요성도 점점 커지고 있습니다.
CareNest는 이러한 문제를 해결하기 위해, 온라인 기반의 간병인 검색 및 예약 시스템을 도입하고, 정찰제 가격 모델, AI 및 실시간 알림 기능, 자동 정산 시스템 등을 통해 보다 투명하고 신뢰할 수 있는 돌봄 서비스 플랫폼을 구축했습니다.
<br>

## ⛳ 프로젝트 핵심 목표

### 🧸 대규모 트래픽 대응

- **MSA** 아키텍처 기반으로 시스템의 확장성과 유연한 유지보수 가능
- **Redis**와 **Kafka**를 ** 고트래픽 환경에서도 안정적인 서비스 운영** 보장

### 🧸 성능 테스트

- 일반 사용자가 몰리는 **피크 타임**을 가정해 **트래픽 부하 시나리오** 실행
- Redis, Kafka 적용 전, 적용 후 DB의 처리량 및 응답 시간 측정
- TPS(Transactions Per Second), 평균 응답 시간, 최대 응답 시간 등을 모니터링

### 🧸 모니터링 시스템 구축

- **Prometheus**와 **Grafana**로 **서비스 지표를 실시간 수집 및 시각화**, 안정적인 시스템 운영 지원

## ✨ **KEY Summary**

### 🕊️ **Kafka 기반 비동기 이벤트 통신 구조**

- 각 마이크로서비스 간 도메인 독립성을 유지하기 위해 Kafka 기반 이벤트 통신을 도입
- 리뷰, 예약, 결제, 알림 서비스 등에서 필요한 시점에 이벤트를 발행
- Kafka Consumer가 이를 수신하여 비동기적으로 평점 갱신, 결제 상태 반영, 실시간 알림 등을 수행.
- 서비스 간 결합도를 낮추고, 확장성과 유연성을 확보

### 🕊️ **Redis 기반 실시간 예약 상태 관리**

- 예약 대기 상태, 도착 알림 등 실시간 상태 데이터를 Redis로 관리.
- DB 접근을 최소화하여 시스템 전체 부하 감소

### 🕊️ **Gemini API 기반 AI 시스템**

- 사용자의 자연어 입력을 분석하고, 간병인 추천 조건을 자동 추출.
- 분석된 키워드로 간병인 조건 자동 생성.
- 리뷰 번역 기능.


### 🕊️ **다양한 외부 API 연동**

- 다양한 외부 API 활용, 검증된 서비스를 통해 개발 효율성과 코스트를 절약하여 사용자 경험 향상
- **Slack API**: 예약 성공 또는 취소와 같은 주요 이벤트 발생 시 사용자에게 Slack 개인 DM을 통해 실시간 알림 전송
- **Toss Payments**: 사용자 결제 처리를 위해 토스 API를 연동해 안전하고 신뢰성 있는 결제 서비스 제공

<br>

## ERD
<img width="1576" alt="image" src="https://github.com/user-attachments/assets/fe92ef5d-9876-4c16-8210-0ae74dfbd178" />

<br>

## 인프라 아키텍처

### 아키텍처 다이어그램
![인프라설계도](https://github.com/user-attachments/assets/56cdeecd-94b1-4b00-9049-cda80e799c3e)

위 아키텍처는 **MSA 기반의 서비스** 구조를 나타냅니다.  
각 모듈은 OpenFeign, Kafka를 통해 통신하며, Docker로 컨테이너화되었습니다.

<br>

## 시퀀스 다이어그램
![시퀀스](https://github.com/user-attachments/assets/33dced0c-c01a-41ae-8630-6b752d057702)

## **서비스 흐름: *사용자 → 간병인 → 예약 → 결제 → 알림 → 관리자*

> > 사용자가 간병인을 검색 및 선택하여 예약을 신청합니다.<br>
신청된 예약 요청은 예약 승인 대기 상태로 등록됩니다.<br>
간병인이 자신의 일정 확인 후 예약을 수락하면, 사용자는 결제를 진행합니다.<br>
결제가 완료되면, 예약이 확정되며 해당 예약은 일정에 반영됩니다.<br>
예약 확정과 동시에 알림 등을 통해 사용자에게 알림을 전송합니다.<br>
모든 예약과 결제 정보는 관리자 페이지에 실시간 반영되어 관리자가 상태를 확인 및 대응할 수 있도록 구성됩니다. 그리고 관리자가 정산을 처리해줍니다.
 

## 주요 기능
### 신뢰 기반 간병인 매칭

> 1. 자격 검증과 경력 기반 매칭
     간병인의 요양보호사 자격증, 경력 연차, 제출 서류(S3 연동)를 검증하고 보호자 조건과 매칭
     지역 및 서비스 유형 필터(예: 서울, 야간 돌봄, 24시간 돌봄 등)로 정밀 검색 가능
> 2. 평점 기반 추천 시스템
     간병인 평점(1~5) 및 리뷰 수를 기반으로 실시간 인기 간병인 리스트 제공
     보호자는 평균 평점과 누적 리뷰 수를 바탕으로 신뢰도 있는 선택 가능
>

---

### 정찰제 결제 및 정산

> 1. 고정 요금제 기반 예약 및 결제
     시간 단가 / 일 단가를 사전에 설정하여 보호자에게 가격 투명성 제공
     예약 생성 시 총 결제 금액 자동 계산 및 토스페이먼츠 API 연동 결제 흐름 구현
> 2. 관리자가 결제 내역 기반으로 특정 간병인의 급여 정산
>

---

### 실시간 상태 알림 시스템

> 1. Kafka 기반 이벤트 알림
     예약 생성, 결제 완료, 정산 완료 등 주요 상태 변경 시 Kafka로 메시지 전송
> 2. 1:1 채팅 기능
     보호자-간병인 간 실시간 WebSocket 채팅 지원
>

---

### 다국어 지원

> 1. AI 기반 번역 기능 제공
     보호자/간병인이 사용하는 언어가 다른 경우 실시간 번역 API 호출을 통해 채팅 및 서비스 요청 가능
     예: "안녕하세요" → "Hello" 자동 변환
>

<br>

## 기술적 의사결정
🥨 [@AuthUser Custom Header 생성 및 전달 로직 설계](https://github.com/Sparta-CareNest/carnest-backend/wiki/기술적-의사결정/AuthUser-Header-전달-설계)

🥨 [MSA에서 Swagger 통합 문서화에 대한 기술적 의사결정](https://github.com/Sparta-CareNest/carnest-backend/wiki/기술적-의사결정/Swagger-통합-문서화-설계)

🥨 [Toss Payments 도입을 통한 결제 서비스 구현](https://github.com/Sparta-CareNest/carnest-backend/wiki/기술적-의사결정/Toss-Payments-결제-흐름)

🥨 [카프카(kafka) vs RabbitMQ 비교 및 선정 이유](https://github.com/Sparta-CareNest/carnest-backend/wiki/기술적-의사결정/Kafka-vs-RabbitMQ)

<br>

## 트러블슈팅
### BackEnd

<br>

## 기술스택
### 🧰 프레임워크 / 라이브러리

| JDK | Spring Boot | Eureka | OpenFeign | Gateway | WebFlux | Config | QueryDSL | JPA |
|-----|-------------|--------|-----------|---------|---------|--------|----------|-----|
| ![JDK 17](https://img.shields.io/badge/JDK%2017-ED8B00.svg?style=for-the-badge&logo=java&logoColor=white) | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white) | ![Eureka](https://img.shields.io/badge/Eureka-6DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) | ![OpenFeign](https://img.shields.io/badge/OpenFeign-6DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) | ![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-6DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) | ![Spring WebFlux](https://img.shields.io/badge/Spring%20WebFlux-6DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) | ![Spring Config](https://img.shields.io/badge/Spring%20Config-6DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) | ![QueryDSL](https://img.shields.io/badge/QueryDSL-4B8BBE.svg?style=for-the-badge&logoColor=white) | ![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) |

---

### 🗄️ 데이터베이스 / 캐시

| PostgreSQL | Redis |
|------------|-------|
| ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192.svg?style=for-the-badge&logo=postgresql&logoColor=white) | ![Redis](https://img.shields.io/badge/Redis-DD0031.svg?style=for-the-badge&logo=redis&logoColor=white) |

---

### 📈 모니터링 

| Grafana | Prometheus |
|---------|------------|
| ![Grafana](https://img.shields.io/badge/Grafana-F46800.svg?style=for-the-badge&logo=grafana&logoColor=white) | ![Prometheus](https://img.shields.io/badge/Prometheus-E6522C.svg?style=for-the-badge&logo=prometheus&logoColor=white) |

---

### 🧪 성능 테스트

| JMeter |
|--------|
| ![Apache JMeter](https://img.shields.io/badge/Apache%20JMeter-CA2027.svg?style=for-the-badge&logo=apachejmeter&logoColor=white) |


### 🔌 외부 연동 API

| Toss Payments | Slack | Gemini |
|---------------|-------|--------|
| ![Toss Payments](https://img.shields.io/badge/Toss%20Payments-0064FF.svg?style=for-the-badge&logoColor=white) | ![Slack](https://img.shields.io/badge/Slack-4A154B.svg?style=for-the-badge&logo=slack&logoColor=white) | ![Gemini API](https://img.shields.io/badge/Gemini%20API-7D4AEA.svg?style=for-the-badge&logoColor=white) |

---

### 🛠️ 인프라
| Docker | Docker Compose | AWS |
|--------|----------------|-----|
| ![Docker](https://img.shields.io/badge/Docker-2496ED.svg?style=for-the-badge&logo=docker&logoColor=white) | ![Docker Compose](https://img.shields.io/badge/Docker%20Compose-2496ED.svg?style=for-the-badge&logo=docker&logoColor=white) | ![AWS](https://img.shields.io/badge/AWS-FF9900.svg?style=for-the-badge&logo=amazonaws&logoColor=white) |

---

### 🤝 협업 도구
| GitHub | Discord | Notion |
|--------|---------|--------|
| ![GitHub](https://img.shields.io/badge/GitHub-181717.svg?style=for-the-badge&logo=github&logoColor=white) | ![Discord](https://img.shields.io/badge/Discord-5865F2.svg?style=for-the-badge&logo=discord&logoColor=white) | ![Notion](https://img.shields.io/badge/Notion-000000.svg?style=for-the-badge&logo=notion&logoColor=white) |


### 📚 API 문서화

| Swagger |
|---------|
| ![Swagger](https://img.shields.io/badge/Swagger-85EA2D.svg?style=for-the-badge&logo=swagger&logoColor=white) |


## 역할 분담
### **Detail Role**


|                    이름                    |      포지션         | 담당(개인별 기여점)                                                                                                                                                                                                                                                                                                                                                                     | Github 링크              |
|:----------------------------------------:|:-------------------:|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|
| 정은선 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; | 리더 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; | ▶ **알림서비스**: <br> - 예약 확정, 메시지 수신 등 주요 이벤트 발생 시 Kafka 컨슈머를 활용하여 WebSocket 기반 실시간 알림 기능 구현<br>▶ **채팅서비스**<br>-  WebSocket 기반 실시간 채팅 서비스 구현<br>- 채팅방 생성, 메시지 송수신 처리, 사용자 단위 채팅 기록 저장<br>▶ **관리자 정산**<br>- 결제 내역을 기반으로 간병인 수익을 계산하고, 정산 생성 시 OpenFeign을 통해 알림 서비스에 이벤트 전송                                                                                                          | [GitHub](https://github.com/jeongeunsun) |
|                   박종민                    |부리더  | ▶ **간병인 서비스**: <br> - 간병인 생성 시 S3 업로드를 통한 이미지/서류 저장 및 승인 처리 로직 구현 <br> - 평점 업데이트 이벤트 발생 시 Kafka를 통한 비동기 이벤트 처리 <br> - 리뷰 데이터를 기반으로 간병인 Top 10을 동기 호출로 조회하고, Redis에 캐싱하여 빠르게 제공 <br> ▶ **모니터링 시스템**: <br>- 각 마이크로서비스의 상태 정보를 Prometheus를 통해 수집 <br> - 수집된 메트릭을 Grafana 대시보드로 시각화하여 실시간 모니터링 <br> - CPU 사용률이 50% 이상 초과 시 알림(Alert) 설정으로 운영 대응 체계 구축 <br> ▶ **인프라 환경**: <br>- 프로젝트 초기 멀티 모듈 기반 환경 구성 (공통 모듈, 도메인 모듈 등 구조 분리) <br> - API 문서화를 위해 전역적으로 Swagger 통합 설정                                                                                                               | [GitHub](https://github.com/codejomo99)    |
|                   손민주                    | 테크리더     | ▶ **예약 서비스**: <br> - Kafka를 활용한 비동기 이벤트 기반 아키텍처 적용으로 예약 상태 변경 시 관련 서비스에 이벤트 발행. <br> - 사용자(보호자/간병인) 역할별 예약 상태 흐름 제어 및 권한 관리 <br> ▶ **결제 서비스**: <br> - 외부 PG사(토스페이먼츠) 연동을 통한 실제 결제 프로세스 구현  <br> - Circuit Breaker 패턴(Resilience4j) 적용으로 외부 시스템 장애 대응 <br> -취소/환불 정책 적용 및 자동화된 트랜잭션 관리 <br> ▶ **DevOps**: <br> -  Docker, Docker Compose를 활용한 개발 환경 표준화 및 컨테이너화   | [GitHub](https://github.com/mango606)   |
|                   박준혁                    | 팀원     | ▶ **게이트웨이**: <br> - JWT 기반 인증 필터 구현 <br> - 인증 제외 경로(whitelist) 처리 <br> - Custom Header (AuthUser) 처리 및 전달 로직 설계 <br> - Global Logging Filter 적용 <br> ▶ **유저 서비스**: <br> - 회원가입/로그인 기능 및 JWT 발급, 갱신 <br> - ADMIN 가입 시 secret key 확인 로직 수행 <br> - Redis 블랙리스트를 통한 로그아웃 기능 구현  <br> - 회원 탈퇴 시 관련 토큰 무효화 처리                                                                                   | [GitHub](https://github.com/sall6550)    |
|                   이용재                    | 팀원     | ▶ **리뷰 서비스**: <br> -  Kafka 이벤트 기반의 간병인 평점 비동기 갱신 시스템 구현 <br> - 리뷰 작성/수정/삭제 시 간병인 서비스로 이벤트 발행하여 서비스 간 결합도 최소화 <br> - 평균 평점을 기반으로 인기 간병인 Top10 API 조회 기능 지원 <br> ▶ **Ai 서비스**: <br> - Google Gemini API를 활용해 서비스 구현 <br> - 자연어 기반 간병인 추천 기능 설계 및 구현 <br> -리뷰 번역 기능 설계 및 구현                                                                | [GitHub](https://github.com/dydwo6018)    |