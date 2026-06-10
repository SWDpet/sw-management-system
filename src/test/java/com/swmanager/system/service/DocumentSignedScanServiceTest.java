package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * doc-signed-scan-upload — 파일명 정제·라벨·PDF 매직바이트 단위 테스트.
 * (게이트/경로안전/원자적 교체는 회사 PC 통합검증 — 실 D드라이브 필요.)
 */
class DocumentSignedScanServiceTest {

    @Test
    void sanitize_removesPathIllegalChars() {
        assertEquals("사업명", DocumentSignedScanService.sanitize("사\\업/명:*?\"<>|"));
    }

    @Test
    void sanitize_compressesWhitespaceAndTrims() {
        assertEquals("강진 군", DocumentSignedScanService.sanitize("  강진   군  "));
    }

    @Test
    void sanitize_removesControlChars() {
        assertEquals("ab", DocumentSignedScanService.sanitize("ab"));
    }

    @Test
    void sanitize_capsLengthTo80() {
        String raw = "가".repeat(120);
        String out = DocumentSignedScanService.sanitize(raw);
        assertTrue(out.length() <= 80, "길이 80 이하여야 함: " + out.length());
    }

    @Test
    void sanitize_nullAndEmptyFallback() {
        assertEquals("사업미상", DocumentSignedScanService.sanitize(null));
        assertEquals("사업미상", DocumentSignedScanService.sanitize("   "));
        assertEquals("사업미상", DocumentSignedScanService.sanitize("\\/:*?\"<>|")); // 모두 금지문자 → 빈문자 → fallback
    }

    @Test
    void seg_usesCustomFallbackForEmpty() {
        assertEquals("연도미상", DocumentSignedScanService.seg(null, "연도미상"));
        assertEquals("시도미상", DocumentSignedScanService.seg("   ", "시도미상"));
        assertEquals("시군구미상", DocumentSignedScanService.seg("\\/:*?\"<>|", "시군구미상"));
        assertEquals("전라남도", DocumentSignedScanService.seg("전라남도", "시도미상"));
    }

    @Test
    void label_mapsThreeTypes() {
        assertEquals("착수계", DocumentSignedScanService.label(DocumentType.COMMENCE));
        assertEquals("기성계", DocumentSignedScanService.label(DocumentType.INTERIM));
        assertEquals("준공계", DocumentSignedScanService.label(DocumentType.COMPLETION));
    }

    @Test
    void isPdf_acceptsMagicBytes() {
        byte[] pdf = "%PDF-1.7\n...".getBytes(StandardCharsets.US_ASCII);
        assertTrue(DocumentSignedScanService.isPdf(pdf));
    }

    @Test
    void isPdf_rejectsNonPdfAndShort() {
        assertFalse(DocumentSignedScanService.isPdf("PKzip".getBytes(StandardCharsets.US_ASCII)));
        assertFalse(DocumentSignedScanService.isPdf("%PD".getBytes(StandardCharsets.US_ASCII)));
        assertFalse(DocumentSignedScanService.isPdf(new byte[0]));
    }
}
