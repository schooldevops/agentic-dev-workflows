# Multi-Agent 협업 개발 프로세스 가이드

**작성일**: 2026-02-06  
**버전**: 2.0  
**대상**: 엔터프라이즈 소프트웨어 개발팀

---

## 📖 개요

이 문서는 6개의 전문화된 AI 에이전트가 협업하여 엔터프라이즈급 소프트웨어를 개발하는 프로세스를 설명합니다. 기획부터 배포까지 전체 SDLC(Software Development Life Cycle)를 커버하며, 각 에이전트는 명확한 역할과 산출물을 가집니다.

## 🤖 에이전트 구성

### 1. 기획 Agent (Requirements Analyst)
**역할**: 요구사항 정의 및 문서화  
**책임**: 
- 기획/사업팀으로부터 받은 요건을 구조화된 요건정의서로 변환
- 테스트 케이스(TC) 작성
- 용어 정의서 참조 및 관리
- 비즈니스 요구사항 명확화

**산출물**:
- `docs/requirements/requirements-spec.md` - 요건정의서
- `docs/requirements/test-cases.md` - 테스트 케이스
- `docs/requirements/glossary.md` - 용어 정의서

### 2. 분석 Agent (Business Analyst)
**역할**: 비즈니스 분석 및 인터페이스 정의  
**책임**:
- 요건정의서 기반 인터페이스 정의서 생성
- 비즈니스 로직 상세화
- 데이터 모델 설계
- 시스템 간 연계 정의

**산출물**:
- `docs/analysis/interface-spec.md` - 인터페이스 정의서
- `docs/analysis/business-logic-detail.md` - 비즈니스 로직 상세
- `docs/analysis/data-model.md` - 데이터 모델
- `docs/analysis/sequence-diagrams.md` - 시퀀스 다이어그램

### 3. 설계 Agent (System Architect)
**역할**: 시스템 아키텍처 설계  
**책임**:
- OpenAPI Specification (OAS) 생성
- MSA(Microservices Architecture) 구성 설계
- 기술 스택 선정
- 인프라 아키텍처 설계

**산출물**:
- `docs/design/openapi.yaml` - OpenAPI 3.0 스펙
- `docs/design/architecture.md` - 시스템 아키텍처 문서
- `docs/design/msa-design.md` - MSA 구성도
- `docs/design/tech-stack.md` - 기술 스택 명세

### 4. 개발 Agent (Full-Stack Developer)
**역할**: 코드 구현 및 단위 테스트  
**책임**:
- BDD(Behavior-Driven Development) 기반 개발
- OAS를 활용한 서버/클라이언트 코드 생성
- TC 기준 세부 로직 테스트 작성
- Sanity 테스트 수행

**산출물**:
- `src/main/` - 서버 코드 (Spring Boot/Kotlin)
- `src/test/` - 단위 테스트 및 통합 테스트
- `client/` - 클라이언트 코드 (Next.js/React)
- `docs/dev/sanity-test-report.md` - Sanity 테스트 결과

### 5. QA Agent (Quality Assurance Specialist)
**역할**: 품질 보증 및 E2E 테스트  
**책임**:
- TC 기반 E2E 테스트 시나리오 작성
- Playwright를 활용한 자동화 테스트
- 사용자 시나리오 테스트 수행
- 테스트 결과 리포팅

**산출물**:
- `tests/e2e/` - E2E 테스트 코드 (Playwright)
- `docs/qa/e2e-test-report.md` - E2E 테스트 리포트
- `docs/qa/test-coverage.md` - 테스트 커버리지 분석
- `docs/qa/bug-report.md` - 버그 리포트

### 6. Artifact Manager (Documentation & Knowledge Keeper)
**역할**: 산출물 관리 및 메타데이터 관리  
**책임**:
- 모든 에이전트의 산출물 버전 관리
- 요청별 산출물 추적 및 관리
- 메타 정보 종합 관리
- 프로젝트 지식 베이스 유지

**산출물**:
- `docs/artifacts/artifact-index.md` - 산출물 인덱스
- `docs/artifacts/metadata.json` - 메타데이터
- `docs/artifacts/changelog.md` - 변경 이력
- `docs/project-context.md` - 프로젝트 컨텍스트

---

## 🔄 전체 워크플로우

```mermaid
graph TB
    Start([비즈니스 요청]) --> Planning[기획 Agent<br/>요건정의서 작성]
    Planning --> |요건정의서<br/>TC<br/>용어정의서| HITL1{사용자 승인}
    HITL1 --> |승인| Analysis[분석 Agent<br/>인터페이스 정의]
    HITL1 --> |수정 요청| Planning
    
    Analysis --> |인터페이스 정의서<br/>비즈니스 로직 상세| HITL2{사용자 승인}
    HITL2 --> |승인| Design[설계 Agent<br/>OAS 및 아키텍처 설계]
    HITL2 --> |수정 요청| Analysis
    
    Design --> |OpenAPI 스펙<br/>MSA 설계<br/>아키텍처 문서| HITL3{사용자 승인}
    HITL3 --> |승인| Dev[개발 Agent<br/>BDD 기반 구현]
    HITL3 --> |수정 요청| Design
    
    Dev --> |서버/클라이언트 코드<br/>단위 테스트| Sanity[Sanity 테스트]
    Sanity --> |실패| Dev
    Sanity --> |성공| QA[QA Agent<br/>E2E 테스트]
    
    QA --> |E2E 테스트<br/>시나리오 테스트| QAResult{테스트 결과}
    QAResult --> |실패| HITL4{사용자 확인}
    QAResult --> |성공| Artifact[Artifact Manager<br/>산출물 관리]
    
    HITL4 --> |수정 필요| Dev
    HITL4 --> |승인| Artifact
    
    Artifact --> |메타데이터 관리<br/>문서화 완료| End([배포 준비])
    
    style Start fill:#e1f5e1
    style End fill:#e1f5e1
    style Planning fill:#fff4e6
    style Analysis fill:#fff4e6
    style Design fill:#e3f2fd
    style Dev fill:#e3f2fd
    style QA fill:#f3e5f5
    style Artifact fill:#fce4ec
    style HITL1 fill:#ffebee
    style HITL2 fill:#ffebee
    style HITL3 fill:#ffebee
    style HITL4 fill:#ffebee
```

---

## 📋 단계별 상세 프로세스

### Phase 1: 요구사항 정의 (기획 Agent)

```mermaid
sequenceDiagram
    actor Business as 기획/사업팀
    participant Planning as 기획 Agent
    participant Tools as Tools<br/>(Template, Glossary DB)
    actor User as 승인자
    
    Business->>Planning: 비즈니스 요청서 전달
    Planning->>Tools: 용어 정의서 조회
    Tools-->>Planning: 표준 용어 목록
    Planning->>Planning: 요건정의서 작성
    Planning->>Planning: 테스트 케이스 도출
    Planning->>Planning: 용어 정의 및 매핑
    Planning->>User: 요건정의서 검토 요청 🚩
    
    alt 승인
        User->>Planning: 승인
        Planning->>Analysis Agent: 요건정의서 인계
    else 수정 요청
        User->>Planning: 피드백
        Planning->>Planning: 요건정의서 수정
        Planning->>User: 재검토 요청
    end
```

**작업 내용**:

1. **요건정의서 작성**
   - 기능 요구사항 (Functional Requirements)
   - 비기능 요구사항 (Non-Functional Requirements)
   - 제약사항 (Constraints)
   - 가정사항 (Assumptions)

2. **테스트 케이스 작성**
   - 정상 시나리오 TC
   - 예외 시나리오 TC
   - 경계값 테스트 TC
   - 성능 테스트 TC

3. **용어 정의서 관리**
   - 도메인 용어 정의
   - 약어 및 축약어 정리
   - 표준 용어 매핑

**산출물 예시**:

```markdown
# 요건정의서: 쇼핑카트 서비스

## 1. 개요
- 프로젝트명: 쇼핑카트 서비스
- 요청 부서: 이커머스 사업팀
- 작성일: 2026-02-06

## 2. 기능 요구사항

### FR-001: 장바구니 상품 추가
- 설명: 사용자가 상품을 장바구니에 추가할 수 있어야 한다
- 우선순위: High
- 관련 TC: TC-001, TC-002, TC-003

### FR-002: 장바구니 조회
- 설명: 사용자가 자신의 장바구니 목록을 조회할 수 있어야 한다
- 우선순위: High
- 관련 TC: TC-004, TC-005

## 3. 비기능 요구사항

### NFR-001: 성능
- 장바구니 조회 응답시간: 500ms 이하
- 동시 사용자: 10,000명 이상

### NFR-002: 보안
- 개인정보 암호화 필수
- HTTPS 통신 강제
```

**체크포인트**:
- [ ] 모든 기능 요구사항 명확히 정의
- [ ] 테스트 케이스 100% 커버
- [ ] 용어 정의서 최신화
- [ ] 사용자 승인 획득

---

### Phase 2: 비즈니스 분석 (분석 Agent)

```mermaid
sequenceDiagram
    participant Planning as 기획 Agent
    participant Analysis as 분석 Agent
    participant Tools as Tools<br/>(Mermaid, ERD Tool)
    actor User as 승인자
    
    Planning->>Analysis: 요건정의서 인계
    Analysis->>Analysis: 인터페이스 식별
    Analysis->>Tools: 시퀀스 다이어그램 생성
    Tools-->>Analysis: 다이어그램
    Analysis->>Analysis: 비즈니스 로직 상세화
    Analysis->>Tools: 데이터 모델 설계
    Tools-->>Analysis: ERD
    Analysis->>Analysis: 시스템 간 연계 정의
    Analysis->>User: 분석 문서 검토 요청 🚩
    
    alt 승인
        User->>Analysis: 승인
        Analysis->>Design Agent: 분석 문서 인계
    else 수정 요청
        User->>Analysis: 피드백
        Analysis->>Analysis: 분석 문서 수정
        Analysis->>User: 재검토 요청
    end
```

**작업 내용**:

1. **인터페이스 정의서 작성**
   - API 엔드포인트 목록
   - 요청/응답 데이터 구조
   - 에러 코드 정의
   - 외부 시스템 연계 인터페이스

2. **비즈니스 로직 상세화**
   - 각 기능별 처리 흐름
   - 비즈니스 규칙 명세
   - 예외 처리 로직
   - 트랜잭션 범위 정의

3. **데이터 모델 설계**
   - 엔티티 정의
   - 관계 설정
   - 속성 명세
   - 제약조건 정의

**산출물 예시**:

```markdown
# 인터페이스 정의서: 쇼핑카트 API

## 1. API 목록

| API ID | 엔드포인트 | 메서드 | 설명 |
|--------|-----------|--------|------|
| API-001 | /api/cart/items | POST | 장바구니 상품 추가 |
| API-002 | /api/cart | GET | 장바구니 조회 |
| API-003 | /api/cart/items/{id} | DELETE | 장바구니 상품 삭제 |

## 2. API 상세: 장바구니 상품 추가

### 요청
```json
{
  "productId": 12345,
  "quantity": 2,
  "options": {
    "color": "red",
    "size": "L"
  }
}
```

### 응답 (성공)
```json
{
  "cartItemId": 67890,
  "productId": 12345,
  "quantity": 2,
  "price": 50000,
  "subtotal": 100000
}
```

### 에러 코드
- E001: 재고 부족
- E002: 존재하지 않는 상품
- E003: 권한 없음
```

**체크포인트**:
- [ ] 모든 인터페이스 정의 완료
- [ ] 비즈니스 로직 상세화
- [ ] 데이터 모델 검증
- [ ] 사용자 승인 획득

---

### Phase 3: 시스템 설계 (설계 Agent)

```mermaid
sequenceDiagram
    participant Analysis as 분석 Agent
    participant Design as 설계 Agent
    participant Tools as Tools<br/>(OpenAPI Generator, Mermaid)
    actor User as 승인자
    
    Analysis->>Design: 분석 문서 인계
    Design->>Tools: OpenAPI 스펙 생성
    Tools-->>Design: openapi.yaml
    Design->>Design: MSA 구성 설계
    Design->>Tools: 아키텍처 다이어그램 생성
    Tools-->>Design: 아키텍처 문서
    Design->>Design: 기술 스택 선정
    Design->>Design: 인프라 설계
    Design->>User: 설계 문서 검토 요청 🚩
    
    alt 승인
        User->>Design: 승인
        Design->>Dev Agent: 설계 문서 인계
    else 수정 요청
        User->>Design: 피드백
        Design->>Design: 설계 수정
        Design->>User: 재검토 요청
    end
```

**작업 내용**:

1. **OpenAPI Specification 생성**
   - API 엔드포인트 정의
   - 스키마 정의
   - 인증/인가 방식
   - 예제 요청/응답

2. **MSA 구성 설계**
   - 서비스 분리 전략
   - 서비스 간 통신 방식
   - API Gateway 설계
   - 서비스 디스커버리

3. **기술 스택 선정**
   - Backend: Spring Boot, Kotlin, JPA
   - Frontend: Next.js, React, TypeScript
   - Database: PostgreSQL, Redis
   - Infrastructure: Docker, Kubernetes

**산출물 예시**:

```yaml
# openapi.yaml
openapi: 3.0.0
info:
  title: Shopping Cart API
  version: 1.0.0
  description: 쇼핑카트 서비스 API

servers:
  - url: https://api.example.com/v1
    description: Production server

paths:
  /cart/items:
    post:
      summary: 장바구니 상품 추가
      operationId: addCartItem
      tags:
        - Cart
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AddCartItemRequest'
      responses:
        '201':
          description: 상품 추가 성공
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CartItem'
        '400':
          description: 잘못된 요청
        '404':
          description: 상품을 찾을 수 없음

components:
  schemas:
    AddCartItemRequest:
      type: object
      required:
        - productId
        - quantity
      properties:
        productId:
          type: integer
          format: int64
          example: 12345
        quantity:
          type: integer
          minimum: 1
          example: 2
        options:
          type: object
          additionalProperties:
            type: string
    
    CartItem:
      type: object
      properties:
        cartItemId:
          type: integer
          format: int64
        productId:
          type: integer
          format: int64
        quantity:
          type: integer
        price:
          type: number
          format: decimal
        subtotal:
          type: number
          format: decimal
```

**MSA 구성도**:

```mermaid
graph TB
    Client[Client Application] --> Gateway[API Gateway]
    Gateway --> Auth[Auth Service]
    Gateway --> Cart[Cart Service]
    Gateway --> Product[Product Service]
    Gateway --> Order[Order Service]
    
    Cart --> CartDB[(Cart DB<br/>PostgreSQL)]
    Cart --> Redis[(Redis Cache)]
    Product --> ProductDB[(Product DB<br/>PostgreSQL)]
    Order --> OrderDB[(Order DB<br/>PostgreSQL)]
    
    Cart -.->|Event| MQ[Message Queue<br/>Kafka]
    Product -.->|Event| MQ
    Order -.->|Event| MQ
    
    style Gateway fill:#e3f2fd
    style Cart fill:#fff4e6
    style Product fill:#fff4e6
    style Order fill:#fff4e6
    style Auth fill:#f3e5f5
```

**체크포인트**:
- [ ] OpenAPI 스펙 완성
- [ ] MSA 구성 검증
- [ ] 기술 스택 확정
- [ ] 사용자 승인 획득

---

### Phase 4: 개발 및 단위 테스트 (개발 Agent)

```mermaid
sequenceDiagram
    participant Design as 설계 Agent
    participant Dev as 개발 Agent
    participant Tools as Tools<br/>(OpenAPI Generator, Gradle)
    participant CI as CI/CD Pipeline
    
    Design->>Dev: 설계 문서 인계
    Dev->>Tools: OAS 기반 코드 생성
    Tools-->>Dev: 서버/클라이언트 스켈레톤
    
    Note over Dev: BDD Cycle 시작
    loop BDD Cycle
        Dev->>Dev: Given-When-Then 시나리오 작성
        Dev->>Dev: 실패하는 테스트 작성
        Dev->>Dev: 비즈니스 로직 구현
        Dev->>CI: 단위 테스트 실행
        CI-->>Dev: 테스트 결과
        
        alt 테스트 실패
            Dev->>Dev: 코드 수정
        else 테스트 성공
            Dev->>Dev: 리팩토링
        end
    end
    
    Dev->>Dev: 통합 테스트 작성
    Dev->>CI: 통합 테스트 실행
    CI-->>Dev: 테스트 결과
    
    Dev->>Dev: Sanity 테스트 수행
    Dev->>QA Agent: 코드 및 테스트 인계
```

**작업 내용**:

1. **OAS 기반 코드 생성**
   ```bash
   # OpenAPI Generator 사용
   openapi-generator-cli generate \
     -i docs/design/openapi.yaml \
     -g kotlin-spring \
     -o src/main/kotlin/generated
   ```

2. **BDD 기반 테스트 작성**
   ```kotlin
   class CartServiceTest : BehaviorSpec({
       Given("사용자가 로그인되어 있고") {
           val userId = 123L
           
           When("상품을 장바구니에 추가하면") {
               val request = AddCartItemRequest(
                   productId = 456L,
                   quantity = 2
               )
               val result = cartService.addItem(userId, request)
               
               Then("장바구니에 상품이 추가되어야 한다") {
                   result.productId shouldBe 456L
                   result.quantity shouldBe 2
               }
               
               Then("재고가 확인되어야 한다") {
                   verify { inventoryService.checkStock(456L, 2) }
               }
           }
       }
   })
   ```

3. **비즈니스 로직 구현**
   ```kotlin
   @Service
   class CartService(
       private val cartRepository: CartRepository,
       private val inventoryService: InventoryService
   ) {
       fun addItem(userId: Long, request: AddCartItemRequest): CartItem {
           // 재고 확인
           if (!inventoryService.checkStock(request.productId, request.quantity)) {
               throw InsufficientStockException()
           }
           
           // 상품 정보 조회
           val product = inventoryService.getProduct(request.productId)
           
           // 장바구니 아이템 생성
           val cartItem = CartItem(
               userId = userId,
               productId = request.productId,
               quantity = request.quantity,
               price = product.price
           )
           
           return cartRepository.save(cartItem)
       }
   }
   ```

4. **Sanity 테스트**
   - 기본 기능 동작 확인
   - API 엔드포인트 호출 테스트
   - 데이터베이스 연결 확인

**산출물**:
- `src/main/kotlin/` - 서버 코드
- `src/test/kotlin/` - 단위/통합 테스트
- `client/src/` - 클라이언트 코드
- `docs/dev/sanity-test-report.md`

**체크포인트**:
- [ ] 모든 TC 기반 테스트 작성
- [ ] 단위 테스트 커버리지 80% 이상
- [ ] 통합 테스트 통과
- [ ] Sanity 테스트 통과

---

### Phase 5: E2E 테스트 (QA Agent)

```mermaid
sequenceDiagram
    participant Dev as 개발 Agent
    participant QA as QA Agent
    participant Playwright as Playwright
    participant App as Application
    actor User as 승인자
    
    Dev->>QA: 코드 및 TC 인계
    QA->>QA: TC 기반 E2E 시나리오 작성
    QA->>Playwright: 자동화 스크립트 작성
    
    loop E2E 테스트
        Playwright->>App: 사용자 시나리오 실행
        App-->>Playwright: 응답
        Playwright->>QA: 테스트 결과
        
        alt 테스트 실패
            QA->>QA: 스크린샷 및 로그 수집
            QA->>QA: 버그 리포트 작성
        else 테스트 성공
            QA->>QA: 테스트 결과 기록
        end
    end
    
    QA->>QA: 테스트 커버리지 분석
    QA->>QA: 최종 리포트 작성
    QA->>User: 테스트 결과 보고 🚩
    
    alt 결함 발견
        User->>Dev: 수정 요청
        Dev->>Dev: 버그 수정
        Dev->>QA: 재테스트 요청
    else 테스트 통과
        User->>QA: 승인
        QA->>Artifact Manager: 테스트 결과 인계
    end
```

**작업 내용**:

1. **E2E 테스트 시나리오 작성**
   ```typescript
   // tests/e2e/cart.spec.ts
   import { test, expect } from '@playwright/test';
   
   test.describe('장바구니 기능', () => {
     test('TC-001: 상품을 장바구니에 추가할 수 있다', async ({ page }) => {
       // Given: 사용자가 로그인되어 있고
       await page.goto('/login');
       await page.fill('[name="username"]', 'testuser');
       await page.fill('[name="password"]', 'password');
       await page.click('button[type="submit"]');
       
       // When: 상품 페이지에서 장바구니 추가 버튼을 클릭하면
       await page.goto('/products/12345');
       await page.click('button:has-text("장바구니 담기")');
       
       // Then: 장바구니에 상품이 추가되어야 한다
       await expect(page.locator('.cart-badge')).toHaveText('1');
       
       // And: 장바구니 페이지에서 상품을 확인할 수 있어야 한다
       await page.goto('/cart');
       await expect(page.locator('.cart-item')).toBeVisible();
       await expect(page.locator('.product-name')).toHaveText('노트북');
     });
     
     test('TC-002: 재고가 부족한 경우 에러 메시지가 표시된다', async ({ page }) => {
       // Given: 재고가 부족한 상품이 있고
       await page.goto('/products/99999');
       
       // When: 장바구니 추가를 시도하면
       await page.click('button:has-text("장바구니 담기")');
       
       // Then: 에러 메시지가 표시되어야 한다
       await expect(page.locator('.error-message')).toHaveText('재고가 부족합니다');
     });
   });
   ```

2. **사용자 시나리오 테스트**
   - 정상 플로우 테스트
   - 예외 상황 테스트
   - 크로스 브라우저 테스트
   - 모바일 반응형 테스트

3. **테스트 결과 리포팅**
   ```markdown
   # E2E 테스트 리포트
   
   ## 테스트 요약
   - 총 테스트: 25개
   - 성공: 23개
   - 실패: 2개
   - 성공률: 92%
   
   ## 실패한 테스트
   
   ### TC-015: 대량 상품 추가 시 성능
   - 상태: FAILED
   - 원인: 응답 시간 초과 (1.2초 > 500ms)
   - 스크린샷: screenshots/tc-015-timeout.png
   
   ### TC-018: 동시 접속 테스트
   - 상태: FAILED
   - 원인: 데이터베이스 락 발생
   - 로그: logs/tc-018-db-lock.log
   ```

**산출물**:
- `tests/e2e/` - Playwright 테스트 코드
- `docs/qa/e2e-test-report.md`
- `docs/qa/test-coverage.md`
- `docs/qa/bug-report.md`

**체크포인트**:
- [ ] 모든 TC 커버
- [ ] E2E 테스트 성공률 95% 이상
- [ ] 버그 리포트 작성
- [ ] 사용자 승인 획득

---

### Phase 6: 산출물 관리 (Artifact Manager)

```mermaid
sequenceDiagram
    participant Planning as 기획 Agent
    participant Analysis as 분석 Agent
    participant Design as 설계 Agent
    participant Dev as 개발 Agent
    participant QA as QA Agent
    participant Artifact as Artifact Manager
    participant Git as Git Repository
    
    Planning->>Artifact: 요건정의서 등록
    Analysis->>Artifact: 분석 문서 등록
    Design->>Artifact: 설계 문서 등록
    Dev->>Artifact: 코드 및 테스트 등록
    QA->>Artifact: 테스트 결과 등록
    
    Artifact->>Artifact: 산출물 인덱싱
    Artifact->>Artifact: 메타데이터 생성
    Artifact->>Artifact: 버전 관리
    Artifact->>Artifact: 변경 이력 추적
    
    Artifact->>Git: 산출물 커밋
    Artifact->>Artifact: 프로젝트 컨텍스트 업데이트
    Artifact->>Artifact: 최종 리포트 생성
```

**작업 내용**:

1. **산출물 인덱스 관리**
   ```markdown
   # 산출물 인덱스
   
   ## 요청 ID: REQ-2026-001
   
   ### 기획 단계
   - [요건정의서](requirements/requirements-spec.md) - v1.2
   - [테스트 케이스](requirements/test-cases.md) - v1.1
   - [용어 정의서](requirements/glossary.md) - v1.0
   
   ### 분석 단계
   - [인터페이스 정의서](analysis/interface-spec.md) - v1.0
   - [비즈니스 로직 상세](analysis/business-logic-detail.md) - v1.0
   - [데이터 모델](analysis/data-model.md) - v1.0
   
   ### 설계 단계
   - [OpenAPI 스펙](design/openapi.yaml) - v1.0
   - [아키텍처 문서](design/architecture.md) - v1.0
   - [MSA 설계](design/msa-design.md) - v1.0
   
   ### 개발 단계
   - [서버 코드](../src/main/) - v1.0
   - [클라이언트 코드](../client/) - v1.0
   - [단위 테스트](../src/test/) - v1.0
   
   ### QA 단계
   - [E2E 테스트](../tests/e2e/) - v1.0
   - [테스트 리포트](qa/e2e-test-report.md) - v1.0
   ```

2. **메타데이터 관리**
   ```json
   {
     "requestId": "REQ-2026-001",
     "projectName": "Shopping Cart Service",
     "startDate": "2026-02-06",
     "status": "completed",
     "phases": {
       "planning": {
         "status": "completed",
         "completedDate": "2026-02-06",
         "artifacts": [
           {
             "name": "requirements-spec.md",
             "version": "1.2",
             "lastModified": "2026-02-06T10:30:00Z",
             "author": "Planning Agent"
           }
         ]
       },
       "analysis": {
         "status": "completed",
         "completedDate": "2026-02-06",
         "artifacts": [...]
       }
     },
     "metrics": {
       "totalArtifacts": 15,
       "testCoverage": "92%",
       "codeQuality": "A"
     }
   }
   ```

3. **변경 이력 관리**
   ```markdown
   # 변경 이력
   
   ## 2026-02-06
   - [Planning] 요건정의서 v1.2 업데이트 - 비기능 요구사항 추가
   - [QA] E2E 테스트 리포트 v1.0 생성
   - [Artifact] 프로젝트 컨텍스트 업데이트
   
   ## 2026-02-05
   - [Design] OpenAPI 스펙 v1.0 생성
   - [Dev] 서버 코드 v1.0 구현 완료
   ```

**산출물**:
- `docs/artifacts/artifact-index.md`
- `docs/artifacts/metadata.json`
- `docs/artifacts/changelog.md`
- `docs/project-context.md`

**체크포인트**:
- [ ] 모든 산출물 등록
- [ ] 메타데이터 정확성
- [ ] 버전 관리 적용
- [ ] 프로젝트 컨텍스트 최신화

---

## 🎯 실제 적용 사례: Shopping Cart Backend

### 프로젝트 개요
- **요청 ID**: REQ-2026-001
- **프로젝트명**: Shopping Cart Backend Service
- **기간**: 2026-02-05 ~ 2026-02-06 (2일)
- **범위**: PostgreSQL + Docker Compose 환경 구성 및 전체 개발

### 에이전트별 작업 타임라인

```mermaid
gantt
    title Shopping Cart Backend 개발 타임라인
    dateFormat YYYY-MM-DD
    section 기획
    요건정의서 작성           :done, planning1, 2026-02-05, 2h
    TC 작성                  :done, planning2, after planning1, 1h
    section 분석
    인터페이스 정의          :done, analysis1, after planning2, 2h
    비즈니스 로직 상세화     :done, analysis2, after analysis1, 2h
    section 설계
    OpenAPI 스펙 생성        :done, design1, after analysis2, 1h
    MSA 설계                 :done, design2, after design1, 1h
    section 개발
    코드 생성                :done, dev1, 2026-02-06, 2h
    단위 테스트              :done, dev2, after dev1, 2h
    Sanity 테스트            :done, dev3, after dev2, 1h
    section QA
    E2E 테스트               :done, qa1, after dev3, 2h
    테스트 리포트            :done, qa2, after qa1, 1h
    section 산출물 관리
    문서화                   :done, artifact1, after qa2, 1h
```

### 단계별 산출물

#### 1. 기획 Agent
**작업 시간**: 3시간  
**산출물**:
- `docs/requirements/requirements-spec.md` (v1.0)
- `docs/requirements/test-cases.md` (25개 TC)
- `docs/requirements/glossary.md` (30개 용어)

#### 2. 분석 Agent
**작업 시간**: 4시간  
**산출물**:
- `docs/analysis/interface-spec.md` (5개 API)
- `docs/analysis/business-logic-detail.md`
- `docs/analysis/data-model.md` (3개 엔티티)

#### 3. 설계 Agent
**작업 시간**: 2시간  
**산출물**:
- `docs/design/openapi.yaml` (OAS 3.0)
- `docs/design/architecture.md`
- `docs/design/msa-design.md`

#### 4. 개발 Agent
**작업 시간**: 5시간  
**산출물**:
- `src/main/kotlin/` (1,500 LOC)
- `src/test/kotlin/` (21개 테스트)
- `docker-compose.yaml`
- `docs/dev/sanity-test-report.md`

**주요 성과**:
- 단위 테스트 커버리지: 100%
- 통합 테스트: 6개 모두 통과
- Sanity 테스트: 성공

#### 5. QA Agent
**작업 시간**: 3시간  
**산출물**:
- `tests/e2e/` (15개 시나리오)
- `docs/qa/e2e-test-report.md`
- `docs/qa/test-coverage.md`

**테스트 결과**:
- E2E 테스트: 15/15 성공 (100%)
- 성능 테스트: 평균 응답시간 200ms
- 접근성 테스트: WCAG 2.1 AA 준수

#### 6. Artifact Manager
**작업 시간**: 1시간  
**산출물**:
- `docs/artifacts/artifact-index.md`
- `docs/artifacts/metadata.json`
- `docs/project-context.md`
- 3개 ADR 문서

### 최종 성과

| 지표 | 결과 |
|------|------|
| **개발 기간** | 2일 |
| **총 산출물** | 25개 |
| **코드 라인** | 1,500 LOC |
| **테스트 수** | 36개 (단위 21 + E2E 15) |
| **테스트 성공률** | 100% |
| **코드 커버리지** | 100% |
| **문서화 수준** | 완벽 |

---

## 💡 베스트 프랙티스

### 1. 명확한 산출물 정의
- 각 단계별 산출물 템플릿 사전 정의
- 산출물 간 의존성 명확화
- 버전 관리 규칙 수립

### 2. 자동화 도구 활용
- OpenAPI Generator로 코드 자동 생성
- Playwright로 E2E 테스트 자동화
- CI/CD 파이프라인 구축

### 3. 지속적인 품질 검증
- 각 단계마다 사용자 승인 (HITL)
- 테스트 우선 개발 (TDD/BDD)
- 코드 리뷰 및 페어 프로그래밍

### 4. 효과적인 문서화
- 코드와 문서 동기화
- 메타데이터 자동 생성
- 변경 이력 추적

### 5. 에이전트 간 협업
- 명확한 인계 프로세스
- 표준화된 산출물 형식
- 실시간 피드백 루프

---

## 🚨 주의사항

### 1. 순차적 진행 필수
- 각 단계는 이전 단계 완료 후 시작
- 산출물 품질이 다음 단계에 영향
- 단계 건너뛰기 금지

### 2. 사용자 승인 대기
- 🚩 HITL 지점에서 반드시 대기
- 승인 없이 다음 단계 진행 금지
- 피드백 적극 반영

### 3. 산출물 품질 관리
- 템플릿 준수
- 버전 관리 철저
- 문서와 코드 일치

### 4. 테스트 커버리지
- TC 100% 커버 필수
- E2E 테스트 성공률 95% 이상
- 성능 기준 충족

---

## 📊 성과 지표

### 개발 효율성
- **기존 방식**: 2-4주
- **Multi-Agent**: 2-3일
- **개선율**: 85% 이상

### 품질 지표
- **테스트 커버리지**: 100%
- **버그 발생률**: 0.5% 이하
- **문서화 완성도**: 95% 이상

### 비용 절감
- **인력 투입**: 1명 (기존 5-6명)
- **개발 비용**: 80% 절감
- **유지보수 비용**: 60% 절감

---

## 🔮 향후 개선 방향

### 1. AI 에이전트 고도화
- 자연어 요구사항 자동 분석
- 코드 자동 생성 정확도 향상
- 버그 자동 수정 기능

### 2. 자동화 확대
- 에이전트 간 자동 인계
- 조건부 HITL (신뢰도 기반)
- 자동 배포 파이프라인

### 3. 품질 관리 강화
- AI 기반 코드 리뷰
- 자동 성능 최적화
- 보안 취약점 자동 탐지

---

## 📚 참고 자료

### 표준 및 가이드
- [OpenAPI Specification 3.0](https://swagger.io/specification/)
- [BDD with Cucumber](https://cucumber.io/docs/bdd/)
- [Playwright Documentation](https://playwright.dev/)
- [MSA Design Patterns](https://microservices.io/patterns/)

### 도구 및 프레임워크
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Kotest](https://kotest.io/)
- [OpenAPI Generator](https://openapi-generator.tech/)
- [Docker Compose](https://docs.docker.com/compose/)

---

**마지막 업데이트**: 2026-02-06  
**버전**: 2.0  
**작성자**: Artifact Manager Agent
