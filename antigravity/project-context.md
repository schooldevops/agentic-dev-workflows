# Project Context: 상품관리 시스템 개발

## Context Management
컨텍스트 관리는 project-context.md 파일에 요건 내용을 차근차근 기록하고, 결과물은 docs 폴더 하위에 요청이름/하위디렉토리에 순서대로 확인할 수 있도록 번호를 매겨서 생성하여 컨텍스트를 관리해야한다.

## 프로젝트 정보
- **프로젝트명**: 차세대 플랫폼 - 상품관리
- **요청 부서**: 이커머스 사업팀
- **시작일**: 2026-02-06
- **현재 단계**: 요구사항 정의 (Planning)

## Current Phase: System Design (설계 단계) - 완료

## 완료된 산출물
- [x] 요건정의서 (v1.0) - `docs/상품관리/01-requirements/01-requirements-spec.md`
  - 13개 기능/비기능 요구사항 정의
  - 10개 핵심 예외 케이스 식별
  - 20개 리스크 기반 테스트 케이스 작성
- [x] 테스트 케이스 (v1.0) - `docs/상품관리/01-requirements/02-test-cases.md`
  - 상세 테스트 시나리오
  - Kotest/Playwright 자동화 스크립트
  - 테스트 실행 계획 (6일)
- [x] 용어 정의서 (v1.0) - `docs/상품관리/01-requirements/03-glossary.md`
  - 도메인 용어 30개 정의
  - 에러 코드 8개 정의
  - 데이터 타입 명세
- [x] 인터페이스 정의서 (v1.0) - `docs/상품관리/02-analysis/01-interface-spec.md`
  - 12개 API 엔드포인트 정의
  - 요청/응답 스키마 상세
  - 인증/인가 규칙
  - Rate Limiting 정책
- [x] 비즈니스 로직 상세 (v1.0) - `docs/상품관리/02-analysis/02-business-logic-detail.md`
  - 6개 주요 기능 처리 흐름
  - 13개 비즈니스 규칙 (의사코드)
  - 예외 처리 전략
  - 트랜잭션 관리
- [x] 데이터 모델 (v1.0) - `docs/상품관리/02-analysis/03-data-model.md`
  - ERD (7개 엔티티)
  - 엔티티 상세 정의
  - 인덱스 및 제약조건
  - 파티셔닝 전략
- [x] 시퀀스 다이어그램 (v1.0) - `docs/상품관리/02-analysis/04-sequence-diagrams.md`
  - 7개 주요 시나리오
  - 동시성 제어 흐름
  - 캐싱 전략
  - 비동기 처리 흐름
- [x] OpenAPI 스펙 (v1.0) - `docs/상품관리/03-design/01-openapi.yaml`
  - OpenAPI 3.0 표준
  - 12개 엔드포인트 상세
  - 스키마 정의 (Request/Response)
  - 보안 정의 (JWT Bearer)
  - 에러 응답 예시
- [x] 시스템 아키텍처 (v1.0) - `docs/상품관리/03-design/02-architecture.md`
  - C4 모델 다이어그램 (Context, Container, Component)
  - AWS 배포 아키텍처 (ECS, RDS, ElastiCache, S3)
  - 보안 아키텍처 (WAF, JWT, RBAC, Encryption)
  - 확장성 전략 (Auto Scaling, Read Replica)
  - 고가용성 (Multi-AZ, Failover, Backup)
  - 모니터링 메트릭 (Prometheus, Grafana)
  - 성능 최적화 (캐싱, 인덱스, 쿼리)
- [x] MSA 설계 (v1.0) - `docs/상품관리/03-design/03-msa-design.md`
  - 서비스 경계 정의 (6개 서비스)
  - 통신 패턴 (gRPC, Kafka)
  - 데이터 관리 (Database per Service)
  - Saga Pattern (재고 차감)
  - CQRS Pattern (조회 최적화)
  - API Gateway 설계 (Spring Cloud Gateway)
  - Circuit Breaker (Resilience4j)
  - Service Mesh (Istio)
- [x] 기술 스택 (v1.0) - `docs/상품관리/03-design/04-tech-stack.md`
  - Backend: Spring Boot 3.2, Kotlin 1.9
  - Frontend: Next.js 15, React 19
  - Database: PostgreSQL 15, Redis 7, Elasticsearch 8
  - Messaging: Kafka 3.6
  - Infrastructure: Kubernetes 1.28, Istio 1.20
  - Observability: Prometheus, Grafana, Jaeger
  - CI/CD: GitHub Actions, ArgoCD
  - 개발 환경 설정 (Docker Compose)

## 주요 요구사항 요약

### 기능 요구사항 (9개)
1. FR-001: 신규 상품 등록 (P0)
2. FR-002: 기존 상품 정보 수정 (P0)
3. FR-003: 상품 논리 삭제 (P1)
4. FR-004: 상품 목록 조회 (P0)
5. FR-005: 상품 상세 조회 (P0)
6. FR-006: 재고 수량 조정 (P0)
7. FR-007: 상품 상태 변경 (P1)
8. FR-008: 상품 카테고리 설정 (P1)
9. FR-009: 상품 이미지 업로드/삭제 (P1)

### 비기능 요구사항 (3개)
1. NFR-001: 성능 (응답시간 500ms 이하)
2. NFR-002: 보안 (JWT 기반 인증, RBAC)
3. NFR-003: 가용성 (99.9% 이상)

### 핵심 예외 케이스
- 동시성 제어 실패 (재고 동시 수정)
- 대용량 이미지 업로드 타임아웃
- CDN 장애 시 이미지 조회 불가
- 재고 마이너스 발생
- 가격 변경 중 주문 발생

## Next Steps
1. **분석 Agent 활성화**: 인터페이스 정의서 작성
   - API 엔드포인트 정의
   - 요청/응답 스키마 설계
   - 비즈니스 로직 상세화
   - 데이터 모델 설계 (ERD)

2. **사용자 승인 대기** 🚩
   - 요건정의서 검토
   - 테스트 케이스 검토
   - 용어 정의서 검토

## 기술 스택 (예정)
- Backend: Spring Boot 3.4+, Kotlin, JPA
- Frontend: Next.js 15, React, TypeScript
- Database: PostgreSQL 15
- Cache: Redis 7
- Storage: AWS S3 (이미지)
- CDN: AWS CloudFront

## 제약사항
- 초기 상품 데이터: 1,000개 이하
- 월간 활성 사용자(MAU): 10,000명 이하
- 동시 사용자: 1,000명 이상 지원

## 산출물 디렉토리 구조
```
docs/상품관리/
├── 01-requirements/          # 기획 단계 (완료)
│   ├── 01-requirements-spec.md
│   ├── 02-test-cases.md
│   └── 03-glossary.md
├── 02-analysis/              # 분석 단계 (예정)
│   ├── 01-interface-spec.md
│   ├── 02-business-logic-detail.md
│   └── 03-data-model.md
├── 03-design/                # 설계 단계 (예정)
│   ├── 01-openapi.yaml
│   ├── 02-architecture.md
│   └── 03-msa-design.md
├── 04-dev/                   # 개발 단계 (예정)
│   └── 01-sanity-test-report.md
└── 05-qa/                    # QA 단계 (예정)
    ├── 01-e2e-test-report.md
    └── 02-test-coverage.md
```Human Decision Required: 
- AI 응답 지연 시 타임아웃 정책 확정 필요