// File: src/main/kotlin/com/shop/product/config/SecurityConfig.kt
package com.shop.product.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!local") // local 프로파일이 아닐 때만 활성화
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
                .csrf { it.disable() } // REST API는 CSRF 비활성화 (stateless)
                .cors { it.configurationSource(corsConfigurationSource()) }
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .authorizeHttpRequests { auth ->
                    auth
                            // Public endpoints
                            .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                            .permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/products/**")
                            .permitAll()

                            // Authenticated endpoints
                            .requestMatchers(HttpMethod.POST, "/api/products")
                            .authenticated()
                            .requestMatchers(HttpMethod.PUT, "/api/products/**")
                            .authenticated()
                            .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                            .authenticated()
                            .requestMatchers("/api/stock/**")
                            .authenticated()
                            .anyRequest()
                            .authenticated()
                }
                .headers { headers ->
                    headers
                            .contentSecurityPolicy { csp ->
                                csp.policyDirectives(
                                        "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"
                                )
                            }
                            .xssProtection {}
                            .frameOptions { it.deny() }
                }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:3000", "http://localhost:8080")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
