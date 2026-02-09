// File: src/main/kotlin/com/shop/product/domain/StockHistory.kt
package com.shop.product.domain

import jakarta.persistence.*
import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "stock_history")
@EntityListeners(AuditingEntityListener::class)
data class StockHistory(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
        @Column(nullable = false, length = 20) val productCode: String,
        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        val adjustmentType: AdjustmentType,
        @Column(nullable = false) val quantity: Int,
        @Column(nullable = false) val previousStock: Int,
        @Column(nullable = false) val currentStock: Int,
        @Column(length = 200) val reason: String? = null,
        @Column(nullable = false) val adjustedBy: Long,
        @CreatedDate
        @Column(nullable = false, updatable = false)
        var adjustedAt: Instant = Instant.now()
)

enum class AdjustmentType {
    INCREASE, // 입고
    DECREASE, // 출고
    CORRECTION // 재고 조정
}
