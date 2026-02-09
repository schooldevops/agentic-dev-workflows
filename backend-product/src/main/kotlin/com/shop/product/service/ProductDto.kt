// File: src/main/kotlin/com/shop/product/service/ProductDto.kt
package com.shop.product.service

import com.shop.product.domain.Product
import com.shop.product.domain.ProductStatus
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.Instant

// Request DTOs
data class CreateProductRequest(
        @field:NotBlank(message = "상품 코드는 필수입니다")
        @field:Size(min = 3, max = 20, message = "상품 코드는 3~20자여야 합니다")
        @field:Pattern(regexp = "^PRD-[0-9]{6}$", message = "상품 코드 형식: PRD-XXXXXX")
        val productCode: String,
        @field:NotBlank(message = "상품명은 필수입니다")
        @field:Size(min = 2, max = 100, message = "상품명은 2~100자여야 합니다")
        @field:Pattern(regexp = "^[가-힣a-zA-Z0-9\\s!@#]+$", message = "허용되지 않은 문자가 포함되어 있습니다")
        val productName: String,
        @field:Size(max = 1000, message = "설명은 1000자 이하여야 합니다") val description: String? = null,
        @field:NotNull(message = "가격은 필수입니다")
        @field:DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다")
        @field:DecimalMax(value = "100000000.0", message = "가격은 1억 이하여야 합니다")
        val price: BigDecimal,
        @field:Min(value = 0, message = "재고는 0 이상이어야 합니다") val stock: Int = 0,
        @field:NotNull(message = "판매자 ID는 필수입니다") val sellerId: Long,
        @field:NotNull(message = "카테고리 ID는 필수입니다") val categoryId: Long
)

data class UpdateProductRequest(
        @field:Size(min = 2, max = 100, message = "상품명은 2~100자여야 합니다")
        val productName: String? = null,
        @field:Size(max = 1000, message = "설명은 1000자 이하여야 합니다") val description: String? = null,
        @field:DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다")
        @field:DecimalMax(value = "100000000.0", message = "가격은 1억 이하여야 합니다")
        val price: BigDecimal? = null,
        val version: Long
)

data class AdjustStockRequest(
        @field:NotBlank(message = "조정 유형은 필수입니다")
        val adjustmentType: String, // INCREASE, DECREASE, CORRECTION
        @field:Min(value = 1, message = "수량은 1 이상이어야 합니다") val quantity: Int,
        @field:Size(max = 200, message = "사유는 200자 이하여야 합니다") val reason: String? = null
)

// Response DTOs
data class ProductResponse(
        val id: Long,
        val productCode: String,
        val productName: String,
        val description: String?,
        val price: BigDecimal,
        val stock: Int,
        val status: ProductStatus,
        val sellerId: Long,
        val categoryId: Long,
        val viewCount: Long,
        val version: Long,
        val createdAt: Instant,
        val updatedAt: Instant
) {
    companion object {
        fun from(product: Product) =
                ProductResponse(
                        id = product.id,
                        productCode = product.productCode,
                        productName = product.productName,
                        description = product.description,
                        price = product.price,
                        stock = product.stock,
                        status = product.status,
                        sellerId = product.sellerId,
                        categoryId = product.categoryId,
                        viewCount = product.viewCount,
                        version = product.version,
                        createdAt = product.createdAt,
                        updatedAt = product.updatedAt
                )
    }
}

data class StockHistoryResponse(
        val id: Long,
        val productCode: String,
        val adjustmentType: String,
        val quantity: Int,
        val previousStock: Int,
        val currentStock: Int,
        val reason: String?,
        val adjustedBy: Long,
        val adjustedAt: Instant
)

data class PageResponse<T>(
        val content: List<T>,
        val totalElements: Long,
        val totalPages: Int,
        val currentPage: Int,
        val size: Int
)
