package com.swmanager.system.dto.workplan;

/**
 * /document/api/project-systems-all (연도별 전체 시스템) 응답 행 dto.
 *
 * 기존 DocumentController.getAllSystemsForYear 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 document-batch-rows-dto). 키셋·값 동치로 무손실. sysNmEn 오름차순 정렬은 호출부에서 유지.
 */
public record SystemAllRow(String sysNmEn, String sysNm) {
}
