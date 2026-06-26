package com.swmanager.system.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * doc-bulk-export — ZIP 엔트리명 sanitize(경로 인젝션 방지) + 유형 라벨 단위테스트.
 * (엔드포인트 403/400/413 동작은 회사 PC 라이브 서버에서 검증.)
 */
class DocumentControllerBulkZipTest {

    @Test
    void zipSafe_removesPathChars() {
        // 경로 금지문자(/ \ : * ? " < > |)·travers(..) 제거 → ZIP 엔트리 경로 안전
        assertEquals("사업명", DocumentDownloadController.zipSafe("사\\업/명:*?\"<>|"));
        assertFalse(DocumentDownloadController.zipSafe("../../etc").contains(".."));
        assertFalse(DocumentDownloadController.zipSafe("a/b\\c").contains("/"));
        assertFalse(DocumentDownloadController.zipSafe("a/b\\c").contains("\\"));
    }

    @Test
    void zipSafe_compressesWhitespaceAndCaps() {
        assertEquals("강진 군", DocumentDownloadController.zipSafe("  강진   군  "));
        assertTrue(DocumentDownloadController.zipSafe("가".repeat(120)).length() <= 60);
    }

    @Test
    void zipSafe_nullEmptyFallback() {
        assertEquals("미상", DocumentDownloadController.zipSafe(null));
        assertEquals("미상", DocumentDownloadController.zipSafe("   "));
        assertEquals("미상", DocumentDownloadController.zipSafe("\\/:*?\"<>|"));
    }

    @Test
    void bulkTypeLabel_maps() {
        assertEquals("공문", DocumentDownloadController.bulkTypeLabel("letter"));
        assertEquals("전체", DocumentDownloadController.bulkTypeLabel("all"));
        assertEquals("기성내역서", DocumentDownloadController.bulkTypeLabel("interim"));
        assertEquals("산출물", DocumentDownloadController.bulkTypeLabel("unknown"));
    }
}
