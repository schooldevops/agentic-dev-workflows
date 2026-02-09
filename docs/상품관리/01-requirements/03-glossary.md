# 용어 정의서: 상품관리

**프로젝트명**: 차세대 플랫폼 - 상품관리  
**작성일**: 2026-02-06  
**작성자**: 기획 Agent  
**버전**: v1.0

---

## 도메인 용어

| 한글 용어 | 영문 용어 | 약어 | 정의 | 사용 예시 |
|---|---|---|---|---|
| 상품 | Product | PRD | 판매 가능한 물품 또는 서비스 | "노트북 상품 등록" |
| 상품코드 | Product Code | - | 상품을 고유하게 식별하는 코드 (형식: PRD-YYYYMMDD-NNNN) | "PRD-20260206-0001" |
| 상품명 | Product Name | - | 상품의 이름 또는 제목 | "삼성 노트북 갤럭시북" |
| 카테고리 | Category | CAT | 상품을 분류하는 계층적 구조 (대/중/소) | "전자제품 > 노트북 > 게이밍" |
| 가격 | Price | - | 상품의 판매 금액 (KRW) | "1,500,000원" |
| 재고 | Stock / Inventory | STK | 판매 가능한 상품의 수량 | "재고 10개" |
| 재고 수량 | Stock Quantity | - | 현재 보유하고 있는 상품의 개수 | "현재 재고: 5개" |
| 입고 | Stock In | - | 재고를 증가시키는 작업 | "100개 입고" |
| 출고 | Stock Out | - | 재고를 감소시키는 작업 | "10개 출고" |
| 재고 조정 | Stock Adjustment | - | 재고 수량을 임의로 변경하는 작업 | "재고 조정: +50개" |

---

## 상태 관련 용어

| 한글 용어 | 영문 용어 | 코드 | 정의 | 전이 가능 상태 |
|---|---|---|---|---|
| 임시저장 | Draft | DRAFT | 상품 정보가 완전하지 않은 초기 상태 | → 판매대기 |
| 판매대기 | Pending | PENDING | 판매 준비가 완료되었으나 아직 판매 시작 전 | → 판매중 |
| 판매중 | On Sale | ON_SALE | 현재 판매가 진행 중인 상태 | ↔ 판매중지, → 판매종료 |
| 판매중지 | Suspended | SUSPENDED | 일시적으로 판매를 중단한 상태 | ↔ 판매중, → 판매종료 |
| 판매종료 | Ended | ENDED | 판매가 완전히 종료된 상태 | - |
| 삭제됨 | Deleted | DELETED | 논리적으로 삭제된 상태 (복구 가능) | - |

---

## 기술 용어

| 한글 용어 | 영문 용어 | 약어 | 정의 | 비고 |
|---|---|---|---|---|
| 논리 삭제 | Soft Delete | - | 데이터를 물리적으로 삭제하지 않고 상태만 변경 | 복구 가능 |
| 물리 삭제 | Hard Delete | - | 데이터베이스에서 실제로 레코드를 삭제 | 복구 불가 |
| 상태 전이 | State Transition | - | 상품의 상태가 한 단계에서 다른 단계로 변경 | 규칙 준수 필요 |
| 낙관적 락 | Optimistic Lock | - | 충돌이 드물다고 가정하고 버전으로 동시성 제어 | JPA @Version |
| 비관적 락 | Pessimistic Lock | - | 충돌이 빈번하다고 가정하고 레코드를 잠금 | SELECT FOR UPDATE |
| 트랜잭션 | Transaction | TXN | 하나의 논리적 작업 단위 | ACID 보장 |
| 동시성 제어 | Concurrency Control | - | 여러 사용자의 동시 접근을 제어하는 메커니즘 | 데이터 정합성 보장 |
| 이력 관리 | Audit Trail | - | 변경 사항을 시간순으로 기록 | 추적 가능성 |

---

## 이미지 관련 용어

| 한글 용어 | 영문 용어 | 약어 | 정의 | 사양 |
|---|---|---|---|---|
| 대표 이미지 | Thumbnail Image | - | 상품 목록에서 표시되는 주요 이미지 | 200x200px |
| 중간 이미지 | Medium Image | - | 상품 상세에서 표시되는 중간 크기 이미지 | 800x800px |
| 원본 이미지 | Original Image | - | 업로드된 원본 이미지 | 최대 5MB |
| 이미지 리사이징 | Image Resizing | - | 이미지 크기를 변경하는 작업 | 자동 처리 |
| CDN | Content Delivery Network | - | 정적 콘텐츠를 빠르게 제공하는 네트워크 | AWS CloudFront 등 |

---

## 권한 관련 용어

| 한글 용어 | 영문 용어 | 코드 | 정의 | 권한 범위 |
|---|---|---|---|---|
| 관리자 | Administrator | ADMIN | 시스템 전체를 관리하는 최고 권한 | 모든 기능 |
| 판매자 | Seller | SELLER | 상품을 등록하고 판매하는 사용자 | 자신의 상품만 |
| 일반 사용자 | User | USER | 상품을 조회하고 구매하는 사용자 | 조회만 |
| RBAC | Role-Based Access Control | - | 역할 기반 접근 제어 | 권한 관리 방식 |

---

## 비즈니스 규칙 용어

| 한글 용어 | 영문 용어 | 정의 | 적용 규칙 |
|---|---|---|---|
| 가격 절사 | Price Rounding | 가격을 특정 단위로 내림 | 100원 단위 절사 |
| 가격 변동 제한 | Price Change Limit | 판매중 상품의 급격한 가격 변동 방지 | 10% 이내 변경 가능 |
| 필수 정보 | Required Information | 상품 판매를 위해 반드시 필요한 정보 | 상품명, 가격, 재고, 이미지 |
| 대량 작업 | Bulk Operation | 여러 건의 데이터를 한 번에 처리 | 100건 이상 시 승인 필요 |

---

## 약어 및 축약어

| 약어 | 전체 명칭 | 한글 |
|---|---|---|
| PRD | Product | 상품 |
| CAT | Category | 카테고리 |
| STK | Stock | 재고 |
| TXN | Transaction | 트랜잭션 |
| CDN | Content Delivery Network | 콘텐츠 전송 네트워크 |
| RBAC | Role-Based Access Control | 역할 기반 접근 제어 |
| JWT | JSON Web Token | JSON 웹 토큰 |
| API | Application Programming Interface | 애플리케이션 프로그래밍 인터페이스 |
| CRUD | Create, Read, Update, Delete | 생성, 조회, 수정, 삭제 |
| ORM | Object-Relational Mapping | 객체-관계 매핑 |
| JPA | Java Persistence API | 자바 영속성 API |

---

## 데이터 타입 정의

| 필드명 | 데이터 타입 | 길이/범위 | 필수 여부 | 기본값 |
|---|---|---|---|---|
| productCode | VARCHAR | 20 | Y | 자동 생성 |
| productName | VARCHAR | 100 | Y | - |
| categoryId | BIGINT | - | Y | - |
| price | DECIMAL | (10, 0) | Y | - |
| stock | INTEGER | 0 이상 | Y | 0 |
| description | TEXT | 5000 | N | - |
| status | VARCHAR | 20 | Y | 'DRAFT' |
| createdAt | TIMESTAMP | - | Y | 현재 시각 |
| updatedAt | TIMESTAMP | - | Y | 현재 시각 |
| deletedAt | TIMESTAMP | - | N | NULL |

---

## 에러 코드 정의

| 에러 코드 | 한글 메시지 | 영문 메시지 | HTTP 상태 |
|---|---|---|---|
| E001 | 재고가 부족합니다 | Insufficient stock | 409 |
| E002 | 존재하지 않는 상품입니다 | Product not found | 404 |
| E003 | 권한이 없습니다 | Forbidden | 403 |
| E004 | 필수 정보가 누락되었습니다 | Required field missing | 400 |
| E005 | 잘못된 상태 전이입니다 | Invalid state transition | 400 |
| E006 | 이미지 크기가 초과되었습니다 | Image size exceeded | 413 |
| E007 | 주문 이력이 있어 삭제할 수 없습니다 | Cannot delete product with orders | 409 |
| E008 | 가격 변동 폭이 제한을 초과했습니다 | Price change limit exceeded | 400 |

---

**참고사항**:
- 모든 용어는 프로젝트 전체에서 일관되게 사용
- 새로운 용어 추가 시 이 문서 업데이트 필수
- 영문 용어는 코드 및 API에서 사용
- 한글 용어는 문서 및 UI에서 사용

**다음 단계**: 분석 Agent에게 인계하여 인터페이스 정의서 작성
