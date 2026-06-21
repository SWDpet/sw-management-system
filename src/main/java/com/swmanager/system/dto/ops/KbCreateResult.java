package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swmanager.system.domain.ops.OpsKb;

/**
 * KB 등록(create) 응답 dto — {@code {success, kb_id, kb_code, status}} (P6).
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 opskb-create-result-record).
 * snake_case 키({@code kb_id}/{@code kb_code})는 {@link JsonProperty} 로 보존 — 무손실.
 * (프론트 kb-form.html 은 success/status 만 소비하나 헌장 §0 무손실로 4키 전부 유지.)
 */
public record KbCreateResult(
        boolean success,
        @JsonProperty("kb_id") Long kbId,
        @JsonProperty("kb_code") String kbCode,
        String status) {

    public static KbCreateResult of(OpsKb saved) {
        return new KbCreateResult(true, saved.getKbId(), saved.getKbCode(), saved.getStatus());
    }
}
