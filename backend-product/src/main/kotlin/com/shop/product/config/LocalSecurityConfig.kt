// File: src/main/kotlin/com/shop/product/config/LocalSecurityConfig.kt
package com.shop.product.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Local 개발 환경용 Security 설정 모든 요청을 인증 없이 허용하여 API 테스트를 용이하게 함
 *
 * 주의: 이 설정은 local 프로파일에서만 활성화됩니다. 프로덕션 환경에서는 절대 사용하지 마세요!
 */
@Configuration
@EnableWebSecurity
@Profile("local")
class LocalSecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
                .csrf { it.disable() }
                .cors { it.disable() }
                .authorizeHttpRequests { auth -> auth.anyRequest().permitAll() }
                .headers { headers ->
                    headers.frameOptions { it.sameOrigin() }.contentSecurityPolicy { csp ->
                        csp.policyDirectives(
                                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"
                        )
                    }
                }

        return http.build()
    }
}
