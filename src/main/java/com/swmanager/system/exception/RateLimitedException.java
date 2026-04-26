package com.swmanager.system.exception;

/**
 * 분당 재연결 한도 초과 → 429.
 * (sprint team-monitor-dashboard, 개발계획 §R-3-d / §NFR-2-b)
 */
public class RateLimitedException extends RuntimeException {
    private final int retryAfterSec;
    public RateLimitedException(int retryAfterSec) {
        super("rate limit exceeded");
        this.retryAfterSec = retryAfterSec;
    }
    public int getRetryAfterSec() { return retryAfterSec; }
}
