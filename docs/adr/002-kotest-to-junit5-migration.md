# ADR-002: Kotest에서 JUnit 5로 마이그레이션

**날짜**: 2026-02-06  
**상태**: ✅ Accepted  
**결정자**: Development Team

---

## Context

프로젝트 초기에 Kotest를 테스트 프레임워크로 선택했으나, 테스트 실행 시 `UnsupportedClassVersionError`가 발생했다. 테스트 클래스는 Java 21로 컴파일되었지만, Gradle이 Java 17로 테스트를 실행하려고 시도하면서 버전 불일치 문제가 발생했다.

### 문제점
- Kotest 5.8.0이 Java 21과 호환성 문제
- Gradle Toolchain 설정에도 불구하고 버전 충돌
- 모든 테스트 실행 실패 (12/12 failed)
- 빌드 시간 지연

### 에러 메시지
```
UnsupportedClassVersionError: com/shop/product/domain/ProductTest 
has been compiled by a more recent version of the Java Runtime 
(class file version 65.0), this version of the Java Runtime only 
recognizes class file versions up to 61.0
```

---

## Decision

Kotest를 JUnit 5로 마이그레이션하고, MockK를 모킹 라이브러리로 유지한다.

### 변경 내용
1. `build.gradle.kts` 의존성 변경
   - Kotest 제거
   - JUnit 5 추가
   - MockK 유지

2. 테스트 코드 변환
   - `ProductTest.kt`: Kotest → JUnit 5 (7개 테스트)
   - `ProductServiceTest.kt`: Kotest → JUnit 5 (5개 테스트)

3. Java Toolchain 명시
   ```kotlin
   java {
       toolchain {
           languageVersion = JavaLanguageVersion.of(21)
       }
   }
   ```

---

## Consequences

### Positive
- ✅ 모든 테스트 정상 실행 (12/12 passed, 100%)
- ✅ Java 21 완벽 호환
- ✅ Spring Boot 3.2와 안정적 통합
- ✅ 더 넓은 커뮤니티 지원
- ✅ 빌드 시간 단축

### Negative
- ⚠️ Kotest의 DSL 스타일 포기
- ⚠️ 테스트 코드 재작성 필요 (2시간 소요)
- ⚠️ Kotest 특화 기능 사용 불가

### Neutral
- 🔄 MockK는 계속 사용 (Kotlin 친화적)
- 🔄 테스트 가독성은 유사한 수준 유지

---

## Alternatives Considered

### 1. Kotest 버전 업그레이드
- **장점**: 기존 코드 유지
- **단점**: 최신 버전도 Java 21 호환성 불확실
- **결론**: 안정성 부족으로 기각

### 2. Java 17로 다운그레이드
- **장점**: Kotest 호환성 확보
- **단점**: Spring Boot 3.2 최신 기능 사용 불가
- **결론**: 기술 스택 목표와 불일치로 기각

### 3. Spock Framework
- **장점**: BDD 스타일 지원
- **단점**: Groovy 의존성 추가
- **결론**: Kotlin 프로젝트와 맞지 않아 기각

---

## Implementation

### Before (Kotest)
```kotlin
class ProductTest : BehaviorSpec({
    given("상품 생성") {
        `when`("유효한 데이터") {
            then("성공") {
                // test code
            }
        }
    }
})
```

### After (JUnit 5)
```kotlin
@DisplayName("Product 도메인 테스트")
class ProductTest {
    @Test
    @DisplayName("상품 생성 - 유효한 데이터")
    fun `should create product with valid data`() {
        // test code
    }
}
```

---

## Metrics

| 지표 | Before (Kotest) | After (JUnit 5) |
|------|----------------|-----------------|
| 테스트 성공률 | 0% (12/12 failed) | 100% (12/12 passed) |
| 빌드 시간 | 실패 | 8초 |
| 코드 라인 수 | 약간 적음 | 약간 많음 |
| 가독성 | 우수 | 양호 |

---

## References

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Kotest Issue Tracker](https://github.com/kotest/kotest/issues)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- Gradle Build Logs
- Test Execution Reports

---

**작성자**: Artifact Manager  
**검토자**: Development Team
