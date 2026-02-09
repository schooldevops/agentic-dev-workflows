// File: src/test/kotlin/com/shop/product/service/ProductServiceTest.kt
package com.shop.product.service

import com.shop.product.domain.*
import com.shop.product.exception.*
import io.mockk.*
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class ProductServiceTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var stockHistoryRepository: StockHistoryRepository
    private lateinit var productService: ProductService

    @BeforeEach
    fun setUp() {
        productRepository = mockk()
        stockHistoryRepository = mockk()
        productService = ProductService(productRepository, stockHistoryRepository)
    }

    @Test
    fun `정상적인 상품 등록 요청이면 상품이 생성되어야 한다`() {
        // Given
        val request =
                CreateProductRequest(
                        productCode = "PRD-000001",
                        productName = "테스트 상품",
                        description = "테스트 설명",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )

        every { productRepository.existsByProductCode(any()) } returns false
        every { productRepository.save(any()) } answers { firstArg() }
        every { stockHistoryRepository.save(any()) } answers { firstArg() }

        // When
        val result = productService.createProduct(request)

        // Then
        assertNotNull(result)
        assertEquals("PRD-000001", result.productCode)
        assertEquals("테스트 상품", result.productName)
        assertEquals(BigDecimal("10000"), result.price)
        assertEquals(100, result.stock)

        verify(exactly = 1) { productRepository.save(any()) }
        verify(exactly = 1) { stockHistoryRepository.save(any()) }
    }

    @Test
    fun `중복된 상품 코드가 있으면 ProductCodeDuplicateException이 발생해야 한다`() {
        // Given
        val request =
                CreateProductRequest(
                        productCode = "PRD-000001",
                        productName = "테스트 상품",
                        description = "테스트 설명",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )

        every { productRepository.existsByProductCode(any()) } returns true

        // When & Then
        assertThrows<ProductCodeDuplicateException> { productService.createProduct(request) }
    }

    @Test
    fun `존재하는 상품이면 상품 정보를 반환해야 한다`() {
        // Given
        val productCode = "PRD-000001"
        val product =
                Product(
                        id = 1L,
                        productCode = productCode,
                        productName = "테스트 상품",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )

        every { productRepository.findByProductCode(productCode) } returns product

        // When
        val result = productService.getProduct(productCode)

        // Then
        assertEquals(productCode, result.productCode)
        assertEquals("테스트 상품", result.productName)
    }

    @Test
    fun `존재하지 않는 상품이면 ProductNotFoundException이 발생해야 한다`() {
        // Given
        val productCode = "PRD-000001"
        every { productRepository.findByProductCode(productCode) } returns null

        // When & Then
        assertThrows<ProductNotFoundException> { productService.getProduct(productCode) }
    }

    @Test
    fun `페이징 요청이면 페이징된 상품 목록을 반환해야 한다`() {
        // Given
        val products =
                listOf(
                        Product(
                                id = 1L,
                                productCode = "PRD-000001",
                                productName = "상품1",
                                price = BigDecimal("10000"),
                                stock = 100,
                                sellerId = 1L,
                                categoryId = 1L
                        ),
                        Product(
                                id = 2L,
                                productCode = "PRD-000002",
                                productName = "상품2",
                                price = BigDecimal("20000"),
                                stock = 50,
                                sellerId = 1L,
                                categoryId = 1L
                        )
                )

        val pageable = PageRequest.of(0, 20)
        val page = PageImpl(products, pageable, products.size.toLong())

        every { productRepository.findAllActive(pageable) } returns page

        // When
        val result = productService.getProducts(pageable)

        // Then
        assertEquals(2, result.content.size)
        assertEquals(2, result.totalElements)
        assertEquals(0, result.currentPage)
        assertEquals(20, result.size)
    }
}
