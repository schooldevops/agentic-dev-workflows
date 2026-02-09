# 시퀀스 다이어그램: 상품관리

**프로젝트명**: 차세대 플랫폼 - 상품관리  
**작성일**: 2026-02-06  
**작성자**: 분석 Agent  
**버전**: v1.0  
**기반 문서**: 요건정의서 v1.0, 비즈니스 로직 상세 v1.0

---

## 1. 상품 등록 시퀀스

```mermaid
sequenceDiagram
    actor User as 판매자
    participant Client as Frontend
    participant Gateway as API Gateway
    participant Auth as Auth Service
    participant Product as Product Service
    participant Image as Image Service
    participant S3 as AWS S3
    participant DB as PostgreSQL
    participant Cache as Redis

    User->>Client: 상품 정보 입력
    Client->>Gateway: POST /api/products
    Gateway->>Auth: JWT 토큰 검증
    Auth-->>Gateway: 인증 성공 (sellerId: 123)
    
    Gateway->>Product: createProduct(request)
    
    Product->>Product: 필수 필드 검증
    Product->>Product: 가격 100원 단위 절사
    Product->>DB: 카테고리 존재 확인
    DB-->>Product: 카테고리 정보
    
    Product->>Product: 상품코드 생성<br/>(PRD-20260206-0001)
    
    Product->>DB: BEGIN TRANSACTION
    Product->>DB: INSERT INTO product
    DB-->>Product: 저장 성공
    
    par 이미지 처리 (비동기)
        Product->>Image: processImages(urls)
        Image->>Image: 이미지 다운로드
        Image->>Image: 리사이징<br/>(thumb, medium, original)
        Image->>S3: 이미지 업로드
        S3-->>Image: CDN URL 반환
        Image->>DB: INSERT INTO product_image
        Image-->>Product: 이미지 처리 완료
    end
    
    Product->>DB: INSERT INTO stock_history<br/>(초기 재고 기록)
    Product->>DB: COMMIT
    
    Product->>Cache: 상품 정보 캐싱 (5분)
    
    Product-->>Gateway: 201 Created<br/>{productCode, status, images}
    Gateway-->>Client: 응답 반환
    Client-->>User: 등록 성공 메시지 표시
```

---

## 2. 상품 수정 시퀀스 (낙관적 락)

```mermaid
sequenceDiagram
    actor User as 판매자
    participant Client as Frontend
    participant Gateway as API Gateway
    participant Product as Product Service
    participant DB as PostgreSQL
    participant Cache as Redis

    User->>Client: 상품 정보 수정
    Client->>Gateway: PUT /api/products/PRD-20260206-0001
    Gateway->>Product: updateProduct(productCode, request)
    
    Product->>DB: SELECT * FROM product<br/>WHERE product_code = ?<br/>FOR UPDATE
    DB-->>Product: 상품 정보 (version: 1)
    
    alt 버전 불일치
        Product->>Product: 요청 버전 != DB 버전
        Product-->>Gateway: 409 VERSION_CONFLICT
        Gateway-->>Client: 최신 버전 정보 반환
        Client-->>User: 다른 사용자가 수정 중입니다
    else 버전 일치
        Product->>Product: 권한 확인<br/>(본인 상품 또는 ADMIN)
        
        alt 가격 변경 시
            Product->>Product: 상태가 ON_SALE?
            Product->>Product: 변동률 계산
            alt 변동률 > 10%
                Product-->>Gateway: 400 E008<br/>가격 변동 폭 초과
            end
        end
        
        Product->>DB: BEGIN TRANSACTION
        Product->>DB: UPDATE product<br/>SET ..., version = version + 1
        Product->>DB: INSERT INTO product_history<br/>(변경 이력)
        Product->>DB: COMMIT
        
        Product->>Cache: 캐시 무효화
        
        Product-->>Gateway: 200 OK<br/>{product, changeHistory}
        Gateway-->>Client: 응답 반환
        Client-->>User: 수정 성공
    end
```

---

## 3. 재고 조정 시퀀스 (동시성 제어)

```mermaid
sequenceDiagram
    actor User1 as 판매자 A
    actor User2 as 판매자 B
    participant Product as Product Service
    participant DB as PostgreSQL

    par 동시 재고 출고 요청
        User1->>Product: adjustStock(OUT, 5개)
        User2->>Product: adjustStock(OUT, 5개)
    end
    
    Product->>DB: BEGIN TRANSACTION<br/>(SERIALIZABLE)
    Product->>DB: SELECT * FROM product<br/>WHERE product_code = ?<br/>FOR UPDATE
    
    Note over DB: 비관적 락 획득<br/>(User1이 먼저 획득)
    
    DB-->>Product: 상품 정보 (재고: 10개)
    
    Product->>Product: 재고 충분성 검증<br/>(10 >= 5)
    Product->>Product: 새 재고 계산<br/>(10 - 5 = 5)
    
    Product->>DB: UPDATE product<br/>SET stock = 5
    Product->>DB: INSERT INTO stock_history
    Product->>DB: COMMIT
    
    Note over DB: 락 해제
    
    Product->>DB: BEGIN TRANSACTION<br/>(User2 차례)
    Product->>DB: SELECT * FROM product<br/>FOR UPDATE
    DB-->>Product: 상품 정보 (재고: 5개)
    
    Product->>Product: 재고 충분성 검증<br/>(5 >= 5)
    alt 재고 부족
        Product->>DB: ROLLBACK
        Product-->>User2: 409 E001<br/>재고가 부족합니다
    else 재고 충분
        Product->>DB: UPDATE product<br/>SET stock = 0
        Product->>DB: COMMIT
        Product-->>User2: 200 OK
    end
    
    Product-->>User1: 200 OK<br/>(재고: 5개)
```

---

## 4. 상품 목록 조회 시퀀스 (캐싱)

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Client as Frontend
    participant Gateway as API Gateway
    participant Product as Product Service
    participant Cache as Redis
    participant DB as PostgreSQL

    User->>Client: 상품 목록 요청
    Client->>Gateway: GET /api/products?<br/>search=노트북&page=1
    Gateway->>Product: getProducts(criteria)
    
    Product->>Product: 캐시 키 생성<br/>(products:list:노트북:1:20)
    
    Product->>Cache: GET cache_key
    
    alt 캐시 히트
        Cache-->>Product: 캐시된 데이터
        Product-->>Gateway: 200 OK<br/>{content, page}
    else 캐시 미스
        Cache-->>Product: NULL
        
        Product->>DB: SELECT COUNT(*)<br/>FROM product<br/>WHERE ...
        DB-->>Product: totalElements: 150
        
        Product->>DB: SELECT *<br/>FROM product<br/>WHERE ...<br/>ORDER BY created_at DESC<br/>LIMIT 20 OFFSET 0
        DB-->>Product: 상품 목록 (20건)
        
        Product->>Product: 응답 구성<br/>{content, page}
        
        Product->>Cache: SET cache_key<br/>TTL 5분
        
        Product-->>Gateway: 200 OK
    end
    
    Gateway-->>Client: 응답 반환
    Client-->>User: 상품 목록 표시
```

---

## 5. 상품 상세 조회 시퀀스 (조회수 증가)

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Client as Frontend
    participant Gateway as API Gateway
    participant Product as Product Service
    participant Cache as Redis
    participant DB as PostgreSQL
    participant Queue as Message Queue

    User->>Client: 상품 상세 요청
    Client->>Gateway: GET /api/products/PRD-20260206-0001
    Gateway->>Product: getProduct(productCode)
    
    Product->>Cache: GET product:PRD-20260206-0001
    
    alt 캐시 히트
        Cache-->>Product: 상품 정보
    else 캐시 미스
        Product->>DB: SELECT * FROM product<br/>LEFT JOIN product_image
        DB-->>Product: 상품 정보
        Product->>Cache: SET product:...<br/>TTL 10분
    end
    
    par 조회수 증가 (비동기)
        Product->>Product: 사용자 식별<br/>(IP 또는 userId)
        Product->>Cache: EXISTS view:PRD-...:user123
        
        alt 1시간 내 중복 조회 아님
            Cache-->>Product: FALSE
            Product->>Cache: SET view:PRD-...:user123<br/>TTL 1시간
            Product->>Cache: INCR viewCount:PRD-20260206-0001
            Product->>Queue: 조회수 증가 이벤트 발행
        else 중복 조회
            Cache-->>Product: TRUE
            Note over Product: 조회수 증가 안 함
        end
    end
    
    Product-->>Gateway: 200 OK<br/>{product, images, seller}
    Gateway-->>Client: 응답 반환
    Client-->>User: 상품 상세 표시
    
    Note over Queue,DB: 배치 프로세스 (10분마다)
    Queue->>DB: UPDATE product<br/>SET view_count = view_count + N
```

---

## 6. 상품 상태 변경 시퀀스 (상태 전이 검증)

```mermaid
sequenceDiagram
    actor User as 판매자
    participant Client as Frontend
    participant Gateway as API Gateway
    participant Product as Product Service
    participant DB as PostgreSQL

    User->>Client: 상태 변경 (DRAFT → ON_SALE)
    Client->>Gateway: PATCH /api/products/PRD-../status
    Gateway->>Product: changeStatus(productCode, ON_SALE)
    
    Product->>DB: SELECT * FROM product
    DB-->>Product: 상품 정보 (status: DRAFT)
    
    Product->>Product: 상태 전이 규칙 검증
    
    alt 잘못된 전이
        Product->>Product: DRAFT → ON_SALE 불가<br/>(PENDING 거쳐야 함)
        Product-->>Gateway: 400 E005<br/>잘못된 상태 전이입니다
        Gateway-->>Client: 에러 반환
        Client-->>User: 에러 메시지 표시
    else 올바른 전이
        alt ON_SALE로 전환 시
            Product->>Product: 필수 정보 검증
            Product->>Product: - 상품명 존재?
            Product->>Product: - 가격 > 0?
            Product->>Product: - 재고 > 0?
            Product->>Product: - 이미지 >= 1?
            
            alt 필수 정보 부족
                Product-->>Gateway: 400 MISSING_REQUIRED_INFO
            end
        end
        
        Product->>DB: BEGIN TRANSACTION
        Product->>DB: UPDATE product<br/>SET status = 'ON_SALE'
        Product->>DB: INSERT INTO status_history
        Product->>DB: COMMIT
        
        Product-->>Gateway: 200 OK<br/>{previousStatus, currentStatus}
        Gateway-->>Client: 응답 반환
        Client-->>User: 상태 변경 성공
    end
```

---

## 7. 이미지 업로드 시퀀스 (청크 업로드)

```mermaid
sequenceDiagram
    actor User as 판매자
    participant Client as Frontend
    participant Gateway as API Gateway
    participant Product as Product Service
    participant Image as Image Service
    participant S3 as AWS S3
    participant DB as PostgreSQL

    User->>Client: 이미지 파일 선택 (5MB)
    Client->>Client: 파일 크기 검증
    Client->>Client: 파일을 청크로 분할<br/>(1MB씩)
    
    loop 각 청크마다
        Client->>Gateway: POST /api/products/PRD-../images/upload<br/>(chunk 1/5)
        Gateway->>Product: uploadImageChunk(chunk, metadata)
        Product->>Image: 청크 저장 (임시)
        Image-->>Product: 청크 저장 완료
        Product-->>Gateway: 200 OK
        Gateway-->>Client: 진행률 반환 (20%)
        Client-->>User: 업로드 진행률 표시
    end
    
    Client->>Gateway: POST /api/products/PRD-../images/complete
    Gateway->>Product: completeImageUpload()
    
    Product->>Image: 청크 병합
    Image->>Image: 이미지 검증<br/>(형식, 크기)
    Image->>Image: 리사이징
    Image->>S3: 업로드
    S3-->>Image: CDN URL
    
    Image->>DB: INSERT INTO product_image
    Image-->>Product: 이미지 정보
    
    Product-->>Gateway: 201 Created<br/>{imageId, urls}
    Gateway-->>Client: 응답 반환
    Client-->>User: 이미지 업로드 완료
```

---

**다음 단계**: 설계 Agent에게 인계하여 OpenAPI 스펙 작성
