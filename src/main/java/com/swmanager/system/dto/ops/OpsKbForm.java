package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * /ops-kb/api (POST 등록 / PUT 수정) 요청 바디.
 *
 * 기존 {@code @RequestBody} 비타입 Map 수신을 타입 DTO 로 대체한다(§6-4 opskb-request-dto).
 * 프론트(kb-form.html:76)가 보내는 JSON 키를 무손실 보존한다.
 * 전역 PropertyNamingStrategy 미설정이라 snake_case 키({@code sys_type})만 {@link JsonProperty} 로 매핑하고,
 * 단일어 키(gubun/symptom/…)는 컴포넌트명=JSON키로 자동 바인딩한다.
 *
 * <p>{@code category} 는 폼에서 전송하지 않으나(수정 시 기존 분류 보존), 외부 수신 호환을 위해 필드를 유지한다.
 * 기존 Map 수신은 미지 키를 무시했으므로 {@link JsonIgnoreProperties}{@code (ignoreUnknown=true)} 로 동일 관용을 보장한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpsKbForm(
        @JsonProperty("sys_type") String sysType,
        String gubun,
        String symptom,
        String cause,
        String action,
        String keywords,
        String prevention,
        String category) {
}
