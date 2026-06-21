package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.swmanager.system.domain.ops.OpsKb;

/**
 * /ops-kb/api/list (검색) · /ops-kb/api/{id} (단건) 조회 응답 dto.
 *
 * 기존 {@code toDto()} 의 비타입 {@code Map}(LinkedHashMap) 동적 조립을 타입 record 로 대체한다
 * (§6-4 opskb-todto-record). 소비자(kb-list.html JS · doc-fault/support/patch/install 추천)는 JSON 키로
 * 접근하므로 키·값·null 포함 동작을 무손실 보존한다.
 *
 * <p><b>무손실 설계</b>:
 * <ul>
 *   <li>전역 PropertyNamingStrategy 미설정 → 다어절 키({@code kb_id} 등)만 {@link JsonProperty} 매핑,
 *       단일어 키(gubun/category/…)는 컴포넌트명=JSON키.</li>
 *   <li>{@code @JsonInclude} 미부착 → 현행 LinkedHashMap 이 17키를 null 값이어도 항상 담던 동작을 동치 보존
 *       (전역 jackson default-property-inclusion 미설정 = null 포함).</li>
 *   <li>{@link JsonPropertyOrder} 로 현행 put 순(kb_id…created_by)을 고정 — Jackson 이 {@code @JsonProperty}
 *       부착 컴포넌트를 앞으로 재정렬하는 기본 동작을 눌러 바이트 키순서까지 보존.</li>
 *   <li>{@code action} 은 현행과 동일하게 공백이면 {@code causeDesc} 로 폴백.</li>
 * </ul>
 */
@JsonPropertyOrder({"kb_id", "kb_code", "gubun", "sys_type", "category", "symptom", "cause",
        "summary", "action", "prevention", "keywords", "case_count", "source", "status",
        "reject_reason", "reviewed_by", "created_by"})
public record OpsKbDto(
        @JsonProperty("kb_id") Long kbId,
        @JsonProperty("kb_code") String kbCode,
        String gubun,
        @JsonProperty("sys_type") String sysType,
        String category,
        String symptom,
        String cause,
        String summary,
        String action,
        String prevention,
        String keywords,
        @JsonProperty("case_count") Integer caseCount,
        String source,
        String status,
        @JsonProperty("reject_reason") String rejectReason,
        @JsonProperty("reviewed_by") String reviewedBy,
        @JsonProperty("created_by") String createdBy) {

    /** 엔티티 → 응답 dto. 현행 {@code toDto()} 와 1:1 동치(action 폴백 포함). */
    public static OpsKbDto from(OpsKb k) {
        String action = (k.getAction() != null && !k.getAction().isBlank()) ? k.getAction() : k.getCauseDesc();
        return new OpsKbDto(
                k.getKbId(), k.getKbCode(), k.getGubun(), k.getSysType(), k.getCategory(),
                k.getSymptom(), k.getCause(), k.getSummary(), action, k.getPrevention(),
                k.getKeywords(), k.getCaseCount(), k.getSource(), k.getStatus(),
                k.getRejectReason(), k.getReviewedBy(), k.getCreatedBy());
    }
}
