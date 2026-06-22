package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.workplan.DocumentAttachment;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AttachmentRow 직렬화 골든 테스트 (document-file-rows-dto §6-4).
 *
 * DocumentController.getAttachments 의 컨트롤러-로컬 HashMap(attachId/fileName/fileSize/mimeType/uploadedAt)
 * 응답조립을 record 로 바꾸면서 /api/attachments/{docId} 응답 JSON 무손실(키셋·값·uploadedAt toString)을 고정.
 * 현행 HashMap 이라 키순서 비결정 → JsonNode tree 동치.
 */
class AttachmentRowTest {

    private final ObjectMapper om = new ObjectMapper();

    /** 현행 컨트롤러 로직 복제(검증 기준). */
    private Map<String, Object> legacy(DocumentAttachment a) {
        Map<String, Object> m = new HashMap<>();
        m.put("attachId", a.getAttachId());
        m.put("fileName", a.getFileName());
        m.put("fileSize", a.getFileSize());
        m.put("mimeType", a.getMimeType());
        m.put("uploadedAt", a.getUploadedAt().toString());
        return m;
    }

    @Test
    void attachmentRow_matchesLegacy() {
        DocumentAttachment a = new DocumentAttachment();
        a.setAttachId(7);
        a.setFileName("계약서.pdf");
        a.setFileSize(123456L);
        a.setMimeType("application/pdf");
        a.setUploadedAt(LocalDateTime.of(2026, 6, 22, 10, 30, 0));

        JsonNode x = om.valueToTree(AttachmentRow.from(a));
        JsonNode y = om.valueToTree(legacy(a));
        assertThat(x).isEqualTo(y);
        assertThat(x.size()).isEqualTo(5);
        assertThat(x.get("uploadedAt").asText()).isEqualTo("2026-06-22T10:30");
    }

    @Test
    void attachmentRow_nullScalars_preserved() {
        // fileSize/mimeType null 보존(키 유지) — uploadedAt 은 @PrePersist 로 non-null 이나 record 는 toString 필요
        DocumentAttachment a = new DocumentAttachment();
        a.setAttachId(8);
        a.setFileName("x");
        a.setUploadedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        JsonNode x = om.valueToTree(AttachmentRow.from(a));
        assertThat(x.has("fileSize")).isTrue();
        assertThat(x.get("fileSize").isNull()).isTrue();
        assertThat(x.get("mimeType").isNull()).isTrue();
    }
}
