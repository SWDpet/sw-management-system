package com.swmanager.system.exception;

/**
 * SSE 연결 시 Origin 헤더가 화이트리스트에 없음 → 403.
 * (sprint team-monitor-dashboard, 개발계획 §R-5)
 */
public class OriginNotAllowedException extends RuntimeException {
    public OriginNotAllowedException(String origin) {
        super("origin not allowed: " + origin);
    }
}
