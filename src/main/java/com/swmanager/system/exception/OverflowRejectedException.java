package com.swmanager.system.exception;

/**
 * SSE 동시 연결 상한 도달 → reject 모드에서 던짐.
 * (sprint team-monitor-dashboard, 개발계획 §NFR-2-b)
 */
public class OverflowRejectedException extends RuntimeException {
    private final int maxEmitters;
    public OverflowRejectedException(int maxEmitters) {
        super("SSE capacity reached: " + maxEmitters);
        this.maxEmitters = maxEmitters;
    }
    public int getMaxEmitters() { return maxEmitters; }
}
