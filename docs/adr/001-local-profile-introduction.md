# ADR-001: Local 프로파일 도입

**날짜**: 2026-02-06  
**상태**: ✅ Accepted  
**결정자**: Development Team, QA Team

---

## Context

API 테스트 과정에서 Spring Security 인증으로 인해 모든 API 요청이 `403 Forbidden`을 반환하는 문제가 발생했다. 로컬 개발 환경에서 빠른 테스트와 개발을 위해 인증을 우회할 방법이 필요했다.

### 문제점
- API 테스트 시 매번 인증 토큰 필요
- JWT 구현 전까지 API 테스트 불가
- 개발 생산성 저하
- REST Client 테스트 어려움

---

## Decision

로컬 개발 환경 전용 Spring Profile(`local`)을 도입하고, 해당 프로파일에서는 Spring Security를 비활성화한다.

### 구현 내용
1. `application-local.yml` 생성
2. `LocalSecurityConfig.kt` 생성 (`@Profile("local")`)
   - 모든 요청 허용 (`permitAll()`)
   - CSRF 비활성화
   - CORS 비활성화
3. `SecurityConfig.kt` 수정 (`@Profile("!local")`)
   - local 프로파일이 아닐 때만 활성화

---

## Consequences

### Positive
- ✅ 로컬 개발 환경에서 즉시 API 테스트 가능
- ✅ REST Client 파일로 간편한 테스트
- ✅ 개발 생산성 향상
- ✅ JWT 구현 전에도 기능 검증 가능
- ✅ 프로덕션 Security 설정과 명확히 분리

### Negative
- ⚠️ 프로파일 관리 복잡도 증가
- ⚠️ 실수로 프로덕션에 local 프로파일 사용 가능성
- ⚠️ 환경별 설정 파일 증가

### Risks
- **프로덕션 오배포**: local 프로파일로 배포 시 보안 취약
  - **완화책**: CI/CD에서 프로파일 검증 추가
  - **완화책**: 프로덕션 환경 변수 강제 설정

---

## Alternatives Considered

### 1. TestSecurityConfig만 사용
- **장점**: 테스트 환경에서만 Security 비활성화
- **단점**: 로컬 개발 시 매번 테스트 모드로 실행 필요
- **결론**: 개발 편의성 부족으로 기각

### 2. JWT 먼저 구현
- **장점**: 프로덕션과 동일한 환경
- **단점**: 개발 일정 지연 (4시간 추가)
- **결론**: 빠른 검증이 우선이므로 기각

### 3. Basic Auth 임시 사용
- **장점**: 간단한 인증 구현
- **단점**: 프로덕션과 다른 인증 방식
- **결론**: 일관성 부족으로 기각

---

## Implementation

```kotlin
// LocalSecurityConfig.kt
@Configuration
@EnableWebSecurity
@Profile("local")
class LocalSecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
        return http.build()
    }
}

// SecurityConfig.kt
@Configuration
@EnableWebSecurity
@Profile("!local")  // local이 아닐 때만 활성화
class SecurityConfig { ... }
```

---

## References

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [Spring Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- QA Test Report v2.0
- LOCAL_PROFILE_GUIDE.md

---

**작성자**: Artifact Manager  
**검토자**: Development Team, QA Team
