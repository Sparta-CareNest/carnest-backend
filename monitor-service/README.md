# 📈 모니터링 서비스 (Monitoring Service)

Spring Cloud 기반 MSA 아키텍처에서 각 서비스의 메트릭 정보를 수집하고 시각화하는 모니터링 도구입니다.  
Prometheus와 Grafana를 기반으로 구성되어 있습니다.

---

## 🚀 실행 방법

모든 컨테이너를 백그라운드 모드로 실행하려면 아래 명령어를 실행하세요:

```bash
docker-compose up -d
```

# 🔍 포함 구성 요소
- Prometheus
각 서비스의 /actuator/prometheus 엔드포인트에서 메트릭 정보를 수집합니다.
- Grafana
Prometheus에서 수집한 데이터를 시각화하여 대시보드로 제공합니다.


# 📌 기본 설정
- Prometheus UI: http://localhost:9090
- Grafana UI: http://localhost:3000
- 기본 ID/PW: admin / admin
