package com.swmanager.system.util;

/**
 * 예외 메시지 null-안전 헬퍼.
 *
 * <p>{@code java.util.Map.of("error", e.getMessage())} 처럼 null 값을 허용하지 않는 응답 빌더에서
 * {@code getMessage()==null}(예: NPE) 이면 NPE 가 재발생해 정상 에러 응답이 500/배치 전체 실패로
 * 둔갑한다. 본 헬퍼로 message 가 없으면 예외 클래스 단순명으로 대체한다(harden-document-batch-upload-nullsafe).
 */
public final class ExceptionMessages {

    private ExceptionMessages() {}

    /** message 가 있으면 그대로, 없으면 예외 클래스 단순명(둘 다 불가하면 빈 문자열). */
    public static String safe(Throwable e) {
        if (e == null) return "";
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
