package com.swmanager.system.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityLoginProperties @Validated 경계값 검증 단위 테스트.
 * 개발계획서 v2 §1 Step 5 / NFR-7 (표현 정정: property=0 위반).
 *
 * Spring Boot 전체 부팅 없이 순수 Jakarta Bean Validation 으로 제약을 검증 — 빠르고 격리.
 * 실제 부팅 실패 확인은 런타임 스모크(T7)에서 수동 검증.
 */
class SecurityLoginPropertiesValidationTest {

    private final Validator validator = factory().getValidator();

    private static ValidatorFactory factory() {
        return Validation.buildDefaultValidatorFactory();
    }

    @Test
    void defaults_valid() {
        SecurityLoginProperties p = new SecurityLoginProperties();
        assertThat(p.getMaxFailedAttempts()).isEqualTo(5);
        assertThat(p.getLockTimeMinutes()).isEqualTo(15);
        Set<ConstraintViolation<SecurityLoginProperties>> violations = validator.validate(p);
        assertThat(violations).isEmpty();
    }

    @Test
    void maxFailedAttempts_zero_violates_Min() {
        SecurityLoginProperties p = new SecurityLoginProperties();
        p.setMaxFailedAttempts(0);  // property=0 위반
        Set<ConstraintViolation<SecurityLoginProperties>> violations = validator.validate(p);
        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("maxFailedAttempts");
    }

    @Test
    void maxFailedAttempts_over100_violates_Max() {
        SecurityLoginProperties p = new SecurityLoginProperties();
        p.setMaxFailedAttempts(101);
        Set<ConstraintViolation<SecurityLoginProperties>> violations = validator.validate(p);
        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("maxFailedAttempts");
    }

    @Test
    void lockTimeMinutes_zero_violates_Min() {
        SecurityLoginProperties p = new SecurityLoginProperties();
        p.setLockTimeMinutes(0);
        Set<ConstraintViolation<SecurityLoginProperties>> violations = validator.validate(p);
        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("lockTimeMinutes");
    }

    @Test
    void lockTimeMinutes_over1440_violates_Max() {
        SecurityLoginProperties p = new SecurityLoginProperties();
        p.setLockTimeMinutes(1441);
        Set<ConstraintViolation<SecurityLoginProperties>> violations = validator.validate(p);
        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("lockTimeMinutes");
    }
}
