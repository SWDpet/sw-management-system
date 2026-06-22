package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.workplan.DocumentAttachment;

/**
 * /document/api/attachments/{docId} (문서 첨부파일 목록) 응답 행 dto.
 *
 * 기존 DocumentController.getAttachments 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 document-file-rows-dto). 키셋·값 동치로 무손실. uploadedAt 은 현행과 동일하게 toString().
 */
public record AttachmentRow(Integer attachId, String fileName, Long fileSize,
                            String mimeType, String uploadedAt) {

    public static AttachmentRow from(DocumentAttachment a) {
        return new AttachmentRow(a.getAttachId(), a.getFileName(), a.getFileSize(),
                a.getMimeType(), a.getUploadedAt().toString());
    }
}
