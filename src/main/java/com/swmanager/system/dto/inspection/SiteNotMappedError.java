package com.swmanager.system.dto.inspection;

/**
 * inspection-qr-batch site_not_mapped 예외(422) 응답 dto.
 *
 * 기존 InspectionQrBatchController.handleSiteNotMapped 의 컨트롤러-로컬 LinkedHashMap 응답조립을
 * record 로 대체한다(§6-4 qrbatch-adminuser-rows-dto). 키셋·값 동치로 무손실.
 * {@code error}="site_not_mapped" 고정, {@code site}=사이트코드, {@code hint}=안내문구.
 */
public record SiteNotMappedError(String error, String site, String hint) {
}
