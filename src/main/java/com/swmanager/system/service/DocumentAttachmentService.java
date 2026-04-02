package com.swmanager.system.service;

import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentAttachment;
import com.swmanager.system.repository.workplan.DocumentAttachmentRepository;
import com.swmanager.system.repository.workplan.DocumentRepository;

import org.springframework.beans.factory.annotation.Autowired;
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

@Service
@Transactional
public class DocumentAttachmentService {

    @Autowired private DocumentAttachmentRepository attachmentRepository;
    @Autowired private DocumentRepository documentRepository;

    @Value("${file.upload-dir:./uploads/documents}")
    private String uploadDir;

    /**
     * 첨부파일 저장
     */
    public DocumentAttachment saveAttachment(Integer docId, MultipartFile file) throws IOException {
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다."));

        // 저장 경로 생성
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Path dirPath = Paths.get(uploadDir, dateDir);
        Files.createDirectories(dirPath);

        // 파일명 생성 (UUID + 원본확장자)
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String savedName = UUID.randomUUID().toString() + ext;
        Path filePath = dirPath.resolve(savedName);

        // 파일 저장
        file.transferTo(filePath.toFile());

        // DB 저장
        DocumentAttachment attachment = new DocumentAttachment();
        attachment.setDocument(doc);
        attachment.setFileName(originalName);
        attachment.setFilePath(filePath.toString());
        attachment.setFileSize(file.getSize());
        attachment.setMimeType(file.getContentType());

        return attachmentRepository.save(attachment);
    }

    /**
     * 문서의 첨부파일 목록
     */
    @Transactional(readOnly = true)
    public List<DocumentAttachment> getAttachments(Integer docId) {
        return attachmentRepository.findByDocument_DocId(docId);
    }

    /**
     * 첨부파일 삭제
     */
    public void deleteAttachment(Integer attachId) {
        DocumentAttachment attachment = attachmentRepository.findById(attachId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));

        // 파일 삭제
        try {
            Path path = Paths.get(attachment.getFilePath());
            Files.deleteIfExists(path);
        } catch (IOException ignored) {}

        attachmentRepository.deleteById(attachId);
    }
}
