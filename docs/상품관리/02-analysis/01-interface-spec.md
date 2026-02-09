# 인터페이스 정의서: 상품관리 API

**프로젝트명**: 차세대 플랫폼 - 상품관리  
**작성일**: 2026-02-06  
**작성자**: 분석 Agent  
**버전**: v1.0  
**기반 문서**: 요건정의서 v1.0

---

## 1. API 목록

| API ID | 엔드포인트 | 메서드 | 설명 | 인증 필요 | 권한 | 우선순위 |
|--------|-----------|--------|------|----------|------|---------|
| **API-001** | `/api/products` | POST | 신규 상품 등록 | ✓ | SELLER, ADMIN | P0 |
| **API-002** | `/api/products/{id}` | PUT | 상품 정보 수정 | ✓ | SELLER, ADMIN | P0 |
| **API-003** | `/api/products/{id}` | DELETE | 상품 삭제 (논리 삭제) | ✓ | ADMIN | P1 |
| **API-004** | `/api/products` | GET | 상품 목록 조회 | ✗ | ALL | P0 |
| **API-005** | `/api/products/{id}` | GET | 상품 상세 조회 | ✗ | ALL | P0 |
| **API-006** | `/api/products/{id}/stock` | PATCH | 재고 수량 조정 | ✓ | SELLER, ADMIN | P0 |
| **API-007** | `/api/products/{id}/status` | PATCH | 상품 상태 변경 | ✓ | SELLER, ADMIN | P1 |
| **API-008** | `/api/products/{id}/category` | PATCH | 카테고리 변경 | ✓ | SELLER, ADMIN | P1 |
| **API-009** | `/api/products/{id}/images` | POST | 이미지 업로드 | ✓ | SELLER, ADMIN | P1 |
| **API-010** | `/api/products/{id}/images/{imageId}` | DELETE | 이미지 삭제 | ✓ | SELLER, ADMIN | P1 |
| **API-011** | `/api/products/{id}/images/order` | PUT | 이미지 순서 변경 | ✓ | SELLER, ADMIN | P2 |
| **API-012** | `/api/categories` | GET | 카테고리 목록 조회 | ✗ | ALL | P1 |

---

## 2. API 상세 스펙

### API-001: 신규 상품 등록

**엔드포인트**: `POST /api/products`  
**인증**: Required (JWT Bearer Token)  
**권한**: SELLER, ADMIN

#### 요청 (Request)

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Body**:
```json
{
  "productName": "삼성 노트북 갤럭시북",
  "categoryId": 123,
  "price": 1500000,
  "stock": 10,
  "description": "최신 모델 노트북입니다.",
  "options": {
    "color": "silver",
    "storage": "512GB"
  },
  "images": [
    "https://cdn.example.com/temp/image1.jpg",
    "https://cdn.example.com/temp/image2.jpg"
  ]
}
```

**필드 설명**:
| 필드 | 타입 | 필수 | 제약조건 | 설명 |
|------|------|------|---------|------|
| productName | string | ✓ | 2~100자, 특수문자 제한 | 상품명 |
| categoryId | integer | ✓ | 존재하는 카테고리 ID | 카테고리 ID |
| price | integer | ✓ | ≥ 0, 100원 단위 | 가격 (KRW) |
| stock | integer | ✓ | ≥ 0 | 재고 수량 |
| description | string | ✗ | ≤ 5000자 | 상품 설명 |
| options | object | ✗ | - | 상품 옵션 (key-value) |
| images | array[string] | ✗ | ≤ 10개, URL 형식 | 이미지 URL 목록 |

#### 응답 (Response)

**성공 (201 Created)**:
```json
{
  "productCode": "PRD-20260206-0001",
  "productName": "삼성 노트북 갤럭시북",
  "categoryId": 123,
  "price": 1500000,
  "stock": 10,
  "status": "DRAFT",
  "createdAt": "2026-02-06T11:36:00Z",
  "updatedAt": "2026-02-06T11:36:00Z",
  "images": [
    {
      "imageId": 1,
      "url": "https://cdn.example.com/products/PRD-20260206-0001/image1.jpg",
      "order": 1,
      "isThumbnail": true
    }
  ]
}
```

**실패 응답**:

| HTTP 상태 | 에러 코드 | 메시지 | 발생 조건 |
|----------|----------|--------|---------|
| 400 | E004 | 필수 정보가 누락되었습니다 | 필수 필드 누락 |
| 400 | INVALID_PRICE | 가격은 100원 단위여야 합니다 | 가격 단위 오류 |
| 404 | CATEGORY_NOT_FOUND | 존재하지 않는 카테고리입니다 | 잘못된 categoryId |
| 401 | UNAUTHORIZED | 인증이 필요합니다 | JWT 토큰 없음 |
| 403 | E003 | 권한이 없습니다 | 권한 부족 |

---

### API-002: 상품 정보 수정

**엔드포인트**: `PUT /api/products/{id}`  
**인증**: Required (JWT Bearer Token)  
**권한**: SELLER (본인 상품만), ADMIN

#### 요청 (Request)

**Path Parameters**:
- `id` (string): 상품 코드 (예: PRD-20260206-0001)

**Body**:
```json
{
  "productName": "삼성 노트북 갤럭시북 Pro",
  "price": 1600000,
  "stock": 15,
  "description": "업그레이드된 최신 모델입니다."
}
```

**필드 설명**:
- 모든 필드 선택적 (변경할 필드만 전송)
- 제약조건은 등록 API와 동일

#### 응답 (Response)

**성공 (200 OK)**:
```json
{
  "productCode": "PRD-20260206-0001",
  "productName": "삼성 노트북 갤럭시북 Pro",
  "price": 1600000,
  "stock": 15,
  "version": 2,
  "updatedAt": "2026-02-06T12:00:00Z",
  "changeHistory": {
    "historyId": 123,
    "changedFields": ["productName", "price", "stock"],
    "changedBy": "seller@example.com"
  }
}
```

**실패 응답**:

| HTTP 상태 | 에러 코드 | 메시지 | 발생 조건 |
|----------|----------|--------|---------|
| 404 | E002 | 존재하지 않는 상품입니다 | 잘못된 상품 코드 |
| 400 | E008 | 가격 변동 폭이 제한을 초과했습니다 | 판매중 상품 10% 이상 변경 |
| 403 | E003 | 권한이 없습니다 | 다른 판매자의 상품 수정 시도 |
| 409 | VERSION_CONFLICT | 다른 사용자가 수정 중입니다 | 낙관적 락 충돌 |

---

### API-004: 상품 목록 조회

**엔드포인트**: `GET /api/products`  
**인증**: Not Required  
**권한**: ALL

#### 요청 (Request)

**Query Parameters**:
```
GET /api/products?
  search=노트북&
  categoryId=123&
  minPrice=1000000&
  maxPrice=2000000&
  status=ON_SALE&
  sortBy=createdAt&
  sortOrder=desc&
  page=1&
  size=20
```

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| search | string | ✗ | - | 상품명 검색 (부분 일치) |
| categoryId | integer | ✗ | - | 카테고리 ID |
| minPrice | integer | ✗ | 0 | 최소 가격 |
| maxPrice | integer | ✗ | - | 최대 가격 |
| status | string | ✗ | - | 상품 상태 (DRAFT, PENDING, ON_SALE, SUSPENDED, ENDED) |
| sortBy | string | ✗ | createdAt | 정렬 기준 (createdAt, price, productName) |
| sortOrder | string | ✗ | desc | 정렬 순서 (asc, desc) |
| page | integer | ✗ | 1 | 페이지 번호 (1부터 시작) |
| size | integer | ✗ | 20 | 페이지 크기 (최대 100) |

#### 응답 (Response)

**성공 (200 OK)**:
```json
{
  "content": [
    {
      "productCode": "PRD-20260206-0001",
      "productName": "삼성 노트북 갤럭시북",
      "price": 1500000,
      "stock": 10,
      "status": "ON_SALE",
      "thumbnailUrl": "https://cdn.example.com/products/PRD-20260206-0001/thumb.jpg",
      "categoryName": "전자제품 > 노트북",
      "createdAt": "2026-02-06T11:36:00Z"
    }
  ],
  "page": {
    "number": 1,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

**필드 설명**:
- `content`: 상품 목록 배열
- `page.number`: 현재 페이지 번호
- `page.size`: 페이지 크기
- `page.totalElements`: 전체 상품 수
- `page.totalPages`: 전체 페이지 수

---

### API-005: 상품 상세 조회

**엔드포인트**: `GET /api/products/{id}`  
**인증**: Not Required  
**권한**: ALL (단, DELETED 상태는 ADMIN만)

#### 요청 (Request)

**Path Parameters**:
- `id` (string): 상품 코드

#### 응답 (Response)

**성공 (200 OK)**:
```json
{
  "productCode": "PRD-20260206-0001",
  "productName": "삼성 노트북 갤럭시북",
  "categoryId": 123,
  "categoryPath": "전자제품 > 노트북 > 게이밍",
  "price": 1500000,
  "stock": 10,
  "status": "ON_SALE",
  "description": "최신 모델 노트북입니다.",
  "options": {
    "color": "silver",
    "storage": "512GB"
  },
  "images": [
    {
      "imageId": 1,
      "url": "https://cdn.example.com/products/PRD-20260206-0001/image1.jpg",
      "thumbnailUrl": "https://cdn.example.com/products/PRD-20260206-0001/thumb1.jpg",
      "order": 1,
      "isThumbnail": true
    }
  ],
  "seller": {
    "sellerId": 123,
    "sellerName": "테크스토어"
  },
  "viewCount": 1523,
  "createdAt": "2026-02-06T11:36:00Z",
  "updatedAt": "2026-02-06T12:00:00Z"
}
```

---

### API-006: 재고 수량 조정

**엔드포인트**: `PATCH /api/products/{id}/stock`  
**인증**: Required (JWT Bearer Token)  
**권한**: SELLER (본인 상품만), ADMIN

#### 요청 (Request)

**Body**:
```json
{
  "adjustmentType": "IN",
  "quantity": 50,
  "reason": "신규 입고"
}
```

**필드 설명**:
| 필드 | 타입 | 필수 | 값 | 설명 |
|------|------|------|-----|------|
| adjustmentType | string | ✓ | IN, OUT, ADJUST | 조정 유형 (입고/출고/조정) |
| quantity | integer | ✓ | > 0 | 조정 수량 (절대값) |
| reason | string | ✓ | ≤ 200자 | 조정 사유 |

#### 응답 (Response)

**성공 (200 OK)**:
```json
{
  "productCode": "PRD-20260206-0001",
  "previousStock": 10,
  "adjustedStock": 60,
  "stockHistory": {
    "historyId": 456,
    "adjustmentType": "IN",
    "quantity": 50,
    "reason": "신규 입고",
    "adjustedBy": "seller@example.com",
    "adjustedAt": "2026-02-06T13:00:00Z"
  }
}
```

**실패 응답**:

| HTTP 상태 | 에러 코드 | 메시지 | 발생 조건 |
|----------|----------|--------|---------|
| 409 | E001 | 재고가 부족합니다 | OUT 시 재고 부족 |
| 400 | NEGATIVE_STOCK | 재고는 0 이하로 감소할 수 없습니다 | 결과 재고 < 0 |
| 403 | APPROVAL_REQUIRED | 대량 조정은 관리자 승인이 필요합니다 | 100개 이상 조정 |

---

### API-007: 상품 상태 변경

**엔드포인트**: `PATCH /api/products/{id}/status`  
**인증**: Required (JWT Bearer Token)  
**권한**: SELLER (본인 상품만), ADMIN

#### 요청 (Request)

**Body**:
```json
{
  "status": "ON_SALE"
}
```

**상태 전이 규칙**:
```
DRAFT → PENDING → ON_SALE ↔ SUSPENDED → ENDED
```

#### 응답 (Response)

**성공 (200 OK)**:
```json
{
  "productCode": "PRD-20260206-0001",
  "previousStatus": "PENDING",
  "currentStatus": "ON_SALE",
  "statusChangedAt": "2026-02-06T14:00:00Z"
}
```

**실패 응답**:

| HTTP 상태 | 에러 코드 | 메시지 | 발생 조건 |
|----------|----------|--------|---------|
| 400 | E005 | 잘못된 상태 전이입니다 | 상태 전이 규칙 위반 |
| 400 | MISSING_REQUIRED_INFO | 판매중 전환을 위한 필수 정보가 부족합니다 | 이미지 없음 등 |

---

## 3. 페이징, 정렬, 필터링 규칙

### 페이징
- **기본 페이지 크기**: 20
- **최대 페이지 크기**: 100
- **페이지 번호**: 1부터 시작
- **최대 조회 건수**: 1,000건

### 정렬
- **기본 정렬**: `createdAt DESC` (최신순)
- **지원 필드**: `createdAt`, `price`, `productName`, `viewCount`
- **정렬 순서**: `asc` (오름차순), `desc` (내림차순)

### 필터링
- **검색어**: 상품명 부분 일치 (대소문자 구분 없음)
- **가격 범위**: `minPrice` ~ `maxPrice` (포함)
- **카테고리**: 하위 카테고리 포함
- **상태**: 정확히 일치

---

## 4. 에러 코드 정의

| 에러 코드 | HTTP 상태 | 메시지 | 설명 |
|----------|----------|--------|------|
| E001 | 409 | 재고가 부족합니다 | 재고 부족 |
| E002 | 404 | 존재하지 않는 상품입니다 | 상품 없음 |
| E003 | 403 | 권한이 없습니다 | 권한 부족 |
| E004 | 400 | 필수 정보가 누락되었습니다 | 필수 필드 누락 |
| E005 | 400 | 잘못된 상태 전이입니다 | 상태 전이 규칙 위반 |
| E006 | 413 | 이미지 크기가 초과되었습니다 | 이미지 용량 초과 |
| E007 | 409 | 주문 이력이 있어 삭제할 수 없습니다 | 삭제 불가 |
| E008 | 400 | 가격 변동 폭이 제한을 초과했습니다 | 가격 변경 제한 |
| UNAUTHORIZED | 401 | 인증이 필요합니다 | JWT 토큰 없음 |
| INVALID_TOKEN | 401 | 유효하지 않은 토큰입니다 | JWT 토큰 만료/손상 |
| VERSION_CONFLICT | 409 | 다른 사용자가 수정 중입니다 | 낙관적 락 충돌 |
| RATE_LIMIT_EXCEEDED | 429 | 요청 한도를 초과했습니다 | Rate Limiting |

---

## 5. 인증 및 인가

### JWT 토큰 구조
```json
{
  "sub": "user@example.com",
  "userId": 123,
  "role": "SELLER",
  "exp": 1704556800
}
```

### 권한 레벨
- **ALL**: 모든 사용자 (비로그인 포함)
- **USER**: 로그인한 일반 사용자
- **SELLER**: 판매자
- **ADMIN**: 관리자

### 권한 검증 규칙
1. 상품 수정/삭제: 본인 상품 또는 ADMIN
2. 재고 조정: 본인 상품 또는 ADMIN
3. 상품 삭제: ADMIN만 가능
4. 조회: 모든 사용자 (단, DELETED 상태는 ADMIN만)

---

## 6. Rate Limiting

| 엔드포인트 | 제한 | 기준 |
|-----------|------|------|
| POST /api/products | 10회/분 | IP 주소 |
| PUT /api/products/{id} | 30회/분 | 사용자 |
| GET /api/products | 100회/분 | IP 주소 |
| GET /api/products/{id} | 200회/분 | IP 주소 |

---

**다음 단계**: 비즈니스 로직 상세 문서 작성
