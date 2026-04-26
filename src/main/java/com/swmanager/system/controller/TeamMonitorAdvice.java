package com.swmanager.system.controller;

import com.swmanager.system.exception.OriginNotAllowedException;
import com.swmanager.system.exception.OverflowRejectedException;
import com.swmanager.system.exception.RateLimitedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * team-monitor 전용 예외 → HTTP 응답 매핑.
 * (sprint team-monitor-dashboard, 개발계획 Step 4-2)
 */
@RestControllerAdvice(basePackages = "com.swmanager.system.controller",
        assignableTypes = TeamMonitorController.class)
public class TeamMonitorAdvice {

    @ExceptionHandler(OverflowRejectedException.class)
    public ResponseEntity<Map<String, Object>> onOverflow(OverflowRejectedException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "sse_capacity");
        body.put("maxEmitters", e.getMaxEmitters());
        body.put("retryAfterSec", 30);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "30")
                .body(body);
    }

    @ExceptionHandler(RateLimitedException.class)
    public ResponseEntity<Map<String, Object>> onRateLimit(RateLimitedException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "sse_rate_limited");
        body.put("retryAfterSec", e.getRetryAfterSec());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(e.getRetryAfterSec()))
                .body(body);
    }

    @ExceptionHandler(OriginNotAllowedException.class)
    public ResponseEntity<Map<String, Object>> onOrigin(OriginNotAllowedException e) {
        Map<String, Object> body = Map.of("error", "forbidden_origin");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}
