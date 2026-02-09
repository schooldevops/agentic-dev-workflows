// File: src/main/kotlin/com/shop/product/exception/Exceptions.kt
package com.shop.product.exception

class ProductNotFoundException(message: String = "존재하지 않는 상품입니다") : RuntimeException(message)

class ProductCodeDuplicateException(message: String = "이미 존재하는 상품 코드입니다") :
        RuntimeException(message)

class InsufficientStockException(message: String = "재고가 부족합니다") : RuntimeException(message)

class InvalidPriceException(message: String = "유효하지 않은 가격입니다") : RuntimeException(message)

class UnauthorizedException(message: String = "권한이 없습니다") : RuntimeException(message)

class VersionConflictException(message: String = "다른 사용자가 먼저 수정했습니다. 다시 시도해주세요") :
        RuntimeException(message)
