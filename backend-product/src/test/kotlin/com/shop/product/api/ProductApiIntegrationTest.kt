// File: src/test/kotlin/com/shop/product/api/ProductApiIntegrationTest.kt
package com.shop.product.api

import com.shop.product.domain.ProductRepository
import com.shop.product.domain.StockHistoryRepository
import com.shop.product.service.*
import java.math.BigDecimal
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ProductApiIntegrationTest {

    @Autowired private lateinit var restTemplate: TestRestTemplate

    @Autowired private lateinit var productRepository: ProductRepository

    @Autowired private lateinit var stockHistoryRepository: StockHistoryRepository

    private fun createHeaders(userId: Long = 1L): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("X-User-Id", userId.toString())
        return headers
    }

    @BeforeEach
    fun cleanup() {
        stockHistoryRepository.deleteAll()
        productRepository.deleteAll()
    }

    // ========== Product CRUD Tests ==========

    @Test
    @Order(1)
    fun `TC-001 Create Product - Success`() {
        // Given
        val request =
                CreateProductRequest(
                        productCode = "PRD-TEST001",
                        productName = "테스트 상품",
                        description = "테스트 설명",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )

        // When
        val response =
                restTemplate.postForEntity(
                        "/api/products",
                        HttpEntity(request, createHeaders()),
                        ProductResponse::class.java
                )

        // Then
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertEquals("PRD-TEST001", response.body?.productCode)
        assertEquals("테스트 상품", response.body?.productName)
        assertEquals(BigDecimal("10000"), response.body?.price)
        assertEquals(100, response.body?.stock)
    }

    @Test
    @Order(2)
    fun `TC-002 Create Product - Missing Required Fields`() {
        // Given - price is missing
        val invalidJson =
                """
            {
                "productCode": "PRD-TEST002",
                "productName": "테스트 상품",
                "stock": 100,
                "sellerId": 1,
                "categoryId": 1
            }
        """.trimIndent()

        val headers = createHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        // When
        val response =
                restTemplate.postForEntity(
                        "/api/products",
                        HttpEntity(invalidJson, headers),
                        String::class.java
                )

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    @Order(3)
    fun `TC-003 Create Product - Duplicate Product Code`() {
        // Given - create first product
        val request1 =
                CreateProductRequest(
                        productCode = "PRD-DUP001",
                        productName = "첫번째 상품",
                        description = "설명",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )
        restTemplate.postForEntity(
                "/api/products",
                HttpEntity(request1, createHeaders()),
                ProductResponse::class.java
        )

        // When - try to create duplicate
        val request2 =
                CreateProductRequest(
                        productCode = "PRD-DUP001", // same code
                        productName = "중복 상품",
                        description = "설명",
                        price = BigDecimal("20000"),
                        stock = 50,
                        sellerId = 1L,
                        categoryId = 1L
                )

        val response =
                restTemplate.postForEntity(
                        "/api/products",
                        HttpEntity(request2, createHeaders()),
                        ProductResponse::class.java
                )

        // Then
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
    }

    @Test
    @Order(4)
    fun `TC-004 Get Product by Code - Success`() {
        // Given - create product first
        val createRequest =
                CreateProductRequest(
                        productCode = "PRD-GET001",
                        productName = "조회 테스트 상품",
                        description = "설명",
                        price = BigDecimal("15000"),
                        stock = 50,
                        sellerId = 1L,
                        categoryId = 1L
                )
        restTemplate.postForEntity(
                "/api/products",
                HttpEntity(createRequest, createHeaders()),
                ProductResponse::class.java
        )

        // When
        val response =
                restTemplate.getForEntity("/api/products/PRD-GET001", ProductResponse::class.java)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("PRD-GET001", response.body?.productCode)
        assertEquals("조회 테스트 상품", response.body?.productName)
    }

    @Test
    @Order(5)
    fun `TC-005 Update Product - Success (Own Product)`() {
        // Given - create product
        val createRequest =
                CreateProductRequest(
                        productCode = "PRD-UPD001",
                        productName = "수정 전 상품",
                        description = "설명",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )
        val created =
                restTemplate.postForEntity(
                                "/api/products",
                                HttpEntity(createRequest, createHeaders(1L)),
                                ProductResponse::class.java
                        )
                        .body!!

        // When - update product
        val updateRequest =
                UpdateProductRequest(
                        productName = "수정된 상품명",
                        description = "수정된 설명",
                        price = BigDecimal("20000"),
                        version = created.version
                )

        val response =
                restTemplate.exchange(
                        "/api/products/PRD-UPD001",
                        HttpMethod.PUT,
                        HttpEntity(updateRequest, createHeaders(1L)),
                        ProductResponse::class.java
                )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("수정된 상품명", response.body?.productName)
        assertEquals(BigDecimal("20000"), response.body?.price)
    }

    @Test
    @Order(6)
    fun `TC-006 Update Product - Forbidden (Other User's Product)`() {
        // Given - create product with user 1
        val createRequest =
                CreateProductRequest(
                        productCode = "PRD-AUTH001",
                        productName = "사용자1의 상품",
                        description = "설명",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )
        val created =
                restTemplate.postForEntity(
                                "/api/products",
                                HttpEntity(createRequest, createHeaders(1L)),
                                ProductResponse::class.java
                        )
                        .body!!

        // When - try to update with user 2
        val updateRequest =
                UpdateProductRequest(
                        productName = "해킹 시도",
                        price = BigDecimal("1"),
                        version = created.version
                )

        val response =
                restTemplate.exchange(
                        "/api/products/PRD-AUTH001",
                        HttpMethod.PUT,
                        HttpEntity(updateRequest, createHeaders(2L)), // different user
                        ProductResponse::class.java
                )

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    @Order(7)
    fun `TC-007 Delete Product - Success`() {
        // Given - create product
        val createRequest =
                CreateProductRequest(
                        productCode = "PRD-DEL001",
                        productName = "삭제할 상품",
                        description = "설명",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )
        restTemplate.postForEntity(
                "/api/products",
                HttpEntity(createRequest, createHeaders(1L)),
                ProductResponse::class.java
        )

        // When
        val response =
                restTemplate.exchange(
                        "/api/products/PRD-DEL001",
                        HttpMethod.DELETE,
                        HttpEntity<Void>(createHeaders(1L)),
                        Void::class.java
                )

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        // Verify product is marked as deleted
        val product = productRepository.findByProductCode("PRD-DEL001")
        assertTrue(product?.isDeleted() == true)
    }

    // ========== Stock Management Tests ==========

    @Test
    @Order(10)
    fun `TC-010 Increase Stock - Success`() {
        // Given - create product
        val createRequest =
                CreateProductRequest(
                        productCode = "PRD-STK001",
                        productName = "재고 테스트 상품",
                        description = "설명",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )
        restTemplate.postForEntity(
                "/api/products",
                HttpEntity(createRequest, createHeaders()),
                ProductResponse::class.java
        )

        // When - increase stock
        val adjustRequest =
                AdjustStockRequest(adjustmentType = "INCREASE", quantity = 50, reason = "입고")

        val response =
                restTemplate.postForEntity(
                        "/api/stock/PRD-STK001/adjust",
                        HttpEntity(adjustRequest, createHeaders()),
                        ProductResponse::class.java
                )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(150, response.body?.stock)
    }

    @Test
    @Order(11)
    fun `TC-011 Decrease Stock - Success`() {
        // Given - create product with stock
        val createRequest =
                CreateProductRequest(
                        productCode = "PRD-STK002",
                        productName = "재고 감소 테스트",
                        description = "설명",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )
        restTemplate.postForEntity(
                "/api/products",
                HttpEntity(createRequest, createHeaders()),
                ProductResponse::class.java
        )

        // When - decrease stock
        val adjustRequest =
                AdjustStockRequest(adjustmentType = "DECREASE", quantity = 30, reason = "출고")

        val response =
                restTemplate.postForEntity(
                        "/api/stock/PRD-STK002/adjust",
                        HttpEntity(adjustRequest, createHeaders()),
                        ProductResponse::class.java
                )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(70, response.body?.stock)
    }

    @Test
    @Order(12)
    fun `TC-012 Decrease Stock - Insufficient Stock`() {
        // Given - create product with limited stock
        val createRequest =
                CreateProductRequest(
                        productCode = "PRD-STK003",
                        productName = "재고 부족 테스트",
                        description = "설명",
                        price = BigDecimal("10000"),
                        stock = 50,
                        sellerId = 1L,
                        categoryId = 1L
                )
        restTemplate.postForEntity(
                "/api/products",
                HttpEntity(createRequest, createHeaders()),
                ProductResponse::class.java
        )

        // When - try to decrease more than available
        val adjustRequest =
                AdjustStockRequest(
                        adjustmentType = "DECREASE",
                        quantity = 100, // more than 50
                        reason = "출고"
                )

        val response =
                restTemplate.postForEntity(
                        "/api/stock/PRD-STK003/adjust",
                        HttpEntity(adjustRequest, createHeaders()),
                        ProductResponse::class.java
                )

        // Then
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
    }

    // ========== Security Tests ==========

    @Test
    @Order(20)
    fun `TC-020 XSS Prevention - HTML Escaping`() {
        // Given - request with XSS attempt
        val request =
                CreateProductRequest(
                        productCode = "PRD-XSS001",
                        productName = "<script>alert('XSS')</script>",
                        description = "<img src=x onerror=alert('XSS')>",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )

        // When
        val response =
                restTemplate.postForEntity(
                        "/api/products",
                        HttpEntity(request, createHeaders()),
                        ProductResponse::class.java
                )

        // Then
        assertEquals(HttpStatus.CREATED, response.statusCode)
        // Verify HTML is escaped
        assertTrue(response.body?.productName?.contains("&lt;script&gt;") == true)
        assertTrue(response.body?.description?.contains("&lt;img") == true)
    }

    @Test
    @Order(21)
    fun `TC-021 Price Validation - Must be divisible by 100`() {
        // Given - price not divisible by 100
        val request =
                CreateProductRequest(
                        productCode = "PRD-PRICE001",
                        productName = "가격 검증 테스트",
                        description = "설명",
                        price = BigDecimal("10050"), // not divisible by 100
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )

        // When
        val response =
                restTemplate.postForEntity(
                        "/api/products",
                        HttpEntity(request, createHeaders()),
                        ProductResponse::class.java
                )

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    // ========== List and Pagination Tests ==========

    @Test
    @Order(30)
    fun `TC-030 Get Product List - With Pagination`() {
        // Given - create multiple products
        repeat(5) { i ->
            val request =
                    CreateProductRequest(
                            productCode = "PRD-LIST00$i",
                            productName = "상품 $i",
                            description = "설명",
                            price = BigDecimal("10000"),
                            stock = 100,
                            sellerId = 1L,
                            categoryId = 1L
                    )
            restTemplate.postForEntity(
                    "/api/products",
                    HttpEntity(request, createHeaders()),
                    ProductResponse::class.java
            )
        }

        // When
        val response =
                restTemplate.exchange(
                        "/api/products?page=0&size=3",
                        HttpMethod.GET,
                        null,
                        object :
                                org.springframework.core.ParameterizedTypeReference<
                                        PageResponse<ProductResponse>>() {}
                )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(3, response.body?.content?.size)
        assertTrue((response.body?.totalElements ?: 0) >= 5)
    }
}
