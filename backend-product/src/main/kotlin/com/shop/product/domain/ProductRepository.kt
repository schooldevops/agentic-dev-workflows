// File: src/main/kotlin/com/shop/product/domain/ProductRepository.kt
package com.shop.product.domain

import jakarta.persistence.LockModeType
import java.math.BigDecimal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProductRepository : JpaRepository<Product, Long> {

    fun findByProductCode(productCode: String): Product?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productCode = :productCode")
    fun findByProductCodeWithLock(@Param("productCode") productCode: String): Product?

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.status != 'DELETED'")
    fun findAllActive(pageable: Pageable): Page<Product>

    @Query(
            "SELECT p FROM Product p WHERE p.categoryId = :categoryId AND p.deletedAt IS NULL AND p.status != 'DELETED'"
    )
    fun findByCategoryId(@Param("categoryId") categoryId: Long, pageable: Pageable): Page<Product>

    @Query(
            "SELECT p FROM Product p WHERE p.productName LIKE %:keyword% AND p.deletedAt IS NULL AND p.status != 'DELETED'"
    )
    fun searchByName(@Param("keyword") keyword: String, pageable: Pageable): Page<Product>

    @Query(
            "SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.deletedAt IS NULL AND p.status != 'DELETED'"
    )
    fun findByPriceRange(
            @Param("minPrice") minPrice: BigDecimal,
            @Param("maxPrice") maxPrice: BigDecimal,
            pageable: Pageable
    ): Page<Product>

    fun existsByProductCode(productCode: String): Boolean
}

interface StockHistoryRepository : JpaRepository<StockHistory, Long> {

    fun findByProductCode(productCode: String, pageable: Pageable): Page<StockHistory>

    @Query(
            "SELECT s FROM StockHistory s WHERE s.productCode = :productCode ORDER BY s.adjustedAt DESC"
    )
    fun findRecentHistory(
            @Param("productCode") productCode: String,
            pageable: Pageable
    ): Page<StockHistory>
}
