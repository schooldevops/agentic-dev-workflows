# 테스트 케이스: 상품관리

**프로젝트명**: 차세대 플랫폼 - 상품관리  
**작성일**: 2026-02-06  
**작성자**: 기획 Agent  
**버전**: v1.0

---

## TC 매핑 테이블

| 요구사항 ID | TC ID 목록 | 커버리지 |
|---|---|---|
| FR-001 (상품등록) | TC-001, TC-002, TC-003, TC-004 | 100% |
| FR-002 (상품수정) | TC-006 | 100% |
| FR-003 (상품삭제) | TC-007, TC-017, TC-018 | 100% |
| FR-004 (상품조회) | TC-010, TC-015 | 100% |
| FR-005 (상품상세조회) | TC-011, TC-020 | 100% |
| FR-006 (재고관리) | TC-005, TC-014, TC-019 | 100% |
| FR-007 (상태관리) | TC-008 | 100% |
| FR-008 (카테고리관리) | TC-012 | 100% |
| FR-009 (이미지관리) | TC-013, TC-016 | 100% |
| NFR-001 (성능) | TC-015 | 100% |
| NFR-002 (보안) | TC-009 | 100% |

---

## 상세 테스트 케이스

### TC-001: 정상적인 상품 등록

**관련 요구사항**: FR-001  
**우선순위**: High  
**테스트 유형**: 기능 테스트

**전제조건**:
- 사용자가 판매자 또는 관리자 권한으로 로그인되어 있음
- 카테고리가 사전에 정의되어 있음

**테스트 단계**:
1. 상품 등록 API 호출
2. 다음 정보 입력:
   - 상품명: "삼성 노트북 갤럭시북"
   - 카테고리: "전자제품 > 노트북"
   - 가격: 1,500,000원
   - 재고: 10개
   - 상품설명: "최신 모델 노트북"
   - 이미지: 3장 (각 2MB)

**기대 결과**:
- HTTP 201 Created
- 응답 본문:
  ```json
  {
    "productCode": "PRD-20260206-0001",
    "productName": "삼성 노트북 갤럭시북",
    "status": "임시저장",
    "createdAt": "2026-02-06T11:25:00Z",
    "message": "상품이 성공적으로 등록되었습니다"
  }
  ```

---

### TC-002: 필수 정보 누락 시 등록 실패

**관련 요구사항**: FR-001  
**우선순위**: High  
**테스트 유형**: 부정 테스트

**전제조건**:
- 사용자가 판매자 권한으로 로그인되어 있음

**테스트 단계**:
1. 상품 등록 API 호출
2. 가격 필드 누락하고 입력:
   - 상품명: "노트북"
   - 재고: 10개

**기대 결과**:
- HTTP 400 Bad Request
- 응답 본문:
  ```json
  {
    "error": "VALIDATION_ERROR",
    "message": "가격은 필수 입력 항목입니다",
    "field": "price"
  }
  ```

---

### TC-005: 동시 재고 감소 시 정합성 검증

**관련 요구사항**: FR-006  
**우선순위**: Critical  
**테스트 유형**: 동시성 테스트

**전제조건**:
- 상품코드 "PRD-20260206-0001"의 재고가 10개

**테스트 단계**:
1. 두 개의 동시 요청 생성
2. 요청 1: 재고 5개 출고
3. 요청 2: 재고 5개 출고 (동시 실행)

**기대 결과**:
- 요청 1: HTTP 200 OK, 재고 5개로 감소
- 요청 2: HTTP 409 Conflict
  ```json
  {
    "error": "INSUFFICIENT_STOCK",
    "message": "재고가 부족합니다",
    "currentStock": 5,
    "requestedQuantity": 5
  }
  ```

---

### TC-009: 권한 없는 사용자의 상품 수정 시도

**관련 요구사항**: NFR-002  
**우선순위**: Critical  
**테스트 유형**: 보안 테스트

**전제조건**:
- 판매자 A가 등록한 상품 "PRD-20260206-0001"
- 판매자 B로 로그인

**테스트 단계**:
1. 판매자 B의 JWT 토큰으로 상품 수정 API 호출
2. 상품코드: "PRD-20260206-0001"
3. 가격을 1,000,000원으로 변경 시도

**기대 결과**:
- HTTP 403 Forbidden
- 응답 본문:
  ```json
  {
    "error": "FORBIDDEN",
    "message": "해당 상품을 수정할 권한이 없습니다",
    "productCode": "PRD-20260206-0001"
  }
  ```

---

### TC-015: 상품 목록 조회 응답 시간 검증

**관련 요구사항**: NFR-001  
**우선순위**: High  
**테스트 유형**: 성능 테스트

**전제조건**:
- 데이터베이스에 1,000개의 상품 데이터 존재

**테스트 단계**:
1. 상품 목록 조회 API 호출
2. 필터 조건 없음
3. 페이지 크기: 20
4. 응답 시간 측정

**기대 결과**:
- HTTP 200 OK
- 응답 시간: 500ms 이하
- 응답 본문: 20개 상품 목록 반환
- 총 건수: 1,000

---

## 테스트 자동화 스크립트 (샘플)

### Kotest 기반 단위 테스트

```kotlin
class ProductServiceTest : BehaviorSpec({
    Given("판매자가 로그인되어 있고") {
        val sellerId = 123L
        val productService = ProductService(productRepository, imageService)
        
        When("정상적인 상품 정보로 등록하면") {
            val request = CreateProductRequest(
                productName = "삼성 노트북 갤럭시북",
                categoryId = 1L,
                price = BigDecimal("1500000"),
                stock = 10,
                description = "최신 모델 노트북",
                images = listOf("image1.jpg", "image2.jpg", "image3.jpg")
            )
            val result = productService.createProduct(sellerId, request)
            
            Then("상품이 성공적으로 등록되어야 한다") {
                result.productCode shouldStartWith "PRD-"
                result.status shouldBe ProductStatus.DRAFT
                result.productName shouldBe "삼성 노트북 갤럭시북"
            }
        }
        
        When("필수 정보가 누락되면") {
            val invalidRequest = CreateProductRequest(
                productName = "노트북",
                categoryId = 1L,
                price = null, // 가격 누락
                stock = 10
            )
            
            Then("예외가 발생해야 한다") {
                val exception = shouldThrow<ValidationException> {
                    productService.createProduct(sellerId, invalidRequest)
                }
                exception.message shouldContain "가격은 필수"
            }
        }
    }
})
```

### Playwright 기반 E2E 테스트

```typescript
test.describe('상품 관리', () => {
  test('TC-001: 정상적인 상품 등록', async ({ page }) => {
    // Given: 판매자로 로그인
    await page.goto('/login');
    await page.fill('[name="email"]', 'seller@example.com');
    await page.fill('[name="password"]', 'password');
    await page.click('button[type="submit"]');
    
    // When: 상품 등록 페이지에서 정보 입력
    await page.goto('/products/new');
    await page.fill('[name="productName"]', '삼성 노트북 갤럭시북');
    await page.selectOption('[name="category"]', '전자제품 > 노트북');
    await page.fill('[name="price"]', '1500000');
    await page.fill('[name="stock"]', '10');
    await page.fill('[name="description"]', '최신 모델 노트북');
    
    // 이미지 업로드
    await page.setInputFiles('[name="images"]', [
      'test-images/image1.jpg',
      'test-images/image2.jpg',
      'test-images/image3.jpg'
    ]);
    
    await page.click('button:has-text("등록")');
    
    // Then: 성공 메시지 확인
    await expect(page.locator('.success-message')).toHaveText(
      '상품이 성공적으로 등록되었습니다'
    );
    
    // 상품코드 생성 확인
    const productCode = await page.locator('.product-code').textContent();
    expect(productCode).toMatch(/^PRD-\d{8}-\d{4}$/);
  });
  
  test('TC-009: 권한 없는 사용자의 상품 수정 시도', async ({ page }) => {
    // Given: 판매자 B로 로그인
    await loginAs(page, 'sellerB@example.com');
    
    // When: 판매자 A의 상품 수정 시도
    await page.goto('/products/PRD-20260206-0001/edit');
    
    // Then: 권한 없음 메시지 표시
    await expect(page.locator('.error-message')).toHaveText(
      '해당 상품을 수정할 권한이 없습니다'
    );
  });
});
```

---

## 테스트 실행 계획

### Phase 1: 단위 테스트 (1일)
- 모든 비즈니스 로직 테스트
- 목표 커버리지: 80% 이상

### Phase 2: 통합 테스트 (1일)
- API 엔드포인트 테스트
- 데이터베이스 연동 테스트

### Phase 3: E2E 테스트 (2일)
- 사용자 시나리오 기반 테스트
- 크로스 브라우저 테스트

### Phase 4: 성능 테스트 (1일)
- 부하 테스트 (1,000 동시 사용자)
- 응답 시간 검증

### Phase 5: 보안 테스트 (1일)
- 권한 검증
- SQL Injection 방어 확인

---

**총 예상 테스트 기간**: 6일  
**다음 단계**: QA Agent에게 인계하여 E2E 테스트 자동화
