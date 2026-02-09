// File: src/main/kotlin/com/shop/product/domain/Product.kt
package com.shop.product.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "product")
@EntityListeners(AuditingEntityListener::class)
data class Product(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
        @Column(nullable = false, unique = true, length = 20) val productCode: String,
        @Column(nullable = false, length = 100) var productName: String,
        @Column(columnDefinition = "TEXT") var description: String? = null,
        @Column(nullable = false, precision = 12, scale = 2) var price: BigDecimal,
        @Column(nullable = false) var stock: Int = 0,
        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        var status: ProductStatus = ProductStatus.ACTIVE,
        @Column(nullable = false) val sellerId: Long,
        @Column(nullable = false) val categoryId: Long,
        @Column(nullable = false) var viewCount: Long = 0,
        @Version var version: Long = 0,
        @CreatedDate
        @Column(nullable = false, updatable = false)
        var createdAt: Instant = Instant.now(),
        @LastModifiedDate @Column(nullable = false) var updatedAt: Instant = Instant.now(),
        @Column var deletedAt: Instant? = null
) {
    fun update(name: String?, description: String?, price: BigDecimal?) {
        name?.let { this.productName = it }
        description?.let { this.description = it }
        price?.let { this.price = it }
    }

    fun changeStatus(newStatus: ProductStatus) {
        this.status = newStatus
    }

    fun adjustStock(quantity: Int) {
        this.stock += quantity
        require(this.stock >= 0) { "재고는 0 미만이 될 수 없습니다" }
    }

    fun incrementViewCount() {
        this.viewCount++
    }

    fun delete() {
        this.deletedAt = Instant.now()
        this.status = ProductStatus.DELETED
    }

    fun isDeleted(): Boolean = deletedAt != null
}

enum class ProductStatus {
    ACTIVE, // 판매중
    INACTIVE, // 판매중지
    SOLD_OUT, // 품절
    DELETED // 삭제됨
}
