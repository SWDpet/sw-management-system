package com.swmanager.system.service.ops;

import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.domain.ops.OpsDocumentAttachment;
import com.swmanager.system.repository.ops.OpsDocumentAttachmentRepository;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 운영·유지보수 문서 첨부파일 서비스. uploads/ops-docs/yyyyMM/ 별도 디렉토리.
 * (DocumentAttachmentService 와 동일 패턴, 디렉토리만 분리 — FR-7)
 */
@Service
@Transactional
@RequiredArgsConstructor
public class OpsDocAttachmentService {

    private final OpsDocumentAttachmentRepository attachmentRepository;
    private final OpsDocumentRepository opsDocumentRepository;

    @Value("${file.ops-upload-dir:./uploads/ops-docs}")
    private String opsUploadDir;

    public OpsDocumentAttachment saveAttachment(Long docId, MultipartFile file) throws IOException {
        OpsDocument doc = opsDocumentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("ops_doc not found: " + docId));

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Path dirPath = Paths.get(opsUploadDir, dateDir);
        Files.createDirectories(dirPath);

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String savedName = UUID.randomUUID() + ext;
        Path filePath = dirPath.resolve(savedName);

        file.transferTo(filePath.toFile());

        OpsDocumentAttachment att = new OpsDocumentAttachment();
        att.setDocument(doc);
        att.setFileName(originalName);
        att.setFilePath(filePath.toString());
        att.setFileSize(file.getSize());
        att.setMimeType(file.getContentType());
        return attachmentRepository.save(att);
    }

    @Transactional(readOnly = true)
    public List<OpsDocumentAttachment> getAttachments(Long docId) {
        return attachmentRepository.findByDocument_DocIdOrderByUploadedAtDesc(docId);
    }

    public void deleteAttachment(Long attachId) {
        OpsDocumentAttachment att = attachmentRepository.findById(attachId)
                .orElseThrow(() -> new IllegalArgumentException("attachment not found: " + attachId));
        try {
            Files.deleteIfExists(Paths.get(att.getFilePath()));
        } catch (IOException ignored) {}
        attachmentRepository.deleteById(attachId);
    }
}
