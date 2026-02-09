// File: src/main/kotlin/com/shop/product/service/ProductService.kt
package com.shop.product.service

import com.shop.product.domain.*
import com.shop.product.exception.*
import java.math.BigDecimal
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProductService(
        private val productRepository: ProductRepository,
        private val stockHistoryRepository: StockHistoryRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createProduct(request: CreateProductRequest): ProductResponse {
        logger.info("Creating product: productCode={}", request.productCode)

        // 1. 중복 체크
        if (productRepository.existsByProductCode(request.productCode)) {
            throw ProductCodeDuplicateException("이미 존재하는 상품 코드입니다: ${request.productCode}")
        }

        // 2. 가격 검증 (100원 단위)
        require(request.price.remainder(BigDecimal(100)) == BigDecimal.ZERO) { "가격은 100원 단위여야 합니다" }

        // 3. XSS 방지: HTML 이스케이프
        val sanitizedName = StringEscapeUtils.escapeHtml4(request.productName)
        val sanitizedDescription = request.description?.let { StringEscapeUtils.escapeHtml4(it) }

        // 4. 엔티티 생성 및 저장
        val product =
                Product(
                        productCode = request.productCode,
                        productName = sanitizedName,
                        description = sanitizedDescription,
                        price = request.price,
                        stock = request.stock,
                        sellerId = request.sellerId,
                        categoryId = request.categoryId
                )

        val savedProduct = productRepository.save(product)

        // 5. 재고 이력 생성 (초기 재고)
        if (request.stock > 0) {
            val history =
                    StockHistory(
                            productCode = request.productCode,
                            adjustmentType = AdjustmentType.INCREASE,
                            quantity = request.stock,
                            previousStock = 0,
                            currentStock = request.stock,
                            reason = "초기 재고",
                            adjustedBy = request.sellerId
                    )
            stockHistoryRepository.save(history)
        }

        logger.info("Product created successfully: productCode={}", request.productCode)
        return ProductResponse.from(savedProduct)
    }

    fun getProduct(productCode: String): ProductResponse {
        val product =
                productRepository.findByProductCode(productCode)
                        ?: throw ProductNotFoundException("상품을 찾을 수 없습니다: $productCode")

        if (product.isDeleted()) {
            throw ProductNotFoundException("삭제된 상품입니다: $productCode")
        }

        return ProductResponse.from(product)
    }

    fun getProducts(pageable: Pageable): PageResponse<ProductResponse> {
        val page = productRepository.findAllActive(pageable)
        return PageResponse(
                content = page.content.map { ProductResponse.from(it) },
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                currentPage = page.number,
                size = page.size
        )
    }

    @Transactional
    fun updateProduct(
            productCode: String,
            request: UpdateProductRequest,
            userId: Long
    ): ProductResponse {
        logger.info("Updating product: productCode={}, userId={}", productCode, userId)

        val product =
                productRepository.findByProductCode(productCode)
                        ?: throw ProductNotFoundException("상품을 찾을 수 없습니다: $productCode")

        // 권한 검증 (본인 상품만 수정 가능)
        if (product.sellerId != userId) {
            throw UnauthorizedException("본인의 상품만 수정할 수 있습니다")
        }

        // 가격 검증
        request.price?.let { price ->
            require(price.remainder(BigDecimal(100)) == BigDecimal.ZERO) { "가격은 100원 단위여야 합니다" }
        }

        // XSS 방지
        val sanitizedName = request.productName?.let { StringEscapeUtils.escapeHtml4(it) }
        val sanitizedDescription = request.description?.let { StringEscapeUtils.escapeHtml4(it) }

        try {
            product.update(sanitizedName, sanitizedDescription, request.price)
            val updatedProduct = productRepository.save(product)

            logger.info("Product updated successfully: productCode={}", productCode)
            return ProductResponse.from(updatedProduct)
        } catch (ex: ObjectOptimisticLockingFailureException) {
            logger.warn("Version conflict detected: productCode={}", productCode)
            throw VersionConflictException("다른 사용자가 먼저 수정했습니다. 다시 시도해주세요")
        }
    }

    @Transactional
    fun deleteProduct(productCode: String, userId: Long) {
        logger.info("Deleting product: productCode={}, userId={}", productCode, userId)

        val product =
                productRepository.findByProductCode(productCode)
                        ?: throw ProductNotFoundException("상품을 찾을 수 없습니다: $productCode")

        // 권한 검증
        if (product.sellerId != userId) {
            throw UnauthorizedException("본인의 상품만 삭제할 수 있습니다")
        }

        product.delete()
        productRepository.save(product)

        logger.info("Product deleted successfully: productCode={}", productCode)
    }

    @Transactional
    fun adjustStock(
            productCode: String,
            request: AdjustStockRequest,
            userId: Long
    ): ProductResponse {
        logger.info(
                "Adjusting stock: productCode={}, type={}, quantity={}",
                productCode,
                request.adjustmentType,
                request.quantity
        )

        // 비관적 락 사용 (동시성 제어)
        val product =
                productRepository.findByProductCodeWithLock(productCode)
                        ?: throw ProductNotFoundException("상품을 찾을 수 없습니다: $productCode")

        val adjustmentType = AdjustmentType.valueOf(request.adjustmentType)
        val previousStock = product.stock

        // 재고 조정
        val adjustmentQuantity =
                when (adjustmentType) {
                    AdjustmentType.INCREASE -> request.quantity
                    AdjustmentType.DECREASE -> -request.quantity
                    AdjustmentType.CORRECTION -> request.quantity - previousStock
                }

        try {
            product.adjustStock(adjustmentQuantity)
        } catch (ex: IllegalArgumentException) {
            throw InsufficientStockException("재고가 부족합니다. 현재 재고: $previousStock")
        }

        // 재고 상태 자동 변경
        if (product.stock == 0) {
            product.changeStatus(ProductStatus.SOLD_OUT)
        } else if (product.status == ProductStatus.SOLD_OUT && product.stock > 0) {
            product.changeStatus(ProductStatus.ACTIVE)
        }

        val updatedProduct = productRepository.save(product)

        // 재고 이력 저장
        val history =
                StockHistory(
                        productCode = productCode,
                        adjustmentType = adjustmentType,
                        quantity = request.quantity,
                        previousStock = previousStock,
                        currentStock = product.stock,
                        reason = request.reason,
                        adjustedBy = userId
                )
        stockHistoryRepository.save(history)

        logger.info(
                "Stock adjusted successfully: productCode={}, newStock={}",
                productCode,
                product.stock
        )
        return ProductResponse.from(updatedProduct)
    }

    fun getStockHistory(
            productCode: String,
            pageable: Pageable
    ): PageResponse<StockHistoryResponse> {
        val page = stockHistoryRepository.findRecentHistory(productCode, pageable)
        return PageResponse(
                content =
                        page.content.map {
                            StockHistoryResponse(
                                    id = it.id,
                                    productCode = it.productCode,
                                    adjustmentType = it.adjustmentType.name,
                                    quantity = it.quantity,
                                    previousStock = it.previousStock,
                                    currentStock = it.currentStock,
                                    reason = it.reason,
                                    adjustedBy = it.adjustedBy,
                                    adjustedAt = it.adjustedAt
                            )
                        },
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                currentPage = page.number,
                size = page.size
        )
    }
}
