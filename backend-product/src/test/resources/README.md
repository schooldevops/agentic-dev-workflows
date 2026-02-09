# REST Client Test Files

이 디렉토리에는 API 테스트를 위한 REST Client 파일들이 있습니다.

## 사용 방법

### VSCode
1. **REST Client** 확장 설치
2. `.http` 파일 열기
3. 각 요청 위의 `Send Request` 클릭

### IntelliJ IDEA
1. `.http` 파일 열기
2. 각 요청 옆의 ▶️ 아이콘 클릭

### 환경 변수
```
baseUrl = http://localhost:8080
userId = 1
```

## 테스트 파일

- 테스트 실행하기 

```
./gradlew bootRun --args='--spring.profiles.active=local'
```

### product-api.http
상품 관리 API 전체 테스트 케이스

**테스트 카테고리**:
- Product CRUD (10개)
- Stock Management (5개)
- Security & Validation (5개)
- Bulk Data Creation (5개)

**총 테스트**: 25개

## 테스트 실행 순서

### 1. 애플리케이션 시작
```bash
cd backend-product
./gradlew bootRun
```

### 2. 기본 테스트
1. TC-001: Create Product (Success)
2. TC-004: Get Product by Code
3. TC-006: Update Product
4. TC-008: Delete Product

### 3. 재고 테스트
1. TC-011: Increase Stock
2. TC-012: Decrease Stock
3. TC-015: Get Stock History

### 4. 검증 테스트
1. TC-002: Missing Required Field
2. TC-017: Price Validation
3. TC-018: Invalid Product Code

## 주의사항

⚠️ **Security 설정**: 현재 Spring Security가 활성화되어 있어 인증이 필요합니다.
- 테스트 실행 시 403 Forbidden 발생 가능
- 해결: TestSecurityConfig 추가 필요

## 예상 결과

### 성공 케이스
- TC-001, TC-004, TC-006, TC-009, TC-011, TC-012, TC-015

### 실패 케이스 (의도된 실패)
- TC-002: 400 Bad Request (필수 필드 누락)
- TC-003: 409 Conflict (중복 상품코드)
- TC-005: 404 Not Found (존재하지 않는 상품)
- TC-007: 403 Forbidden (권한 없음)
- TC-013: 409 Conflict (재고 부족)
- TC-017: 400 Bad Request (가격 검증 실패)
- TC-018: 400 Bad Request (상품코드 형식 오류)
- TC-019: 400 Bad Request (음수 가격)
- TC-020: 400 Bad Request (음수 재고)
