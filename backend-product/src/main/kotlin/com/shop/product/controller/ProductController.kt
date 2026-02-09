// File: src/main/kotlin/com/shop/product/controller/ProductController.kt
package com.shop.product.controller

import com.shop.product.service.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Product", description = "상품 관리 API")
@RestController
@RequestMapping("/api/products")
class ProductController(private val productService: ProductService) {

    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다")
    @PostMapping
    fun createProduct(
            @Valid @RequestBody request: CreateProductRequest,
            @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ProductResponse> {
        val response = productService.createProduct(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(summary = "상품 조회", description = "상품 코드로 상품을 조회합니다")
    @GetMapping("/{productCode}")
    fun getProduct(@PathVariable productCode: String): ResponseEntity<ProductResponse> {
        val response = productService.getProduct(productCode)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "상품 목록 조회", description = "전체 상품 목록을 페이징하여 조회합니다")
    @GetMapping
    fun getProducts(
            @RequestParam(defaultValue = "0") page: Int,
            @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<ProductResponse>> {
        val pageable = PageRequest.of(page, size)
        val response = productService.getProducts(pageable)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다")
    @PutMapping("/{productCode}")
    fun updateProduct(
            @PathVariable productCode: String,
            @Valid @RequestBody request: UpdateProductRequest,
            @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ProductResponse> {
        val response = productService.updateProduct(productCode, request, userId)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "상품 삭제", description = "상품을 논리적으로 삭제합니다")
    @DeleteMapping("/{productCode}")
    fun deleteProduct(
            @PathVariable productCode: String,
            @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<Void> {
        productService.deleteProduct(productCode, userId)
        return ResponseEntity.noContent().build()
    }
}

@Tag(name = "Stock", description = "재고 관리 API")
@RestController
@RequestMapping("/api/stock")
class StockController(private val productService: ProductService) {

    @Operation(summary = "재고 조정", description = "상품의 재고를 조정합니다")
    @PostMapping("/{productCode}/adjust")
    fun adjustStock(
            @PathVariable productCode: String,
            @Valid @RequestBody request: AdjustStockRequest,
            @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ProductResponse> {
        val response = productService.adjustStock(productCode, request, userId)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "재고 이력 조회", description = "상품의 재고 변경 이력을 조회합니다")
    @GetMapping("/{productCode}/history")
    fun getStockHistory(
            @PathVariable productCode: String,
            @RequestParam(defaultValue = "0") page: Int,
            @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<StockHistoryResponse>> {
        val pageable = PageRequest.of(page, size)
        val response = productService.getStockHistory(productCode, pageable)
        return ResponseEntity.ok(response)
    }
}
