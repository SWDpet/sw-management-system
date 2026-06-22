package com.swmanager.system.dto.workplan;

/**
 * /document/api/save 문서 저장 성공(200) 응답 dto — `{success:true, docId, docNo}`.
 *
 * 기존 DocumentController.saveDocument 의 컨트롤러-로컬 HashMap 성공 응답조립을 record 로 대체한다
 * (§6-4 document-save-result-dto). 키셋·값 동치로 무손실. docNo 는 현행 fallback(null→"")을 보존한다.
 * (403/400/500 등 실패 경로는 Map.of 그대로 보존, 요청바디는 lenient 파싱 보존)
 */
public record DocumentSaveResult(boolean success, Integer docId, String docNo) {
}
