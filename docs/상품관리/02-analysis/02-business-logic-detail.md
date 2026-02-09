# 비즈니스 로직 상세: 상품관리

**프로젝트명**: 차세대 플랫폼 - 상품관리  
**작성일**: 2026-02-06  
**작성자**: 분석 Agent  
**버전**: v1.0  
**기반 문서**: 요건정의서 v1.0, 인터페이스 정의서 v1.0

---

## 1. 상품 등록 (FR-001)

### 처리 흐름

```
1. 요청 검증
   ├─ JWT 토큰 검증 (유효성, 만료 여부)
   ├─ 권한 확인 (SELLER 또는 ADMIN)
   └─ 필수 필드 검증 (productName, categoryId, price, stock)

2. 비즈니스 규칙 검증
   ├─ 상품명 길이 검증 (2~100자)
   ├─ 가격 100원 단위 절사 처리
   ├─ 재고 음수 검증 (>= 0)
   ├─ 카테고리 존재 여부 확인
   └─ 이미지 개수 검증 (<= 10개)

3. 상품코드 생성
   ├─ 형식: PRD-YYYYMMDD-NNNN
   ├─ YYYYMMDD: 현재 날짜
   ├─ NNNN: 일별 순번 (0001부터 시작)
   └─ 중복 확인 (재시도 로직)

4. 이미지 처리 (비동기)
   ├─ 임시 URL에서 이미지 다운로드
   ├─ 이미지 리사이징
   │   ├─ 썸네일: 200x200px
   │   ├─ 중간: 800x800px
   │   └─ 원본: 그대로 유지
   ├─ S3 업로드 (경로: /products/{productCode}/)
   └─ CDN URL 생성

5. 데이터 저장 (트랜잭션)
   ├─ Product 엔티티 저장
   │   ├─ 초기 상태: DRAFT
   │   ├─ 버전: 1
   │   └─ 생성일시: 현재 시각
   ├─ ProductImage 엔티티 저장 (N개)
   └─ StockHistory 엔티티 저장 (초기 재고 기록)

6. 응답 반환
   └─ 생성된 상품 정보 + 이미지 URL 목록
```

### 비즈니스 규칙

#### BR-001: 가격 100원 단위 절사
```kotlin
fun roundPrice(price: BigDecimal): BigDecimal {
    return price.divide(BigDecimal(100), 0, RoundingMode.DOWN)
        .multiply(BigDecimal(100))
}

// 예시
// 입력: 1,500,550원 → 출력: 1,500,500원
```

#### BR-002: 상품코드 생성
```kotlin
fun generateProductCode(date: LocalDate): String {
    val dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    val sequence = getNextSequence(date) // 일별 순번 조회 및 증가
    return "PRD-$dateStr-${sequence.toString().padStart(4, '0')}"
}

// 예시
// 2026-02-06, 순번 1 → PRD-20260206-0001
```

#### BR-003: 첫 번째 이미지를 대표 이미지로 설정
```kotlin
fun setThumbnailImage(images: List<ProductImage>) {
    images.firstOrNull()?.let { it.isThumbnail = true }
}
```

### 예외 처리

| 예외 상황 | 처리 방법 | Retry | Fallback |
|----------|---------|-------|----------|
| 카테고리 없음 | CATEGORY_NOT_FOUND 에러 반환 | ✗ | - |
| 이미지 다운로드 실패 | 해당 이미지 제외하고 계속 진행 | ✓ (3회) | 기본 이미지 |
| S3 업로드 실패 | 전체 트랜잭션 롤백 | ✓ (3회) | - |
| 상품코드 중복 | 순번 재생성 후 재시도 | ✓ (5회) | - |

### 트랜잭션 범위
- **시작**: 비즈니스 규칙 검증 완료 후
- **종료**: 모든 엔티티 저장 완료
- **격리 수준**: READ_COMMITTED
- **타임아웃**: 10초

---

## 2. 상품 수정 (FR-002)

### 처리 흐름

```
1. 요청 검증
   ├─ JWT 토큰 검증
   ├─ 상품 존재 여부 확인
   └─ 권한 확인 (본인 상품 또는 ADMIN)

2. 낙관적 락 검증
   ├─ 현재 버전과 요청 버전 비교
   └─ 불일치 시 VERSION_CONFLICT 에러

3. 비즈니스 규칙 검증
   ├─ 가격 변경 시
   │   ├─ 상태가 ON_SALE인 경우
   │   ├─ 변동률 계산: |새 가격 - 기존 가격| / 기존 가격
   │   └─ 10% 초과 시 에러
   ├─ 재고 감소 시
   │   └─ 현재 재고 이하로만 감소 가능
   └─ 기타 필드는 등록 시와 동일한 검증

4. 변경 이력 기록
   ├─ 변경된 필드 식별
   ├─ 변경 전/후 값 저장
   └─ ProductHistory 엔티티 생성

5. 데이터 수정 (트랜잭션)
   ├─ Product 엔티티 업데이트
   ├─ 버전 증가 (version++)
   ├─ 수정일시 갱신
   └─ ProductHistory 저장

6. 응답 반환
   └─ 수정된 상품 정보 + 변경 이력 ID
```

### 비즈니스 규칙

#### BR-004: 판매중 상품 가격 변동 제한
```kotlin
fun validatePriceChange(
    product: Product,
    newPrice: BigDecimal
): ValidationResult {
    if (product.status != ProductStatus.ON_SALE) {
        return ValidationResult.success()
    }
    
    val changeRate = (newPrice - product.price)
        .abs()
        .divide(product.price, 4, RoundingMode.HALF_UP)
    
    return if (changeRate > BigDecimal("0.1")) {
        ValidationResult.error("E008", "가격 변동 폭이 제한을 초과했습니다")
    } else {
        ValidationResult.success()
    }
}

// 예시
// 기존: 100,000원, 새: 50,000원 → 변동률 50% → 에러
// 기존: 100,000원, 새: 95,000원 → 변동률 5% → 성공
```

#### BR-005: 변경 이력 기록
```kotlin
fun recordChangeHistory(
    product: Product,
    changes: Map<String, Pair<Any?, Any?>>
): ProductHistory {
    return ProductHistory(
        productCode = product.productCode,
        changedFields = changes.keys.toList(),
        beforeValues = changes.mapValues { it.value.first },
        afterValues = changes.mapValues { it.value.second },
        changedBy = getCurrentUser(),
        changedAt = Instant.now()
    )
}
```

### 예외 처리

| 예외 상황 | 처리 방법 | Retry | Fallback |
|----------|---------|-------|----------|
| 상품 없음 | E002 에러 반환 | ✗ | - |
| 권한 없음 | E003 에러 반환 | ✗ | - |
| 버전 충돌 | VERSION_CONFLICT 에러, 최신 버전 반환 | ✗ | 클라이언트 재시도 |
| 가격 변동 초과 | E008 에러 반환 | ✗ | - |

---

## 3. 재고 조정 (FR-006)

### 처리 흐름

```
1. 요청 검증
   ├─ JWT 토큰 검증
   ├─ 상품 존재 여부 확인
   └─ 권한 확인

2. 비즈니스 규칙 검증
   ├─ 조정 유형 검증 (IN, OUT, ADJUST)
   ├─ 수량 양수 검증 (> 0)
   ├─ OUT 시 재고 충분성 검증
   └─ 대량 조정 시 승인 여부 확인 (>= 100개)

3. 재고 계산
   ├─ IN: currentStock + quantity
   ├─ OUT: currentStock - quantity
   └─ ADJUST: quantity (절대값)

4. 동시성 제어
   ├─ 비관적 락 획득 (SELECT FOR UPDATE)
   ├─ 재고 업데이트
   └─ 락 해제

5. 재고 이력 기록
   ├─ StockHistory 엔티티 생성
   ├─ 조정 전/후 재고 기록
   └─ 조정 사유 저장

6. 응답 반환
   └─ 조정 후 재고 + 이력 ID
```

### 비즈니스 규칙

#### BR-006: 재고 동시성 제어
```kotlin
@Transactional(isolation = Isolation.SERIALIZABLE)
fun adjustStock(
    productCode: String,
    adjustmentType: AdjustmentType,
    quantity: Int
): StockAdjustmentResult {
    // 비관적 락으로 상품 조회
    val product = productRepository
        .findByProductCodeWithLock(productCode)
        ?: throw ProductNotFoundException()
    
    // 재고 계산
    val newStock = when (adjustmentType) {
        AdjustmentType.IN -> product.stock + quantity
        AdjustmentType.OUT -> {
            if (product.stock < quantity) {
                throw InsufficientStockException()
            }
            product.stock - quantity
        }
        AdjustmentType.ADJUST -> quantity
    }
    
    // 음수 검증
    if (newStock < 0) {
        throw NegativeStockException()
    }
    
    // 재고 업데이트
    product.stock = newStock
    
    // 이력 기록
    val history = StockHistory(
        productCode = productCode,
        adjustmentType = adjustmentType,
        quantity = quantity,
        previousStock = product.stock,
        adjustedStock = newStock,
        reason = reason,
        adjustedBy = getCurrentUser()
    )
    stockHistoryRepository.save(history)
    
    return StockAdjustmentResult(newStock, history.id)
}
```

#### BR-007: 대량 조정 승인 프로세스
```kotlin
fun requiresApproval(quantity: Int): Boolean {
    return quantity >= 100
}

fun createApprovalRequest(
    productCode: String,
    quantity: Int
): ApprovalRequest {
    return ApprovalRequest(
        requestType = "STOCK_ADJUSTMENT",
        productCode = productCode,
        quantity = quantity,
        status = ApprovalStatus.PENDING,
        requestedBy = getCurrentUser(),
        requestedAt = Instant.now()
    )
}
```

### 예외 처리

| 예외 상황 | 처리 방법 | Retry | Fallback |
|----------|---------|-------|----------|
| 재고 부족 | E001 에러 반환 | ✗ | - |
| 음수 재고 | NEGATIVE_STOCK 에러 반환 | ✗ | - |
| 락 획득 실패 | 타임아웃 후 재시도 | ✓ (3회) | - |
| 대량 조정 | APPROVAL_REQUIRED 에러 반환 | ✗ | 승인 요청 생성 |

### 트랜잭션 범위
- **시작**: 비관적 락 획득
- **종료**: 재고 업데이트 및 이력 저장 완료
- **격리 수준**: SERIALIZABLE
- **타임아웃**: 5초

---

## 4. 상품 상태 변경 (FR-007)

### 처리 흐름

```
1. 요청 검증
   ├─ JWT 토큰 검증
   ├─ 상품 존재 여부 확인
   └─ 권한 확인

2. 상태 전이 규칙 검증
   ├─ 현재 상태 확인
   ├─ 목표 상태 확인
   └─ 전이 가능 여부 검증

3. ON_SALE 전환 시 필수 정보 검증
   ├─ 상품명 존재
   ├─ 가격 > 0
   ├─ 재고 > 0
   └─ 이미지 >= 1개

4. 상태 변경 (트랜잭션)
   ├─ Product.status 업데이트
   ├─ StatusHistory 엔티티 생성
   └─ 상태 변경 일시 기록

5. 응답 반환
   └─ 이전 상태 + 현재 상태 + 변경 일시
```

### 비즈니스 규칙

#### BR-008: 상태 전이 규칙
```kotlin
enum class ProductStatus {
    DRAFT,      // 임시저장
    PENDING,    // 판매대기
    ON_SALE,    // 판매중
    SUSPENDED,  // 판매중지
    ENDED,      // 판매종료
    DELETED     // 삭제됨
}

fun isValidTransition(
    from: ProductStatus,
    to: ProductStatus
): Boolean {
    return when (from) {
        DRAFT -> to == PENDING
        PENDING -> to == ON_SALE
        ON_SALE -> to == SUSPENDED || to == ENDED
        SUSPENDED -> to == ON_SALE || to == ENDED
        ENDED -> false
        DELETED -> false
    }
}
```

#### BR-009: ON_SALE 전환 시 필수 정보 검증
```kotlin
fun validateOnSaleRequirements(product: Product): ValidationResult {
    val errors = mutableListOf<String>()
    
    if (product.productName.isBlank()) {
        errors.add("상품명이 필요합니다")
    }
    if (product.price <= BigDecimal.ZERO) {
        errors.add("가격은 0보다 커야 합니다")
    }
    if (product.stock <= 0) {
        errors.add("재고는 0보다 커야 합니다")
    }
    if (product.images.isEmpty()) {
        errors.add("이미지가 최소 1장 필요합니다")
    }
    
    return if (errors.isEmpty()) {
        ValidationResult.success()
    } else {
        ValidationResult.error("MISSING_REQUIRED_INFO", errors.joinToString(", "))
    }
}
```

---

## 5. 상품 목록 조회 (FR-004)

### 처리 흐름

```
1. 요청 검증
   ├─ 페이지 번호 검증 (>= 1)
   ├─ 페이지 크기 검증 (1~100)
   └─ 정렬 필드 검증

2. 쿼리 조건 구성
   ├─ 검색어 (LIKE %keyword%)
   ├─ 카테고리 (하위 카테고리 포함)
   ├─ 가격 범위 (minPrice ~ maxPrice)
   ├─ 상태 (정확히 일치)
   └─ 삭제됨 제외 (ADMIN 제외)

3. 데이터베이스 조회
   ├─ COUNT 쿼리 (총 건수)
   ├─ SELECT 쿼리 (페이징, 정렬)
   └─ 최대 1,000건 제한

4. 응답 구성
   ├─ content: 상품 목록
   └─ page: 페이징 정보

5. 캐싱 (선택적)
   ├─ 조건별 캐시 키 생성
   ├─ Redis 조회
   └─ TTL: 5분
```

### 비즈니스 규칙

#### BR-010: 동적 쿼리 구성
```kotlin
fun buildQuery(criteria: ProductSearchCriteria): Specification<Product> {
    return Specification { root, query, cb ->
        val predicates = mutableListOf<Predicate>()
        
        // 검색어
        criteria.search?.let {
            predicates.add(
                cb.like(
                    cb.lower(root.get("productName")),
                    "%${it.lowercase()}%"
                )
            )
        }
        
        // 카테고리 (하위 포함)
        criteria.categoryId?.let {
            val category = categoryRepository.findById(it)
            val categoryIds = category.getAllDescendantIds() + it
            predicates.add(root.get<Long>("categoryId").`in`(categoryIds))
        }
        
        // 가격 범위
        criteria.minPrice?.let {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), it))
        }
        criteria.maxPrice?.let {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), it))
        }
        
        // 상태
        criteria.status?.let {
            predicates.add(cb.equal(root.get<ProductStatus>("status"), it))
        }
        
        // 삭제됨 제외 (ADMIN 제외)
        if (!isAdmin()) {
            predicates.add(cb.notEqual(root.get<ProductStatus>("status"), ProductStatus.DELETED))
        }
        
        cb.and(*predicates.toTypedArray())
    }
}
```

#### BR-011: 캐시 키 생성
```kotlin
fun generateCacheKey(criteria: ProductSearchCriteria): String {
    return "products:list:" + listOf(
        criteria.search,
        criteria.categoryId,
        criteria.minPrice,
        criteria.maxPrice,
        criteria.status,
        criteria.sortBy,
        criteria.sortOrder,
        criteria.page,
        criteria.size
    ).joinToString(":")
}
```

### 성능 최적화

| 최적화 기법 | 적용 대상 | 효과 |
|-----------|---------|------|
| 인덱스 | productName, categoryId, price, status, createdAt | 조회 속도 10배 향상 |
| 페이징 | LIMIT, OFFSET | 메모리 사용량 감소 |
| 캐싱 | 자주 조회되는 조건 | 응답 시간 50% 단축 |
| N+1 문제 해결 | JOIN FETCH images | 쿼리 수 감소 |

---

## 6. 조회수 증가 (FR-005)

### 처리 흐름

```
1. 상품 조회
   └─ 캐시 우선 조회 (Redis)

2. 조회수 증가 조건 확인
   ├─ 사용자 식별 (IP 또는 User ID)
   ├─ 1시간 내 중복 조회 확인
   └─ 중복이 아니면 조회수 증가

3. 조회수 증가 (비동기)
   ├─ Redis INCR 명령
   ├─ 조회 이력 저장 (IP, Timestamp)
   └─ 배치로 DB 동기화 (10분마다)

4. 응답 반환
   └─ 상품 상세 정보
```

### 비즈니스 규칙

#### BR-012: 중복 조회 방지
```kotlin
fun shouldIncrementViewCount(
    productCode: String,
    userId: String?
): Boolean {
    val key = "view:$productCode:${userId ?: getClientIp()}"
    val exists = redisTemplate.hasKey(key)
    
    if (!exists) {
        // 1시간 TTL 설정
        redisTemplate.opsForValue().set(key, "1", 1, TimeUnit.HOURS)
        return true
    }
    
    return false
}
```

#### BR-013: 조회수 비동기 증가
```kotlin
@Async
fun incrementViewCount(productCode: String) {
    // Redis에서 조회수 증가
    redisTemplate.opsForValue().increment("viewCount:$productCode")
}

@Scheduled(fixedDelay = 600000) // 10분마다
fun syncViewCountToDatabase() {
    val keys = redisTemplate.keys("viewCount:*")
    keys.forEach { key ->
        val productCode = key.removePrefix("viewCount:")
        val viewCount = redisTemplate.opsForValue().get(key)?.toInt() ?: 0
        
        productRepository.updateViewCount(productCode, viewCount)
        redisTemplate.delete(key)
    }
}
```

---

**다음 단계**: 데이터 모델 설계 (ERD)
