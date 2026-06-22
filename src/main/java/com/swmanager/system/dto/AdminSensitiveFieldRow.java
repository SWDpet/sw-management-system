package com.swmanager.system.dto;

/**
 * /admin/users/api/{userSeq}/sensitive 민감필드 평문 조회 성공(200) 응답 dto — `{field, value}`.
 *
 * 기존 AdminUserController.getSensitiveField 의 컨트롤러-로컬 LinkedHashMap 응답조립을 record 로
 * 대체한다(§6-4 qrbatch-adminuser-rows-dto). 키셋·값 동치로 무손실. 단일 필드만 반환(일괄수집 차단)하는
 * 화이트리스트 응답이므로 신규 키를 추가하지 않는다. {@code value} 는 현행 fallback(null→"")을 보존한다.
 */
public record AdminSensitiveFieldRow(String field, String value) {
}
