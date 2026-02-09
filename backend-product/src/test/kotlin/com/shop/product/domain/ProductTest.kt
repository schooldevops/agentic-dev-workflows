// File: src/test/kotlin/com/shop/product/domain/ProductTest.kt
package com.shop.product.domain

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductTest {

    @Test
    fun `상품 정보를 수정하면 정보가 변경되어야 한다`() {
        // Given
        val product =
                Product(
                        productCode = "PRD-000001",
                        productName = "테스트 상품",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )

        // When
        product.update(name = "수정된 상품", description = "수정된 설명", price = BigDecimal("20000"))

        // Then
        assertEquals("수정된 상품", product.productName)
        assertEquals("수정된 설명", product.description)
        assertEquals(BigDecimal("20000"), product.price)
    }

    @Test
    fun `재고를 증가시키면 재고가 증가해야 한다`() {
        // Given
        val product =
                Product(
                        productCode = "PRD-000001",
                        productName = "테스트 상품",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )

        // When
        product.adjustStock(50)

        // Then
        assertEquals(150, product.stock)
    }

    @Test
    fun `재고를 감소시키면 재고가 감소해야 한다`() {
        // Given
        val product =
                Product(
                        productCode = "PRD-000001",
                        productName = "테스트 상품",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )

        // When
        product.adjustStock(-30)

        // Then
        assertEquals(70, product.stock)
    }

    @Test
    fun `재고가 음수가 되도록 감소시키면 IllegalArgumentException이 발생해야 한다`() {
        // Given
        val product =
                Product(
                        productCode = "PRD-000001",
                        productName = "테스트 상품",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )

        // When & Then
        assertThrows<IllegalArgumentException> { product.adjustStock(-200) }
    }

    @Test
    fun `상품 상태를 변경하면 상태가 변경되어야 한다`() {
        // Given
        val product =
                Product(
                        productCode = "PRD-000001",
                        productName = "테스트 상품",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )

        // When
        product.changeStatus(ProductStatus.SOLD_OUT)

        // Then
        assertEquals(ProductStatus.SOLD_OUT, product.status)
    }

    @Test
    fun `조회수를 증가시키면 조회수가 1 증가해야 한다`() {
        // Given
        val product =
                Product(
                        productCode = "PRD-000001",
                        productName = "테스트 상품",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )
        val initialCount = product.viewCount

        // When
        product.incrementViewCount()

        // Then
        assertEquals(initialCount + 1, product.viewCount)
    }

    @Test
    fun `상품을 삭제하면 삭제 상태가 되어야 한다`() {
        // Given
        val product =
                Product(
                        productCode = "PRD-000001",
                        productName = "테스트 상품",
                        price = BigDecimal("10000"),
                        stock = 100,
                        sellerId = 1L,
                        categoryId = 1L
                )

        // When
        product.delete()

        // Then
        assertTrue(product.isDeleted())
        assertEquals(ProductStatus.DELETED, product.status)
        assertNotNull(product.deletedAt)
    }
}
