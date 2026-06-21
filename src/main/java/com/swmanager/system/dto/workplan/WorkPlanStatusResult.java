package com.swmanager.system.dto.workplan;

/**
 * /api/status/{id} 상태변경 성공(200) 응답 dto — `{success:true, planId, status, statusLabel}`.
 *
 * 기존 WorkPlanController 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 doclookup-workplan-rows-dto). 키셋·값 동치로 무손실. camelCase 키=컴포넌트명.
 * 403(`{error:...}`)은 형태가 달라 record 대상 아님(호출부에서 Map.of 유지).
 */
public record WorkPlanStatusResult(boolean success, Integer planId, String status, String statusLabel) {
}
