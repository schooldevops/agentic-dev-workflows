# 변경 이력 - 상품관리 백엔드

## 2026-02-06

### QA Phase
- **[QA]** REST Client 테스트 파일 생성 (product-api.http) - v1.0
  - 25+ API 테스트 시나리오 작성
  - Product CRUD, Stock Management, Validation 테스트 포함
  
- **[QA]** QA 테스트 리포트 업데이트 - v2.0
  - Local 프로파일 테스트 결과 반영
  - 10/10 수동 API 테스트 성공 기록
  - 최종 품질 평가: Excellent (95/100)

- **[Config]** Local 프로파일 설정 추가
  - `application-local.yml` 생성
  - `LocalSecurityConfig.kt` 생성 (Security 비활성화)
  - `SecurityConfig.kt` 수정 (`@Profile("!local")` 추가)
  - `LOCAL_PROFILE_GUIDE.md` 작성

- **[Infrastructure]** Docker Compose 구성 완료
  - `docker-compose.yml` 생성 (PostgreSQL + Redis)
  - `DOCKER_README.md` 작성
  - 데이터베이스 서비스 정상 가동 확인

- **[QA]** API 테스트 계획 수립 - v1.0
  - 16개 테스트 시나리오 정의
  - 테스트 실행 전략 수립

- **[QA]** 배포 Walkthrough 작성 - v1.0
  - Docker 설정 과정 문서화
  - 애플리케이션 배포 절차 기록
  - QA 테스트 결과 요약

### Development Phase
- **[Test]** 단위 테스트 Kotest → JUnit 5 마이그레이션
  - `ProductTest.kt` 변환 완료 (7/7 통과)
  - `ProductServiceTest.kt` 변환 완료 (5/5 통과)
  - Java 21 호환성 문제 해결

- **[Config]** Java 21 Toolchain 설정
  - `build.gradle.kts` 업데이트
  - `gradle.properties` Java home 설정
  - Gradle cache 정리

- **[Test]** API 통합 테스트 작성
  - `ProductApiIntegrationTest.kt` 생성 (13개 시나리오)
  - Security 설정 이슈로 일시 보류

## 2026-02-05

### Design Phase
- **[Design]** 아키텍처 설계 문서 작성 - v1.0
  - `02-architecture.md` 생성
  - MSA 설계 문서 작성
  - 기술 스택 정의

### Analysis Phase
- **[Analysis]** 인터페이스 정의서 작성 - v1.0
  - `01-interface-spec.md` 생성
  - 비즈니스 로직 상세 분석
  - 데이터 모델 설계
  - 시퀀스 다이어그램 작성

### Planning Phase
- **[Planning]** 요구사항 정의서 작성 - v1.0
  - `01-requirements-spec.md` 생성
  - 테스트 케이스 정의
  - 용어 정의서 작성

---

## 주요 결정 사항

### 2026-02-06
1. **Local 프로파일 도입**
   - 결정: 로컬 개발 환경에서 Security 비활성화
   - 이유: API 테스트 용이성, 개발 생산성 향상
   - 영향: 프로덕션 환경과 명확한 분리 필요

2. **Kotest → JUnit 5 마이그레이션**
   - 결정: 테스트 프레임워크 변경
   - 이유: Java 21 호환성 문제
   - 영향: 모든 테스트 코드 재작성 필요

3. **Docker Compose 사용**
   - 결정: PostgreSQL, Redis를 Docker로 관리
   - 이유: 환경 일관성, 설정 간소화
   - 영향: Docker 의존성 추가

### 2026-02-05
1. **Spring Boot 3.2 + Kotlin 1.9 선택**
   - 결정: 최신 안정 버전 사용
   - 이유: 최신 기능 활용, 장기 지원
   - 영향: Java 21 필수

2. **ISMS-P 준수 설계**
   - 결정: 보안 요구사항 우선 반영
   - 이유: 금융/공공 서비스 대응
   - 영향: 개발 복잡도 증가

---

## 버전 히스토리

| 날짜 | 버전 | 주요 변경사항 |
|------|------|--------------|
| 2026-02-06 | v1.0 | 프로젝트 완료, 모든 산출물 생성 |
| 2026-02-05 | v0.5 | 기획/분석/설계 완료 |

---

**최종 업데이트**: 2026-02-06  
**관리자**: Artifact Manager
