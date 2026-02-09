# 시스템 아키텍처: 상품관리

**프로젝트명**: 차세대 플랫폼 - 상품관리  
**작성일**: 2026-02-06  
**작성자**: 설계 Agent  
**버전**: v1.0  
**기반 문서**: 인터페이스 정의서 v1.0, 데이터 모델 v1.0

---

## 1. 전체 시스템 아키텍처

```mermaid
C4Context
    title 상품관리 시스템 컨텍스트 다이어그램

    Person(customer, "고객", "상품을 조회하고 구매하는 사용자")
    Person(seller, "판매자", "상품을 등록하고 관리하는 사용자")
    Person(admin, "관리자", "전체 시스템을 관리하는 사용자")

    System(productSystem, "상품관리 시스템", "상품 등록, 조회, 수정, 재고 관리")
    
    System_Ext(authSystem, "인증 시스템", "JWT 기반 인증/인가")
    System_Ext(orderSystem, "주문 시스템", "주문 처리 및 재고 차감")
    System_Ext(imageService, "이미지 서비스", "이미지 업로드 및 CDN 배포")
    System_Ext(notificationService, "알림 서비스", "재고 부족 등 알림")
    
    SystemDb(productDB, "Product DB", "PostgreSQL 15")
    SystemDb(cache, "Cache", "Redis 7")
    SystemDb(storage, "Object Storage", "AWS S3")

    Rel(customer, productSystem, "상품 조회", "HTTPS/REST")
    Rel(seller, productSystem, "상품 관리", "HTTPS/REST")
    Rel(admin, productSystem, "시스템 관리", "HTTPS/REST")
    
    Rel(productSystem, authSystem, "인증 요청", "gRPC")
    Rel(orderSystem, productSystem, "재고 확인/차감", "gRPC")
    Rel(productSystem, imageService, "이미지 처리 요청", "Async/Kafka")
    Rel(productSystem, notificationService, "알림 발송", "Async/Kafka")
    
    Rel(productSystem, productDB, "데이터 저장/조회", "JDBC")
    Rel(productSystem, cache, "캐싱", "Redis Protocol")
    Rel(imageService, storage, "이미지 저장", "S3 API")
```

---

## 2. 컨테이너 다이어그램 (C4 Level 2)

```mermaid
graph TB
    subgraph "Client Layer"
        WebApp[Web Application<br/>Next.js 15]
        MobileApp[Mobile App<br/>React Native]
    end
    
    subgraph "API Gateway Layer"
        Gateway[API Gateway<br/>Spring Cloud Gateway]
        RateLimit[Rate Limiter<br/>Redis]
    end
    
    subgraph "Application Layer"
        ProductAPI[Product API<br/>Spring Boot 3.2<br/>Kotlin]
        ImageWorker[Image Worker<br/>Async Processing]
        BatchJob[Batch Job<br/>조회수 동기화]
    end
    
    subgraph "Data Layer"
        ProductDB[(Product DB<br/>PostgreSQL 15)]
        RedisCache[(Redis Cache<br/>Redis 7)]
        S3Storage[(S3 Storage<br/>이미지)]
    end
    
    subgraph "Message Queue"
        Kafka[Kafka<br/>Event Streaming]
    end
    
    subgraph "External Services"
        AuthService[Auth Service<br/>인증/인가]
        OrderService[Order Service<br/>주문 처리]
    end
    
    WebApp -->|HTTPS| Gateway
    MobileApp -->|HTTPS| Gateway
    
    Gateway -->|JWT 검증| AuthService
    Gateway -->|Rate Limit 확인| RateLimit
    Gateway -->|REST| ProductAPI
    
    ProductAPI -->|Read/Write| ProductDB
    ProductAPI -->|Cache| RedisCache
    ProductAPI -->|Publish Event| Kafka
    
    Kafka -->|Subscribe| ImageWorker
    Kafka -->|Subscribe| OrderService
    
    ImageWorker -->|Upload| S3Storage
    ImageWorker -->|Update| ProductDB
    
    BatchJob -->|Sync ViewCount| ProductDB
    BatchJob -->|Read| RedisCache
    
    OrderService -->|gRPC| ProductAPI
```

---

## 3. 컴포넌트 다이어그램 (Product API 내부)

```mermaid
graph TB
    subgraph "Product API"
        subgraph "Presentation Layer"
            ProductController[ProductController<br/>REST Endpoints]
            StockController[StockController<br/>재고 관리]
            ImageController[ImageController<br/>이미지 관리]
        end
        
        subgraph "Application Layer"
            ProductService[ProductService<br/>비즈니스 로직]
            StockService[StockService<br/>재고 로직]
            ImageService[ImageService<br/>이미지 로직]
            CacheService[CacheService<br/>캐시 관리]
        end
        
        subgraph "Domain Layer"
            Product[Product<br/>Entity]
            StockHistory[StockHistory<br/>Entity]
            ProductImage[ProductImage<br/>Entity]
            ProductRepository[ProductRepository<br/>JPA]
        end
        
        subgraph "Infrastructure Layer"
            JwtFilter[JwtAuthFilter<br/>인증 필터]
            RateLimitFilter[RateLimitFilter<br/>Rate Limiting]
            ExceptionHandler[GlobalExceptionHandler<br/>에러 처리]
            EventPublisher[EventPublisher<br/>Kafka Producer]
        end
    end
    
    ProductController --> ProductService
    StockController --> StockService
    ImageController --> ImageService
    
    ProductService --> Product
    ProductService --> ProductRepository
    ProductService --> CacheService
    ProductService --> EventPublisher
    
    StockService --> StockHistory
    StockService --> ProductRepository
    
    ImageService --> ProductImage
    ImageService --> EventPublisher
    
    ProductRepository --> ProductDB[(PostgreSQL)]
    CacheService --> Redis[(Redis)]
    EventPublisher --> Kafka[Kafka]
```

---

## 4. 배포 아키텍처

```mermaid
graph TB
    subgraph "AWS Cloud"
        subgraph "Public Subnet"
            ALB[Application Load Balancer<br/>AWS ALB]
            NAT[NAT Gateway]
        end
        
        subgraph "Private Subnet - AZ-A"
            API1[Product API<br/>ECS Fargate]
            Worker1[Image Worker<br/>ECS Fargate]
        end
        
        subgraph "Private Subnet - AZ-B"
            API2[Product API<br/>ECS Fargate]
            Worker2[Image Worker<br/>ECS Fargate]
        end
        
        subgraph "Data Tier - AZ-A"
            RDS_Primary[(RDS PostgreSQL<br/>Primary)]
            ElastiCache_Primary[(ElastiCache Redis<br/>Primary)]
        end
        
        subgraph "Data Tier - AZ-B"
            RDS_Standby[(RDS PostgreSQL<br/>Standby)]
            ElastiCache_Replica[(ElastiCache Redis<br/>Replica)]
        end
        
        subgraph "Storage"
            S3[S3 Bucket<br/>이미지 저장]
            CloudFront[CloudFront<br/>CDN]
        end
        
        subgraph "Messaging"
            MSK[Amazon MSK<br/>Kafka Cluster]
        end
        
        subgraph "Monitoring"
            CloudWatch[CloudWatch<br/>로그/메트릭]
            XRay[X-Ray<br/>분산 추적]
        end
    end
    
    Internet[Internet] -->|HTTPS| ALB
    ALB --> API1
    ALB --> API2
    
    API1 --> RDS_Primary
    API2 --> RDS_Primary
    
    API1 --> ElastiCache_Primary
    API2 --> ElastiCache_Primary
    
    RDS_Primary -.->|Replication| RDS_Standby
    ElastiCache_Primary -.->|Replication| ElastiCache_Replica
    
    API1 --> MSK
    API2 --> MSK
    
    MSK --> Worker1
    MSK --> Worker2
    
    Worker1 --> S3
    Worker2 --> S3
    
    S3 --> CloudFront
    CloudFront --> Internet
    
    API1 -.->|Logs/Metrics| CloudWatch
    API2 -.->|Logs/Metrics| CloudWatch
    API1 -.->|Traces| XRay
    API2 -.->|Traces| XRay
```

---

## 5. 데이터 흐름 아키텍처

### 5.1 읽기 경로 (Read Path)

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant API
    participant Cache
    participant DB
    
    Client->>Gateway: GET /api/products/PRD-001
    Gateway->>API: Forward Request
    
    API->>Cache: GET product:PRD-001
    
    alt Cache Hit
        Cache-->>API: Product Data
        API-->>Gateway: 200 OK
    else Cache Miss
        Cache-->>API: NULL
        API->>DB: SELECT * FROM product
        DB-->>API: Product Data
        API->>Cache: SET product:PRD-001<br/>TTL 10분
        API-->>Gateway: 200 OK
    end
    
    Gateway-->>Client: Product Response
```

### 5.2 쓰기 경로 (Write Path)

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant API
    participant DB
    participant Cache
    participant Kafka
    
    Client->>Gateway: POST /api/products
    Gateway->>API: Forward Request
    
    API->>DB: BEGIN TRANSACTION
    API->>DB: INSERT INTO product
    API->>DB: INSERT INTO stock_history
    API->>DB: COMMIT
    
    API->>Cache: DELETE product:*<br/>(캐시 무효화)
    
    API->>Kafka: Publish ProductCreatedEvent
    
    API-->>Gateway: 201 Created
    Gateway-->>Client: Product Response
```

---

## 6. 보안 아키텍처

```mermaid
graph TB
    subgraph "Security Layers"
        subgraph "Network Security"
            WAF[AWS WAF<br/>DDoS 방어]
            SecurityGroup[Security Group<br/>방화벽 규칙]
        end
        
        subgraph "Application Security"
            JWT[JWT Authentication<br/>토큰 검증]
            RBAC[RBAC Authorization<br/>역할 기반 권한]
            RateLimit[Rate Limiting<br/>요청 제한]
        end
        
        subgraph "Data Security"
            Encryption[Encryption at Rest<br/>RDS/S3 암호화]
            TLS[TLS 1.3<br/>전송 암호화]
            SecretManager[AWS Secrets Manager<br/>비밀 관리]
        end
        
        subgraph "Audit & Compliance"
            CloudTrail[CloudTrail<br/>API 감사]
            GuardDuty[GuardDuty<br/>위협 탐지]
            Config[AWS Config<br/>규정 준수]
        end
    end
    
    Internet[Internet] --> WAF
    WAF --> SecurityGroup
    SecurityGroup --> JWT
    JWT --> RBAC
    RBAC --> RateLimit
    
    RateLimit --> Encryption
    Encryption --> TLS
    TLS --> SecretManager
    
    SecretManager -.-> CloudTrail
    CloudTrail -.-> GuardDuty
    GuardDuty -.-> Config
```

---

## 7. 확장성 전략

### 7.1 수평 확장 (Horizontal Scaling)

| 컴포넌트 | 확장 방식 | 트리거 | 목표 |
|---------|---------|--------|------|
| Product API | Auto Scaling Group | CPU > 70% | 2~10 인스턴스 |
| Image Worker | ECS Service Auto Scaling | Queue Depth > 100 | 1~5 인스턴스 |
| RDS Read Replica | 수동 추가 | Read 부하 > 80% | 최대 5개 |
| ElastiCache | Cluster Mode | 메모리 사용률 > 75% | 샤딩 |

### 7.2 수직 확장 (Vertical Scaling)

| 컴포넌트 | 현재 스펙 | 확장 스펙 | 조건 |
|---------|---------|---------|------|
| RDS Primary | db.t3.large | db.r5.xlarge | 연결 수 > 500 |
| ElastiCache | cache.t3.medium | cache.r5.large | 메모리 부족 |
| ECS Task | 2 vCPU, 4GB | 4 vCPU, 8GB | 응답 시간 > 1초 |

---

## 8. 고가용성 (High Availability)

### 8.1 장애 복구 시나리오

| 장애 유형 | 감지 방법 | 복구 방법 | RTO | RPO |
|---------|---------|---------|-----|-----|
| API 인스턴스 다운 | Health Check 실패 | Auto Scaling 재시작 | 1분 | 0 |
| RDS Primary 장애 | RDS Failover | Standby 승격 | 2분 | 0 |
| Redis 장애 | Connection Timeout | Replica 승격 | 30초 | 1분 |
| AZ 전체 장애 | Multi-AZ 감지 | 다른 AZ로 트래픽 전환 | 5분 | 0 |
| Region 장애 | Route53 Health Check | DR Region 활성화 | 30분 | 5분 |

### 8.2 백업 전략

```mermaid
graph LR
    subgraph "Backup Strategy"
        RDS[RDS Automated Backup<br/>일 1회, 7일 보관]
        Snapshot[Manual Snapshot<br/>주 1회, 30일 보관]
        S3Versioning[S3 Versioning<br/>이미지 버전 관리]
        WAL[WAL Archiving<br/>Point-in-Time Recovery]
    end
    
    RDS --> Snapshot
    Snapshot --> S3Backup[S3 Cross-Region<br/>Replication]
    S3Versioning --> S3Backup
    WAL --> S3Backup
```

---

## 9. 모니터링 및 관찰성 (Observability)

### 9.1 메트릭 수집

```mermaid
graph TB
    subgraph "Application Metrics"
        API[Product API<br/>Micrometer]
        CustomMetrics[Custom Metrics<br/>비즈니스 메트릭]
    end
    
    subgraph "Infrastructure Metrics"
        ECS[ECS Container Insights]
        RDS_Metrics[RDS Enhanced Monitoring]
        Redis_Metrics[ElastiCache Metrics]
    end
    
    subgraph "Aggregation & Visualization"
        CloudWatch[CloudWatch<br/>중앙 집중 메트릭]
        Grafana[Grafana<br/>대시보드]
        Prometheus[Prometheus<br/>시계열 DB]
    end
    
    API --> CloudWatch
    CustomMetrics --> CloudWatch
    ECS --> CloudWatch
    RDS_Metrics --> CloudWatch
    Redis_Metrics --> CloudWatch
    
    CloudWatch --> Prometheus
    Prometheus --> Grafana
```

### 9.2 주요 모니터링 지표

| 카테고리 | 메트릭 | 임계값 | 알림 |
|---------|--------|--------|------|
| **성능** | API 응답 시간 | p95 > 500ms | Slack |
| **성능** | DB 쿼리 시간 | p99 > 1초 | Email |
| **가용성** | API 에러율 | > 1% | PagerDuty |
| **가용성** | Health Check 실패 | 연속 3회 | PagerDuty |
| **용량** | CPU 사용률 | > 80% | Slack |
| **용량** | 메모리 사용률 | > 85% | Slack |
| **비즈니스** | 상품 등록 실패율 | > 5% | Email |
| **비즈니스** | 재고 부족 발생 | 발생 시 | Slack |

---

## 10. 성능 최적화 전략

### 10.1 캐싱 전략

| 데이터 유형 | 캐시 위치 | TTL | 무효화 전략 |
|-----------|---------|-----|-----------|
| 상품 상세 | Redis | 10분 | Write-Through |
| 상품 목록 | Redis | 5분 | Time-based |
| 카테고리 | Redis | 1시간 | Manual |
| 조회수 | Redis | 실시간 | Batch Sync (10분) |

### 10.2 데이터베이스 최적화

```sql
-- 주요 인덱스
CREATE INDEX idx_product_name ON product(product_name);
CREATE INDEX idx_category_id ON product(category_id);
CREATE INDEX idx_status ON product(status);
CREATE INDEX idx_created_at ON product(created_at DESC);
CREATE INDEX idx_price ON product(price);

-- 복합 인덱스
CREATE INDEX idx_category_status ON product(category_id, status);
CREATE INDEX idx_status_created ON product(status, created_at DESC);

-- 파티셔닝 (월별)
CREATE TABLE product_2026_02 PARTITION OF product
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
```

### 10.3 쿼리 최적화

```kotlin
// N+1 문제 해결
@Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.productCode = :code")
fun findByProductCodeWithImages(code: String): Product?

// 페이징 최적화
@Query("SELECT p FROM Product p WHERE p.status != 'DELETED' ORDER BY p.createdAt DESC")
fun findAllActive(pageable: Pageable): Page<Product>

// 카운트 쿼리 분리
@Query(
    value = "SELECT p FROM Product p WHERE p.status != 'DELETED'",
    countQuery = "SELECT COUNT(p) FROM Product p WHERE p.status != 'DELETED'"
)
fun findAllWithCount(pageable: Pageable): Page<Product>
```

---

**다음 단계**: MSA 설계 문서 작성
