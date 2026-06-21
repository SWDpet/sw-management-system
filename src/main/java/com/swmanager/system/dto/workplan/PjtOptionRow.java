package com.swmanager.system.dto.workplan;

/**
 * /api/pjt-by-region (시군구 → 사업 목록) 응답 행 dto.
 *
 * 기존 WorkPlanController 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 doclookup-workplan-rows-dto). 키셋·값 동치로 무손실. camelCase 키=컴포넌트명.
 * label(연도+사업명) 조립 로직은 호출부(컨트롤러)에 유지하고 record 는 값만 보유한다.
 */
public record PjtOptionRow(Long projId, String label) {
}
