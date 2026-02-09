// File: src/main/kotlin/com/shop/product/exception/GlobalExceptionHandler.kt
package com.shop.product.exception

import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFound(ex: ProductNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("Product not found: {}", ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse(
                                error = "E002",
                                message = ex.message ?: "존재하지 않는 상품입니다",
                                timestamp = Instant.now()
                        )
                )
    }

    @ExceptionHandler(ProductCodeDuplicateException::class)
    fun handleProductCodeDuplicate(
            ex: ProductCodeDuplicateException
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Duplicate product code: {}", ex.message)
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        ErrorResponse(
                                error = "E003",
                                message = ex.message ?: "이미 존재하는 상품 코드입니다",
                                timestamp = Instant.now()
                        )
                )
    }

    @ExceptionHandler(InsufficientStockException::class)
    fun handleInsufficientStock(ex: InsufficientStockException): ResponseEntity<ErrorResponse> {
        logger.warn("Insufficient stock: {}", ex.message)
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        ErrorResponse(
                                error = "E004",
                                message = ex.message ?: "재고가 부족합니다",
                                timestamp = Instant.now()
                        )
                )
    }

    @ExceptionHandler(VersionConflictException::class)
    fun handleVersionConflict(ex: VersionConflictException): ResponseEntity<ErrorResponse> {
        logger.warn("Version conflict: {}", ex.message)
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        ErrorResponse(
                                error = "VERSION_CONFLICT",
                                message = ex.message ?: "다른 사용자가 먼저 수정했습니다",
                                timestamp = Instant.now()
                        )
                )
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(ex: UnauthorizedException): ResponseEntity<ErrorResponse> {
        logger.warn("Unauthorized access: {}", ex.message)
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(
                        ErrorResponse(
                                error = "E006",
                                message = ex.message ?: "권한이 없습니다",
                                timestamp = Instant.now()
                        )
                )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
            ex: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        logger.warn("Validation failed: {}", errors)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorResponse(
                                error = "E001",
                                message = errors.joinToString(", "),
                                timestamp = Instant.now()
                        )
                )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ErrorResponse(
                                error = "INTERNAL_ERROR",
                                message = "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                timestamp = Instant.now()
                        )
                )
    }
}

data class ErrorResponse(val error: String, val message: String, val timestamp: Instant)
