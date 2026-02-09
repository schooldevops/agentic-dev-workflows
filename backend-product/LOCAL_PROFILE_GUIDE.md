# Local Profile 사용 가이드

## 로컬 개발 환경 실행

### 방법 1: Gradle 명령어
```bash
cd backend-product
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 방법 2: 환경 변수
```bash
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun
```

### 방법 3: IntelliJ IDEA
1. Run/Debug Configurations 열기
2. Active profiles: `local` 입력
3. Run

## Local 프로파일 특징

### Security 설정
- ✅ **인증 비활성화**: 모든 API 요청 허용
- ✅ **CSRF 비활성화**: REST API 테스트 용이
- ✅ **CORS 비활성화**: 프론트엔드 개발 편의

⚠️ **주의**: 이 설정은 개발 환경 전용입니다. 프로덕션에서는 절대 사용하지 마세요!

### 로깅
- SQL 쿼리 출력
- 파라미터 바인딩 출력
- Spring Security 디버그 로그

### 에러 메시지
- 상세한 에러 메시지 포함
- 스택 트레이스 출력 (쿼리 파라미터로 제어)

## 프로파일별 Security 설정

| Profile | Security Config | 인증 필요 | 용도 |
|---------|----------------|---------|------|
| local | LocalSecurityConfig | ❌ No | 로컬 개발/테스트 |
| test | TestSecurityConfig | ❌ No | 자동화 테스트 |
| default | SecurityConfig | ✅ Yes | 프로덕션 |

## API 테스트

### REST Client 사용
```bash
# 1. 애플리케이션 시작 (local 프로파일)
./gradlew bootRun --args='--spring.profiles.active=local'

# 2. REST Client 파일 열기
# backend-product/src/test/resources/product-api.http

# 3. Send Request 클릭
```

### curl 사용
```bash
# 상품 생성 (인증 불필요)
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "productCode": "PRD-000001",
    "productName": "테스트 상품",
    "description": "설명",
    "price": 10000,
    "stock": 100,
    "sellerId": 1,
    "categoryId": 1
  }'
```

## 데이터베이스

### PostgreSQL (Docker)
```bash
# 시작
docker-compose up -d postgres

# 접속
docker exec -it product-postgres psql -U postgres -d product_db
```

### Redis (Docker)
```bash
# 시작
docker-compose up -d redis

# 접속
docker exec -it product-redis redis-cli
```

## 프로파일 전환

### 개발 → 프로덕션
```bash
# 프로덕션 모드 (Security 활성화)
./gradlew bootRun
# 또는
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### 개발 → 테스트
```bash
# 테스트 모드 (H2 in-memory)
./gradlew test
```

## 확인 방법

### 현재 활성 프로파일 확인
애플리케이션 시작 로그에서 확인:
```
The following 1 profile is active: "local"
```

### Security 설정 확인
```bash
# local 프로파일: 200 OK
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"productCode":"PRD-TEST",...}'

# default 프로파일: 403 Forbidden
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"productCode":"PRD-TEST",...}'
```
