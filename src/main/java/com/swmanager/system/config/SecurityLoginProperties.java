package com.swmanager.system.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 로그인 실패·잠금 정책 설정.
 *
 * 기획서 FR-B4 / 개발계획서 §1 Step 4:
 *   - `security.login.max-failed-attempts` (기본 5, 범위 1~100)
 *   - `security.login.lock-time-minutes`   (기본 15, 범위 1~1440 = 24h)
 *
 * NFR-7: `@Validated` 위반 시 앱 부팅 실패 (예: `max-failed-attempts=0`).
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.login")
public class SecurityLoginProperties {

    @Min(1)
    @Max(100)
    private int maxFailedAttempts = 5;

    @Min(1)
    @Max(1440)
    private int lockTimeMinutes = 15;
}
